package sancus.service;

import java.io.StringWriter;

import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import sancus.model.BlockchainSmartContract;
import solidity.provider.JsonParserToJSONOnto;
import solidity.provider.SmartContractChainAnalyser;

@Service
public class TranslationRestService {
	
	private JsonParserToJSONOnto jsonldContract = new JsonParserToJSONOnto();
	private SmartContractChainAnalyser scca = new SmartContractChainAnalyser();


	public String getTranslateContractJSONLD(String contract) {
		return jsonldContract.JsonContractToJavaObject(contract, null);
	}
	
	public String getTranslateContractRDF(String contract) {
		JsonObject convertedObject = new Gson().fromJson(jsonldContract.JsonContractToJavaObject(contract, null), JsonObject.class);
		StringWriter stringWriter = new StringWriter();
		SemanticService.toModel(convertedObject,"temp").write(stringWriter, "NT");
		return stringWriter.toString();
		
	}
	
	public String getContractFromHash(BlockchainSmartContract parameters) {
		String contract = scca.configure(parameters);
		return jsonldContract.JsonContractToJavaObject(contract, parameters.getHash());
	}

}
