package solidity.model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FunctionSpecification {
	
	public String hasFunctionName;
	public String hasCode;
//	public Object hasFunctionBehaviour;
	public ArrayList<Object> hasFunctionBehaviour;
	public VisibilitySpecification hasVisibility;
	public ArrayList<ParameterSpecification> paramSpecification;
	public ArrayList<ParameterSpecification> returnParamSpecification;
	
//	public Object getHasFunctionBehaviour() {
//		return hasFunctionBehaviour;
//	}
//	public void setHasFunctionBehaviour(Object hasFunctionBehaviour) {
//		this.hasFunctionBehaviour = hasFunctionBehaviour;
//	}
	
    @JsonProperty(value="hasFunctionBehaviour")
	public ArrayList<Object> getHasFunctionBehaviour() {
		return hasFunctionBehaviour;
	}
	
	public void setHasFunctionBehaviour(ArrayList<Object> hasFunctionBehaviour) {
		this.hasFunctionBehaviour = hasFunctionBehaviour;
	}
	
    @JsonProperty(value="hasFunctionVisibility")
	public VisibilitySpecification getVisibility() {
		return hasVisibility;
	}
	public void setVisibility(VisibilitySpecification hasVisibility) {
		this.hasVisibility = hasVisibility;
	}
	public enum FunctionBehaviour{PURE, VIEW, PAYABLE, VIRTUAL, OVERRIDE, MODIFIER, CONSTANT};
	
	
	public String getHasFunctionName() {
		return hasFunctionName;
	}
	public void setHasFunctionName(String hasFunctionName) {
		this.hasFunctionName = hasFunctionName;
	}
	public String getHasCode() {
		return hasCode;
	}
	public void setHasCode(String hasCode) {
		this.hasCode = hasCode;
	}
	
    @JsonProperty(value="hasFunctionArguments")
	public ArrayList<ParameterSpecification> getParamSpecification() {
		return paramSpecification;
	}
	public void setParamSpecification(ArrayList<ParameterSpecification> paramSpecification) {
		this.paramSpecification = paramSpecification;
	}
	
    @JsonProperty(value="hasFunctionReturn")
	public ArrayList<ParameterSpecification> getReturnParamSpecification() {
		return returnParamSpecification;
	}
	public void setReturnParamSpecification(ArrayList<ParameterSpecification> returnParamSpecification) {
		this.returnParamSpecification = returnParamSpecification;
	}

	

}
