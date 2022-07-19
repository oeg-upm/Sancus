package solidity.model;

public class MemoryTypeSpecification {

	public SingleMemoryTypeSpecification singleMemoryType;
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
