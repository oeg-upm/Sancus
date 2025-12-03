package solidity.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class ArraySpecification {

    @JsonProperty("hasType")
	public Object hasType;

    @JsonProperty("hasArrayDimension")
	public ArrayList<ArrayDimensionSpecification> hasArrayDimension;

	public Object getHasType() {
		return hasType;
	}
	public void setHasType(Object hasType) {
		this.hasType = hasType;
	}
	public ArrayList<ArrayDimensionSpecification> getHasArrayDimension() {
		return hasArrayDimension;
	}
	public void setHasArrayDimension(ArrayList<ArrayDimensionSpecification> hasArrayDimension) {
		this.hasArrayDimension = hasArrayDimension;
	}
	
}
