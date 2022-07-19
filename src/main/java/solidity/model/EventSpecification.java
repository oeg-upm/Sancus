package solidity.model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class EventSpecification {

	public ArrayList<ParameterSpecification> hasEventArguments;
	public String hasEventName;
	public boolean anonymous;
	
	public ArrayList<ParameterSpecification> getHasEventArguments() {
		return hasEventArguments;
	}
	
	public void setHasEventArguments(ArrayList<ParameterSpecification> hasEventArguments) {
		this.hasEventArguments = hasEventArguments;
	}
	
	public String getHasEventName() {
		return hasEventName;
	}
	
	public void setHasEventName(String hasEventName) {
		this.hasEventName = hasEventName;
	}

    @JsonInclude(Include.NON_DEFAULT)
	public boolean isAnonymous() {
		return anonymous;
	}

	public void setAnonymous(boolean anonymous) {
		this.anonymous = anonymous;
	}
	
}
