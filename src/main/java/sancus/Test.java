package sancus;

import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import oeg.upm.solidityantlr.SolidityParser.ContractDefinitionContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import solidity.provider.SolidityToJSONParser;

public class Test {

	//	@SuppressWarnings("resource")
	//	public static void main (String[] args) {
	//		String contractInJSON;
	//		try {
	//			JsonParser parser = new JsonParser();
	//			contractInJSON = new Scanner(new File("./contracts/0x4575f41308ec1483f3d399aa9a2826d74da13deb.sol")).useDelimiter("\\Z").next();
	//			JsonObject contractAntlrFormat = SolidityToJSONParser.contractJsonObject(contractInJSON.toString());
	//			JsonElement jsonElement = parser.parse(contractAntlrFormat.toString());
	//			if (jsonElement.isJsonObject()) {
	//                
	//                if (jsonElement.isJsonObject()) {
	//                	
	//                    JsonObject jsonObject = jsonElement.getAsJsonObject();
	//                    cleanJsonElement(jsonObject, false);
	//                    transformContractDefinitions(jsonObject);
	//                    transformIdentifiers(jsonObject);
	//
	//                    JsonArray sourceUnit = jsonObject.getAsJsonArray("sourceUnit");
	//                    JsonArray groupedArray = groupElements(sourceUnit);
	//                    jsonObject.add("sourceUnit", groupedArray);
	//                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
	//                    File file = new File("./export/test.json");
	////                    writerToFile(file, jsonElement.toString());
	//                    writerToFile(file, gson.toJson(jsonObject));
	//                }
	//            }
	//		} catch (FileNotFoundException e) {
	//			e.printStackTrace();
	//		} catch (Exception e) {
	//            e.printStackTrace();
	//        }
	//	}
	

