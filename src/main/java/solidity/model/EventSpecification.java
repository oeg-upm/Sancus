package solidity.model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EventSpecification {

    @JsonProperty("hasEventArguments")
    public ArrayList<ParameterSpecification> hasEventArguments;

    @JsonProperty("eventName")
    public String hasEventName;

    @JsonProperty("isAnonymous")
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
