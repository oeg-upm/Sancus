package solidity.model;

import java.util.ArrayList;

public class ConstructorSpecification {
	
	public String hasCode;
	public ArrayList<ParameterSpecification> hasConstructorArguments;
	
	public String getHasCode() {
		return hasCode;
	}
	public void setHasCode(String hasCode) {
		this.hasCode = hasCode;
	}
	public ArrayList<ParameterSpecification> getHasConstructorArguments() {
		return hasConstructorArguments;
	}
	public void setHasConstructorArguments(ArrayList<ParameterSpecification> hasConstructorArguments) {
		this.hasConstructorArguments = hasConstructorArguments;
	}

}