	@SuppressWarnings("resource")
	public static void main (String[] args) {
		String directoryPath = "./contractsWithLicense";

		try {
			readAllJSONFiles(directoryPath);
			System.out.println("Finish");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void aaa (String directoryPath) throws IOException {
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directoryPath))) {
			for (Path path : directoryStream) {
				if (!Files.isDirectory(path)) {
					if(path.getFileName().toString().contentEquals("0x00000000000000adc04c56bf30ac9d3c0aaf14dc.sol")) {
						JsonElement jsonElementOr = JsonParser.parseString(checkIsJSONandModify(readFile(path.toString(), StandardCharsets.UTF_8)));
						String resultProcess = checkContractJSONsources(jsonElementOr);
						JsonObject contractAntlrFormat = SolidityToJSONParser.contractJsonObject(resultProcess);
						System.out.println(contractAntlrFormat);
					}
				}
			}
		}
	}
	
	public static void readAllJSONFiles(String directoryPath) throws IOException {
		System.out.println("Start");
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directoryPath))) {
			for (Path path : directoryStream) {
				if (!Files.isDirectory(path)) {
					System.out.println(path.getFileName());
					boolean fileIsJSON = isValid(checkIsJSONandModify(readFile(path.toString(), StandardCharsets.UTF_8)));
					if(!fileIsJSON) {
						JsonObject contractAntlrFormat = SolidityToJSONParser.contractJsonObject(readFile(path.toString(), StandardCharsets.UTF_8));
						if (contractAntlrFormat != null) {
							JsonParser parser = new JsonParser();
							JsonElement jsonElement = parser.parse(contractAntlrFormat.toString());
							if (jsonElement.isJsonObject()) {
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
									Gson gson = new GsonBuilder().setPrettyPrinting().create();
									String fileName = extractValueFromFileName(path.getFileName().toString());
									System.out.println(fileName);
									File file = new File("./export/"+fileName+".json");
									writerToFile(file, jsonObject.toString());
									//writerToFile(file, gson.toJson(jsonObject));
							}
						}
					}else {
						JsonParser parserOr = new JsonParser();
						JsonElement jsonElementOr = parserOr.parse(checkIsJSONandModify(readFile(path.toString(), StandardCharsets.UTF_8)));
						if (jsonElementOr.isJsonObject()) {
							String resultProcess = checkContractJSONsources(jsonElementOr);
							JsonObject contractAntlrFormat = SolidityToJSONParser.contractJsonObject(resultProcess);
							if (contractAntlrFormat != null) {
								JsonParser parser = new JsonParser();
								JsonElement jsonElement = parser.parse(contractAntlrFormat.toString());
								if (jsonElement.isJsonObject()) {
									if (jsonElement.isJsonObject()) {
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
										Gson gson = new GsonBuilder().setPrettyPrinting().create();
										String fileName = extractValueFromFileName(path.getFileName().toString());
										System.out.println(fileName);
										File file = new File("./export/"+fileName+".json");
										writerToFile(file, jsonObject.toString());
										//writerToFile(file, gson.toJson(jsonObject));
									}
								}
							}
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    public static void createValueStateVariableDeclarations(JsonObject jsonObject) {
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

    private static void processStateVariableDeclarationsArrayForValue(JsonArray jsonArray) {
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

    private static String extractAndCombineTextFromExpression(JsonElement expression) {
        StringBuilder valueBuilder = new StringBuilder();
        extractTextRecursively(expression, valueBuilder);
        return valueBuilder.toString();
    }

    private static void extractTextRecursively(JsonElement element, StringBuilder valueBuilder) {
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


    
    

    public static void modifyConstantInStateVariableDeclarations(JsonObject jsonObject) {
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

    private static void processStateVariableDeclarationsArrayForConstant(JsonArray jsonArray) {
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
	
    private static void modifyContractPartJson(JsonObject jsonObject) {
        JsonArray contractParts = jsonObject.getAsJsonArray("contractPart");
        if (contractParts != null) {
            for (JsonElement contractPartElement : contractParts) {
                for (Entry<String, JsonElement> entry : contractPartElement.getAsJsonObject().entrySet()) {
                    jsonObject.add(entry.getKey(), entry.getValue());
                }
            }
            jsonObject.remove("contractPart");
        }
        // Recursivamente modificar elementos anidados en el objeto
        for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
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

	private static void transformIdentifiers(JsonObject jsonObject) {
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

	private static void transformIdentifiersInArray(JsonArray jsonArray) {
		for (int i = 0; i < jsonArray.size(); i++) {
			JsonElement element = jsonArray.get(i);
			if (element.isJsonObject()) {
				transformIdentifiers(element.getAsJsonObject());
			} else if (element.isJsonArray()) {
				transformIdentifiersInArray(element.getAsJsonArray());
			}
		}
	}

	private static void transformContractDefinitions(JsonObject jsonObject) {
		JsonArray sourceUnit = jsonObject.getAsJsonArray("sourceUnit");
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

	private static void transformContractDefinitionElements(JsonArray contractDefinition) {
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

	private static void cleanJsonElement(JsonElement element, boolean insideBlock) {
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
					keysToRemove.add(key); // Siempre elimina el "block" original, independientemente de si "code" está vacío o no
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

	private static String processBlock(JsonArray block) {
		StringBuilder codeBuilder = new StringBuilder();
		processBlockHelper(block, codeBuilder, false);
		return codeBuilder.toString();
	}

	private static void processBlockHelper(JsonArray jsonArray, StringBuilder codeBuilder, boolean isStatement) {
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

	private static boolean shouldAddSpaceBefore(String text, StringBuilder codeBuilder) {
		if (codeBuilder.length() == 0 || text.length() == 0) {
			return false;
		}
		char lastCharInBuilder = codeBuilder.charAt(codeBuilder.length() - 1);
		char firstCharInText = text.charAt(0);

		if (isOperatorChar(firstCharInText)|| firstCharInText == '[' || firstCharInText == ']' || firstCharInText == '(' || firstCharInText == ';' || firstCharInText == '.' || firstCharInText == ',' || firstCharInText == ')') {
			return false; // No añadir espacio antes de un operador, punto, coma o paréntesis de cierre
		}

		if (lastCharInBuilder == '(') {
			return false; // No añadir espacio después de un paréntesis de apertura
		}

		return !(isOperatorChar(lastCharInBuilder) || lastCharInBuilder == '.');
	}


	private static boolean isOperatorChar(char c) {
		return c == '+' || c == '-' || c == '*' || c == '/' || c == '&' || c == '|' || c == '^' || c == '!';
	}


	private static boolean shouldBeRemoved(String value) {
		return value.equals("(") || value.equals(",") || value.equals("is") || value.equals("returns") || value.equals("solidity") || value.equals("pragma") || value.equals("pragma") || value.equals(")") || value.equals(";") || value.equals("{") || value.equals("}") || value.equals("<EOF>");
	}

	private static boolean shouldBeRemoved(JsonElement element) {
		return element.isJsonPrimitive() && shouldBeRemoved(element.getAsString());
	}

    private static JsonArray groupElements(JsonArray sourceUnit) {
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
            } else if(obj.has("importDirective")) {
            	currentGroup.add(element);
                addGroupToResult(result, currentGroup, importDirectives);
            }
        }
        return result;
    }
    
    private static void processImportDirective(JsonObject importDirectiveObj) {
        JsonArray importArray = importDirectiveObj.getAsJsonArray("importDirective");
        if (importArray != null) {
            // Eliminar "text": "import" usando un Iterator
            Iterator<JsonElement> iterator = importArray.iterator();
            while (iterator.hasNext()) {
                JsonElement elem = iterator.next();
                if (elem.isJsonObject() && elem.getAsJsonObject().has("text") && "import".equals(elem.getAsJsonObject().get("text").getAsString())) {
                    iterator.remove();
                }
            }

            // Procesar los últimos elementos con "text"
            if (importArray.size() >= 2) {
                JsonObject secondLast = importArray.get(importArray.size() - 2).getAsJsonObject();
                JsonObject last = importArray.get(importArray.size() - 1).getAsJsonObject();

                if (secondLast.has("text") && last.has("text")) {
                    String lastText = last.get("text").getAsString();
                    importArray.remove(importArray.size() - 2); // Eliminar el penúltimo elemento
                    last.addProperty("from", lastText);
                    last.remove("text");
                }
            }
        }
    }

    private static void addGroupToResult(JsonArray result, ArrayList<JsonElement> group, List<JsonElement> importDirectives) {
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

    private static JsonElement createUnknownPragmaDirective() {
        JsonObject unknownPragmaDirective = new JsonObject();
        unknownPragmaDirective.addProperty("pragmaDirective", "Unknown");
        return unknownPragmaDirective;
    }
	
	private static String concatenateTextValues(JsonArray jsonArray) {
		StringBuilder textBuilder = new StringBuilder();
		for (JsonElement element : jsonArray) {
			concatenateTextValuesHelper(element, textBuilder);
		}
		return textBuilder.toString();
	}

	private static void concatenateTextValuesHelper(JsonElement element, StringBuilder textBuilder) {
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

	private static String checkContractJSONsources(JsonElement element) {
		StringBuilder textBuilder = new StringBuilder();
		if (element.isJsonObject()) {
			if(element.getAsJsonObject().has("sources")){
				JsonObject jsonObject = element.getAsJsonObject().get("sources").getAsJsonObject();
				for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
					String jsonContent = element.getAsJsonObject().get("sources").getAsJsonObject().get(entry.getKey()).getAsJsonObject().get("content").getAsString();
					textBuilder.append(jsonContent);
				}
			}else {
				JsonObject jsonObject = element.getAsJsonObject();
				for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
					if(element.getAsJsonObject().get(entry.getKey()).getAsJsonObject().has("content")) {
						String jsonContent = element.getAsJsonObject().get(entry.getKey()).getAsJsonObject().get("content").getAsString();
						textBuilder.append(jsonContent);
					}
				}
			}
		}
		return textBuilder.toString();
	}

	public static boolean isValid(String json) {
		try {
			JsonParser.parseString(json);
		} catch (JsonSyntaxException e) {
			return false;
		}
		return true;
	}

	public static String checkIsJSONandModify(String code) {
		if(code.startsWith("{{") && code.endsWith("}}")) {
			return code.substring(1, code.length() - 1);
		}
		return code;
	}
	
	
    public static void modifyJsonArrayNotation(JsonObject jsonObject) {
        jsonObject.entrySet().forEach(entry -> {
            JsonElement element = entry.getValue();

            if (element.isJsonObject()) {
                modifyJsonArrayNotation(element.getAsJsonObject());
            } else if (element.isJsonArray()) {
                processJsonArray(element.getAsJsonArray());
            }
        });
    }

    private static void processJsonArray(JsonArray jsonArray) {
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonElement element = jsonArray.get(i);
            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                if (i < jsonArray.size() - 1 && isBracketPair(object, jsonArray.get(i + 1))) {
                    JsonObject newArrayIndicator = new JsonObject();
                    newArrayIndicator.addProperty("isArray", true);
                    jsonArray.set(i, newArrayIndicator);
                    jsonArray.remove(i + 1); // Remove "]" element
                    i--; // Adjust index after removal
                } else {
                    modifyJsonArrayNotation(object);
                }
            } else if (element.isJsonArray()) {
                processJsonArray(element.getAsJsonArray());
            }
        }
    }

    private static boolean isBracketPair(JsonObject currentObj, JsonElement nextElement) {
        if (nextElement.isJsonObject()) {
            JsonObject nextObj = nextElement.getAsJsonObject();
            return currentObj.has("text") && currentObj.get("text").getAsString().equals("[") &&
                   nextObj.has("text") && nextObj.get("text").getAsString().equals("]");
        }
        return false;
    }
	
    public static void modifyStateVariableDeclarations(JsonObject jsonObject) {
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

    private static void processStateVariableDeclarationsArray(JsonArray jsonArray) {
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
    
    public static void modifyVisibilityInStateVariableDeclarations(JsonObject jsonObject) {
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

    private static void processStateVariableDeclarationsArrayForVisibility(JsonArray jsonArray) {
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

    private static boolean isVisibilityKeyword(String text) {
        return "private".equals(text) || "public".equals(text) ||
               "internal".equals(text) || "external".equals(text);
    }

	
//	public static void readAllJSONFiles(String directoryPath) throws IOException {
//		System.out.println("Start");
//		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directoryPath))) {
//			for (Path path : directoryStream) {
//				if (!Files.isDirectory(path)) {
//					System.out.println(path.getFileName());
//					boolean fileIsJSON = isValid(checkIsJSONandModify(readFile(path.toString(), StandardCharsets.UTF_8)));
//					if(!fileIsJSON) {
//						JsonObject contractAntlrFormat = SolidityToJSONParser.contractJsonObject(readFile(path.toString(), StandardCharsets.UTF_8));
//						if (contractAntlrFormat != null) {
//							JsonParser parser = new JsonParser();
//							JsonElement jsonElement = parser.parse(contractAntlrFormat.toString());
//							if (jsonElement.isJsonObject()) {
//
//									JsonObject jsonObject = jsonElement.getAsJsonObject();
//									cleanJsonElement(jsonObject, false);
//									transformContractDefinitions(jsonObject);
//									transformIdentifiers(jsonObject);
//									
//									JsonArray sourceUnit = jsonObject.getAsJsonArray("sourceUnit");
//									JsonArray groupedArray = groupElements(sourceUnit);
//									jsonObject.add("sourceUnit", groupedArray);
//									Gson gson = new GsonBuilder().setPrettyPrinting().create();
//									String fileName = extractValueFromFileName(path.getFileName().toString());
//									System.out.println(fileName);
//									File file = new File("./export/"+fileName+".json");
//									writerToFile(file, jsonObject.toString());
//									//									writerToFile(file, gson.toJson(jsonObject));
//							}
//						}
//					}else {
//						JsonParser parserOr = new JsonParser();
//						JsonElement jsonElementOr = parserOr.parse(checkIsJSONandModify(readFile(path.toString(), StandardCharsets.UTF_8)));
//						if (jsonElementOr.isJsonObject()) {
//							String resultProcess = checkContractJSONsources(jsonElementOr);
//							JsonObject contractAntlrFormat = SolidityToJSONParser.contractJsonObject(resultProcess);
//							if (contractAntlrFormat != null) {
//								JsonParser parser = new JsonParser();
//								JsonElement jsonElement = parser.parse(contractAntlrFormat.toString());
//								if (jsonElement.isJsonObject()) {
//
//									if (jsonElement.isJsonObject()) {
//
//										JsonObject jsonObject = jsonElement.getAsJsonObject();
//										cleanJsonElement(jsonObject, false);
//										transformContractDefinitions(jsonObject);
//										transformIdentifiers(jsonObject);
//										
//										JsonArray sourceUnit = jsonObject.getAsJsonArray("sourceUnit");
//										JsonArray groupedArray = groupElements(sourceUnit);
//										jsonObject.add("sourceUnit", groupedArray);
//										Gson gson = new GsonBuilder().setPrettyPrinting().create();
//										String fileName = extractValueFromFileName(path.getFileName().toString());
//										System.out.println(fileName);
//										File file = new File("./export/"+fileName+".json");
//										writerToFile(file, jsonObject.toString());
//										//										writerToFile(file, gson.toJson(jsonObject));
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	static String readFile(String path, Charset encoding)
			throws IOException
	{
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	public static String extractValueFromFileName(String fileName) {
		String regex = "(0x[a-fA-F0-9]+)\\.sol";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(fileName);

		if (matcher.matches()) {
			return matcher.group(1);
		} else {
			return null;
		}
	}
	
	private static void writerToFile(File file, String toFile) {
		try {
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(toFile);
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static JSONObject readJSONFromFile(Path path) {
		if (!path.toString().endsWith(".json")) {
			System.err.println(path + " no es un archivo JSON. Omitiendo...");
			return null;
		}
		try (InputStream is = Files.newInputStream(path)) {
			JSONTokener tokener = new JSONTokener(is);
			return new JSONObject(tokener);
		} catch (IOException e) {
			System.err.println("Error al leer el archivo: " + path);
			e.printStackTrace();
			return null;
		}
	}

}
