package solidity.model;


public class DualMemoryTypeSpecification {
	
	public short memoryM;
	public short memoryN;
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
