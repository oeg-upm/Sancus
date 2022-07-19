package solidity.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import sancus.model.BlockchainSmartContract;
import sancus.model.Web3jConnector;


public class SmartContractChainAnalyser {

	public static Web3jConnector web3j = new Web3jConnector();

	public SmartContractChainAnalyser() {
		web3j = SmartContractChainAnalyser.web3j;
	}
	
	public String configure(BlockchainSmartContract arg0) {
		String contract = null;
		if(arg0.getHash() != null && arg0.getApi() != null) {
			contract = getEtherscanContracts(arg0.getHash(), arg0.getApi());
		}
		return contract;
	}

	public String getEtherscanContracts(String smartContractDir, String apikey) {
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
				String result = jobject.get(Tokens.SOURCECODE).getAsString();
				return result;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
