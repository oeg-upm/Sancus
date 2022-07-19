package ethereum.model;

import java.io.Serializable;


public class ChainExtractorModel{
	
	private String endPoint;
	private int block;
	private boolean decode;
	private boolean isJSONLD;

	public ChainExtractorModel() {}
	
	public String getUrl() {
		return endPoint;
	}
	public void setUrl(String endPoint) {
		this.endPoint = endPoint;
	}
	public int getBlock() {
		return block;
	}
	public void setBlock(int block) {
		this.block = block;
	}
	public boolean isDecode() {
		return decode;
	}
	public void setDecode(boolean decode) {
		this.decode = decode;
	}
	public boolean isJSONLD() {
		return isJSONLD;
	}

	public void setJSONLD(boolean isJSONLD) {
		this.isJSONLD = isJSONLD;
	}
	
	

}
