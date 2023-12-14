package ethereum.model;

import java.io.Serializable;


public class ChainExtractorModel{
	
	private String endPoint;
	private int block;

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

}
