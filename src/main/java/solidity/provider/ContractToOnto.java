package solidity.provider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ContractToOnto {

	private static JsonArray namesContract = new JsonArray();

	private static void getAllNames(JsonArray contracts) {
		String uuid = UUID.randomUUID().toString();
		for(JsonElement contract : contracts) {
			for(JsonElement name : contract.getAsJsonArray()) {
				if(name.getAsJsonObject().has("contractDefinition")){
					for(JsonElement contractWithoutVersion : name.getAsJsonObject().get("contractDefinition").getAsJsonArray()) {
						if(contractWithoutVersion.getAsJsonObject().has("name")) {
							JsonObject nameAndUUID = new JsonObject();
							nameAndUUID.addProperty("name", contractWithoutVersion.getAsJsonObject().get("name").getAsString());
							nameAndUUID.addProperty("uuid", "uuid:"+uuid+"-"+ contractWithoutVersion.getAsJsonObject().get("name").getAsString());
							namesContract.add(nameAndUUID);
						}
					}
				}
			}
		}
	}

	public JsonArray finalJson(String contractConverted) {
		JsonArray contractInput = JsonParser.parseString(contractConverted.toString()).getAsJsonObject().get("sourceUnit").getAsJsonArray();
		JsonArray result = new JsonArray();
		getAllNames(contractInput);
		for(JsonElement jarry: contractInput) {
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
			for(JsonElement contract : jarry.getAsJsonArray()) {
				finalJSON.addProperty("@context", "https://oeg-upm.github.io/Solidity-ontology/context/context.json");
				if(contract.getAsJsonObject().has("pragmaDirective")) {
					finalJSON.addProperty("version", contract.getAsJsonObject().get("pragmaDirective").getAsString());
				}else if(contract.getAsJsonObject().has("contractDefinition")){
					String contractName = "";
					String contractType = "";
					boolean isAbstract = false;
					for(JsonElement contractWithoutVersion : contract.getAsJsonObject().get("contractDefinition").getAsJsonArray()) {
						if(contractWithoutVersion.getAsJsonObject().has("name")) {
							contractName = contractWithoutVersion.getAsJsonObject().get("name").getAsString();
						}else if (contractWithoutVersion.getAsJsonObject().has("contractType")) {
							contractType = contractWithoutVersion.getAsJsonObject().get("contractType").getAsString();
						}else if (contractWithoutVersion.getAsJsonObject().has("isAbstract")) {
							isAbstract = contractWithoutVersion.getAsJsonObject().get("isAbstract").getAsBoolean();
						}else if (contractWithoutVersion.getAsJsonObject().has("inheritanceSpecifier")) {
							inheritance.add(inheritance(contractWithoutVersion.getAsJsonObject().get("inheritanceSpecifier").getAsJsonArray()));
						}else if (contractWithoutVersion.getAsJsonObject().has("functionDefinition")) {
							if(contractWithoutVersion.getAsJsonObject().get("functionDefinition").getAsJsonArray().get(0).getAsJsonObject().get("functionDescriptor").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString().contentEquals("function")) {
								functions.add(analyseFunction(contractWithoutVersion.getAsJsonObject().get("functionDefinition").getAsJsonArray()));
							}else if(contractWithoutVersion.getAsJsonObject().get("functionDefinition").getAsJsonArray().get(0).getAsJsonObject().get("functionDescriptor").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString().contentEquals("constructor")) {
								constructor.add(analyseConstructor(contractWithoutVersion.getAsJsonObject().get("functionDefinition").getAsJsonArray()));
							}else if(contractWithoutVersion.getAsJsonObject().get("functionDefinition").getAsJsonArray().get(0).getAsJsonObject().get("functionDescriptor").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString().contentEquals("receive")) {
								receive.add(analyseReceive(contractWithoutVersion.getAsJsonObject().get("functionDefinition").getAsJsonArray()));
							}else if(contractWithoutVersion.getAsJsonObject().get("functionDefinition").getAsJsonArray().get(0).getAsJsonObject().get("functionDescriptor").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString().contentEquals("fallback")) {
								fallback.add(analyseFallback(contractWithoutVersion.getAsJsonObject().get("functionDefinition").getAsJsonArray()));
							}
						}else if (contractWithoutVersion.getAsJsonObject().has("usingForDeclaration")) {
							isUsingFor.add(analyseIsUsingFor(contractWithoutVersion.getAsJsonObject().get("usingForDeclaration").getAsJsonArray()));
						}else if (contractWithoutVersion.getAsJsonObject().has("eventDefinition")) {
							events.add(analyseEvents(contractWithoutVersion.getAsJsonObject().get("eventDefinition").getAsJsonArray()));
						}else if (contractWithoutVersion.getAsJsonObject().has("stateVariableDeclaration")) {
							attributes.add(analyseAttribute(contractWithoutVersion.getAsJsonObject().get("stateVariableDeclaration").getAsJsonArray()));
						}else if (contractWithoutVersion.getAsJsonObject().has("structDefinition")) {
							structs.add(analyseStructs(contractWithoutVersion.getAsJsonObject().get("structDefinition").getAsJsonArray()));
						}else if (contractWithoutVersion.getAsJsonObject().has("modifierDefinition")) {
							modifiers.add(analyseModifier(contractWithoutVersion.getAsJsonObject().get("modifierDefinition").getAsJsonArray()));
						}else if (contractWithoutVersion.getAsJsonObject().has("enumDefinition")) {
							attributes.add(analyseEnum(contractWithoutVersion.getAsJsonObject().get("enumDefinition").getAsJsonArray()));
						}
						for(JsonElement name: namesContract) {
							if(name.getAsJsonObject().get("name").getAsString().contentEquals(contractName)) {
								finalJSON.addProperty("@id", name.getAsJsonObject().get("uuid").getAsString());
							}
						}
						if(!finalJSON.has("@id")) {
							finalJSON.addProperty("@id", "uuid:"+UUID.randomUUID().toString()+"-"+contractType);
						}
						finalJSON.addProperty("contractName", contractName);
						finalJSON.addProperty("@type", contractType);
						finalJSON.addProperty("isAbstract", isAbstract);
					}
				}else if(contract.getAsJsonObject().has("importDirective")){
					imports.add(analyseImports(contract.getAsJsonObject().get("importDirective").getAsJsonArray()));
				}
			}
			if(!inheritance.isEmpty()) {
				finalJSON.add("inheritance", inheritance);
			}
			if(!imports.isEmpty()) {
				finalJSON.add("hasImport", imports);
			}
			if(!events.isEmpty()) {
				finalJSON.add("hasImplementationEvent", events);
			}
			if(!modifiers.isEmpty()) {
				finalJSON.add("hasImplementationModifier", modifiers);
			}
			if(!functions.isEmpty()) {
				finalJSON.add("hasImplementationFunction", functions);
			}
			if(!constructor.isEmpty()) {
				finalJSON.add("hasContractConstructor", constructor);
			}
			if(!fallback.isEmpty()) {
				finalJSON.add("hasContractFallback", fallback);
			}
			if(!receive.isEmpty()) {
				finalJSON.add("hasContractReceive", receive);
			}
			if(!attributes.isEmpty()) {
				finalJSON.add("hasContractAttribute", attributes);
			}
			if(!isUsingFor.isEmpty()) {
				finalJSON.add("hasContractUsingForDirective", isUsingFor);
			}
			if(!structs.isEmpty()) {
				finalJSON.add("hasImplementationStructType", structs);
			}
			result.add(finalJSON);
		}
		return result;
	}

	private static JsonArray inheritance(JsonArray importInheritance) {
		JsonArray inheritance = new JsonArray();
		for(JsonElement jelem: importInheritance.getAsJsonArray()) {
			if(jelem.getAsJsonObject().has("userDefinedTypeName")) {
				String importName = extractImportName(jelem.getAsJsonObject().get("userDefinedTypeName").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString());
				inheritance.add(searchNameAndInclude(importName));
			}
		}
		return inheritance;
	}

	private static JsonObject analyseIsUsingFor(JsonArray importJson) {
		JsonObject usingFor = new JsonObject();
		for(JsonElement jelem: importJson.getAsJsonArray()) {
			if(jelem.getAsJsonObject().has("name")) {
				usingFor.addProperty("usingForName", jelem.getAsJsonObject().get("name").getAsString());
			}else if(jelem.getAsJsonObject().has("typeName")) {
				if(jelem.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().has("userDefinedTypeName")) {
					String value = jelem.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().get("userDefinedTypeName").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString();				
					usingFor.addProperty("isUsingLibrary",searchNameAndInclude(value));
				}else if(jelem.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().has("elementaryTypeName")){
					String value = jelem.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().get("elementaryTypeName").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString();
					usingFor.add("isUsingLibrary", catalogueElementaryType(value));
				}
			}
		}
		return usingFor;
	}

	private static JsonObject analyseStructs(JsonArray importJson) {
		JsonObject struct = new JsonObject();
		JsonArray elementsInStruct = new JsonArray();
		for(JsonElement jelem: importJson.getAsJsonArray()) {
			if(jelem.getAsJsonObject().has("name")) {
				struct.addProperty("structName", jelem.getAsJsonObject().get("name").getAsString());
			}else if(jelem.getAsJsonObject().has("variableDeclaration")) {
				JsonObject structElement = new JsonObject();
				if(jelem.getAsJsonObject().get("variableDeclaration").getAsJsonArray().get(0).getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().has("mapping")) {
					structElement.add("hasNonConstantType", analyseMapping(jelem.getAsJsonObject().get("variableDeclaration").getAsJsonArray().get(0).getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().get("mapping").getAsJsonArray()));
					structElement.get("hasNonConstantType").getAsJsonObject().addProperty("structAttributeName", jelem.getAsJsonObject().get("variableDeclaration").getAsJsonArray().get(1).getAsJsonObject().get("name").getAsString());
					structElement.get("hasNonConstantType").getAsJsonObject().addProperty("@type", "MapType");
					elementsInStruct.add(structElement);
				}else if(jelem.getAsJsonObject().get("variableDeclaration").getAsJsonArray().get(0).getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().has("elementaryTypeName")) {
					String typeElement = jelem.getAsJsonObject().get("variableDeclaration").getAsJsonArray().get(0).getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().get("elementaryTypeName").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString();
					structElement.add("hasNonConstantType",catalogueElementaryType(typeElement));
					structElement.get("hasNonConstantType").getAsJsonObject().addProperty("structAttributeName", jelem.getAsJsonObject().get("variableDeclaration").getAsJsonArray().get(1).getAsJsonObject().get("name").getAsString());
					elementsInStruct.add(structElement);
				}else if(jelem.getAsJsonObject().get("variableDeclaration").getAsJsonArray().get(0).getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().has("typeName")) {
					structElement.add("hasNonConstantType", analyseArrays(jelem.getAsJsonObject().get("variableDeclaration").getAsJsonArray().get(0).getAsJsonObject().get("typeName").getAsJsonArray()));
					structElement.get("hasNonConstantType").getAsJsonObject().addProperty("structAttributeName", jelem.getAsJsonObject().get("variableDeclaration").getAsJsonArray().get(1).getAsJsonObject().get("name").getAsString());
					elementsInStruct.add(structElement);
				}else  {
					//TODO
				}
			}
		}
		struct.add("hasNonConstantStructAttribute", elementsInStruct);
		return struct;
	}

	private static JsonObject analyseArrays(JsonArray importJson) {
		JsonObject constantType = new JsonObject();
		JsonObject nonConstantType = new JsonObject();
		JsonObject nonConstantTypeRelation = new JsonObject();
		for(JsonElement arrayElem: importJson) {
			if(arrayElem.getAsJsonObject().has("isArray")) {
				constantType.addProperty("@type", "ArrayType");
			}else if(arrayElem.getAsJsonObject().has("typeName")) {
				if(arrayElem.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().has("elementaryTypeName")) {
					String attributeType = arrayElem.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().get("elementaryTypeName").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString();
					nonConstantTypeRelation.add("@type",catalogueElementaryType(attributeType));
				}else if(arrayElem.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().has("userDefinedTypeName")) {
					String value = arrayElem.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().get("userDefinedTypeName").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString();
					nonConstantTypeRelation.addProperty("@type", searchNameAndInclude(value));
				}else {
					nonConstantTypeRelation.add("hasType", analyseArrays(arrayElem.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().get("typeName").getAsJsonArray()));
				}
			}
		}
		nonConstantType.add("hasNonConstantType", nonConstantTypeRelation);
		constantType.add("hasNonConstantType", nonConstantType);
		return constantType;
	}

	private static String searchNameAndInclude(String value) {
		String valueName = new String();
		boolean found = false;
		for(JsonElement name: namesContract) {
			if(value.contentEquals(name.getAsJsonObject().get("name").getAsString())) {
				valueName = name.getAsJsonObject().get("uuid").getAsString();
				found = true;
			}
		}
		if(!found) {
			JsonObject newName = new JsonObject();
			valueName = "uuid:"+ UUID.randomUUID().toString()+"-"+value;
			newName.addProperty("name", value);
			newName.addProperty("uuid", valueName);
			namesContract.add(newName);
		}
		return valueName;
	}

	private static JsonObject catalogueElementaryType(String value) {
		JsonObject typeElementObject = new JsonObject();
		if(value.contains("bytes") || value.contains("int")) {
			String [] bytesNumber = splitBytesAndNumber(value);
			typeElementObject.addProperty("@type", bytesNumber[0]);
			if(bytesNumber[1].isEmpty() && value.contains("bytes")) {
				bytesNumber[1]="32";
			}else if(bytesNumber[1].isEmpty() && value.contains("int")) {
				bytesNumber[1]="256";
			}
			typeElementObject.addProperty("memory", Integer.parseInt(bytesNumber[1]));
		}else {
			typeElementObject.addProperty("@type", value);
		}
		return typeElementObject;
	}

	public static String extractImportName(String importFullName) {
		int lastSlashIndex = importFullName.lastIndexOf('/');
		String importName = (lastSlashIndex == -1) ? importFullName : importFullName.substring(lastSlashIndex + 1);
		int lastDotIndex = importName.lastIndexOf('.');
		if (lastDotIndex != -1) {
			importName = importName.substring(0, lastDotIndex);
		}
		return importName;
	}

	private static JsonObject analyseImports(JsonArray importJson) {
		JsonArray names = new JsonArray();
		JsonObject from = new JsonObject();
		for(JsonElement jelem: importJson.getAsJsonArray()) {
			if(jelem.getAsJsonObject().has("importDeclaration")) {
				String importName = extractImportName(jelem.getAsJsonObject().get("importDeclaration").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString());
				names.add(searchNameAndInclude(importName));
			}
			if(jelem.getAsJsonObject().has("from")) {
				from.addProperty("from",searchNameAndInclude(jelem.getAsJsonObject().get("from").getAsString()));
			}
		}
		from.add("name", names);
		return from;
	}

	private static JsonObject analyseEvents(JsonArray importEvent) {
		JsonObject event = new JsonObject();
		JsonArray eventArguments = new JsonArray();
		for(JsonElement jelem: importEvent.getAsJsonArray()) {
			if(jelem.getAsJsonObject().has("name")) {
				event.addProperty("name", jelem.getAsJsonObject().get("name").getAsString());
			}else if(jelem.getAsJsonObject().has("eventParameterList")) {
				int position = 0;
				for(JsonElement jparam: jelem.getAsJsonObject().get("eventParameterList").getAsJsonArray()) {
					JsonObject eventArgument = new JsonObject();
					eventArgument.addProperty("hasParameterPosition", position);
					if(jparam.getAsJsonObject().has("eventParameter")){
						for(JsonElement jEventParam: jparam.getAsJsonObject().get("eventParameter").getAsJsonArray()) {
							if(jEventParam.getAsJsonObject().has("name")){
								eventArgument.addProperty("hasParameterName", jEventParam.getAsJsonObject().get("name").getAsString());
							}else if (jEventParam.getAsJsonObject().has("text")){
								if(jEventParam.getAsJsonObject().get("text").getAsString().contentEquals("indexed")) {
									eventArgument.addProperty("isIndexed", true);
								}else {
									eventArgument.addProperty("isIndexed", false);
								}
							}else if (jEventParam.getAsJsonObject().has("typeName")){
								JsonObject parameterArgumentType = new JsonObject();
								boolean isArray = false;
								for(JsonElement jEventTypeNameParam: jEventParam.getAsJsonObject().get("typeName").getAsJsonArray()) {
									if(jEventTypeNameParam.getAsJsonObject().has("elementaryTypeName")) {
										if(jEventTypeNameParam.getAsJsonObject().get("elementaryTypeName").getAsJsonArray().get(0).getAsJsonObject().has("text")) {
											parameterArgumentType = catalogueElementaryType(jEventTypeNameParam.getAsJsonObject().get("elementaryTypeName").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString());
										}
									}else if(jEventTypeNameParam.getAsJsonObject().has("userDefinedTypeName")){
										if(jEventTypeNameParam.getAsJsonObject().get("userDefinedTypeName").getAsJsonArray().get(0).getAsJsonObject().has("name")) {
											parameterArgumentType.addProperty("@type",searchNameAndInclude(jEventTypeNameParam.getAsJsonObject().get("userDefinedTypeName").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString()));
										}
									}else if(jEventTypeNameParam.getAsJsonObject().has("typeName") || isArray){
										if(!isArray) {
											if(jEventTypeNameParam.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().has("userDefinedTypeName")) {
												parameterArgumentType.addProperty("hasType",jEventTypeNameParam.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().get("userDefinedTypeName").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString());
											}else if(jEventTypeNameParam.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().has("elementaryTypeName")) {
												parameterArgumentType = catalogueElementaryType(jEventTypeNameParam.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().get("elementaryTypeName").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString());
											}
										}
										isArray = true;
										if(jEventTypeNameParam.getAsJsonObject().has("isArray")){
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
			}else if(jelem.getAsJsonObject().has("text")) {
				if(jelem.getAsJsonObject().get("text").getAsString().contentEquals("anonymous")) {
					event.addProperty("isAnonymous", true);
				}else {
					event.addProperty("isAnonymous", false);
				}
			}
		}
		if(!eventArguments.isEmpty()) {
			event.add("hasEventArguments", eventArguments);
		}
		return event;
	}

	private static JsonObject analyseModifier(JsonArray importModifier) {
		JsonObject modifier = new JsonObject();
		for(JsonElement jelem: importModifier.getAsJsonArray()) {
			if(jelem.getAsJsonObject().has("name")) {
				modifier.addProperty("modifierName", jelem.getAsJsonObject().get("name").getAsString());
			}else if(jelem.getAsJsonObject().has("code")) {
				modifier.addProperty("modifierCode", jelem.getAsJsonObject().get("code").getAsString());
			}else if(jelem.getAsJsonObject().has("parameterList")) {
				modifier.add("hasModifierArgument", analyseInputParam(jelem.getAsJsonObject().get("parameterList").getAsJsonArray()));
			}else if(jelem.getAsJsonObject().has("text")){
				if(jelem.getAsJsonObject().get("text").getAsString().contains("virtual") || jelem.getAsJsonObject().get("text").getAsString().contains("override")) {
					modifier.addProperty("hasModifierBehaviour", jelem.getAsJsonObject().get("text").getAsString());
				}
			}
		}
		return modifier;
	}
	
	private static JsonObject analyseEnum(JsonArray importEvent) {
		JsonObject event = new JsonObject();
		JsonObject option = new JsonObject();
		JsonArray options = new JsonArray();
		for(JsonElement jelem: importEvent.getAsJsonArray()) {
			if(jelem.getAsJsonObject().has("name")) {
				event.addProperty("hasName", jelem.getAsJsonObject().get("name").getAsString());
			}else if(jelem.getAsJsonObject().has("enumValue")) {
				options.add(jelem.getAsJsonObject().get("enumValue").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString());
			}
		}
		option.add("option", options);
		event.add("hasNonConstantType", option);
		return event;
	}

	private static JsonObject analyseFunction(JsonArray importFunction) {
		JsonObject function = new JsonObject();
		if(importFunction.getAsJsonArray().get(0).getAsJsonObject().get("functionDescriptor").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString().contentEquals("function")) {
			for(JsonElement jelem: importFunction.getAsJsonArray()) {
				if(jelem.getAsJsonObject().has("functionDescriptor")) {
					if(jelem.getAsJsonObject().get("functionDescriptor").getAsJsonArray().size()>1) {
						if(jelem.getAsJsonObject().get("functionDescriptor").getAsJsonArray().get(1).getAsJsonObject().has("name")) {
							String nameFunction = jelem.getAsJsonObject().get("functionDescriptor").getAsJsonArray().get(1).getAsJsonObject().get("name").getAsString();
							function.add("functionName", catalogueElementaryType(nameFunction));
						}
					}
				}else if(jelem.getAsJsonObject().has("code")) {
					function.addProperty("functionCode", jelem.getAsJsonObject().get("code").getAsString());
				}else if(jelem.getAsJsonObject().has("modifierList")) {
					function.add("hasFunctionBehaviour", analyseModifiersParam(jelem.getAsJsonObject().get("modifierList").getAsJsonArray()));
				}else if(jelem.getAsJsonObject().has("parameterList")) {
					function.add("hasFunctionArguments", analyseInputParam(jelem.getAsJsonObject().get("parameterList").getAsJsonArray()));
				}else if(jelem.getAsJsonObject().has("returnParameters")) {
					function.add("hasFunctionReturn", analyseInputParam(jelem.getAsJsonObject().get("returnParameters").getAsJsonArray().get(0).getAsJsonObject().get("parameterList").getAsJsonArray()));
				}
			}
		}
		return function;
	}

	private static JsonObject analyseModifiersParam(JsonArray importJson) {
		JsonObject modifiersList = new JsonObject();
		JsonObject modifierSpec = new JsonObject();
		for(JsonElement jelemList: importJson.getAsJsonArray()) {
			if(jelemList.getAsJsonObject().has("text")) {
				modifiersList.addProperty("hasFunctionVisibility", jelemList.getAsJsonObject().get("text").getAsString());
			}else if(jelemList.getAsJsonObject().has("modifierInvocation")) {
				modifierSpec.addProperty("modifierName", jelemList.getAsJsonObject().get("modifierInvocation").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString());
			}else if(jelemList.getAsJsonObject().has("stateMutability")) {
				modifiersList.addProperty("@type", jelemList.getAsJsonObject().get("stateMutability").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString());
			}else if(jelemList.getAsJsonObject().has("overrideSpecifier")) {
				modifierSpec.addProperty("hasModifierBehaviour", jelemList.getAsJsonObject().get("overrideSpecifier").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString());
			}
		}
		if(!modifiersList.has("hasFunctionVisibility")) {
			modifiersList.addProperty("hasFunctionVisibility", "public");
		}
		if(!modifiersList.has("@type")) {
			modifiersList.addProperty("@type", "public");
		}
		if(!modifierSpec.entrySet().isEmpty()) {
			modifiersList.add("isDefinedAs", modifierSpec);
		}
		return modifiersList;
	}

	private static JsonArray analyseInputParam(JsonArray importJson) {
		JsonArray paramsList = new JsonArray();
		int position = 0;
		for(JsonElement jelemList: importJson.getAsJsonArray()) {
			JsonObject param = new JsonObject();
			for(JsonElement jelem: jelemList.getAsJsonObject().get("parameter").getAsJsonArray()) {
				if(jelem.getAsJsonObject().has("name")) {
					param.addProperty("hasParameterName", jelem.getAsJsonObject().get("name").getAsString());
				}else if(jelem.getAsJsonObject().has("storageLocation")) {
					param.addProperty("hasParameterTypeWithDataLocation", jelem.getAsJsonObject().get("storageLocation").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString());
				}else if(jelem.getAsJsonObject().has("typeName")) {
					if(jelem.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().has("userDefinedTypeName")) {
						String name = jelem.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().get("userDefinedTypeName").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString();
						param.addProperty("hasParameterType",searchNameAndInclude(name));
					}else if(jelem.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().has("elementaryTypeName")) {
						String name = jelem.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().get("elementaryTypeName").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString();
						param.add("hasParameterType",catalogueElementaryType(name));
					}else if(jelem.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().has("typeName")) {
						param.add("hasParameterType",analyseArrays(jelem.getAsJsonObject().get("typeName").getAsJsonArray()));
					}else {
						param.add("hasParameterType",analyseMapping(jelem.getAsJsonObject().get("typeName").getAsJsonArray()));
					}
				}
			}
			param.addProperty("hasParameterPosition", position);
			position++;
			paramsList.add(param);
		}
		return paramsList;
	}

	private static JsonObject analyseReceive(JsonArray importReceive) {
		JsonObject modifier = new JsonObject();
		if(importReceive.getAsJsonArray().get(0).getAsJsonObject().get("functionDescriptor").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString().contentEquals("receive")) {
			for(JsonElement jelem: importReceive.getAsJsonArray()) {		
				if(jelem.getAsJsonObject().has("code")) {
					modifier.addProperty("receiveCode", jelem.getAsJsonObject().get("code").getAsString());
				}else {
					modifier.addProperty("hasReceiveBehaviour", "payable");
					modifier.addProperty("hasReceiveVisibility", "external");
				}
			}
		}
		return modifier;
	}

	private static JsonObject analyseFallback(JsonArray importFallback) {
		JsonObject modifier = new JsonObject();
		if(importFallback.getAsJsonArray().get(0).getAsJsonObject().get("functionDescriptor").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString().contentEquals("fallback")) {
			for(JsonElement jelem: importFallback.getAsJsonArray()) {
				if(jelem.getAsJsonObject().has("code")) {
					modifier.addProperty("fallbackCode", jelem.getAsJsonObject().get("code").getAsString());
				}else {
					modifier.addProperty("hasFallbackBehaviour", "payable");
					modifier.addProperty("hasFallbackVisibility", "external");
				}
			}
		}
		return modifier;
	}

	private static JsonObject analyseConstructor(JsonArray importConstructor) {
		JsonObject constructor = new JsonObject();
		if(importConstructor.getAsJsonArray().get(0).getAsJsonObject().get("functionDescriptor").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString().contentEquals("constructor")) {
			for(JsonElement jelem: importConstructor.getAsJsonArray()) {
				if(jelem.getAsJsonObject().has("code")) {
					constructor.addProperty("constructorCode", jelem.getAsJsonObject().get("code").getAsString());
				}else if(jelem.getAsJsonObject().has("parameterList")) {
					constructor.add("hasConstructorArguments", analyseInputParam(jelem.getAsJsonObject().get("parameterList").getAsJsonArray()));
				}else if(jelem.getAsJsonObject().has("modifierList")) {
					//TODO
				}
			}
		}
		return constructor;
	}

	private static JsonObject analyseAttribute(JsonArray importAttribute) {
		JsonObject attribute = new JsonObject();
		String type = new String();
		for(JsonElement jelem: importAttribute.getAsJsonArray()) {		
			if(jelem.getAsJsonObject().has("visibility")) {
				attribute.addProperty("hasAttributeVisibility", jelem.getAsJsonObject().get("visibility").getAsString());
			}else if(jelem.getAsJsonObject().has("name")) {
				attribute.addProperty("attributeName", jelem.getAsJsonObject().get("name").getAsString());
			}else if(jelem.getAsJsonObject().has("value")) {
				JsonObject newValue = new JsonObject();
				if(type.contentEquals("int") || type.contentEquals("uint") ) {
					newValue.addProperty("simpleInt", jelem.getAsJsonObject().get("value").getAsString());
				}else if(type.contentEquals("bool")) {
					newValue.addProperty("simpleString", jelem.getAsJsonObject().get("value").getAsBoolean());
				}else if(type.contentEquals("address") || type.contentEquals("string") || type.contentEquals("bytes") ) {
					newValue.addProperty("simpleString", jelem.getAsJsonObject().get("value").getAsString());
				}else {
					newValue.addProperty("simpleGeneric", jelem.getAsJsonObject().get("value").getAsString());
				}
				attribute.add("hasAttributeValue", newValue);
			}else if(jelem.getAsJsonObject().has("typeName")) {
				if(jelem.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().has("userDefinedTypeName")) {
					JsonObject typeUDT = new JsonObject();
					typeUDT.addProperty("@type", searchNameAndInclude(jelem.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().get("userDefinedTypeName").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString()));
					attribute.add("hasNonConstantType",typeUDT);
				}else if(jelem.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().has("mapping")) {
					JsonObject nonConstantTypeMapping = new JsonObject();
					nonConstantTypeMapping = analyseMapping(jelem.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().get("mapping").getAsJsonArray());
					nonConstantTypeMapping.addProperty("@type", "MapType");
					attribute.add("hasConstantType", nonConstantTypeMapping);
				}else if(jelem.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().has("elementaryTypeName")) {
					String attributeType = jelem.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().get("elementaryTypeName").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString();
					attribute.addProperty("@type", "nonConstantAttributeSpecification");
					attribute.add("hasNonConstantType", catalogueElementaryType(attributeType));
				}else{
					JsonObject constantType = new JsonObject();
					JsonObject nonConstantType = new JsonObject();
					JsonObject nonConstantTypeRelation = new JsonObject();
					for(JsonElement arrayElem: jelem.getAsJsonObject().get("typeName").getAsJsonArray()) {
						if(arrayElem.getAsJsonObject().has("isArray")) {
							constantType.addProperty("@type", "ArrayType");
						}else if(arrayElem.getAsJsonObject().has("typeName")) {
							if(arrayElem.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().has("elementaryTypeName")) {
								String attributeType = arrayElem.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().get("elementaryTypeName").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString();
								nonConstantTypeRelation.add("hasNonConstantType", catalogueElementaryType(attributeType));
							}else if(arrayElem.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().has("userDefinedTypeName")) {
								String value = arrayElem.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().get("userDefinedTypeName").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString();
								nonConstantTypeRelation.addProperty("hasNonConstantType", searchNameAndInclude(value));
							}
						}
					}
					nonConstantType.add("hasNonConstantType", nonConstantTypeRelation);
					constantType.add("hasNonConstantType", nonConstantType);
					attribute.add("hasConstantType", constantType);
				}
			}else if(jelem.getAsJsonObject().has("isImmutable")) {
				attribute.addProperty("isInmutable", jelem.getAsJsonObject().get("isImmutable").getAsBoolean());
			}else if(jelem.getAsJsonObject().has("isConstant")) {
				attribute.addProperty("isConstant", jelem.getAsJsonObject().get("isConstant").getAsBoolean());
			}else if(jelem.getAsJsonObject().has("overrideSpecifier")){
				//TODO
			}
		}
		return attribute;
	}

	private static JsonObject analyseMapping (JsonArray input) {
		JsonObject nonConstantTypeMapping = new JsonObject();
		for(JsonElement mappingElement : input) {
			if(mappingElement.getAsJsonObject().has("elementaryTypeName")) {
				nonConstantTypeMapping.addProperty("hasKeyMap", mappingElement.getAsJsonObject().get("elementaryTypeName").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString());
			}else if(mappingElement.getAsJsonObject().has("typeName")) {
				if(mappingElement.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().has("elementaryTypeName")){
					String attributeType = mappingElement.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().get("elementaryTypeName").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString();
					nonConstantTypeMapping.add("hasValueMap", catalogueElementaryType(attributeType));
				}else if(mappingElement.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().has("userDefinedTypeName")){
					nonConstantTypeMapping.addProperty("hasValueMap",searchNameAndInclude(mappingElement.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().get("userDefinedTypeName").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString()));
				}else if(mappingElement.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().has("mapping")){
					nonConstantTypeMapping.add("hasValueMap", analyseMapping(mappingElement.getAsJsonObject().get("typeName").getAsJsonArray().get(0).getAsJsonObject().get("mapping").getAsJsonArray()));
					nonConstantTypeMapping.get("hasValueMap").getAsJsonObject().addProperty("@type", "MapType");
				}else {
					nonConstantTypeMapping.add("hasParameterType",analyseArrays(mappingElement.getAsJsonObject().get("typeName").getAsJsonArray()));
				}
			}
		}
		return nonConstantTypeMapping;
	}
	
	public static String[] splitBytesAndNumber(String entrada) {
		String parteNoNumerica = entrada.replaceAll("[0-9]", "");
		String parteNumerica = entrada.replaceAll("[^0-9]", "");
		return new String[] {parteNoNumerica, parteNumerica};
	}

}
