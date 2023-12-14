package solidity.provider;

public class Tokens {
	
	//JSON Antlr4
	public static final String CONTRACTDEFINITION = "contractDefinition";
	public static final String NAME = "name";

	public static final String stateVariableDeclaration = "stateVariableDeclaration";
	public static final String variableDeclaration = "variableDeclaration";
	public static final String enumDefinition = "enumDefinition";
	public static final String structDefinition = "structDefinition";
	public static final String contractPart = "contractPart";
	public static final String functionDefinition = "functionDefinition";
	public static final String functionDescriptor = "functionDescriptor";
	public static final String modifierDefinition = "modifierDefinition";
	public static final String eventDefinition = "eventDefinition";
	public static final String function = "function";
	public static final String constructor = "constructor";
	public static final String identifier = "identifier";
	public static final String text = "text";
	public static final String statement = "statement";
	public static final String modifierList = "modifierList";
	public static final String pragmaDirective = "pragmaDirective";
	public static final String inheritanceSpecifier = "inheritanceSpecifier";
	public static final String USINGFORDECLARATION = "usingForDeclaration";
	public static final String ANONYMOUS = "anonymous";
	public static final String IMPORTDIRECTIVE = "importDirective";

	//JSON Antlr4 types
	public static final String type = "type";
	public static final String typeName = "typeName";
	public static final String elementaryTypeName = "elementaryTypeName";
	public static final String userDefinedTypeName = "userDefinedTypeName";
	public static final String MAPPING = "mapping";
	public static final String struct = "structDefinition";
	public static final String enumValue = "enumValue";
	//JSON Antlr4 parameter
	public static final String parameters = "parameter";
	public static final String parametersList = "parameterList";
	public static final String returnParameters = "returnParameters";
	public static final String eventParametersList = "eventParameterList";
	public static final String eventParameters = "eventParameter";
	
	//JSON Antlr4 Array
	public static final String numberLiteral = "numberLiteral";

	//General
	public static final String dotcomma = ";";
	
	//Types
	public static final String[] singleMemoryTypes = {"uint", "int", "bytes", "byte"};
	public static final String[] visibility = {"public","private","external","internal"};
	
	//JSON-LD
	public static final String ID_LD = "@id";
	public static final String TYPE_LD = "@type";
	public static final String VOCAB_LD = "@vocab";
	public static final String ID = "id";
	public static final String DESCRIPTION = "description";

	
	//SmartContract Analyser
	public static final String ETHERSCANURL = "https://api.etherscan.io/api?module=contract&action=getsourcecode&address=";
	public static final String APIKEY = "&apikey=";
	
	
	public static final String DASH = "-";
	public static final String ASTERISK = "*";
	public static final String BLOCKS = "blocks";
	public static final String API = "api";
	public static final String URL = "url";
	
	public static final String ABI = "ABI";

	public static final String SOURCECODE = "SourceCode";
	public static final String MESSAGE = "message";
	public static final String RESULT = "result";
	public static final String OK = "OK";
	public static final String ERROR = "ERROR";
}
