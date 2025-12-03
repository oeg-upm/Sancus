package solidity.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class ConstructorSpecification {

    @JsonProperty("constructorCode")
    public String hasCode;

    @JsonProperty("hasConstructorArguments")
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
