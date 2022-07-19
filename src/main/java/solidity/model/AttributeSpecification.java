package solidity.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AttributeSpecification {
	
	public String hasName;
	public VisibilitySpecification hasVisibility;
	public NonConstantSpecification hasNonConstantAttribute;
	public ArraySpecification hasArrayType;
	public MappingSpecification hasMapType;
	public StructSpecification hasStructType;
	public Object hasEnumAttribute;
	public boolean immutableProperty;
	public boolean constantProperty;
	public boolean payableProperty;
	public String hasValue;
	
    @JsonProperty(value="hasVisibility")
	public VisibilitySpecification getVisibility() {
		return hasVisibility;
	}

	public NonConstantSpecification getHasNonConstantAttribute() {
		return hasNonConstantAttribute;
	}

	public void setHasNonConstantAttribute(NonConstantSpecification hasNonConstantAttribute) {
		this.hasNonConstantAttribute = hasNonConstantAttribute;
	}

	public ArraySpecification getHasArrayType() {
		return hasArrayType;
	}

	public void setHasArrayType(ArraySpecification hasArrayType) {
		this.hasArrayType = hasArrayType;
	}

	public void setVisibility(VisibilitySpecification hasVisibility) {
		this.hasVisibility = hasVisibility;
	}

	public String getHasName() {
		return hasName;
	}

	public void setHasName(String hasName) {
		this.hasName = hasName;
	}

    @JsonProperty(value="isInmutable")
    @JsonInclude(Include.NON_DEFAULT)
	public boolean isImmutableProperty() {
		return immutableProperty;
	}

	public void setImmutableProperty(boolean inmutableProperty) {
		this.immutableProperty = inmutableProperty;
	}

	public MappingSpecification getHasMapType() {
		return hasMapType;
	}

	public void setHasMapType(MappingSpecification hasMapType) {
		this.hasMapType = hasMapType;
	}

	public Object getHasEnumAttribute() {
		return hasEnumAttribute;
	}

	public void setHasEnumAttribute(Object hasEnumAttribute) {
		this.hasEnumAttribute = hasEnumAttribute;
	}

	public StructSpecification getHasStructType() {
		return hasStructType;
	}

	public void setHasStructType(StructSpecification hasStructType) {
		this.hasStructType = hasStructType;
	}
	
    @JsonProperty(value="isConstant")
    @JsonInclude(Include.NON_DEFAULT)
	public boolean isConstantProperty() {
		return constantProperty;
	}

	public void setConstantProperty(boolean constantProperty) {
		this.constantProperty = constantProperty;
	}

    @JsonProperty(value="hasValue")
	public String getHasValue() {
		return hasValue;
	}

	public void setHasValue(String hasValue) {
		this.hasValue = hasValue;
	}

    @JsonProperty(value="isPayable")
    @JsonInclude(Include.NON_DEFAULT)
	public boolean isPayableProperty() {
		return payableProperty;
	}

	public void setPayableProperty(boolean payableProperty) {
		this.payableProperty = payableProperty;
	}
	
	

}
