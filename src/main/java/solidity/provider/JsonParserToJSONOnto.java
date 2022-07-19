package solidity.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.EnumUtils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import solidity.model.ArrayDimensionSpecification;
import solidity.model.ArraySpecification;
import solidity.model.AttributeSpecification;
import solidity.model.ConstructorSpecification;
import solidity.model.ContractSpecification;
import solidity.model.DualMemoryTypeSpecification;
import solidity.model.ElementaryTypeSpecification;
import solidity.model.EnumSpecification;
import solidity.model.EventSpecification;
import solidity.model.FunctionSpecification;
import solidity.model.GlobalContractSpecification;
import solidity.model.ImplementationSpecification;
import solidity.model.InterfaceSpecification;
import solidity.model.LibrarySpecification;
import solidity.model.MappingSpecification;
import solidity.model.MemoryTypeSpecification;
import solidity.model.ModifierSpecification;
import solidity.model.NonConstantSpecification;
import solidity.model.ParameterSpecification;
import solidity.model.SingleMemoryTypeSpecification;
import solidity.model.StructSpecification;
import solidity.model.UsingForSpecification;
import solidity.model.VisibilitySpecification;
import solidity.model.DualMemoryTypeSpecification.DualType;
import solidity.model.ElementaryTypeSpecification.ElementaryType;
import solidity.model.ParameterSpecification.DataLocation;
import solidity.model.SingleMemoryTypeSpecification.SingleType;

public class JsonParserToJSONOnto {

	private JsonObject contractAntlrFormat;
	private ImplementationSpecification implementationSpecification = new ImplementationSpecification();
	private ArrayList<ImplementationSpecification> implementationSpecificationList = new ArrayList<ImplementationSpecification>();

	private ArrayList<String> versionList = new ArrayList<String>();
	private ArrayList<String> importList = new ArrayList<String>();

	//General contract to convert Java Object to JSON
	private GlobalContractSpecification globalContractSpecification = new GlobalContractSpecification();
	//Contract
	private ContractSpecification contractSpecification = new ContractSpecification();
	private ArrayList<String> inheritanceList = new ArrayList<String>();
	//Library
	private LibrarySpecification librarySpecification = new LibrarySpecification();
	//Interface
	private InterfaceSpecification interfaceSpecification = new InterfaceSpecification();

	//Attributes
	private ArrayList<AttributeSpecification> attributeSpecificationList = new ArrayList<AttributeSpecification>();
	private AttributeSpecification attributeSpecification = new AttributeSpecification();

	private MemoryTypeSpecification elementaryTypeName = new MemoryTypeSpecification();

	private ArrayList<UsingForSpecification> usingForSpecificationList = new ArrayList<UsingForSpecification>();
	private UsingForSpecification usingForSpecification = new UsingForSpecification();

	//Constructor
	private ConstructorSpecification constructorSpecification = new ConstructorSpecification();

	//Events
	private ArrayList<EventSpecification> eventSpecificationList = new ArrayList<EventSpecification>();
	private EventSpecification eventSpecification = new EventSpecification();

	//Modifiers
	private ArrayList<ModifierSpecification> modifierSpecificationList = new ArrayList<ModifierSpecification>();
	private ModifierSpecification modifierSpecification = new ModifierSpecification();

	//Functions
	private ArrayList<FunctionSpecification> functionSpecificationList = new ArrayList<FunctionSpecification>();
	private FunctionSpecification functionSpecification = new FunctionSpecification();
	private ArrayList<ParameterSpecification> parameterSpecificationList = new ArrayList<ParameterSpecification>();
	private ParameterSpecification parameterSpecification = new ParameterSpecification();

	//Types
	private SingleMemoryTypeSpecification singleMemoryTypeSpecification = new SingleMemoryTypeSpecification();
	private DualMemoryTypeSpecification dualMemoryTypeSpecification = new DualMemoryTypeSpecification();
	private ElementaryTypeSpecification elementaryTypeSpecification = new ElementaryTypeSpecification();
	private NonConstantSpecification nonConstantSpecification = new NonConstantSpecification();
	private ArraySpecification arraySpecification = new ArraySpecification();
	private MappingSpecification mapSpecification = new MappingSpecification();
	private EnumSpecification enumSpecification = new EnumSpecification();
	private StructSpecification structSpecification = new StructSpecification();

	private ArrayDimensionSpecification arrayDimensionSpecification = new ArrayDimensionSpecification();
	private ArrayList<ArrayDimensionSpecification> arrayDimensionSpecificationList = new ArrayList<ArrayDimensionSpecification>();

