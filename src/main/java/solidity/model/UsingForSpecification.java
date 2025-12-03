package solidity.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UsingForSpecification {

    @JsonProperty("usingForName")
    public String library;

    @JsonProperty("hasType")
    public String type;
	
    @JsonProperty(value="hasUsingForName")
	public String getType() {
		return type;
	}
	
    @JsonProperty(value="usingLibrary")
	public String getLibrary() {
		return library;
	}

	public void setLibrary(String library) {
		this.library = library;
	}

	public void setType(String type) {
		this.type = type;
	}

}
