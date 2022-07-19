package sancus.service;

import solidity.provider.SmartContractABIAnalyser;
import solidity.provider.SmartContractChainAnalyser;

import java.io.StringWriter;

import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import sancus.model.BlockchainSmartContract;

@Service
public class SmartContractABIService {
	
	private SmartContractABIAnalyser scABIa = new SmartContractABIAnalyser();

	public String getABIContractJSONLD(BlockchainSmartContract contractConf) {
		return scABIa.configureABIContract(contractConf);
	}
	
	public String getABIContractRDF(BlockchainSmartContract contractConf) {
		JsonObject convertedObject = new Gson().fromJson(scABIa.configureABIContract(contractConf), JsonObject.class);
		StringWriter stringWriter = new StringWriter();
		SemanticService.toModel(convertedObject,"temp").write(stringWriter, "NT");
		return stringWriter.toString();
	}

}
