package solidity.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ParameterSpecification {

	public short hasParameterPosition;
	public DataLocation hasParameterTypeWithDataLocation;
	public String hasParameterName;
	public SingleMemoryTypeSpecification memoryType;
	public DualMemoryTypeSpecification dualMemoryType;
	public ElementaryTypeSpecification elementaryType;
	public String userParameterType;
	
	public enum DataLocation{CALLDATA, MEMORY, STORAGE, INDEXED, PAYABLE}
	
	public short getHasParameterPosition() {
		return hasParameterPosition;
	}

	public void setHasParameterPosition(short hasParameterPosition) {
		this.hasParameterPosition = hasParameterPosition;
	}

    @JsonProperty(value="hasParameterTypeWithDataLocation")
	public DataLocation getHasParameterTypeWithDataLocation() {
		return hasParameterTypeWithDataLocation;
	}

	public void setHasParameterTypeWithDataLocation(DataLocation hasParameterTypeWithDataLocation) {
		this.hasParameterTypeWithDataLocation = hasParameterTypeWithDataLocation;
	}

	public String getHasParameterName() {
		return hasParameterName;
	}

	public void setHasParameterName(String hasParameterName) {
		this.hasParameterName = hasParameterName;
	}
	
	public SingleMemoryTypeSpecification getMemoryType() {
		return memoryType;
	}

	public void setMemoryType(SingleMemoryTypeSpecification memoryType) {
		this.memoryType = memoryType;
	}

	public DualMemoryTypeSpecification getDualMemoryType() {
		return dualMemoryType;
	}

	public void setDualMemoryType(DualMemoryTypeSpecification dualMemoryType) {
		this.dualMemoryType = dualMemoryType;
	}

	public ElementaryTypeSpecification getElementaryType() {
		return elementaryType;
	}

	public void setElementaryType(ElementaryTypeSpecification elementaryType) {
		this.elementaryType = elementaryType;
	}

	public String getUserParameterType() {
		return userParameterType;
	}

	public void setUserParameterType(String userParameterType) {
		this.userParameterType = userParameterType;
	}

}
