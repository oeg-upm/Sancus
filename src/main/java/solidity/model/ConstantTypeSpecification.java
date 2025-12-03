package solidity.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConstantTypeSpecification {
	
	//Structs or Enums
    @JsonProperty("hasNonConstantStructAttribute")
    public Object hasNonConstantStructAttribute;

	public Object getHasNonConstantStructAttribute() {
		return hasNonConstantStructAttribute;
	}

	public void setHasNonConstantStructAttribute(Object hasNonConstantStructAttribute) {
		this.hasNonConstantStructAttribute = hasNonConstantStructAttribute;
	}
	
	

}
