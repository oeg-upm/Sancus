package solidity.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import sancus.model.BlockchainSmartContract;
import sancus.model.Web3jConnector;

public class SmartContractABIAnalyser {
	
	public static Web3jConnector web3j = new Web3jConnector();

	public SmartContractABIAnalyser() {
		web3j = SmartContractABIAnalyser.web3j;
	}
	
	public String configureABIContract(BlockchainSmartContract arg0) {
		String contract = null;
		if(arg0.getHash() != null && arg0.getApi() != null) {
			contract = getEtherscanABIContracts(arg0.getHash(), arg0.getApi());
		}
		return contract;
	}

	public String getEtherscanABIContracts(String smartContractDir, String apikey) {
		try {
			URL url;
			url = new URL(Tokens.ETHERSCANURL + smartContractDir + Tokens.APIKEY + apikey);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.getResponseCode();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			JsonObject  jobject = JsonParser.parseString(content.toString()).getAsJsonObject();
			if(jobject.get(Tokens.MESSAGE).getAsString().contentEquals(Tokens.OK)) {
				JsonArray jarray = jobject.getAsJsonArray(Tokens.RESULT);
				jobject = jarray.get(0).getAsJsonObject();
				JsonObject abiJson = new JsonObject();
				abiJson.add("@context", contextSpecification());
				JsonArray convertedObject = new Gson().fromJson(jobject.get(Tokens.ABI).getAsString(), JsonArray.class);
				abiJson.add("ABI", convertedObject);
				return abiJson.toString();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public JsonObject contextSpecification() {
		//ABI 
		JsonObject context = new JsonObject();
		context.addProperty(Tokens.VOCAB_LD, "https://ethon.consensys.net/Contracts");
		context.addProperty(Tokens.DESCRIPTION, "http://www.w3.org/2000/01/rdf-schema#label");
		context.addProperty(Tokens.ID,Tokens.ID_LD);
		JsonObject name = new JsonObject();
		name.addProperty(Tokens.ID_LD, TokensABI.NAMEURL);
		name.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(TokensABI.NAME, name);
		JsonObject type = new JsonObject();
		type.addProperty(Tokens.ID_LD, TokensABI.TYPEURL);
		type.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(TokensABI.TYPE, type);
		JsonObject input = new JsonObject();
		input.addProperty(Tokens.ID_LD, TokensABI.INPUTURL);
		input.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(TokensABI.INPUT, input);
		JsonObject output = new JsonObject();
		output.addProperty(Tokens.ID_LD, TokensABI.OUTPUTURL);
		output.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(TokensABI.OUTPUT, output);		
		JsonObject payable = new JsonObject();
		payable.addProperty(Tokens.ID_LD, TokensABI.PAYABLEURL);
		payable.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(TokensABI.PAYABLE, payable);		
		JsonObject constant = new JsonObject();
		constant.addProperty(Tokens.ID_LD, TokensABI.CONSTANTURL);
		constant.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(TokensABI.CONSTANT, constant);
		return context;
	}

}
