package solidity.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NonConstantSpecification {

    @JsonProperty("nonConstantAttribute")
    public Object nonConstantAttribute;

	public Object getNonConstantAttribute() {
		return nonConstantAttribute;
	}

	public void setNonConstantAttribute(Object nonConstantAttribute) {
		this.nonConstantAttribute = nonConstantAttribute;
	}

}
