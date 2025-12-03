package solidity.model;


import com.fasterxml.jackson.annotation.JsonProperty;

public class DualMemoryTypeSpecification {

    @JsonProperty("memoryM")
    public short memoryM;

    @JsonProperty("memoryN")
    public short memoryN;

    @JsonProperty("dualMemoryType")
    private DualType type;

	
	public enum DualType{FIXED, UFIXED};
	
	public DualType getType() {
		return type;
	}
	public void setType(DualType type) {
		this.type = type;
	}
	public short getMemoryM() {
		return memoryM;
	}
	public void setMemoryM(short memoryM) {
		this.memoryM = memoryM;
	}
	public short getMemoryN() {
		return memoryN;
	}
	public void setMemoryN(short memoryN) {
		this.memoryN = memoryN;
	}
}
