package ethereum.provider;

import java.io.IOException;
import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class EthereumBlockExtractor {

	public Web3jConnector web3jConnector;
	private JSONManipulation jsonManipulation;
	static Logger logger = LoggerFactory.getLogger(EthereumBlockExtractor.class);


	public EthereumBlockExtractor() {
		web3jConnector = EthereumDataProvider.web3j;
		jsonManipulation = new JSONManipulation();
	}

	/**
	 * 
	 * Extract the entire blockchain or extract the range of one block to the final block of the blockchain.
	 * 
	 */
	public String BlockhainExtractor(BigInteger blockNumber) throws IOException {
		Block block = web3jConnector.getConnection().ethGetBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), true).send().getBlock();
		JsonObject blockJson = new JsonObject();
		blockJson.add("@context", contextSpecification());
		JsonArray transactionsArray = new JsonArray();
		blockJson.addProperty("Number", block.getNumber());
		blockJson.addProperty("Difficulty", block.getDifficulty());
		blockJson.addProperty("ExtraData", block.getExtraData());
		blockJson.addProperty("GasLimit", block.getGasLimit());
		blockJson.addProperty("GasUsed", block.getGasUsed());
		blockJson.addProperty("Hash", block.getHash());
		blockJson.addProperty("ParentHash", block.getParentHash());
		blockJson.addProperty("Size", block.getSize());
		blockJson.addProperty("Timestamp", block.getTimestamp());
		blockJson.addProperty("LogsBloom", block.getLogsBloom());
		blockJson.addProperty("Miner", block.getMiner());
		blockJson.addProperty("MixHash", block.getMixHash());
		blockJson.addProperty("ReceiptsRoot", block.getReceiptsRoot());
		blockJson.addProperty("StateRoot", block.getStateRoot());
		blockJson.addProperty("TransactionsRoot", block.getTransactionsRoot());
		blockJson.addProperty("Nonce", block.getNonce());
		blockJson.addProperty("TotalDifficulty", block.getTotalDifficulty());
		//Inside each block, can be 1 or more transactions.
		for(int j=0; j<block.getTransactions().size(); j++) {
			TransactionResult<Transaction> tr = block.getTransactions().get(j);
			JsonObject transactionJson = new JsonObject();
			transactionJson.addProperty("BlockHash", tr.get().getBlockHash());
			transactionJson.addProperty("BlockNumber", tr.get().getBlockNumber());
			transactionJson.addProperty("TxFrom", tr.get().getFrom());
			transactionJson.addProperty("TxGas", tr.get().getGas());
			transactionJson.addProperty("TxGasPrice", tr.get().getGasPrice());
			transactionJson.addProperty("TxHash", tr.get().getHash());
			//Check if the user want to decode the input data inside the transaction
			if(!web3jConnector.isDecoder()) {
				transactionJson.addProperty("Input", tr.get().getInput());	
			}else {
				transactionJson.add("Input", jsonManipulation.HexToString(tr.get().getInput().substring(2)));
			}
			transactionJson.addProperty("TxTransactionIndex", tr.get().getTransactionIndex());	
			transactionJson.addProperty("TxCreates", tr.get().getCreates());
			transactionJson.addProperty("TxPublicKey", tr.get().getPublicKey());
			transactionJson.addProperty("TxNonce", tr.get().getNonce());
			transactionJson.addProperty("TxValue", tr.get().getValue());
			transactionJson.addProperty("TxTo", tr.get().getTo());
			transactionJson.addProperty("TxChainId", tr.get().getChainId());
			transactionJson.addProperty("TxS", tr.get().getS());
			transactionJson.addProperty("TxV", tr.get().getV());
			transactionJson.addProperty("TxR", tr.get().getR());
			transactionsArray.add(transactionJson);
			blockJson.add("Transactions", transactionsArray);
		}
		return blockJson.toString();
	}
	
	public JsonObject contextSpecification() {
		JsonObject myData = new JsonObject();
		//Block
		JsonObject context = new JsonObject();
		context.addProperty(Tokens.VOCAB_LD, "https://ethon.consensys.net/");
		context.addProperty(Tokens.DESCRIPTION, "http://www.w3.org/2000/01/rdf-schema#label");
		context.addProperty(Tokens.ID,Tokens.ID_LD);
		JsonObject number = new JsonObject();
		number.addProperty(Tokens.ID_LD, Tokens.NUMBERURL);
		number.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.NUMBER, number);
		JsonObject blockDifficulty = new JsonObject();
		blockDifficulty.addProperty(Tokens.ID_LD, Tokens.DIFFICULTYURL);
		blockDifficulty.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.DIFFICULTY, blockDifficulty);
		JsonObject extraData = new JsonObject();
		extraData.addProperty(Tokens.ID_LD, Tokens.EXTRADATAURL);
		extraData.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.EXTRADATA, extraData);
		JsonObject gasLimit = new JsonObject();
		gasLimit.addProperty(Tokens.ID_LD, Tokens.GASLIMITURL);
		gasLimit.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.GASLIMIT, gasLimit);
		JsonObject gasUsed = new JsonObject();
		gasUsed.addProperty(Tokens.ID_LD, Tokens.GASUSEDURL);
		gasUsed.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.GASUSED, gasUsed);
		JsonObject hash = new JsonObject();
		hash.addProperty(Tokens.ID_LD, Tokens.HASHURL);
		hash.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.HASH, hash);
		JsonObject parentHash = new JsonObject();
		parentHash.addProperty(Tokens.ID_LD, Tokens.PARENTHASHURL);
		parentHash.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.PARENTHASH, parentHash);
		JsonObject size = new JsonObject();
		size.addProperty(Tokens.ID_LD, Tokens.SIZEURL);
		size.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.SIZE, size);	
		JsonObject timestamp = new JsonObject();
		timestamp.addProperty(Tokens.ID_LD, Tokens.TIMESTAMPURL);
		timestamp.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.TIMESTAMP, timestamp);	
		JsonObject logsBloom = new JsonObject();
		logsBloom.addProperty(Tokens.ID_LD, Tokens.LOGSBLOOMURL);
		logsBloom.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.LOGSBLOOM, logsBloom);	
		JsonObject miner = new JsonObject();
		miner.addProperty(Tokens.ID_LD, Tokens.MINERURL);
		miner.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.MINER, miner);	
		JsonObject mixHash = new JsonObject();
		mixHash.addProperty(Tokens.ID_LD, Tokens.MIXHASHURL);
		mixHash.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.MIXHASH, mixHash);	
		JsonObject receiptsRoot = new JsonObject();
		receiptsRoot.addProperty(Tokens.ID_LD, Tokens.RECEPITSROOTURL);
		receiptsRoot.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.RECEPITSROOT, receiptsRoot);	
		JsonObject stateRoot = new JsonObject();
		stateRoot.addProperty(Tokens.ID_LD, Tokens.STATEROOTURL);
		stateRoot.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.STATEROOT, stateRoot);	
		JsonObject transactionsRoot = new JsonObject();
		transactionsRoot.addProperty(Tokens.ID_LD, Tokens.TRANSACTIONSROOTURL);
		transactionsRoot.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.TRANSACTIONSROOT, transactionsRoot);	
		JsonObject nonce = new JsonObject();
		nonce.addProperty(Tokens.ID_LD, Tokens.NONCEURL);
		nonce.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.NONCE, nonce);	
		JsonObject totalDifficulty = new JsonObject();
		totalDifficulty.addProperty(Tokens.ID_LD, Tokens.TOTALDIFFICULTYURL);
		totalDifficulty.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.TOTALDIFFICULTY, totalDifficulty);	
		//Transactions
		JsonObject trBlockHash = new JsonObject();
		trBlockHash.addProperty(Tokens.ID_LD, Tokens.HASHURL);
		trBlockHash.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.HASH, trBlockHash);
		
		JsonObject trBlockNumber = new JsonObject();
		trBlockNumber.addProperty(Tokens.ID_LD, Tokens.NUMBERURL);
		trBlockNumber.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.NUMBER, trBlockNumber);
		JsonObject trFrom = new JsonObject();
		trFrom.addProperty(Tokens.ID_LD, Tokens.TRFROMURL);
		trFrom.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.TRFROM, trFrom);
		JsonObject trGas = new JsonObject();
		trGas.addProperty(Tokens.ID_LD, Tokens.TRGASURL);
		trGas.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.TRGAS, trGas);
		JsonObject trGasPrice = new JsonObject();
		trGasPrice.addProperty(Tokens.ID_LD, Tokens.TRGASPRICEURL);
		trGasPrice.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.TRGASPRICE, trGasPrice);
		JsonObject trHash = new JsonObject();
		trHash.addProperty(Tokens.ID_LD, Tokens.TRHASHURL);
		trHash.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.TRHASH, trHash);
		JsonObject trInput = new JsonObject();
		trInput.addProperty(Tokens.ID_LD, Tokens.TRINPUTURL);
		trInput.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.TRINPUT, trInput);
		JsonObject trTransactionIndex = new JsonObject();
		trTransactionIndex.addProperty(Tokens.ID_LD, Tokens.TRTRANSACTIONINDEXURL);
		trTransactionIndex.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.TRTRANSACTIONINDEX, trTransactionIndex);
		JsonObject trCreates = new JsonObject();
		trCreates.addProperty(Tokens.ID_LD, Tokens.TRCREATESURL);
		trCreates.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.TRCREATES, trCreates);
		JsonObject trPublicKey = new JsonObject();
		trPublicKey.addProperty(Tokens.ID_LD, Tokens.TRPUBLICKEYURL);
		trPublicKey.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.TRPUBLICKEY, trPublicKey);
		JsonObject trNonce = new JsonObject();
		trNonce.addProperty(Tokens.ID_LD, Tokens.TRNONCEURL);
		trNonce.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.TRNONCE, trNonce);
		JsonObject trValue = new JsonObject();
		trValue.addProperty(Tokens.ID_LD, Tokens.TRVALUEURL);
		trValue.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.TRVALUE, trValue);
		JsonObject trTo = new JsonObject();
		trTo.addProperty(Tokens.ID_LD, Tokens.TRTOURL);
		trTo.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.TRTO, trTo);
		JsonObject trChainId = new JsonObject();
		trChainId.addProperty(Tokens.ID_LD, Tokens.TRCHAINIDURL);
		trChainId.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.TRCHAINID, trChainId);
		JsonObject trS = new JsonObject();
		trS.addProperty(Tokens.ID_LD, Tokens.TRSURL);
		trS.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.TRS, trS);
		JsonObject trV = new JsonObject();
		trV.addProperty(Tokens.ID_LD, Tokens.TRVURL);
		trV.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.TRV, trV);
		JsonObject trR = new JsonObject();
		trR.addProperty(Tokens.ID_LD, Tokens.TRRURL);
		trR.addProperty(Tokens.TYPE_LD, Tokens.VOCAB_LD);
		context.add(Tokens.TRR, trR);
		
//		myData.add("@context", context);
		return context;
	}
	
}
