package solidity.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MemoryTypeSpecification {

    @JsonProperty("singleMemoryType")
    public SingleMemoryTypeSpecification singleMemoryType;

    @JsonProperty("dualMemoryType")
    public DualMemoryTypeSpecification dualMemoryType;

	public SingleMemoryTypeSpecification getSingleMemoryType() {
		return singleMemoryType;
	}

	public void setSingleMemoryType(SingleMemoryTypeSpecification singleMemoryType) {
		this.singleMemoryType = singleMemoryType;
	}

	public DualMemoryTypeSpecification getDualMemoryType() {
		return dualMemoryType;
	}

	public void setDualMemoryType(DualMemoryTypeSpecification dualMemoryType) {
		this.dualMemoryType = dualMemoryType;
	}
	
}
