package sancus.service;

import java.io.StringWriter;

import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ethereum.model.ChainExtractorModel;
import ethereum.provider.EthereumDataProvider;

@Service
public class SemanticEthereumService {
	
	private EthereumDataProvider blockProvider = new EthereumDataProvider();
	
	public String getBlockJSONLD(ChainExtractorModel blockConf) {
		return blockProvider.retrieveBlock(blockConf);
	}
	
	public String getBlockRDF(ChainExtractorModel blockConf) {
		JsonObject convertedObject = new Gson().fromJson(blockProvider.retrieveBlock(blockConf), JsonObject.class);
		StringWriter stringWriter = new StringWriter();
		SemanticService.toModel(convertedObject,"temp").write(stringWriter, "NT");
		return stringWriter.toString();
	}

}
