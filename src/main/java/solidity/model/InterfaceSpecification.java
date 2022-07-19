package solidity.model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


public class InterfaceSpecification {
	
	public ArrayList<String> hasImports;
	public ArrayList<String> hasVersion;
	public ArrayList<FunctionSpecification> hasFunctions;
	public ArrayList<EventSpecification> hasEvents;
	
	public ArrayList<String> getHasImports() {
		return hasImports;
	}

	public void setHasImports(ArrayList<String> hasImports) {
		this.hasImports = hasImports;
	}
	
    @JsonProperty(value="hasVersion")
    @JsonInclude(Include.NON_DEFAULT)
    public ArrayList<String> getHasVersion() {
		return hasVersion;
	}

	public void setHasVersion(ArrayList<String> hasVersion) {
		this.hasVersion = hasVersion;
	}

	@JsonProperty(value="hasInterfaceFunction")
	public ArrayList<FunctionSpecification> getHasFunctions() {
		return hasFunctions;
	}

	public void setHasFunctions(ArrayList<FunctionSpecification> hasFunctions) {
		this.hasFunctions = hasFunctions;
	}

	public ArrayList<EventSpecification> getHasEvents() {
		return hasEvents;
	}

	public void setHasEvents(ArrayList<EventSpecification> hasEvents) {
		this.hasEvents = hasEvents;
	}

}
