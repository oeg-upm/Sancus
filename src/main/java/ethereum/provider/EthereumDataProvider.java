package ethereum.provider;

import java.io.IOException;
import java.math.BigInteger;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import ethereum.model.ChainExtractorModel;

public class EthereumDataProvider {

	private EthereumBlockExtractor results = new EthereumBlockExtractor();
	public static Web3jConnector web3j = new Web3jConnector();

	public String retrieveBlock(ChainExtractorModel block) {
		try {
			String result = null;
			if(block.getUrl() != null) {
				web3j.setConnection(Web3j.build(new HttpService(block.getUrl())));
			}
			result =results.BlockhainExtractor(BigInteger.valueOf(block.getBlock()));
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "[\"result\": \"error\"]";
	}

}
