package solidity.model;

import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class StructSpecification {
	
	public ArrayList<AttributeSpecification> hasAttributesTest;
	public String hasName;

    @JsonProperty(value="hasNonConstantStructAttribute")
    @JsonInclude(Include.NON_DEFAULT)
	public ArrayList<AttributeSpecification> getHasAttributesTest() {
		return hasAttributesTest;
	}

	public void setHasAttributesTest(ArrayList<AttributeSpecification> hasAttributesTest) {
		this.hasAttributesTest = hasAttributesTest;
	}

    @JsonProperty(value="hasStructName")
    @JsonInclude(Include.NON_DEFAULT)
	public String getHasName() {
		return hasName;
	}

	public void setHasName(String hasName) {
		this.hasName = hasName;
	}
	
	
	
}
