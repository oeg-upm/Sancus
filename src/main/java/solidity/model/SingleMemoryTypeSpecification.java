package solidity.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SingleMemoryTypeSpecification {
    @JsonProperty("memory")
    public short memory;

    @JsonProperty("singleMemoryType")
    private SingleType type;
	
	public SingleType getType() {
		return type;
	}
	public void setType(SingleType type) {
		this.type = type;
	}
	public enum SingleType{INT, BYTES, UINT};
	
	public short getMemory() {
		return memory;
	}
	public void setMemory(short memory) {
		this.memory = memory;
	}
	
}
