package solidity.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ethereum.provider.EthereumBlockExtractor;
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

				
				JsonObject abi = abiConverter(jobject, smartContractDir); 
				
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				return gson.toJson(abi);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (org.json.JSONException e) {
			e.printStackTrace();
		}
		return "[\"result\": \"error\"]";
	}
	
    public static JsonObject abiConverter(JsonObject jsonInput, String address) {

    	JSONArray arrayABI = new JSONArray(jsonInput.get("ABI").getAsString());
    	
    	JsonObject abiJSON = new JsonObject();
    	abiJSON.addProperty("@context", "https://oeg-upm.github.io/Solidity-ABI-ontology/context/context.json");
    	abiJSON.addProperty("@id", "uuid:"+UUID.randomUUID().toString());
    	abiJSON.addProperty("address", address);
    	abiJSON.addProperty("name", jsonInput.get("ContractName").getAsString());

    	JsonArray eventArray = new JsonArray();
    	JsonArray errorArray = new JsonArray();
    	JsonArray functionArray = new JsonArray();

    	for(int i = 0; i < arrayABI.length(); i++) {
    		if(arrayABI.getJSONObject(i).getString("type").contentEquals("event")) {
    			JsonObject event = new JsonObject();
    			event.addProperty("name", arrayABI.getJSONObject(i).getString("name"));
    			if(arrayABI.getJSONObject(i).has("anonymous")) {
    				event.addProperty("isAnonymous", arrayABI.getJSONObject(i).getBoolean("anonymous"));
    			}
    			
    			if(arrayABI.getJSONObject(i).has("inputs")) {
    				JsonArray inputs = new JsonArray();
        			checkInputs(arrayABI.getJSONObject(i).getJSONArray("inputs"), inputs);
        			event.add("hasInput", inputs);
        			
    			}
    			if(arrayABI.getJSONObject(i).has("outputs")) {
    				JsonArray outputs = new JsonArray();
    				checkInputs(arrayABI.getJSONObject(i).getJSONArray("outputs"), outputs);
    				event.add("hasOutput", outputs);
    			}   
    			
    			eventArray.add(event);
    		}else if(arrayABI.getJSONObject(i).getString("type").contentEquals("error")) {
    			JsonObject error = new JsonObject();
    			error.addProperty("name", arrayABI.getJSONObject(i).getString("name"));
    			
    			if(arrayABI.getJSONObject(i).has("inputs")) {
    				JsonArray inputs = new JsonArray();
        			checkInputs(arrayABI.getJSONObject(i).getJSONArray("inputs"), inputs);
        			error.add("hasInput", inputs);
        			
    			}
    			if(arrayABI.getJSONObject(i).has("outputs")) {
    				JsonArray outputs = new JsonArray();
    				checkInputs(arrayABI.getJSONObject(i).getJSONArray("outputs"), outputs);
    				error.add("hasOutput", outputs);
    			}
    			
    			errorArray.add(error);
    		}else {
    			JsonObject function = new JsonObject();
    			function.addProperty("@type", arrayABI.getJSONObject(i).getString("type"));
    			if(arrayABI.getJSONObject(i).has("name")) {
    				function.addProperty("name", arrayABI.getJSONObject(i).getString("name"));
        			
    			}
    			if(arrayABI.getJSONObject(i).has("stateMutability")) {
        			function.addProperty("hasStateMutability", arrayABI.getJSONObject(i).getString("stateMutability"));
    			}
    			
    			if(arrayABI.getJSONObject(i).has("inputs")) {
    				JsonArray inputs = new JsonArray();
        			checkInputs(arrayABI.getJSONObject(i).getJSONArray("inputs"), inputs);
        			function.add("hasInput", inputs);
        			
    			}
    			if(arrayABI.getJSONObject(i).has("outputs")) {
    				JsonArray outputs = new JsonArray();
    				checkInputs(arrayABI.getJSONObject(i).getJSONArray("outputs"), outputs);
        			function.add("hasOutput", outputs);
    			}
    			
    			functionArray.add(function);
    		}
        }
    	abiJSON.add("hasFunction", functionArray);
    	abiJSON.add("hasEvent", eventArray);
    	abiJSON.add("hasError", errorArray);

//    	System.out.println(abiJSON);
    	return abiJSON;
    }
        
    public static JsonArray checkInputs(JSONArray inputList, JsonArray finalArray) {
//    	System.out.println(inputList);
    	for(int i = 0; i < inputList.length(); i++) {
    		JsonObject input = new JsonObject();
    		if(inputList.getJSONObject(i).has("name")) {
    			input.addProperty("name", inputList.getJSONObject(i).getString("name"));
			}
    		if(inputList.getJSONObject(i).has("internalType")) {
    			input.addProperty("internalType", inputList.getJSONObject(i).getString("internalType"));
			}
    		if(inputList.getJSONObject(i).has("type")) {
    			input.addProperty("type", inputList.getJSONObject(i).getString("type"));
			}
    		if(inputList.getJSONObject(i).has("indexed")) {
    			input.addProperty("indexed", inputList.getJSONObject(i).getBoolean("indexed"));
			}
    		if(inputList.getJSONObject(i).has("components")) {
    			JsonArray components = new JsonArray();
    			checkComponents(inputList.getJSONObject(i).getJSONArray("components"), components);
    			input.add("hasComponent", components);
			}
    		finalArray.add(input);
    	}
		return finalArray;
    }
    
    public static JsonArray checkComponents(JSONArray inputList, JsonArray finalArray) {
    	for(int i = 0; i < inputList.length(); i++) {
    		JsonObject input = new JsonObject();
    		if(inputList.getJSONObject(i).has("name")) {
    			input.addProperty("name", inputList.getJSONObject(i).getString("name"));
			}
    		if(inputList.getJSONObject(i).has("internalType")) {
    			input.addProperty("internalType", inputList.getJSONObject(i).getString("internalType"));
			}
    		if(inputList.getJSONObject(i).has("type")) {
    			input.addProperty("type", inputList.getJSONObject(i).getString("type"));
			}
    		if(inputList.getJSONObject(i).has("indexed")) {
    			input.addProperty("indexed", inputList.getJSONObject(i).getBoolean("indexed"));
			}
    		if(inputList.getJSONObject(i).has("components")) {
    			JsonArray components = new JsonArray();
    			checkComponents(inputList.getJSONObject(i).getJSONArray("components"), components);
    			input.add("hasSubComponent", components);
			}
    		finalArray.add(input);
    	}
		return finalArray;
    }
    
}
