package solidity.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ElementaryTypeSpecification {

    @JsonProperty("elementaryType")
    private ElementaryType elementaryType;

	public enum ElementaryType{ADDRESS, BOOL, STRING}
	
	public ElementaryType getElementaryType() {
		return elementaryType;
	}
	
	public void setElementaryType(ElementaryType elementaryType) {
		this.elementaryType = elementaryType;
	}
	
}
