package ethereum.provider;

import java.io.IOException;
import java.math.BigInteger;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import ethereum.model.ChainExtractorModel;
import sancus.Keys;

public class EthereumDataProvider {

	public static Web3jConnector web3j = new Web3jConnector();
	private EthereumBlockExtractor results = new EthereumBlockExtractor();

	public String retrieveBlock(ChainExtractorModel block) {
		try {
			String url = block.getUrl();
			// Use default key from Keys if no URL is provided
			if (url == null || url.isEmpty()) {
				url = Keys.getEthKey();
			}
			if (url != null && !url.isEmpty()) {
				web3j.setConnection(Web3j.build(new HttpService(url)));
			} else {
				throw new IOException("No Ethereum endpoint URL configured. Use --ethkey=\"...\" to set one.");
			}
			String result = results.BlockhainExtractor(BigInteger.valueOf(block.getBlock()));
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return "{\"error\": \"" + e.getMessage() + "\"}";
		} catch (Exception e) {
			e.printStackTrace();
			return "{\"error\": \"" + e.getMessage() + "\"}";
		}
	}

}
