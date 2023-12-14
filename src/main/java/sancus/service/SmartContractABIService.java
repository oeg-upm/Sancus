package sancus.service;

import org.springframework.stereotype.Service;

import abi.provider.SmartContractABIAnalyser;
import sancus.model.BlockchainSmartContract;

@Service
public class SmartContractABIService {
	
	private SmartContractABIAnalyser scABIa = new SmartContractABIAnalyser();

	public String getABIContractJSONLD(BlockchainSmartContract contractConf) {
		return scABIa.configureABIContract(contractConf);
	}
}
