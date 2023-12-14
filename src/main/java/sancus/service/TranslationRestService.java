package sancus.service;

import java.io.StringWriter;

import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import sancus.model.BlockchainSmartContract;
import solidity.provider.SolidityProvider;
import solidity.provider.SmartContractChainAnalyser;

@Service
public class TranslationRestService {
	
	private SolidityProvider jsonldContract = new SolidityProvider();
	private SmartContractChainAnalyser scca = new SmartContractChainAnalyser();


	public String getTranslateContractJSONLD(String contract) {
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		return gson.toJson(jsonldContract.JsonContractToJavaObject(contract));
	}
	
	public String getContractFromHash(BlockchainSmartContract parameters) {
		String contract = scca.configure(parameters);
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		return gson.toJson(jsonldContract.JsonContractToJavaObject(contract));
	}

}
