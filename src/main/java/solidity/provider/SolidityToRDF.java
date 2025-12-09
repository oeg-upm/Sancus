package solidity.provider;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class SolidityToRDF {

    private JsonArray namesContract = new JsonArray();


    public String JsonContractToJavaObject(String contractInSolidity, String contractHash) {
        try {
            JsonObject contractAntlrFormat = SolidityToJSONParser.contractJsonObject(contractInSolidity);
            if (contractAntlrFormat == null) {
                return "{}";
            }

            JsonElement jsonElement = JsonParser.parseString(contractAntlrFormat.toString());
            if (!jsonElement.isJsonObject()) {
                return "{}";
            }

            JsonObject jsonObject = jsonElement.getAsJsonObject();

            cleanJsonElement(jsonObject, false);
            transformContractDefinitions(jsonObject);
            transformIdentifiers(jsonObject);
            modifyContractPartJson(jsonObject);
            modifyJsonArrayNotation(jsonObject);
            modifyStateVariableDeclarations(jsonObject);
            modifyVisibilityInStateVariableDeclarations(jsonObject);
            modifyConstantInStateVariableDeclarations(jsonObject);
            createValueStateVariableDeclarations(jsonObject);

            JsonArray sourceUnit = jsonObject.getAsJsonArray("sourceUnit");
            JsonArray groupedArray = groupElements(sourceUnit);
            jsonObject.add("sourceUnit", groupedArray);

            JsonObject finalJson = buildFinalJsonLd(jsonObject);

            if (finalJson == null) {
                return "{}";
            }



            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.toJson(finalJson);

        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }

    
    private void createValueStateVariableDeclarations(JsonObject jsonObject) {
        jsonObject.entrySet().forEach(entry -> {
            JsonElement element = entry.getValue();

            if (element.isJsonObject()) {
                createValueStateVariableDeclarations(element.getAsJsonObject());
            } else if (element.isJsonArray() && "stateVariableDeclaration".equals(entry.getKey())) {
                processStateVariableDeclarationsArrayForValue(element.getAsJsonArray());
            } else if (element.isJsonArray()) {
                element.getAsJsonArray().forEach(item -> {
                    if (item.isJsonObject()) {
                        createValueStateVariableDeclarations(item.getAsJsonObject());
                    }
                });
            }
        });
    }

    private void processStateVariableDeclarationsArrayForValue(JsonArray jsonArray) {
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonElement element = jsonArray.get(i);
            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                if (obj.has("text") && "=".equals(obj.get("text").getAsString())) {
                    jsonArray.remove(i);
                    i--;
                } else if (obj.has("expression")) {
                    String value = extractAndCombineTextFromExpression(obj.get("expression"));
                    JsonObject newValueObject = new JsonObject();
                    newValueObject.addProperty("value", value);
                    jsonArray.set(i, newValueObject);
                }
            }
        }
    }

    private String extractAndCombineTextFromExpression(JsonElement expression) {
        StringBuilder valueBuilder = new StringBuilder();
        extractTextRecursively(expression, valueBuilder);
        return valueBuilder.toString();
    }

    private void extractTextRecursively(JsonElement element, StringBuilder valueBuilder) {
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.has("text")) {
                valueBuilder.append(obj.get("text").getAsString());
            } else {
                obj.entrySet().forEach(entry -> extractTextRecursively(entry.getValue(), valueBuilder));
            }
        } else if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(item -> extractTextRecursively(item, valueBuilder));
        }
    }

    private void modifyConstantInStateVariableDeclarations(JsonObject jsonObject) {
        jsonObject.entrySet().forEach(entry -> {
            JsonElement element = entry.getValue();

            if (element.isJsonObject()) {
                modifyConstantInStateVariableDeclarations(element.getAsJsonObject());
            } else if (element.isJsonArray() && "stateVariableDeclaration".equals(entry.getKey())) {
                processStateVariableDeclarationsArrayForConstant(element.getAsJsonArray());
            } else if (element.isJsonArray()) {
                element.getAsJsonArray().forEach(item -> {
                    if (item.isJsonObject()) {
                        modifyConstantInStateVariableDeclarations(item.getAsJsonObject());
                    }
                });
            }
        });
    }

    private void processStateVariableDeclarationsArrayForConstant(JsonArray jsonArray) {
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonElement element = jsonArray.get(i);
            if (element.isJsonObject() && element.getAsJsonObject().has("text") &&
                    "constant".equals(element.getAsJsonObject().get("text").getAsString())) {
                JsonObject newObject = new JsonObject();
                newObject.addProperty("isConstant", true);
                jsonArray.set(i, newObject);
            }
        }
    }

    private void modifyContractPartJson(JsonObject jsonObject) {
        JsonArray contractParts = jsonObject.getAsJsonArray("contractPart");
        if (contractParts != null) {
            for (JsonElement contractPartElement : contractParts) {
                for (Map.Entry<String, JsonElement> entry : contractPartElement.getAsJsonObject().entrySet()) {
                    jsonObject.add(entry.getKey(), entry.getValue());
                }
            }
            jsonObject.remove("contractPart");
        }
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            if (entry.getValue().isJsonObject()) {
                modifyContractPartJson(entry.getValue().getAsJsonObject());
            } else if (entry.getValue().isJsonArray()) {
                for (JsonElement childElement : entry.getValue().getAsJsonArray()) {
                    if (childElement.isJsonObject()) {
                        modifyContractPartJson(childElement.getAsJsonObject());
                    }
                }
            }
        }
    }

    private void transformIdentifiers(JsonObject jsonObject) {
        Set<String> keysToRemove = new HashSet<>();
        JsonObject newEntries = new JsonObject();

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            if (value.isJsonObject()) {
                transformIdentifiers(value.getAsJsonObject());
            } else if (value.isJsonArray() && key.equals("identifier") && value.getAsJsonArray().size() == 1) {
                JsonObject identifierObj = value.getAsJsonArray().get(0).getAsJsonObject();
                if (identifierObj.has("text")) {
                    String name = identifierObj.get("text").getAsString();
                    keysToRemove.add(key);
                    newEntries.addProperty("name", name);
                }
            } else if (value.isJsonArray()) {
                transformIdentifiersInArray(value.getAsJsonArray());
            }
        }

        keysToRemove.forEach(jsonObject::remove);
        for (Map.Entry<String, JsonElement> entry : newEntries.entrySet()) {
            jsonObject.add(entry.getKey(), entry.getValue());
        }
    }

    private void transformIdentifiersInArray(JsonArray jsonArray) {
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonElement element = jsonArray.get(i);
            if (element.isJsonObject()) {
                transformIdentifiers(element.getAsJsonObject());
            } else if (element.isJsonArray()) {
                transformIdentifiersInArray(element.getAsJsonArray());
            }
        }
    }

    private void transformContractDefinitions(JsonObject jsonObject) {
        JsonArray sourceUnit = jsonObject.getAsJsonArray("sourceUnit");
        if (sourceUnit == null) return;

        for (JsonElement element : sourceUnit) {
            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                if (obj.has("contractDefinition")) {
                    JsonArray contractDefinition = obj.getAsJsonArray("contractDefinition");
                    transformContractDefinitionElements(contractDefinition);
                }
            }
        }
    }

    private void transformContractDefinitionElements(JsonArray contractDefinition) {
        boolean isAbstract = false;
        String contractType = null;
        for (int i = 0; i < contractDefinition.size(); i++) {
            JsonObject element = contractDefinition.get(i).getAsJsonObject();
            if (element.has("text")) {
                String text = element.get("text").getAsString();
                if ("contract".equals(text) || "library".equals(text) || "interface".equals(text)) {
                    contractType = text;
                    contractDefinition.remove(i);
                    i--;
                } else if ("abstract".equals(text)) {
                    isAbstract = true;
                    contractDefinition.remove(i);
                    i--;
                }
            }
        }
        if (contractType != null) {
            JsonObject typeObject = new JsonObject();
            typeObject.addProperty("contractType", contractType);
            contractDefinition.add(typeObject);
        }
        JsonObject abstractObject = new JsonObject();
        abstractObject.addProperty("isAbstract", isAbstract);
        contractDefinition.add(abstractObject);
    }

    private void cleanJsonElement(JsonElement element, boolean insideBlock) {
        if (element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();
            Set<String> keysToRemove = new HashSet<>();

            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                String key = entry.getKey();
                JsonElement value = entry.getValue();

                if ("block".equals(key)) {
                    String code = processBlock(value.getAsJsonArray());
                    if (!code.isEmpty()) {
                        jsonObject.addProperty("code", code);
                    }
                    keysToRemove.add(key);
                } else if (!insideBlock && (shouldBeRemoved(key) || shouldBeRemoved(value))) {
                    keysToRemove.add(key);
                } else {
                    cleanJsonElement(value, "block".equals(key) || insideBlock);
                    if (value.isJsonObject() && value.getAsJsonObject().size() == 0 ||
                            value.isJsonArray() && value.getAsJsonArray().size() == 0) {
                        keysToRemove.add(key);
                    }
                }
            }
            keysToRemove.forEach(jsonObject::remove);
        } else if (element.isJsonArray()) {
            JsonArray jsonArray = element.getAsJsonArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonElement arrayElement = jsonArray.get(i);
                if (!insideBlock && shouldBeRemoved(arrayElement)) {
                    jsonArray.remove(i);
                    i--;
                } else {
                    cleanJsonElement(arrayElement, insideBlock);
                    if (arrayElement.isJsonObject() && arrayElement.getAsJsonObject().size() == 0 ||
                            arrayElement.isJsonArray() && arrayElement.getAsJsonArray().size() == 0) {
                        jsonArray.remove(i);
                        i--;
                    }
                }
            }
        }
    }

    private String processBlock(JsonArray block) {
        StringBuilder codeBuilder = new StringBuilder();
        processBlockHelper(block, codeBuilder, false);
        return codeBuilder.toString();
    }

    private void processBlockHelper(JsonArray jsonArray, StringBuilder codeBuilder, boolean isStatement) {
        for (JsonElement element : jsonArray) {
            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                if (obj.has("text")) {
                    String text = obj.get("text").getAsString();
                    if (!"{".equals(text) && !"}".equals(text)) {
                        if (shouldAddSpaceBefore(text, codeBuilder)) {
                            codeBuilder.append(" ");
                        }
                        codeBuilder.append(text);
                    }
                } else {
                    for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                        if (entry.getValue().isJsonArray()) {
                            boolean newIsStatement = "statement".equals(entry.getKey());
                            if (isStatement && codeBuilder.length() > 0) {
                                codeBuilder.append("\n");
                            }
                            processBlockHelper(entry.getValue().getAsJsonArray(), codeBuilder, newIsStatement);
                        }
                    }
                }
            }
        }
    }

    private boolean shouldAddSpaceBefore(String text, StringBuilder codeBuilder) {
        if (codeBuilder.length() == 0 || text.length() == 0) {
            return false;
        }
        char lastCharInBuilder = codeBuilder.charAt(codeBuilder.length() - 1);
        char firstCharInText = text.charAt(0);

        if (isOperatorChar(firstCharInText) || firstCharInText == '[' || firstCharInText == ']' ||
                firstCharInText == '(' || firstCharInText == ';' || firstCharInText == '.' ||
                firstCharInText == ',' || firstCharInText == ')') {
            return false;
        }
        if (lastCharInBuilder == '(') {
            return false;
        }
        return !(isOperatorChar(lastCharInBuilder) || lastCharInBuilder == '.');
    }

    private boolean isOperatorChar(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '&' || c == '|' || c == '^' || c == '!';
    }

    private boolean shouldBeRemoved(String value) {
        return value.equals("(") || value.equals(",") || value.equals("is") ||
                value.equals("returns") || value.equals("solidity") || value.equals("pragma") ||
                value.equals(")") || value.equals(";") || value.equals("{") || value.equals("}") ||
                value.equals("<EOF>");
    }

    private boolean shouldBeRemoved(JsonElement element) {
        return element.isJsonPrimitive() && shouldBeRemoved(element.getAsString());
    }

    private JsonArray groupElements(JsonArray sourceUnit) {
        JsonArray result = new JsonArray();
        JsonElement lastPragmaDirective = createUnknownPragmaDirective();
        ArrayList<JsonElement> currentGroup = new ArrayList<>();
        List<JsonElement> importDirectives = new ArrayList<>();

        for (JsonElement element : sourceUnit) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.has("importDirective")) {
                processImportDirective(obj);
                importDirectives.add(element);
                continue;
            }
            if (obj.has("pragmaDirective")) {
                lastPragmaDirective = element;
                String pragmaText = concatenateTextValues(obj.getAsJsonArray("pragmaDirective"));
                lastPragmaDirective.getAsJsonObject().addProperty("pragmaDirective", pragmaText);
                if (!currentGroup.isEmpty()) {
                    addGroupToResult(result, currentGroup, importDirectives);
                }
            } else if (obj.has("contractDefinition")) {
                if (!currentGroup.contains(lastPragmaDirective)) {
                    currentGroup.add(lastPragmaDirective);
                }
                currentGroup.add(element);
                addGroupToResult(result, currentGroup, importDirectives);
            } else if (obj.has("importDirective")) {
                currentGroup.add(element);
                addGroupToResult(result, currentGroup, importDirectives);
            }
        }
        return result;
    }

    private void processImportDirective(JsonObject importDirectiveObj) {
        JsonArray importArray = importDirectiveObj.getAsJsonArray("importDirective");
        if (importArray != null) {
            Iterator<JsonElement> iterator = importArray.iterator();
            while (iterator.hasNext()) {
                JsonElement elem = iterator.next();
                if (elem.isJsonObject() && elem.getAsJsonObject().has("text") &&
                        "import".equals(elem.getAsJsonObject().get("text").getAsString())) {
                    iterator.remove();
                }
            }

            if (importArray.size() >= 2) {
                JsonObject secondLast = importArray.get(importArray.size() - 2).getAsJsonObject();
                JsonObject last = importArray.get(importArray.size() - 1).getAsJsonObject();

                if (secondLast.has("text") && last.has("text")) {
                    String lastText = last.get("text").getAsString();
                    importArray.remove(importArray.size() - 2);
                    last.addProperty("from", lastText);
                    last.remove("text");
                }
            }
        }
    }

    private void addGroupToResult(JsonArray result, ArrayList<JsonElement> group, List<JsonElement> importDirectives) {
        if (group.size() == 1 && importDirectives.isEmpty()) {
            result.add(group.get(0));
        } else {
            JsonArray newGroup = new JsonArray();
            for (JsonElement el : group) {
                newGroup.add(el);
            }
            for (JsonElement importDirective : importDirectives) {
                newGroup.add(importDirective);
            }
            result.add(newGroup);
        }
        group.clear();
        importDirectives.clear();
    }

    private JsonElement createUnknownPragmaDirective() {
        JsonObject unknownPragmaDirective = new JsonObject();
        unknownPragmaDirective.addProperty("pragmaDirective", "Unknown");
        return unknownPragmaDirective;
    }

    private String concatenateTextValues(JsonArray jsonArray) {
        StringBuilder textBuilder = new StringBuilder();
        for (JsonElement element : jsonArray) {
            concatenateTextValuesHelper(element, textBuilder);
        }
        return textBuilder.toString();
    }

    private void concatenateTextValuesHelper(JsonElement element, StringBuilder textBuilder) {
        if (element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                if ("text".equals(entry.getKey()) && entry.getValue().isJsonPrimitive()) {
                    textBuilder.append(entry.getValue().getAsString());
                } else if (entry.getValue().isJsonObject() || entry.getValue().isJsonArray()) {
                    concatenateTextValuesHelper(entry.getValue(), textBuilder);
                }
            }
        } else if (element.isJsonArray()) {
            for (JsonElement arrayElement : element.getAsJsonArray()) {
                concatenateTextValuesHelper(arrayElement, textBuilder);
            }
        }
    }

    public boolean isValid(String json) {
        try {
            JsonParser.parseString(json);
        } catch (JsonSyntaxException e) {
            return false;
        }
        return true;
    }

    public String checkIsJSONandModify(String code) {
        if (code.startsWith("{{") && code.endsWith("}}")) {
            return code.substring(1, code.length() - 1);
        }
        return code;
    }

    public void modifyJsonArrayNotation(JsonObject jsonObject) {
        jsonObject.entrySet().forEach(entry -> {
            JsonElement element = entry.getValue();

            if (element.isJsonObject()) {
                modifyJsonArrayNotation(element.getAsJsonObject());
            } else if (element.isJsonArray()) {
                processJsonArray(element.getAsJsonArray());
            }
        });
    }

    private void processJsonArray(JsonArray jsonArray) {
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonElement element = jsonArray.get(i);
            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                if (i < jsonArray.size() - 1 && isBracketPair(object, jsonArray.get(i + 1))) {
                    JsonObject newArrayIndicator = new JsonObject();
                    newArrayIndicator.addProperty("isArray", true);
                    jsonArray.set(i, newArrayIndicator);
                    jsonArray.remove(i + 1);
                    i--;
                } else {
                    modifyJsonArrayNotation(object);
                }
            } else if (element.isJsonArray()) {
                processJsonArray(element.getAsJsonArray());
            }
        }
    }

    private boolean isBracketPair(JsonObject currentObj, JsonElement nextElement) {
        if (nextElement.isJsonObject()) {
            JsonObject nextObj = nextElement.getAsJsonObject();
            return currentObj.has("text") && currentObj.get("text").getAsString().equals("[") &&
                    nextObj.has("text") && nextObj.get("text").getAsString().equals("]");
        }
        return false;
    }

    public void modifyStateVariableDeclarations(JsonObject jsonObject) {
        jsonObject.entrySet().forEach(entry -> {
            JsonElement element = entry.getValue();

            if (element.isJsonObject()) {
                modifyStateVariableDeclarations(element.getAsJsonObject());
            } else if (element.isJsonArray() && "stateVariableDeclaration".equals(entry.getKey())) {
                processStateVariableDeclarationsArray(element.getAsJsonArray());
            } else if (element.isJsonArray()) {
                element.getAsJsonArray().forEach(item -> {
                    if (item.isJsonObject()) {
                        modifyStateVariableDeclarations(item.getAsJsonObject());
                    }
                });
            }
        });
    }

    private void processStateVariableDeclarationsArray(JsonArray jsonArray) {
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonElement element = jsonArray.get(i);
            if (element.isJsonObject() && element.getAsJsonObject().has("text") &&
                    "immutable".equals(element.getAsJsonObject().get("text").getAsString())) {
                JsonObject newObject = new JsonObject();
                newObject.addProperty("isImmutable", true);
                jsonArray.set(i, newObject);
            }
        }
    }

    public void modifyVisibilityInStateVariableDeclarations(JsonObject jsonObject) {
        jsonObject.entrySet().forEach(entry -> {
            JsonElement element = entry.getValue();

            if (element.isJsonObject()) {
                modifyVisibilityInStateVariableDeclarations(element.getAsJsonObject());
            } else if (element.isJsonArray() && "stateVariableDeclaration".equals(entry.getKey())) {
                processStateVariableDeclarationsArrayForVisibility(element.getAsJsonArray());
            } else if (element.isJsonArray()) {
                element.getAsJsonArray().forEach(item -> {
                    if (item.isJsonObject()) {
                        modifyVisibilityInStateVariableDeclarations(item.getAsJsonObject());
                    }
                });
            }
        });
    }

    private void processStateVariableDeclarationsArrayForVisibility(JsonArray jsonArray) {
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonElement element = jsonArray.get(i);
            if (element.isJsonObject() && element.getAsJsonObject().has("text")) {
                String textValue = element.getAsJsonObject().get("text").getAsString();
                if (isVisibilityKeyword(textValue)) {
                    JsonObject newObject = new JsonObject();
                    newObject.addProperty("visibility", textValue);
                    jsonArray.set(i, newObject);
                }
            }
        }
    }

    private boolean isVisibilityKeyword(String text) {
        return "private".equals(text) || "public".equals(text) ||
                "internal".equals(text) || "external".equals(text);
    }

    private JsonObject buildFinalJsonLd(JsonObject intermediate) {
        JsonArray contractInput = intermediate.getAsJsonArray("sourceUnit");
        if (contractInput == null) {
            return null;
        }

        namesContract = new JsonArray();
        getAllNames(contractInput);

        JsonObject lastFinalJSON = null;

        for (JsonElement jarry : contractInput) {
            if (!jarry.isJsonArray()) continue;
            JsonObject finalJSON = new JsonObject();
            JsonArray imports = new JsonArray();
            JsonArray constructor = new JsonArray();
            JsonArray functions = new JsonArray();
            JsonArray fallback = new JsonArray();
            JsonArray receive = new JsonArray();
            JsonArray attributes = new JsonArray();
            JsonArray isUsingFor = new JsonArray();
            JsonArray modifiers = new JsonArray();
            JsonArray structs = new JsonArray();
            JsonArray events = new JsonArray();
            JsonArray inheritance = new JsonArray();

            for (JsonElement contract : jarry.getAsJsonArray()) {
                finalJSON.addProperty("@context", "https://oeg-upm.github.io/Solidity-ontology/context/context.json");

                JsonObject cObj = contract.getAsJsonObject();

                if (cObj.has("pragmaDirective")) {
                    finalJSON.addProperty("version", cObj.get("pragmaDirective").getAsString());
                } else if (cObj.has("contractDefinition")) {
                    String contractName = "";
                    String contractType = "";
                    boolean isAbstract = false;
                    for (JsonElement contractWithoutVersion : cObj.get("contractDefinition").getAsJsonArray()) {
                        JsonObject cd = contractWithoutVersion.getAsJsonObject();
                        if (cd.has("name")) {
                            contractName = cd.get("name").getAsString();
                        } else if (cd.has("contractType")) {
                            contractType = cd.get("contractType").getAsString();
                        } else if (cd.has("isAbstract")) {
                            isAbstract = cd.get("isAbstract").getAsBoolean();
                        } else if (cd.has("inheritanceSpecifier")) {
                            inheritance.add(inheritance(cd.get("inheritanceSpecifier").getAsJsonArray()));
                        } else if (cd.has("functionDefinition")) {
                            JsonArray fd = cd.get("functionDefinition").getAsJsonArray();
                            String descriptor = fd.get(0).getAsJsonObject()
                                    .get("functionDescriptor").getAsJsonArray()
                                    .get(0).getAsJsonObject()
                                    .get("text").getAsString();
                            switch (descriptor) {
                                case "function":
                                    functions.add(analyseFunction(fd));
                                    break;
                                case "constructor":
                                    constructor.add(analyseConstructor(fd));
                                    break;
                                case "receive":
                                    receive.add(analyseReceive(fd));
                                    break;
                                case "fallback":
                                    fallback.add(analyseFallback(fd));
                                    break;
                            }
                        } else if (cd.has("usingForDeclaration")) {
                            isUsingFor.add(analyseIsUsingFor(cd.get("usingForDeclaration").getAsJsonArray()));
                        } else if (cd.has("eventDefinition")) {
                            events.add(analyseEvents(cd.get("eventDefinition").getAsJsonArray()));
                        } else if (cd.has("stateVariableDeclaration")) {
                            attributes.add(analyseAttribute(cd.get("stateVariableDeclaration").getAsJsonArray()));
                        } else if (cd.has("structDefinition")) {
                            structs.add(analyseStructs(cd.get("structDefinition").getAsJsonArray()));
                        } else if (cd.has("modifierDefinition")) {
                            modifiers.add(analyseModifier(cd.get("modifierDefinition").getAsJsonArray()));
                        } else if (cd.has("enumDefinition")) {
                            attributes.add(analyseEnum(cd.get("enumDefinition").getAsJsonArray()));
                        }

                        for (JsonElement name : namesContract) {
                            if (name.getAsJsonObject().get("name").getAsString().contentEquals(contractName)) {
                                finalJSON.addProperty("@id", name.getAsJsonObject().get("uuid").getAsString());
                            }
                        }
                        if (!finalJSON.has("@id")) {
                            finalJSON.addProperty("@id", "uuid:" + UUID.randomUUID().toString() + "-"
                                    + contractType.replace("./", "")
                                                  .replace("../", "")
                                                  .replace("\"", "-")
                                                  .replace("/", "-")
                                                  .replace(".sol", ""));
                        }

                        finalJSON.addProperty("contractName", contractName);
                        finalJSON.addProperty("@type", contractType);
                        finalJSON.addProperty("isAbstract", isAbstract);
                    }
                } else if (cObj.has("importDirective")) {
                    imports.add(analyseImports(cObj.get("importDirective").getAsJsonArray()));
                }
            }

            if (!inheritance.isEmpty()) {
                finalJSON.add("inheritance", inheritance);
            }
            if (!imports.isEmpty()) {
                finalJSON.add("hasImport", imports);
            }
            if (!events.isEmpty()) {
                finalJSON.add("hasImplementationEvent", events);
            }
            if (!modifiers.isEmpty()) {
                finalJSON.add("hasImplementationModifier", modifiers);
            }
            if (!functions.isEmpty()) {
                finalJSON.add("hasImplementationFunction", functions);
            }
            if (!constructor.isEmpty()) {
                finalJSON.add("hasContractConstructor", constructor);
            }
            if (!fallback.isEmpty()) {
                finalJSON.add("hasContractFallback", fallback);
            }
            if (!receive.isEmpty()) {
                finalJSON.add("hasContractReceive", receive);
            }
            if (!attributes.isEmpty()) {
                finalJSON.add("hasContractAttribute", attributes);
            }
            if (!isUsingFor.isEmpty()) {
                finalJSON.add("hasContractUsingForDirective", isUsingFor);
            }
            if (!structs.isEmpty()) {
                finalJSON.add("hasImplementationStructType", structs);
            }

            lastFinalJSON = finalJSON; 
        }

        return lastFinalJSON;
    }

    private void getAllNames(JsonArray contracts) {
        String uuid = UUID.randomUUID().toString();
        for (JsonElement contract : contracts) {
            if (!contract.isJsonArray()) continue;
            for (JsonElement name : contract.getAsJsonArray()) {
                if (name.getAsJsonObject().has("contractDefinition")) {
                    for (JsonElement contractWithoutVersion : name.getAsJsonObject().get("contractDefinition").getAsJsonArray()) {
                        if (contractWithoutVersion.getAsJsonObject().has("name")) {
                            JsonObject nameAndUUID = new JsonObject();
                            nameAndUUID.addProperty("name", contractWithoutVersion.getAsJsonObject().get("name").getAsString());
                            nameAndUUID.addProperty(
                                    "uuid",
                                    "uuid:" + uuid + "-" +
                                            contractWithoutVersion.getAsJsonObject().get("name").getAsString()
                                                    .replace("./", "")
                                                    .replace("../", "")
                                                    .replace("\"", "-")
                                                    .replace("/", "-")
                                                    .replace(".sol", "")
                            );
                            namesContract.add(nameAndUUID);
                        }
                    }
                }
            }
        }
    }

    private JsonArray inheritance(JsonArray importInheritance) {
        JsonArray inheritance = new JsonArray();
        for (JsonElement jelem : importInheritance.getAsJsonArray()) {
            if (jelem.getAsJsonObject().has("userDefinedTypeName")) {
                String importName = extractImportName(
                        jelem.getAsJsonObject()
                                .get("userDefinedTypeName").getAsJsonArray()
                                .get(0).getAsJsonObject()
                                .get("name").getAsString());
                inheritance.add(searchNameAndInclude(importName));
            }
        }
        return inheritance;
    }

    private JsonObject analyseIsUsingFor(JsonArray importJson) {
        JsonObject usingFor = new JsonObject();
        for (JsonElement jelem : importJson.getAsJsonArray()) {
            JsonObject obj = jelem.getAsJsonObject();
            if (obj.has("name")) {
                usingFor.addProperty("usingForName", obj.get("name").getAsString());
            } else if (obj.has("typeName")) {
                JsonObject t = obj.get("typeName").getAsJsonArray().get(0).getAsJsonObject();
                if (t.has("userDefinedTypeName")) {
                    String value = t.get("userDefinedTypeName").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString();
                    usingFor.addProperty("isUsingLibrary", searchNameAndInclude(value));
                } else if (t.has("elementaryTypeName")) {
                    String value = t.get("elementaryTypeName").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString();
                    usingFor.add("isUsingLibrary", catalogueElementaryType(value));
                }
            }
        }
        return usingFor;
    }

    private JsonObject analyseStructs(JsonArray importJson) {
        JsonObject struct = new JsonObject();
        JsonArray elementsInStruct = new JsonArray();
        for (JsonElement jelem : importJson.getAsJsonArray()) {
            JsonObject obj = jelem.getAsJsonObject();
            if (obj.has("name")) {
                struct.addProperty("structName", obj.get("name").getAsString());
            } else if (obj.has("variableDeclaration")) {
                JsonObject structElement = new JsonObject();
                JsonArray varDecl = obj.get("variableDeclaration").getAsJsonArray();
                JsonObject typeNameObj = varDecl.get(0).getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject();
                if (typeNameObj.has("mapping")) {
                    structElement.add("hasNonConstantType",
                            analyseMapping(typeNameObj.get("mapping").getAsJsonArray()));
                    structElement.get("hasNonConstantType").getAsJsonObject()
                            .addProperty("structAttributeName", varDecl.get(1).getAsJsonObject().get("name").getAsString());
                    structElement.get("hasNonConstantType").getAsJsonObject().addProperty("@type", "MapType");
                    elementsInStruct.add(structElement);
                } else if (typeNameObj.has("elementaryTypeName")) {
                    String typeElement = typeNameObj.get("elementaryTypeName").getAsJsonArray()
                            .get(0).getAsJsonObject().get("text").getAsString();
                    structElement.add("hasNonConstantType", catalogueElementaryType(typeElement));
                    structElement.get("hasNonConstantType").getAsJsonObject()
                            .addProperty("structAttributeName", varDecl.get(1).getAsJsonObject().get("name").getAsString());
                    elementsInStruct.add(structElement);
                } else if (typeNameObj.has("typeName")) {
                    structElement.add("hasNonConstantType",
                            analyseArrays(typeNameObj.get("typeName").getAsJsonArray()));
                    structElement.get("hasNonConstantType").getAsJsonObject()
                            .addProperty("structAttributeName", varDecl.get(1).getAsJsonObject().get("name").getAsString());
                    elementsInStruct.add(structElement);
                }
            }
        }
        struct.add("hasNonConstantStructAttribute", elementsInStruct);
        return struct;
    }

    private JsonObject analyseArrays(JsonArray importJson) {
        JsonObject constantType = new JsonObject();
        JsonObject nonConstantType = new JsonObject();
        JsonObject nonConstantTypeRelation = new JsonObject();
        for (JsonElement arrayElem : importJson) {
            JsonObject obj = arrayElem.getAsJsonObject();
            if (obj.has("isArray")) {
                constantType.addProperty("@type", "ArrayType");
            } else if (obj.has("typeName")) {
                JsonObject typeName = obj.get("typeName").getAsJsonArray().get(0).getAsJsonObject();
                if (typeName.has("elementaryTypeName")) {
                    String attributeType = typeName.get("elementaryTypeName").getAsJsonArray()
                            .get(0).getAsJsonObject().get("text").getAsString();
                    nonConstantTypeRelation.add("@type", catalogueElementaryType(attributeType));
                } else if (typeName.has("userDefinedTypeName")) {
                    String value = typeName.get("userDefinedTypeName").getAsJsonArray()
                            .get(0).getAsJsonObject().get("name").getAsString();
                    nonConstantTypeRelation.addProperty("@type", searchNameAndInclude(value));
                } else {
                    nonConstantTypeRelation.add("hasType",
                            analyseArrays(typeName.get("typeName").getAsJsonArray()));
                }
            }
        }
        nonConstantType.add("hasNonConstantType", nonConstantTypeRelation);
        constantType.add("hasNonConstantType", nonConstantType);
        return constantType;
    }

    private String searchNameAndInclude(String value) {
        String valueName = "";
        boolean found = false;
        for (JsonElement name : namesContract) {
            if (value.contentEquals(name.getAsJsonObject().get("name").getAsString())) {
                valueName = name.getAsJsonObject().get("uuid").getAsString();
                found = true;
            }
        }
        if (!found) {
            JsonObject newName = new JsonObject();
            valueName = "uuid:" + UUID.randomUUID().toString() + "-" +
                    value.replace("./", "")
                         .replace("../", "")
                         .replace("\"", "-")
                         .replace("/", "-")
                         .replace(".sol", "");
            newName.addProperty("name", value);
            newName.addProperty("uuid", valueName);
            namesContract.add(newName);
        }
        return valueName;
    }

    private JsonObject catalogueElementaryType(String value) {
        JsonObject typeElementObject = new JsonObject();
        if (value.contains("bytes") || value.contains("int")) {
            String[] bytesNumber = splitBytesAndNumber(value);
            typeElementObject.addProperty("@type", bytesNumber[0]);
            if (bytesNumber[1].isEmpty() && value.contains("bytes")) {
                bytesNumber[1] = "32";
            } else if (bytesNumber[1].isEmpty() && value.contains("int")) {
                bytesNumber[1] = "256";
            }
            typeElementObject.addProperty("memory", Integer.parseInt(bytesNumber[1]));
        } else {
            typeElementObject.addProperty("@type", value);
        }
        return typeElementObject;
    }

    public String extractImportName(String importFullName) {
        int lastSlashIndex = importFullName.lastIndexOf('/');
        String importName = (lastSlashIndex == -1) ? importFullName : importFullName.substring(lastSlashIndex + 1);
        int lastDotIndex = importName.lastIndexOf('.');
        if (lastDotIndex != -1) {
            importName = importName.substring(0, lastDotIndex);
        }
        return importName;
    }

    private JsonObject analyseImports(JsonArray importJson) {
        JsonArray names = new JsonArray();
        JsonObject from = new JsonObject();
        for (JsonElement jelem : importJson.getAsJsonArray()) {
            JsonObject obj = jelem.getAsJsonObject();
            if (obj.has("importDeclaration")) {
                String importName = extractImportName(
                        obj.get("importDeclaration").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString());
                names.add(searchNameAndInclude(importName));
            }
            if (obj.has("from")) {
                from.addProperty("from", searchNameAndInclude(obj.get("from").getAsString()));
            }
        }
        from.add("name", names);
        return from;
    }

    private JsonObject analyseEvents(JsonArray importEvent) {
        JsonObject event = new JsonObject();
        JsonArray eventArguments = new JsonArray();
        for (JsonElement jelem : importEvent.getAsJsonArray()) {
            JsonObject obj = jelem.getAsJsonObject();
            if (obj.has("name")) {
                event.addProperty("name", obj.get("name").getAsString());
            } else if (obj.has("eventParameterList")) {
                int position = 0;
                for (JsonElement jparam : obj.get("eventParameterList").getAsJsonArray()) {
                    JsonObject eventArgument = new JsonObject();
                    eventArgument.addProperty("hasParameterPosition", position);
                    JsonObject pObj = jparam.getAsJsonObject();
                    if (pObj.has("eventParameter")) {
                        for (JsonElement jEventParam : pObj.get("eventParameter").getAsJsonArray()) {
                            JsonObject ep = jEventParam.getAsJsonObject();
                            if (ep.has("name")) {
                                eventArgument.addProperty("hasParameterName", ep.get("name").getAsString());
                            } else if (ep.has("text")) {
                                if (ep.get("text").getAsString().contentEquals("indexed")) {
                                    eventArgument.addProperty("isIndexed", true);
                                } else {
                                    eventArgument.addProperty("isIndexed", false);
                                }
                            } else if (ep.has("typeName")) {
                                JsonObject parameterArgumentType = new JsonObject();
                                boolean isArray = false;
                                for (JsonElement jEventTypeNameParam : ep.get("typeName").getAsJsonArray()) {
                                    JsonObject et = jEventTypeNameParam.getAsJsonObject();
                                    if (et.has("elementaryTypeName")) {
                                        if (et.get("elementaryTypeName").getAsJsonArray().get(0).getAsJsonObject().has("text")) {
                                            parameterArgumentType = catalogueElementaryType(
                                                    et.get("elementaryTypeName").getAsJsonArray().get(0)
                                                            .getAsJsonObject().get("text").getAsString());
                                        }
                                    } else if (et.has("userDefinedTypeName")) {
                                        if (et.get("userDefinedTypeName").getAsJsonArray().get(0).getAsJsonObject().has("name")) {
                                            parameterArgumentType.addProperty(
                                                    "@type",
                                                    searchNameAndInclude(
                                                            et.get("userDefinedTypeName").getAsJsonArray().get(0)
                                                                    .getAsJsonObject().get("name").getAsString()));
                                        }
                                    } else if (et.has("typeName") || isArray) {
                                        if (!isArray) {
                                            JsonObject innerType = et.get("typeName").getAsJsonArray().get(0).getAsJsonObject();
                                            if (innerType.has("userDefinedTypeName")) {
                                                parameterArgumentType.addProperty("hasType",
                                                        innerType.get("userDefinedTypeName").getAsJsonArray().get(0)
                                                                .getAsJsonObject().get("name").getAsString());
                                            } else if (innerType.has("elementaryTypeName")) {
                                                parameterArgumentType = catalogueElementaryType(
                                                        innerType.get("elementaryTypeName").getAsJsonArray().get(0)
                                                                .getAsJsonObject().get("text").getAsString());
                                            }
                                        }
                                        isArray = true;
                                        if (et.has("isArray")) {
                                            parameterArgumentType.addProperty("@type", "ArrayType");
                                            isArray = false;
                                        }
                                    }
                                }
                                eventArgument.add("hasParameterType", parameterArgumentType);
                            }
                        }
                    }
                    position++;
                    eventArguments.add(eventArgument);
                }
                event.add("hasEventArguments", eventArguments);
            } else if (obj.has("text")) {
                if (obj.get("text").getAsString().contentEquals("anonymous")) {
                    event.addProperty("isAnonymous", true);
                } else {
                    event.addProperty("isAnonymous", false);
                }
            }
        }
        if (!eventArguments.isEmpty()) {
            event.add("hasEventArguments", eventArguments);
        }
        return event;
    }

    private JsonObject analyseModifier(JsonArray importModifier) {
        JsonObject modifier = new JsonObject();
        for (JsonElement jelem : importModifier.getAsJsonArray()) {
            JsonObject obj = jelem.getAsJsonObject();
            if (obj.has("name")) {
                modifier.addProperty("modifierName", obj.get("name").getAsString());
            } else if (obj.has("code")) {
                modifier.addProperty("modifierCode", obj.get("code").getAsString());
            } else if (obj.has("parameterList")) {
                modifier.add("hasModifierArgument",
                        analyseInputParam(obj.get("parameterList").getAsJsonArray()));
            } else if (obj.has("text")) {
                if (obj.get("text").getAsString().contains("virtual") ||
                        obj.get("text").getAsString().contains("override")) {
                    modifier.addProperty("hasModifierBehaviour", obj.get("text").getAsString());
                }
            }
        }
        return modifier;
    }

    private JsonObject analyseEnum(JsonArray importEvent) {
        JsonObject event = new JsonObject();
        JsonObject option = new JsonObject();
        JsonArray options = new JsonArray();
        for (JsonElement jelem : importEvent.getAsJsonArray()) {
            JsonObject obj = jelem.getAsJsonObject();
            if (obj.has("name")) {
                event.addProperty("hasName", obj.get("name").getAsString());
            } else if (obj.has("enumValue")) {
                options.add(obj.get("enumValue").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString());
            }
        }
        option.add("option", options);
        event.add("hasNonConstantType", option);
        return event;
    }

    private JsonObject analyseFunction(JsonArray importFunction) {
        JsonObject function = new JsonObject();
        JsonObject first = importFunction.getAsJsonArray().get(0).getAsJsonObject();
        if (first.get("functionDescriptor").getAsJsonArray().get(0).getAsJsonObject()
                .get("text").getAsString().contentEquals("function")) {

            for (JsonElement jelem : importFunction.getAsJsonArray()) {
                JsonObject obj = jelem.getAsJsonObject();
                if (obj.has("functionDescriptor")) {
                    JsonArray fd = obj.get("functionDescriptor").getAsJsonArray();
                    if (fd.size() > 1 && fd.get(1).getAsJsonObject().has("name")) {
                        String nameFunction = fd.get(1).getAsJsonObject().get("name").getAsString();
                        function.addProperty("functionName", nameFunction);
                    }
                } else if (obj.has("code")) {
                    function.addProperty("functionCode", obj.get("code").getAsString());
                } else if (obj.has("modifierList")) {
                    function.add("hasFunctionBehaviour",
                            analyseModifiersParam(obj.get("modifierList").getAsJsonArray()));
                } else if (obj.has("parameterList")) {
                    function.add("hasFunctionArguments",
                            analyseInputParam(obj.get("parameterList").getAsJsonArray()));
                } else if (obj.has("returnParameters")) {
                    function.add("hasFunctionReturn",
                            analyseInputParam(obj.get("returnParameters").getAsJsonArray()
                                    .get(0).getAsJsonObject()
                                    .get("parameterList").getAsJsonArray()));
                }
            }
        }
        return function;
    }

    private JsonObject analyseModifiersParam(JsonArray importJson) {
        JsonObject modifiersList = new JsonObject();
        JsonObject modifierSpec = new JsonObject();
        for (JsonElement jelemList : importJson.getAsJsonArray()) {
            JsonObject obj = jelemList.getAsJsonObject();
            if (obj.has("text")) {
                modifiersList.addProperty("hasFunctionVisibility", obj.get("text").getAsString());
            } else if (obj.has("modifierInvocation")) {
                modifierSpec.addProperty("modifierName",
                        obj.get("modifierInvocation").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString());
            } else if (obj.has("stateMutability")) {
                modifiersList.addProperty("@type",
                        obj.get("stateMutability").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString());
            } else if (obj.has("overrideSpecifier")) {
                modifierSpec.addProperty("hasModifierBehaviour",
                        obj.get("overrideSpecifier").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString());
            }
        }
        if (!modifiersList.has("hasFunctionVisibility")) {
            modifiersList.addProperty("hasFunctionVisibility", "public");
        }
        if (!modifiersList.has("@type")) {
            modifiersList.addProperty("@type", "public");
        }
        if (!modifierSpec.entrySet().isEmpty()) {
            modifiersList.add("isDefinedAs", modifierSpec);
        }
        return modifiersList;
    }

    private JsonArray analyseInputParam(JsonArray importJson) {
        JsonArray paramsList = new JsonArray();
        int position = 0;
        for (JsonElement jelemList : importJson.getAsJsonArray()) {
            JsonObject param = new JsonObject();
            JsonObject listObj = jelemList.getAsJsonObject();
            if (!listObj.has("parameter")) continue;
            for (JsonElement jelem : listObj.get("parameter").getAsJsonArray()) {
                JsonObject p = jelem.getAsJsonObject();
                if (p.has("name")) {
                    param.addProperty("hasParameterName", p.get("name").getAsString());
                } else if (p.has("storageLocation")) {
                    param.addProperty("hasParameterTypeWithDataLocation",
                            p.get("storageLocation").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString());
                } else if (p.has("typeName")) {
                    JsonObject tn = p.get("typeName").getAsJsonArray().get(0).getAsJsonObject();
                    if (tn.has("userDefinedTypeName")) {
                        String name = tn.get("userDefinedTypeName").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString();
                        param.addProperty("hasParameterType", searchNameAndInclude(name));
                    } else if (tn.has("elementaryTypeName")) {
                        String name = tn.get("elementaryTypeName").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString();
                        param.add("hasParameterType", catalogueElementaryType(name));
                    } else if (tn.has("typeName")) {
                        param.add("hasParameterType", analyseArrays(tn.get("typeName").getAsJsonArray()));
                    } else if (tn.has("mapping")) {
                        param.add("hasParameterType", analyseMapping(tn.get("mapping").getAsJsonArray()));
                    }
                }
            }
            param.addProperty("hasParameterPosition", position);
            position++;
            paramsList.add(param);
        }
        return paramsList;
    }

    private JsonObject analyseReceive(JsonArray importReceive) {
        JsonObject modifier = new JsonObject();
        JsonObject first = importReceive.getAsJsonArray().get(0).getAsJsonObject();
        if (first.get("functionDescriptor").getAsJsonArray().get(0).getAsJsonObject()
                .get("text").getAsString().contentEquals("receive")) {
            for (JsonElement jelem : importReceive.getAsJsonArray()) {
                JsonObject obj = jelem.getAsJsonObject();
                if (obj.has("code")) {
                    modifier.addProperty("receiveCode", obj.get("code").getAsString());
                } else {
                    modifier.addProperty("hasReceiveBehaviour", "payable");
                    modifier.addProperty("hasReceiveVisibility", "external");
                }
            }
        }
        return modifier;
    }

    private JsonObject analyseFallback(JsonArray importFallback) {
        JsonObject modifier = new JsonObject();
        JsonObject first = importFallback.getAsJsonArray().get(0).getAsJsonObject();
        if (first.get("functionDescriptor").getAsJsonArray().get(0).getAsJsonObject()
                .get("text").getAsString().contentEquals("fallback")) {
            for (JsonElement jelem : importFallback.getAsJsonArray()) {
                JsonObject obj = jelem.getAsJsonObject();
                if (obj.has("code")) {
                    modifier.addProperty("fallbackCode", obj.get("code").getAsString());
                } else {
                    modifier.addProperty("hasFallbackBehaviour", "payable");
                    modifier.addProperty("hasFallbackVisibility", "external");
                }
            }
        }
        return modifier;
    }

    private JsonObject analyseConstructor(JsonArray importConstructor) {
        JsonObject constructor = new JsonObject();
        JsonObject first = importConstructor.getAsJsonArray().get(0).getAsJsonObject();
        if (first.get("functionDescriptor").getAsJsonArray().get(0).getAsJsonObject()
                .get("text").getAsString().contentEquals("constructor")) {
            for (JsonElement jelem : importConstructor.getAsJsonArray()) {
                JsonObject obj = jelem.getAsJsonObject();
                if (obj.has("code")) {
                    constructor.addProperty("constructorCode", obj.get("code").getAsString());
                } else if (obj.has("parameterList")) {
                    constructor.add("hasConstructorArguments",
                            analyseInputParam(obj.get("parameterList").getAsJsonArray()));
                }
            }
        }
        return constructor;
    }

    private JsonObject analyseAttribute(JsonArray importAttribute) {
        JsonObject attribute = new JsonObject();
        String type = "";
        for (JsonElement jelem : importAttribute.getAsJsonArray()) {
            JsonObject obj = jelem.getAsJsonObject();
            if (obj.has("visibility")) {
                attribute.addProperty("hasAttributeVisibility", obj.get("visibility").getAsString());
            } else if (obj.has("name")) {
                attribute.addProperty("attributeName", obj.get("name").getAsString());
            } else if (obj.has("value")) {
                JsonObject newValue = new JsonObject();
                if (type.contentEquals("int") || type.contentEquals("uint")) {
                    newValue.addProperty("simpleInt", obj.get("value").getAsString());
                } else if (type.contentEquals("bool")) {
                    newValue.addProperty("simpleString", obj.get("value").getAsBoolean());
                } else if (type.contentEquals("address") || type.contentEquals("string") || type.contentEquals("bytes")) {
                    newValue.addProperty("simpleString", obj.get("value").getAsString());
                } else {
                    newValue.addProperty("simpleGeneric", obj.get("value").getAsString());
                }
                attribute.add("hasAttributeValue", newValue);
            } else if (obj.has("typeName")) {
                JsonObject typeName = obj.get("typeName").getAsJsonArray().get(0).getAsJsonObject();
                if (typeName.has("userDefinedTypeName")) {
                    JsonObject typeUDT = new JsonObject();
                    typeUDT.addProperty("@type",
                            searchNameAndInclude(typeName.get("userDefinedTypeName").getAsJsonArray().get(0)
                                    .getAsJsonObject().get("name").getAsString()));
                    attribute.add("hasNonConstantType", typeUDT);
                } else if (typeName.has("mapping")) {
                    JsonObject nonConstantTypeMapping = analyseMapping(typeName.get("mapping").getAsJsonArray());
                    nonConstantTypeMapping.addProperty("@type", "MapType");
                    attribute.add("hasConstantType", nonConstantTypeMapping);
                } else if (typeName.has("elementaryTypeName")) {
                    String attributeType = typeName.get("elementaryTypeName").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString();
                    attribute.addProperty("@type", "nonConstantAttributeSpecification");
                    attribute.add("hasNonConstantType", catalogueElementaryType(attributeType));
                    type = attributeType;
                } else {
                    JsonObject constantType = new JsonObject();
                    JsonObject nonConstantType = new JsonObject();
                    JsonObject nonConstantTypeRelation = new JsonObject();
                    for (JsonElement arrayElem : obj.get("typeName").getAsJsonArray()) {
                        JsonObject ae = arrayElem.getAsJsonObject();
                        if (ae.has("isArray")) {
                            constantType.addProperty("@type", "ArrayType");
                        } else if (ae.has("typeName")) {
                            JsonObject innerTn = ae.get("typeName").getAsJsonArray().get(0).getAsJsonObject();
                            if (innerTn.has("elementaryTypeName")) {
                                String attributeType = innerTn.get("elementaryTypeName").getAsJsonArray()
                                        .get(0).getAsJsonObject().get("text").getAsString();
                                nonConstantTypeRelation.add("hasNonConstantType", catalogueElementaryType(attributeType));
                            } else if (innerTn.has("userDefinedTypeName")) {
                                String value = innerTn.get("userDefinedTypeName").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString();
                                nonConstantTypeRelation.addProperty("hasNonConstantType", searchNameAndInclude(value));
                            }
                        }
                    }
                    nonConstantType.add("hasNonConstantType", nonConstantTypeRelation);
                    constantType.add("hasNonConstantType", nonConstantType);
                    attribute.add("hasConstantType", constantType);
                }
            } else if (obj.has("isImmutable")) {
                attribute.addProperty("isInmutable", obj.get("isImmutable").getAsBoolean());
            } else if (obj.has("isConstant")) {
                attribute.addProperty("isConstant", obj.get("isConstant").getAsBoolean());
            }
        }
        return attribute;
    }

    private JsonObject analyseMapping(JsonArray input) {
        JsonObject nonConstantTypeMapping = new JsonObject();
        for (JsonElement mappingElement : input) {
            JsonObject me = mappingElement.getAsJsonObject();
            if (me.has("elementaryTypeName")) {
                nonConstantTypeMapping.addProperty("hasKeyMap",
                        me.get("elementaryTypeName").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString());
            } else if (me.has("typeName")) {
                JsonObject tn = me.get("typeName").getAsJsonArray().get(0).getAsJsonObject();
                if (tn.has("elementaryTypeName")) {
                    String attributeType = tn.get("elementaryTypeName").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString();
                    nonConstantTypeMapping.add("hasValueMap", catalogueElementaryType(attributeType));
                } else if (tn.has("userDefinedTypeName")) {
                    nonConstantTypeMapping.addProperty("hasValueMap",
                            searchNameAndInclude(tn.get("userDefinedTypeName").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString()));
                } else if (tn.has("mapping")) {
                    nonConstantTypeMapping.add("hasValueMap", analyseMapping(tn.get("mapping").getAsJsonArray()));
                    nonConstantTypeMapping.get("hasValueMap").getAsJsonObject().addProperty("@type", "MapType");
                } else {
                    nonConstantTypeMapping.add("hasParameterType", analyseArrays(tn.get("typeName").getAsJsonArray()));
                }
            }
        }
        return nonConstantTypeMapping;
    }

    public String[] splitBytesAndNumber(String entrada) {
        String parteNoNumerica = entrada.replaceAll("[0-9]", "");
        String parteNumerica = entrada.replaceAll("[^0-9]", "");
        return new String[]{parteNoNumerica, parteNumerica};
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
