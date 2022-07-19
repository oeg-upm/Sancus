package solidity.model;

import java.util.ArrayList;

public class ArraySpecification {
	
	public Object hasType;
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
