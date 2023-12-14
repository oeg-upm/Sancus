package solidity.provider;

import java.io.IOException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class SolidityProvider {

	private ContractConversion cc = new ContractConversion();
	private ContractToOnto cto = new ContractToOnto();
	
	public JsonArray JsonContractToJavaObject(String contract) {
		try {
			JsonObject converted = cc.convertContractAndConvert(contract);
			return cto.finalJson(converted.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
