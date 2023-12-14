package ethereum.provider;

import java.io.IOException;
import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.AccessListObject;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionResult;
import org.web3j.protocol.core.methods.response.EthBlock.Withdrawal;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class EthereumBlockExtractor {

	public Web3jConnector web3jConnector;
	static Logger logger = LoggerFactory.getLogger(EthereumBlockExtractor.class);


	public EthereumBlockExtractor() {
		web3jConnector = EthereumDataProvider.web3j;
	}

	/**
	 * 
	 * Extract the entire blockchain or extract the range of one block to the final block of the blockchain.
	 * 
	 */
	public String BlockhainExtractor(BigInteger blockNumber) throws IOException {
		Block block = web3jConnector.getConnection().ethGetBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), true).send().getBlock();
		JsonObject blockJson = new JsonObject();
		JsonArray transactionsArray = new JsonArray();
		JsonArray withdrawalsArray = new JsonArray();
		blockJson.addProperty("@context", "https://oeg-upm.github.io/Ethereum-ontology/context/context.json");
		blockJson.addProperty("@id", "uuid:"+UUID.randomUUID().toString());
		blockJson.addProperty("author", block.getAuthor());
		try {
			blockJson.addProperty("baseFeePerGas", block.getBaseFeePerGas());
		}catch (Exception e) {}
		blockJson.addProperty("difficulty", block.getDifficulty());
		blockJson.addProperty("extraData", block.getExtraData());
		blockJson.addProperty("gasLimit", block.getGasLimit());
		blockJson.addProperty("gasUsed", block.getGasUsed());
		blockJson.addProperty("hash", block.getHash());
		blockJson.addProperty("logsBloom", block.getLogsBloom());
		blockJson.addProperty("miner", block.getMiner());
		blockJson.addProperty("mixHash", block.getMixHash());
		blockJson.addProperty("nonce", block.getNonce());
		blockJson.addProperty("number", block.getNumber());
		blockJson.addProperty("parentHash", block.getParentHash());
		blockJson.addProperty("receiptsRoot", block.getReceiptsRoot());
		blockJson.addProperty("sha3Uncles", block.getSha3Uncles());
		blockJson.addProperty("size", block.getSize());
		blockJson.addProperty("stateRoot", block.getStateRoot());
		blockJson.addProperty("timestamp", block.getTimestamp());
		blockJson.addProperty("totalDifficulty", block.getTotalDifficulty());
		blockJson.addProperty("transactionsRoot", block.getTransactionsRoot());	
		blockJson.addProperty("withdrawalsRoot", block.getWithdrawalsRoot());	
		for(int j=0; j<block.getTransactions().size(); j++) {
			TransactionResult<Transaction> tr = block.getTransactions().get(j);
			JsonArray accessListArray = new JsonArray();
			JsonObject transactionJson = new JsonObject();
			transactionJson.addProperty("blockHash", tr.get().getBlockHash());
			transactionJson.addProperty("blockNumber", tr.get().getBlockNumber());
			transactionJson.addProperty("chainId", tr.get().getChainId());
			transactionJson.addProperty("creates", tr.get().getCreates());
			transactionJson.addProperty("from", tr.get().getFrom());
			transactionJson.addProperty("gas", tr.get().getGas());
			transactionJson.addProperty("gasPrice", tr.get().getGasPrice());
			transactionJson.addProperty("hash", tr.get().getHash());
			transactionJson.addProperty("input", tr.get().getInput());				
			try {
				transactionJson.addProperty("maxFeePerGas", tr.get().getMaxFeePerGas());	
				transactionJson.addProperty("maxPriorityFeePerGas", tr.get().getMaxPriorityFeePerGas());	
			}catch (Exception e) {}
			transactionJson.addProperty("nonce", tr.get().getNonce());
			transactionJson.addProperty("publicKey", tr.get().getPublicKey());
			transactionJson.addProperty("r", tr.get().getR());
			transactionJson.addProperty("s", tr.get().getS());
			transactionJson.addProperty("to", tr.get().getTo());
			transactionJson.addProperty("transactionIndex", tr.get().getTransactionIndex());
			transactionJson.addProperty("type", tr.get().getType());
			transactionJson.addProperty("v", tr.get().getV());
			transactionJson.addProperty("value", tr.get().getValue());
			if(tr.get().getAccessList()!=null) {
				for(int z=0; z<tr.get().getAccessList().size(); z++) {
					JsonObject accessListJson = new JsonObject();
					AccessListObject alo = tr.get().getAccessList().get(z);
					JsonArray keysArray = new JsonArray();
					accessListJson.addProperty("address", alo.getAddress());
					for(int key=0; key<alo.getStorageKeys().size(); key++) {
						keysArray.add(alo.getStorageKeys().get(key));
						accessListJson.add("keys", keysArray);
					}
					accessListArray.add(accessListJson);
					transactionJson.add("hasAccessList", accessListArray);
				}
			}
			transactionsArray.add(transactionJson);
			blockJson.add("hasTransactions", transactionsArray);
		}
		if(block.getWithdrawals()!=null) {
			for(int j=0; j<block.getWithdrawals().size(); j++) {
				JsonObject withdrawalJson = new JsonObject();
				Withdrawal wd = block.getWithdrawals().get(j);
				withdrawalJson.addProperty("address", wd.getAddress());
				withdrawalJson.addProperty("index", wd.getIndex());
				withdrawalJson.addProperty("validatorIndex", wd.getValidatorIndex());
				withdrawalJson.addProperty("amount", wd.getAmount());
				withdrawalsArray.add(withdrawalJson);
				blockJson.add("hasWithdrawals", withdrawalsArray);
			}
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		return gson.toJson(blockJson);
	}
	
}
