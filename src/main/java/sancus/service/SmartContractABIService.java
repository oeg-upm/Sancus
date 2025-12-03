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
	

}