	/**
	 * Main function. Parse JSON to Java Objects
	 * @param contractInJSON
	 */
	public String JsonContractToJavaObject(String contractInJSON, String contractHash) {
		contractAntlrFormat = SolidityToJSONParser.contractJsonObject(contractInJSON.toString());
		String id = null;
		if(contractHash==null) {
			id = contractInJSON.substring(0, contractInJSON.length()-4);
		}else {
			id = contractHash;
		}
		JsonArray contractArray = contractAntlrFormat.get("sourceUnit").getAsJsonArray();
		boolean resetNeeded = false;
		for(int i = 0; i < contractArray.size(); i++) {
			StringBuilder sb = new StringBuilder();
			JsonObject contractI = contractArray.get(i).getAsJsonObject();
			String keyArray = contractI.keySet().iterator().next();
			if(keyArray.contentEquals(Tokens.pragmaDirective)) {
				String version = solidityVersion(contractI.get(keyArray).getAsJsonArray());
				if(version.contains("ABIEncoderV2")) {
					versionList.add(version.substring(0, version.length()-1));
				}else {
					versionList = new ArrayList<String>();
					versionList.add(version.substring(0, version.length()-1));
				}
			}
			if(keyArray.contentEquals(Tokens.IMPORTDIRECTIVE)) {
				solidityImports(contractI);
			}
			if(keyArray.contentEquals(Tokens.contractDefinition)) {
				implementationSpecification.setHasContractName(solidityContract(contractI.get(Tokens.contractDefinition).getAsJsonArray()));

				solidityInheritance(sb.append("urn:").append(id).append(":").toString(),contractI.get(Tokens.contractDefinition).getAsJsonArray());
				sb = new StringBuilder();
				solidityUsingFor(sb.append("urn:").append(id).append(":").toString(),contractI.get(Tokens.contractDefinition).getAsJsonArray());
				sb.append(implementationSpecification.getHasContractName());
				solidityAttributes(contractI.get(Tokens.contractDefinition).getAsJsonArray());
				solidityConstructor(contractI.get(Tokens.contractDefinition).getAsJsonArray());
				solidityFunctions(contractI.get(Tokens.contractDefinition).getAsJsonArray());
				solidityModifiers(contractI.get(Tokens.contractDefinition).getAsJsonArray());
				solidityEvents(contractI.get(Tokens.contractDefinition).getAsJsonArray());
				resetNeeded = true;
			}
			if(keyArray.contentEquals(Tokens.structDefinition)) {
				try {
					attributeSpecification = new AttributeSpecification();
					sb = new StringBuilder();
					JsonNode structNode = new ObjectMapper().readTree(contractI.toString());
					attributeSpecification = solidityStructAttributes(structNode);
					sb.append("urn:").append(id).append(":").append(attributeSpecification.getHasName());
					structSpecification.setHasName(attributeSpecification.getHasName());
					implementationSpecification.setStructSpecification(structSpecification);
					implementationSpecification.setId(sb.toString());
					implementationSpecificationList.add(implementationSpecification);
					resetSpecifications();
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
			}
			if(resetNeeded == true) {
				resetNeeded = false;
				if(contractI.get(Tokens.contractDefinition).getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString().contentEquals("abstract")) {
					contractSpecification.setAbstract(true);
				}
				if(isLibrary(contractI.get(Tokens.contractDefinition).getAsJsonArray())) {
					//					sb.append(":").append("library");
					librarySpecification.setHasImports(importList);
					librarySpecification.setHasVersion(versionList);
					librarySpecification.setHasUsingFor(usingForSpecificationList);
					librarySpecification.setHasAttributes(attributeSpecificationList);
					librarySpecification.setHasFunctions(functionSpecificationList);
					librarySpecification.setHasModifier(modifierSpecificationList);
					librarySpecification.setHasEvents(eventSpecificationList);
					implementationSpecification.setLibrarySpecification(librarySpecification);
				}else if(isInterface(contractI.get(Tokens.contractDefinition).getAsJsonArray())) {
					//					sb.append(":").append("interface");
					interfaceSpecification.setHasImports(importList);
					interfaceSpecification.setHasVersion(versionList);
					interfaceSpecification.setHasFunctions(functionSpecificationList);
					interfaceSpecification.setHasEvents(eventSpecificationList);
					implementationSpecification.setInterfaceSpecification(interfaceSpecification);
				}else {
					//					sb.append(":").append("contract");
					contractSpecification.setHasImports(importList);
					contractSpecification.setHasVersion(versionList);
					contractSpecification.setHasInheritance(inheritanceList);
					contractSpecification.setHasUsingFor(usingForSpecificationList);
					contractSpecification.setHasAttributes(attributeSpecificationList);
					contractSpecification.setHasConstructor(constructorSpecification);
					contractSpecification.setHasFunctions(functionSpecificationList);
					contractSpecification.setHasModifier(modifierSpecificationList);
					contractSpecification.setHasEvents(eventSpecificationList);
					implementationSpecification.setContractSpecification(contractSpecification);
				}
				implementationSpecification.setId(sb.toString());
				implementationSpecificationList.add(implementationSpecification);
				resetSpecifications();
			}
		}
		globalContractSpecification.setId("urn:" + id);
		globalContractSpecification.setGlobalContract(implementationSpecificationList);
		return finalContract(globalContractSpecification);
	}

	public boolean isLibrary(JsonArray json) {
		if(json.get(0).getAsJsonObject().get(Tokens.text).getAsString().contains("library")) {
			return true;
		}
		return false;
	}

	public boolean isInterface(JsonArray json) {
		if(json.get(0).getAsJsonObject().get(Tokens.text).getAsString().contains("interface")) {
			return true;
		}
		return false;
	}

	public void resetSpecifications() {
		importList = new ArrayList<String>();
		contractSpecification = new ContractSpecification();
		librarySpecification = new LibrarySpecification();
		interfaceSpecification = new InterfaceSpecification();
		implementationSpecification = new ImplementationSpecification();
		constructorSpecification = new ConstructorSpecification();
		usingForSpecificationList = new ArrayList<UsingForSpecification>();
		attributeSpecificationList = new ArrayList<AttributeSpecification>();
		functionSpecificationList = new ArrayList<FunctionSpecification>();
		modifierSpecificationList = new ArrayList<ModifierSpecification>();
		eventSpecificationList = new ArrayList<EventSpecification>();
		inheritanceList = new ArrayList<String>();
	}

	/**
	 * Return the final JSON-LD contract
	 * @param globalContractSpecification
	 * @return
	 */
	public String finalContract(GlobalContractSpecification globalContractSpecification) {
		StringBuilder sb = new StringBuilder();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode myData = contextSpecification(mapper);
		try {
			sb.append(mapper.setSerializationInclusion(Include.NON_NULL).setSerializationInclusion(Include.NON_EMPTY).writeValueAsString(myData));
			sb.deleteCharAt(sb.length()-1);
			sb.append(",").append(mapper.setSerializationInclusion(Include.NON_NULL).setSerializationInclusion(Include.NON_EMPTY).writeValueAsString(globalContractSpecification).substring(1));
//						System.out.println(sb.toString());
			return sb.toString();
		}catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return "Error";
	}

	/**
	 * Set the Context for the JSON-LD
	 * @param mapper
	 * @return
	 */
	public ObjectNode contextSpecification(ObjectMapper mapper) {
		ObjectNode myData = mapper.createObjectNode();
		ObjectNode myContext = mapper.createObjectNode();
		myContext.put(Tokens.VOCAB_LD, "https://w3id.org/def/SolidityOntology#");
		myContext.put("description", "http://www.w3.org/2000/01/rdf-schema#label");
		myContext.put("id",Tokens.ID_LD);
		ObjectNode visibilityContext = mapper.createObjectNode();
		visibilityContext.put(Tokens.ID_LD, "https://w3id.org/def/SolidityOntology#hasVisibility");
		visibilityContext.put(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		myContext.set("hasVisibility", visibilityContext);
		ObjectNode behaviourContext = mapper.createObjectNode();
		behaviourContext.put(Tokens.ID_LD, "https://w3id.org/def/SolidityOntology#hasFunctionBehaviour");
		behaviourContext.put(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		myContext.set("hasFunctionBehaviour", behaviourContext);
		ObjectNode elementaryType = mapper.createObjectNode();
		elementaryType.put(Tokens.ID_LD, "https://w3id.org/def/SolidityOntology#elementaryType");
		elementaryType.put(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		myContext.set("elementaryType", elementaryType);
		ObjectNode singleMemoryType = mapper.createObjectNode();
		singleMemoryType.put(Tokens.ID_LD, "https://w3id.org/def/SolidityOntology#singleMemoryType");
		singleMemoryType.put(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		myContext.set("singleMemoryType", singleMemoryType);
		ObjectNode hasParamTypeWithDataLocation = mapper.createObjectNode();
		hasParamTypeWithDataLocation.put(Tokens.ID_LD, "https://w3id.org/def/SolidityOntology#hasParameterTypeWithDataLocation");
		hasParamTypeWithDataLocation.put(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		myContext.set("hasParameterTypeWithDataLocation", hasParamTypeWithDataLocation);
		ObjectNode hasType = mapper.createObjectNode();
		hasType.put(Tokens.ID_LD, "https://w3id.org/def/SolidityOntology#hasType");
		hasType.put(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		myContext.set("hasType", hasType);
		ObjectNode hasInheritance = mapper.createObjectNode();
		hasInheritance.put(Tokens.ID_LD, "https://w3id.org/def/SolidityOntology#hasInheritance");
		hasInheritance.put(Tokens.TYPE_LD, Tokens.ID_LD);
		myContext.set("hasInheritance", hasInheritance);
		ObjectNode usingLibrary = mapper.createObjectNode();
		usingLibrary.put(Tokens.ID_LD, "https://w3id.org/def/SolidityOntology#usingLibrary");
		usingLibrary.put(Tokens.TYPE_LD, Tokens.ID_LD);
		myContext.set("usingLibrary", usingLibrary);

		myData.set("@context", myContext);
		return myData;
	}

	/**
	 * Check Solidity version
	 * @param entireContract
	 * @return
	 */
	public String solidityVersion(JsonArray entireContract) {
		String value = "";
		try {
			JsonNode jsonNode = new ObjectMapper().readTree(entireContract.get(2).toString());
			List<String> versionValues = jsonNode.findValuesAsText(Tokens.text);
			for(int i = 0; i<versionValues.size(); i++) {
				value += versionValues.get(i) + " ";
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return value;
	}

	/**
	 * Check contract imports
	 * @param entireContract
	 */
	private void solidityImports(JsonObject entireContract) {
		JsonNode jsonNode;
		try {
			jsonNode = new ObjectMapper().readTree(entireContract.toString());
			List<JsonNode> importValue = jsonNode.findValues(Tokens.text);
			StringBuilder importString = new StringBuilder();
			for(int i = 1; i<importValue.size()-1;i++) {
				importString.append(importValue.get(i).asText());
				if(i!=importValue.size()-1 ) {
					importString.append(" ");
				}
			}
			importList.add(importString.toString());
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Check contract inheritance
	 * @param asJsonArray
	 */
	private void solidityInheritance(String URI, JsonArray asJsonArray) {
		JsonNode jsonNode;
		try {
			jsonNode = new ObjectMapper().readTree(asJsonArray.toString());
			List<JsonNode> functionValue = jsonNode.findParents(Tokens.inheritanceSpecifier);
			for(int i = 0; i < functionValue.size(); i++) {
				inheritanceList.add(URI+functionValue.get(i).findParents(Tokens.text).get(0).get(Tokens.text).asText());
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Check the contract's name
	 * @param entireContract
	 * @return
	 */
	public String solidityContract(JsonArray entireContract) {
		String value = "";
		for(int i = 0; i < entireContract.size(); i++) {
			String keyArray = entireContract.get(i).getAsJsonObject().keySet().iterator().next();
			if(keyArray.contentEquals(Tokens.identifier)) {
				value = entireContract.get(i).getAsJsonObject().get(keyArray).getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString();
			}
		}
		return value;
	}

	/**
	 * Check the Solidity Constructor
	 * @param entireContract
	 * @return
	 */
	public void solidityConstructor(JsonArray entireContract) {
		JsonNode jsonNode;
		try {
			jsonNode = new ObjectMapper().readTree(entireContract.toString());
			List<JsonNode> functionValue = jsonNode.findParents(Tokens.functionDefinition);
			for(int i = 0; i < functionValue.size(); i++) {
				List<JsonNode> constructorDescriptor = jsonNode.findParents(Tokens.functionDescriptor);
				if(constructorDescriptor.get(i).findValue(Tokens.text).asText().contentEquals(Tokens.constructor)) {
					List<JsonNode> codeValue = functionValue.get(i).findParents(Tokens.statement);
					constructorSpecification.setHasCode(getCode(codeValue, i));
					JsonNode parameterValues = functionValue.get(i).findParent(Tokens.parametersList);
					constructorSpecification.setHasConstructorArguments(solidityParameters(parameterValues));
				}
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * TODO: Mejorar en la ontología y este método
	 * @param entireContract
	 */
	public void solidityUsingFor(String URI, JsonArray entireContract) {
		JsonNode jsonNode;
		try {
			jsonNode = new ObjectMapper().readTree(entireContract.toString());
			List<JsonNode> variableValue = jsonNode.findParents(Tokens.USINGFORDECLARATION);
			for(int i =0; i<variableValue.size(); i++) {
				usingForSpecification = new UsingForSpecification();
				usingForSpecification.setLibrary(URI+variableValue.get(i).findParent(Tokens.identifier).findParent(Tokens.text).get(Tokens.text).asText());
				List<JsonNode> getType = variableValue.get(i).findValues(Tokens.text);
				usingForSpecification.setType(getType.get(getType.size()-2).asText());
				usingForSpecificationList.add(i,usingForSpecification);
			}
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Recopile the attributes.
	 * @param entireContract
	 */
	public void solidityAttributes(JsonArray entireContract) {
		JsonNode jsonNode;
		try {
			jsonNode = new ObjectMapper().readTree(entireContract.toString());
			List<JsonNode> variableValue = jsonNode.findParents(Tokens.stateVariableDeclaration);
			for(int i = 0; i < variableValue.size(); i++) {		
				attributeSpecification = new AttributeSpecification();
				String attributeType = defineAttributeType(variableValue.get(i));
				if(attributeType.contentEquals("Array")) {
					attributeSpecification.setHasArrayType(completeArrayAttribute(variableValue.get(i)));
					if(defineArrayType(variableValue.get(i)).contentEquals("constant")) {
						attributeSpecification.setHasName(variableValue.get(i).findParent(Tokens.identifier).findValue(Tokens.text).asText());
					}else {
						attributeSpecification.setHasName(variableValue.get(i).findParents(Tokens.identifier).get(1).findValue(Tokens.text).asText());
					}
				}else if(attributeType.contentEquals("NonConstantAttribute")){
					attributeSpecification.setHasNonConstantAttribute(completeNonConstantAttribute(variableValue,i));
					attributeSpecification.setHasName(variableValue.get(i).findParent(Tokens.identifier).findValue(Tokens.text).asText());
				}else if(attributeType.contentEquals("Mapping")){
					attributeSpecification.setHasMapType(completeMappingAttribute(variableValue.get(i)));
					List<JsonNode> mappingIdentifierList = variableValue.get(i).findParents(Tokens.identifier);
					attributeSpecification.setHasName(mappingIdentifierList.get(mappingIdentifierList.size()-1).findValue(Tokens.text).asText());
				}else if(attributeType.contentEquals("struct")){
					attributeSpecification.setHasEnumAttribute(completeStructAttribute(variableValue.get(i)));
					attributeSpecification.setHasName(variableValue.get(i).findParents(Tokens.identifier).get(0).findValue(Tokens.text).asText());
				}else{
					if(!variableValue.get(i).findValues(Tokens.text).get(variableValue.get(i).findValues(Tokens.text).size()-1).asText().contentEquals("(")) {
						attributeSpecification.setHasNonConstantAttribute(completeUserDefinedAttribute(variableValue.get(i)));
						List<JsonNode> getNameAttribute = variableValue.get(i).findParents(Tokens.identifier);
						attributeSpecification.setHasName(getNameAttribute.get(getNameAttribute.size()-1).findValue(Tokens.text).asText());
					}
				}
				List<JsonNode> optionalDefinitions;
				if(variableValue.get(i).findParents(Tokens.text).size() >= 3) {
					optionalDefinitions = variableValue.get(i).findParents(Tokens.text).subList(0, 3);
				}else {
					optionalDefinitions = variableValue.get(i).findParents(Tokens.text).subList(0, 2);
				}
				for(int j=0; j<optionalDefinitions.size();j++) {
					if(optionalDefinitions.get(j).get(Tokens.text).asText().contentEquals("constant")) {
						attributeSpecification.setConstantProperty(true);
					}
					if(optionalDefinitions.get(j).get(Tokens.text).asText().contentEquals("immutable")) {
						attributeSpecification.setImmutableProperty(true);
					}
					if(optionalDefinitions.get(j).get(Tokens.text).asText().contentEquals("payable")) {
						attributeSpecification.setPayableProperty(true);
					}
				}
				attributeSpecification.setHasValue(attributeValue(variableValue.get(i).findParent("expression")));
				attributeSpecification.setVisibility(VisibilitySpecification.valueOf(getVisibility(variableValue.get(i)).toUpperCase()));
				attributeSpecificationList.add(i, attributeSpecification);
			}
			List<JsonNode> enumAttribute = jsonNode.findParents(Tokens.enumDefinition);
			for(int i = 0; i< enumAttribute.size() && enumAttribute.size() > 0; i++) {
				attributeSpecificationList.add(solidityEnumAttributes(enumAttribute.get(i)));
			}
			List<JsonNode> structAttribute = jsonNode.findParents(Tokens.structDefinition);
			for(int i = 0; i< structAttribute.size() && structAttribute.size() > 0; i++) {
				attributeSpecificationList.add(solidityStructAttributes(structAttribute.get(i)));
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	public String attributeValue(JsonNode values) {
		if(values != null) {
			StringBuilder sb = new StringBuilder();
			List<JsonNode> valueText = values.findParents("text");
			for(int i = 0; i<valueText.size();i++) {
				sb.append(valueText.get(i).get(Tokens.text).asText());
			}
			return sb.toString();
		}
		return null;
	}

	/**
	 * Find and recopile all the enums in the Solidity Contract
	 * @param enumDefinition
	 * @return
	 */
	public AttributeSpecification solidityEnumAttributes(JsonNode enumDefinition) {
		attributeSpecification = new AttributeSpecification();
		enumSpecification = new EnumSpecification();
		ArrayList<Object> toIntroduce = new ArrayList<Object>();
		attributeSpecification.setHasName(enumDefinition.findParent(Tokens.identifier).findValue(Tokens.text).asText());;
		List<JsonNode> enumAttribute = enumDefinition.findParents(Tokens.enumValue);
		for(int i=0; i< enumAttribute.size();i++) {
			toIntroduce.add(i, enumAttribute.get(i).findValue(Tokens.text).asText());
		}
		enumSpecification.setOptions(toIntroduce);
		attributeSpecification.setHasEnumAttribute(enumSpecification);
		return attributeSpecification;
	}

	public String defineAttributeType(JsonNode jsonNode) {	
		if(isMapping(jsonNode)){
			return "Mapping";
		}else if(isArray(jsonNode)){
			return "Array";
		}else if(isNonConstant(jsonNode)){
			return "NonConstantAttribute";
		}else{
			return "userDefined";
		}
	}

	public String defineArrayType(JsonNode jsonNode) {
		if(jsonNode.findParents(Tokens.identifier).size() > 1) {
			return "Array";
		}else {
			return "constant";
		}
	}

	public boolean isArray(JsonNode jsonNode) {
		if(jsonNode.findParents(Tokens.typeName).get(0).findParents(Tokens.type).size()>1) {
			List<JsonNode> checkArray = jsonNode.findParents(Tokens.typeName).get(0).findParents(Tokens.type);
			boolean open = false;
			boolean close = false;
			for(int i =0; i<checkArray.size();i++) {
				if(checkArray.get(i).findValue(Tokens.text).asText().contains("[")) {
					open = true;
				}
				if(checkArray.get(i).findValue(Tokens.text).asText().contains("]")) {
					close = true;
				}
				if(open&&close) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isStruct(JsonNode jsonNode) {
		if(jsonNode.findParent(Tokens.struct) != null) {
			return true;
		}
		return false;
	}

	public boolean isMapping(JsonNode jsonNode) {
		if(jsonNode.findParent(Tokens.MAPPING) != null) {
			return true;
		}
		return false;
	}

	public boolean isNonConstant(JsonNode jsonNode) {
		if(jsonNode.findParent(Tokens.elementaryTypeName) != null) {
			return true;
		}
		return false;
	}

	/**
	 * Check this method
	 * @param receivedArguments
	 * @return
	 */
	public StructSpecification completeStructAttribute(JsonNode receivedArguments) {
		System.out.println(receivedArguments);
		return null;
	}

	public MappingSpecification completeMappingAttribute(JsonNode receivedArguments) {
		mapSpecification = new MappingSpecification();

		List<JsonNode> elementaryTypes = receivedArguments.findParent(Tokens.MAPPING).findParents(Tokens.elementaryTypeName);
		if(elementaryTypes.size()==0) {
			JsonNode userDefined = receivedArguments.findParent(Tokens.MAPPING).findParent(Tokens.userDefinedTypeName);
			mapSpecification.setHasKeyMap(userDefined.findValue(Tokens.text).asText());

		}else {
			mapSpecification.setHasKeyMap(completeNonConstantAttribute(elementaryTypes, 0));
		}
		if(receivedArguments.findParent(Tokens.MAPPING).findParent(Tokens.typeName).findParents(Tokens.MAPPING).size()==1) {
			List<JsonNode> aa = receivedArguments.findParent(Tokens.MAPPING).findParent(Tokens.typeName).findParents(Tokens.MAPPING);
			mapSpecification.setHasValueMap(mappingIteration(aa.get(0),new MappingSpecification()));
		}else if(elementaryTypes.size()==1){
			mapSpecification.setHasValueMap(completeUserDefined(receivedArguments));
		}else if(elementaryTypes.size()==0){
			JsonNode userDefined = receivedArguments.findParent(Tokens.MAPPING).findParents(Tokens.userDefinedTypeName).get(1);
			mapSpecification.setHasValueMap(userDefined.findValue(Tokens.text).asText());

		}else {
			mapSpecification.setHasValueMap(completeNonConstantAttribute(elementaryTypes, 1));
		}
		return mapSpecification;
	}

	public MappingSpecification mappingIteration(JsonNode receivedArguments, MappingSpecification mapSpecificationIter) {
		List<JsonNode> elementaryTypes = receivedArguments.findParent(Tokens.MAPPING).findParents(Tokens.elementaryTypeName);
		if(elementaryTypes.size()==0) {
			JsonNode userDefined = receivedArguments.findParent(Tokens.MAPPING).findParent(Tokens.userDefinedTypeName);
			mapSpecification.setHasKeyMap(userDefined.findValue(Tokens.text).asText());

		}else {
			mapSpecification.setHasKeyMap(completeNonConstantAttribute(elementaryTypes, 0));
		}
		if(receivedArguments.findParent(Tokens.MAPPING).findParent(Tokens.typeName).findParents(Tokens.MAPPING).size()==1) {
			List<JsonNode> aa = receivedArguments.findParent(Tokens.MAPPING).findParent(Tokens.typeName).findParents(Tokens.MAPPING);
			mapSpecificationIter.setHasValueMap(mappingIteration(aa.get(0),new MappingSpecification()));
		}else if(elementaryTypes.size()==1){
			mapSpecificationIter.setHasValueMap(completeUserDefined(receivedArguments));
		}else if(elementaryTypes.size()==0){
			JsonNode userDefined = receivedArguments.findParent(Tokens.MAPPING).findParents(Tokens.userDefinedTypeName).get(1);
			mapSpecification.setHasValueMap(userDefined.findValue(Tokens.text).asText());

		}else {
			mapSpecificationIter.setHasValueMap(completeNonConstantAttribute(elementaryTypes, 1));
		}
		return mapSpecificationIter;
	}

	public ArraySpecification completeArrayAttribute(JsonNode receivedArgument) {
		arraySpecification = new ArraySpecification();
		arrayDimensionSpecificationList = new ArrayList<ArrayDimensionSpecification>();
		List<JsonNode> getLenght = receivedArgument.findParents(Tokens.numberLiteral);
		for(int i=0; i<getLenght.size();i++) {
			arrayDimensionSpecification = new ArrayDimensionSpecification();
			arrayDimensionSpecification.setIndex(Integer.valueOf(i).shortValue());
			arrayDimensionSpecification.setLenght(Integer.valueOf(getLenght.get(i).findValue(Tokens.text).asInt()).shortValue());
			arrayDimensionSpecificationList.add(i, arrayDimensionSpecification);
		}
		if(receivedArgument.findParent(Tokens.elementaryTypeName) != null) {
			arraySpecification.setHasType(receivedArgument.findParent(Tokens.elementaryTypeName).findValue(Tokens.text).asText().toUpperCase());
		}else {
			arraySpecification.setHasType(receivedArgument.findParent(Tokens.userDefinedTypeName).findValue(Tokens.text).asText().toUpperCase());
		}
		arraySpecification.setHasArrayDimension(arrayDimensionSpecificationList);
		return arraySpecification;
	}

	public NonConstantSpecification completeUserDefined(JsonNode receivedArgument) {
		nonConstantSpecification = new NonConstantSpecification();
		JsonNode typeAttribute = receivedArgument.findParent(Tokens.userDefinedTypeName);
		String value = typeAttribute.findValue(Tokens.text).asText();
		nonConstantSpecification.setNonConstantAttribute(value.toUpperCase());
		return nonConstantSpecification;
	}

	public NonConstantSpecification completeUserDefinedAttribute(JsonNode receivedArgument) {
		nonConstantSpecification = new NonConstantSpecification();
		JsonNode typeAttribute = receivedArgument.findParent(Tokens.userDefinedTypeName);
		List<JsonNode> value = typeAttribute.findValues(Tokens.text);
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i<value.size();i++) {
			if(i%2 == 0 && value.get(i).asText().contains(".")) {
				sb.append(value.get(i).asText());
			}else if(value.get(i).asText().contains(" ")){
				i=value.size();
			}else {
				sb.append(value.get(i).asText());
			}
		}
		nonConstantSpecification.setNonConstantAttribute(sb.toString().toUpperCase());
		return nonConstantSpecification;
	}

	/**
	 * TODO Array
	 * @param structDefinition
	 * @return
	 */
	public AttributeSpecification solidityStructAttributes(JsonNode structDefinition) {
		structSpecification = new StructSpecification();
		attributeSpecification = new AttributeSpecification();
		ArrayList<AttributeSpecification> toIntroduce = new ArrayList<AttributeSpecification>();
		//		HashMap<String, Object> hasAttributes = new HashMap<String, Object>();
		List<JsonNode> structAttributes = structDefinition.findParents(Tokens.variableDeclaration);
		for(int i = 0; i< structAttributes.size();i++) {
			String attributeType = defineAttributeType(structAttributes.get(i));
			if(attributeType.contentEquals("Array")) {

			}else if(attributeType.contentEquals("NonConstantAttribute")){
				attributeSpecification.setHasName(structAttributes.get(i).findParent(Tokens.identifier).findValue(Tokens.text).asText());
				attributeSpecification.setHasNonConstantAttribute(completeNonConstantAttribute(structAttributes,i));
			}
			toIntroduce.add(i, attributeSpecification);
			attributeSpecification = new AttributeSpecification();
		}
		structSpecification.setHasAttributesTest(toIntroduce);
		attributeSpecification = new AttributeSpecification();
		attributeSpecification.setHasName(structDefinition.findParent(Tokens.identifier).findValue(Tokens.text).asText());
		attributeSpecification.setHasStructType(structSpecification);
		return attributeSpecification;
	}

	public NonConstantSpecification completeNonConstantAttribute(List<JsonNode> receivedArgument, int i) {
		JsonNode typeAttribute = receivedArgument.get(i).findParent(Tokens.elementaryTypeName);
		String value = typeAttribute.findValue(Tokens.text).asText();
		String[] valueSplit = value.split("(?<=\\D)(?=\\d)");
		nonConstantSpecification = new NonConstantSpecification();
		if(valueSplit[0].matches("uint|int|bytes|byte")) {
			nonConstantSpecification.setNonConstantAttribute(attributeTypeName(receivedArgument, i));
		}else if(valueSplit[0].matches("ufixed|fixed")) {
			nonConstantSpecification.setNonConstantAttribute(attributeTypeName(receivedArgument, i));
		}else if(valueSplit[0].matches("address|bool|string")) {
			elementaryTypeSpecification = new ElementaryTypeSpecification();
			elementaryTypeSpecification.setElementaryType(ElementaryType.valueOf(valueSplit[0].toUpperCase()));
			nonConstantSpecification.setNonConstantAttribute(elementaryTypeSpecification);
		}else {
			completeUserDefinedAttribute(receivedArgument.get(i));
		}
		return nonConstantSpecification;
	}

	/**
	 * Get the attribute type
	 * 
	 * @param jsonNode
	 * @param number
	 * @return
	 */
	public MemoryTypeSpecification attributeTypeName(List<JsonNode> jsonNode, int number) {
		JsonNode typeAttribute = jsonNode.get(number).findParent(Tokens.elementaryTypeName);
		elementaryTypeName = new MemoryTypeSpecification();
		if(Stream.of(Tokens.singleMemoryTypes[0], Tokens.singleMemoryTypes[1], Tokens.singleMemoryTypes[2], Tokens.singleMemoryTypes[3]).anyMatch(typeAttribute.findValue(Tokens.text).asText()::contains)) {
			singleMemoryTypeSpecification = new SingleMemoryTypeSpecification();
			String value = typeAttribute.findValue(Tokens.text).asText();
			if(value.contentEquals("byte")) {
				value = "bytes1";
			}
			String[] valueSplit = value.split("(?<=\\D)(?=\\d)");
			if(valueSplit.length>1) {
				singleMemoryTypeSpecification.setMemory(Short.valueOf(valueSplit[1]));
			}else {
				singleMemoryTypeSpecification.setMemory(Short.valueOf("256"));
				if(valueSplit[0].contentEquals("bytes")) {
					singleMemoryTypeSpecification.setMemory(Short.valueOf("32"));
				}
			}
			singleMemoryTypeSpecification.setType(SingleType.valueOf(valueSplit[0].toUpperCase()));	
			elementaryTypeName.setSingleMemoryType(singleMemoryTypeSpecification);
		}else {
			dualMemoryTypeSpecification = new DualMemoryTypeSpecification();
			String value = typeAttribute.findValue(Tokens.text).asText();
			String[] valueSplit = value.split("(?<=\\D)(?=\\d)");
			dualMemoryTypeSpecification.setType(DualType.valueOf(valueSplit[0].toUpperCase()));
			dualMemoryTypeSpecification.setMemoryN(Short.valueOf(valueSplit[1].substring(0,valueSplit[1].length()-1)));
			dualMemoryTypeSpecification.setMemoryM(Short.valueOf(valueSplit[2]));
			elementaryTypeName.setDualMemoryType(dualMemoryTypeSpecification);
		}
		return elementaryTypeName;
	}

	/**
	 * Find and recopile all the functions in the Solidity Contract
	 * @param entireContract
	 */
	public void solidityFunctions(JsonArray entireContract) {
		JsonNode jsonNode;
		try {
			jsonNode = new ObjectMapper().readTree(entireContract.toString());
			List<JsonNode> functionValue = jsonNode.findParents(Tokens.functionDefinition);
			for(int i = 0; i < functionValue.size(); i++) {
				functionSpecification = new FunctionSpecification();
				parameterSpecificationList = new ArrayList<ParameterSpecification>();
				//				returnParameterSpecificationList = new ArrayList<ParameterSpecification>();
				List<JsonNode> functionDescriptor = jsonNode.findParents(Tokens.functionDescriptor);
				if(functionDescriptor.get(i).findValue(Tokens.text).asText().contentEquals(Tokens.function)) {
					functionSpecification.setHasFunctionName(getFunctionName(functionDescriptor, i));
					List<JsonNode> codeValue = functionValue.get(i).findParents(Tokens.statement);
					//					List<JsonNode> codeValue = jsonNode.findParents(Tokens.statement);
					functionSpecification.setHasCode(getCode(codeValue, i));
					List<JsonNode> modifierValue = jsonNode.findParents(Tokens.modifierList);
					functionSpecification.setVisibility(VisibilitySpecification.valueOf(getVisibility(modifierValue.get(i)).toUpperCase()));
					if(getMutability(modifierValue, i) != null) {
						functionSpecification.setHasFunctionBehaviour(getMutability(modifierValue, i));
					}
					JsonNode parameterValues = functionValue.get(i).findParent(Tokens.parametersList);
					functionSpecification.setParamSpecification(solidityParameters(parameterValues));
					JsonNode parameterReturnValues = functionValue.get(i).findParent(Tokens.returnParameters);
					functionSpecification.setReturnParamSpecification(solidityParameters(parameterReturnValues));
					functionSpecificationList.add(functionSpecification);
				}
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Return the modifiers of a contract
	 * @param entireContract
	 */
	public void solidityModifiers(JsonArray entireContract) {
		JsonNode jsonNode;
		try {
			jsonNode = new ObjectMapper().readTree(entireContract.toString());
			List<JsonNode> modifierValue = jsonNode.findParents(Tokens.modifierDefinition);
			for(int i = 0; i < modifierValue.size(); i++) {
				modifierSpecification = new ModifierSpecification();
				modifierSpecification.setHasModifierName(getModifierName(modifierValue, i));
				List<JsonNode> codeValue = modifierValue.get(i).findParents(Tokens.statement);
				modifierSpecification.setHasCode(getCode(codeValue, i));
				JsonNode parameterValues = modifierValue.get(i).findParent(Tokens.parametersList);
				modifierSpecification.setHasModifierArguments(solidityParameters(parameterValues));
				modifierSpecificationList.add(i, modifierSpecification);
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * TODO
	 * @param entireContract
	 */
	public void solidityEvents(JsonArray entireContract) {
		JsonNode jsonNode;
		try {
			jsonNode = new ObjectMapper().readTree(entireContract.toString());
			List<JsonNode> eventValue = jsonNode.findParents(Tokens.eventDefinition);
			for(int i = 0; i < eventValue.size(); i++) {
				eventSpecification = new EventSpecification();
				eventSpecification.setHasEventName(getModifierName(eventValue, i));
				JsonNode parameterValues = eventValue.get(i).findParent(Tokens.eventParametersList);
				eventSpecification.setHasEventArguments(solidityEventParameters(parameterValues));
				List<JsonNode> checkAnonymous = eventValue.get(i).findValues(Tokens.text);
				if(checkAnonymous.get(checkAnonymous.size()-2).asText().contentEquals(Tokens.ANONYMOUS)) {
					eventSpecification.setAnonymous(true);
				}
				eventSpecificationList.add(i, eventSpecification);
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set the list of the function's parameters
	 * @param jsonNode
	 * @return
	 */
	public ArrayList<ParameterSpecification> solidityParameters(JsonNode jsonNode) {
		parameterSpecificationList = new ArrayList<ParameterSpecification>();
		if(jsonNode != null) {
			List<JsonNode> parameter = jsonNode.findParents(Tokens.parameters);
			for(int i = 0; i < parameter.size(); i++) {
				parameterSpecification = new ParameterSpecification();
				JsonNode userDefinedTypeParameter = parameter.get(i).findParent(Tokens.userDefinedTypeName);
				JsonNode elementaryTypeParameter = parameter.get(i).findParent(Tokens.elementaryTypeName);
				if(userDefinedTypeParameter != null) {
					JsonNode identifierParameter = parameter.get(i).findParent(Tokens.identifier);
					parameterSpecification.setUserParameterType(identifierParameter.findValue(Tokens.text).asText());
					if(parameter.get(i).findParents(Tokens.identifier).size() > 1) {
						JsonNode identifierParameterName = parameter.get(i).findParents(Tokens.identifier).get(1);
						parameterSpecification.setHasParameterName(identifierParameterName.findValue(Tokens.text).asText());
					}
				}else {
					String[] elementaryType = splitElementaryType(elementaryTypeParameter.findValue(Tokens.text).asText());
					parameterSpecification = setMemoryTypes(elementaryType);
					if(parameter.get(i).findParent(Tokens.identifier) != null) {
						JsonNode identifierParameter = parameter.get(i).findParent(Tokens.identifier);
						parameterSpecification.setHasParameterName(identifierParameter.findValue(Tokens.text).asText());
					}
				}
				if(parameter.get(i).findParents(Tokens.text).size() > 1) {
					if(EnumUtils.isValidEnum(DataLocation.class, parameter.get(i).findParents(Tokens.text).get(1).findValue(Tokens.text).asText().toUpperCase())) {
						parameterSpecification.setHasParameterTypeWithDataLocation(DataLocation.valueOf(parameter.get(i).findParents(Tokens.text).get(1).findValue(Tokens.text).asText().toUpperCase()));
					}
				}
				parameterSpecification.setHasParameterPosition(Integer.valueOf(i).shortValue());
				parameterSpecificationList.add(i,parameterSpecification);
			}}
		return parameterSpecificationList;
	}

	/**
	 * Return a function's parameter
	 * @param elementaryType
	 * @return
	 */
	public ParameterSpecification setMemoryTypes(String[] elementaryType) {
		parameterSpecification = new ParameterSpecification();
		singleMemoryTypeSpecification = new SingleMemoryTypeSpecification();
		if(elementaryType[0].toUpperCase().contentEquals("BYTE")) {
			elementaryType = new String[2];
			elementaryType[0] = "BYTES";
			elementaryType[1] = "1";
		}
		if(checkElementaryType(elementaryType[0]) == 1) {
			singleMemoryTypeSpecification.setType(SingleType.valueOf(elementaryType[0].toUpperCase()));
			if(elementaryType.length > 1) {
				singleMemoryTypeSpecification.setMemory(Short.valueOf(elementaryType[1]));
			}else {
				singleMemoryTypeSpecification.setMemory(Short.valueOf("256"));
			}
			parameterSpecification.setMemoryType(singleMemoryTypeSpecification);
		}else if(checkElementaryType(elementaryType[0]) == 2) {
			dualMemoryTypeSpecification = new DualMemoryTypeSpecification();
			dualMemoryTypeSpecification.setType(DualType.valueOf(elementaryType[0].toUpperCase()));
			dualMemoryTypeSpecification.setMemoryN(Short.valueOf(elementaryType[1].substring(0,elementaryType[1].length()-1)));
			dualMemoryTypeSpecification.setMemoryM(Short.valueOf(elementaryType[2]));
			parameterSpecification.setDualMemoryType(dualMemoryTypeSpecification);
		}else if(checkElementaryType(elementaryType[0]) == 3){
			singleMemoryTypeSpecification.setType(SingleType.valueOf(elementaryType[0].toUpperCase()));
			if(elementaryType.length > 1) {
				singleMemoryTypeSpecification.setMemory(Short.valueOf(elementaryType[1]));
			}else {
				singleMemoryTypeSpecification.setMemory(Short.valueOf("32"));
			}
			parameterSpecification.setMemoryType(singleMemoryTypeSpecification);
		}else {
			elementaryTypeSpecification = new ElementaryTypeSpecification();

			elementaryTypeSpecification.setElementaryType(ElementaryType.valueOf(elementaryType[0].toUpperCase()));
			parameterSpecification.setElementaryType(elementaryTypeSpecification);
		}
		return parameterSpecification;
	}

	/**
	 * Set the list of the event's parameters
	 * @param jsonNode
	 * @return
	 */
	public ArrayList<ParameterSpecification> solidityEventParameters(JsonNode jsonNode) {
		parameterSpecificationList = new ArrayList<ParameterSpecification>();
		List<JsonNode> parameter = jsonNode.findParents(Tokens.eventParameters);
		for(int i = 0; i < parameter.size(); i++) {
			parameterSpecification = new ParameterSpecification();
			JsonNode elementaryTypeParameter = parameter.get(i).findParent(Tokens.elementaryTypeName);
			if(elementaryTypeParameter == null) {
				elementaryTypeParameter = parameter.get(i).findParent(Tokens.userDefinedTypeName);
				JsonNode identifierParameter = parameter.get(i).findParents(Tokens.identifier).get(1);
				parameterSpecification.setHasParameterName(identifierParameter.findValue(Tokens.text).asText());
				parameterSpecification.setHasParameterPosition(Integer.valueOf(i).shortValue());
				parameterSpecification.setUserParameterType(elementaryTypeParameter.findValue(Tokens.text).asText());
				parameterSpecificationList.add(i,parameterSpecification);
			}else {
				String[] elementaryType = splitElementaryType(elementaryTypeParameter.findValue(Tokens.text).asText());
				parameterSpecification = setMemoryTypes(elementaryType);
				if(parameter.get(i).findParent(Tokens.identifier) != null) {
					JsonNode identifierParameter = parameter.get(i).findParent(Tokens.identifier);
					parameterSpecification.setHasParameterName(identifierParameter.findValue(Tokens.text).asText());
				}
				if(parameter.get(i).findParents(Tokens.text).size()>1) {
					if(EnumUtils.isValidEnum(DataLocation.class, parameter.get(i).findParents(Tokens.text).get(1).findValue(Tokens.text).asText().toUpperCase())) {
						parameterSpecification.setHasParameterTypeWithDataLocation(DataLocation.valueOf(parameter.get(i).findParents(Tokens.text).get(1).findValue(Tokens.text).asText().toUpperCase()));
					}
				}
				parameterSpecification.setHasParameterPosition(Integer.valueOf(i).shortValue());
				parameterSpecificationList.add(i,parameterSpecification);
			}
		}
		return parameterSpecificationList;
	}

	private int checkElementaryType(String type) {
		type = type.toUpperCase();
		if(type.contains("UINT") || type.contains("INT")) {
			return 1;
		}else if(type.contains("UFIXED")) {
			return 2;
		}else if(type.contains("BYTES")){
			return 3;
		}else {
			return 4;
		}
	}

	/**
	 * Get the function's name
	 * @param jsonNode
	 * @param number
	 * @return
	 */
	public String getFunctionName(List<JsonNode> jsonNode, int number) {
		String name = "";

		List<String> functionValues = jsonNode.get(number).findValuesAsText(Tokens.text);
		for(int j = 0; j < functionValues.size(); j++) {
			//Apparently is not used in any case
			if(functionValues.get(j).contentEquals(Tokens.function)) {

			}else {
				name = functionValues.get(j);
			}
		}
		return name;
	}

	/**
	 * Get the modifier's name
	 * @param jsonNode
	 * @param number
	 * @return
	 */
	private String getModifierName(List<JsonNode> modifierValue, int i) {
		String name = "";
		JsonNode nameModifier = modifierValue.get(i).findParent(Tokens.identifier);
		List<String> functionValues = nameModifier.findValuesAsText(Tokens.text);
		for(int j = 0; j < functionValues.size(); j++) {
			//Apparently is not used in any case
			if(functionValues.get(j).contentEquals(Tokens.function)) {

			}else {
				name = functionValues.get(j);
			}
		}
		return name;
	}

	/**
	 * Get the function's code
	 * @param jsonNode
	 * @param number
	 * @return
	 */
	public String getCode(List<JsonNode> jsonNode, int number) {
		//		List<String> codeValue = jsonNode.get(number).findValuesAsText(Tokens.text);
		String code = "";
		for (int i = 0; i<jsonNode.size(); i++) {
			List<String> codeValue = jsonNode.get(i).findValuesAsText(Tokens.text);
			for(int j = 0; j<codeValue.size(); j++) {
				code += codeValue.get(j) + " ";
			}
		}
		return code;
	}

	/**
	 * Get the function's visibility
	 * @param jsonNode
	 * @param number
	 * @return
	 */
	public String getVisibility(JsonNode jsonNode) {
		List<String> visibilityValue = jsonNode.findValuesAsText(Tokens.text);
		//Por defecto, las variables sin especificar son de tipo "INTERNAL"
		String visibility = "INTERNAL";
		for(int j = 0; j<visibilityValue.size(); j++) {
			if(Stream.of(Tokens.visibility[0], Tokens.visibility[1], Tokens.visibility[2], Tokens.visibility[3]).anyMatch(visibilityValue.get(j)::contentEquals) && visibilityValue.get(j) != null) {
				visibility = visibilityValue.get(j);
			}
		}
		return visibility;
	}

	/**
	 * Get the function's mutability
	 * @param jsonNode
	 * @param number
	 * @return
	 */
	public ArrayList<Object> getMutability(List<JsonNode> jsonNode, int number) {
		List<String> mutabilityValue = jsonNode.get(number).findValuesAsText(Tokens.text);
		String mutability = null;
		ArrayList<Object> mutabilityParam = new ArrayList<Object>();
		for(int j = 0; j<mutabilityValue.size(); j++) {
			if(!Stream.of(Tokens.visibility[0], Tokens.visibility[1], Tokens.visibility[2], Tokens.visibility[3]).anyMatch(mutabilityValue.get(j)::contains)) {
				mutability = mutabilityValue.get(j);
				if(!Stream.of("(", ")").anyMatch(mutability::contains) ) {
					mutabilityParam.add(mutability.toUpperCase());
				}
			}
		}
		return mutabilityParam;
	}

	public String[] splitElementaryType(String parameterType){
		String[] valueSplit = parameterType.split("(?<=\\D)(?=\\d)");
		return valueSplit;
	}

}
