package sancus.model;

import io.swagger.annotations.ApiModelProperty;

public class BlockchainSmartContract {
	
//	@ApiModelProperty(notes = "text", example = "text", required = true) 
//	private String url;
	@ApiModelProperty(notes = "text", example = "text", required = true) 
	private String api;
	@ApiModelProperty(notes = "text", example = "text", required = true) 
	private String hash;
	@ApiModelProperty(notes = "Convert to RDF. If not, the contract will be returned as JSON-LD", example = "True or False", required = true) 
	private boolean convertToRDF;
	
//	public String getUrl() {
//		return url;
//	}
//	public void setUrl(String url) {
//		this.url = url;
//	}
	public String getApi() {
		return api;
	}
	public void setApi(String api) {
		this.api = api;
	}
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	public boolean isConvertToRDF() {
		return convertToRDF;
	}
	public void setConvertToRDF(boolean convertToRDF) {
		this.convertToRDF = convertToRDF;
	}

}
