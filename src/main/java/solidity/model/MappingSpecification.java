package solidity.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MappingSpecification {

    @JsonProperty("hasValueMap")
    public Object hasValueMap;

    @JsonProperty("hasKeyMap")
    public Object hasKeyMap;
	
	public Object getHasValueMap() {
		return hasValueMap;
	}
	public void setHasValueMap(Object hasValueMap) {
		this.hasValueMap = hasValueMap;
	}
	public Object getHasKeyMap() {
		return hasKeyMap;
	}
	public void setHasKeyMap(Object hasKeyMap) {
		this.hasKeyMap = hasKeyMap;
	}

}
