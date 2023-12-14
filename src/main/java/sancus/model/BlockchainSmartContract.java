package sancus.model;

import io.swagger.annotations.ApiModelProperty;

public class BlockchainSmartContract {
	
//	@ApiModelProperty(notes = "text", example = "text", required = true) 
//	private String url;
	@ApiModelProperty(notes = "text", example = "text", required = true) 
	private String api;
	@ApiModelProperty(notes = "text", example = "text", required = true) 
	private String hash;
	
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

}
