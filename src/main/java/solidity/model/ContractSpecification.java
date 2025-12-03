package solidity.model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class ContractSpecification {

    @JsonProperty("hasImport")
    public ArrayList<String> hasImports;

    @JsonProperty("version")
    public ArrayList<String> hasVersion;

    @JsonProperty("isAbstract")
    public boolean isAbstract;

    @JsonProperty("inheritance")
    public ArrayList<String> hasInheritance;

    @JsonProperty("hasUsingFor")
    public ArrayList<UsingForSpecification> hasUsingFor;

    @JsonProperty("hasContractAttribute")
    public ArrayList<AttributeSpecification> hasAttributes;

    @JsonProperty("hasContractConstructor")
    public ConstructorSpecification hasConstructor;

    @JsonProperty("hasImplementationModifier")
    public ArrayList<ModifierSpecification> hasModifier;

    @JsonProperty("hasImplementationFunction")
    public ArrayList<FunctionSpecification> hasFunctions;

    @JsonProperty("hasImplementationEvent")
    public ArrayList<EventSpecification> hasEvents;
	
	public ArrayList<String> getHasImports() {
		return hasImports;
	}

	public void setHasImports(ArrayList<String> hasImports) {
		this.hasImports = hasImports;
	}

	public ArrayList<String> getHasInheritance() {
		return hasInheritance;
	}

	public void setHasInheritance(ArrayList<String> hasInheritance) {
		this.hasInheritance = hasInheritance;
	}

	public ArrayList<FunctionSpecification> getHasFunctions() {
		return hasFunctions;
	}

	public void setHasFunctions(ArrayList<FunctionSpecification> hasFunctions) {
		this.hasFunctions = hasFunctions;
	}

	public ArrayList<AttributeSpecification> getHasAttributes() {
		return hasAttributes;
	}

	public void setHasAttributes(ArrayList<AttributeSpecification> hasAttributes) {
		this.hasAttributes = hasAttributes;
	}
	
	public ArrayList<ModifierSpecification> getHasModifier() {
		return hasModifier;
	}

	public void setHasModifier(ArrayList<ModifierSpecification> hasModifier) {
		this.hasModifier = hasModifier;
	}

    @JsonInclude(Include.NON_NULL)
	public ConstructorSpecification getHasConstructor() {
		return hasConstructor;
	}

	public void setHasConstructor(ConstructorSpecification hasConstructor) {
		this.hasConstructor = hasConstructor;
	}

	public ArrayList<EventSpecification> getHasEvents() {
		return hasEvents;
	}

	public void setHasEvents(ArrayList<EventSpecification> hasEvents) {
		this.hasEvents = hasEvents;
	}

    @JsonProperty(value="isAbstract")
    @JsonInclude(Include.NON_DEFAULT)
	public boolean isAbstract() {
		return isAbstract;
	}

	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}

	public ArrayList<UsingForSpecification> getHasUsingFor() {
		return hasUsingFor;
	}

	public void setHasUsingFor(ArrayList<UsingForSpecification> hasUsingFor) {
		this.hasUsingFor = hasUsingFor;
	}

    @JsonProperty(value="hasVersion")
    @JsonInclude(Include.NON_DEFAULT)
	public ArrayList<String> getHasVersion() {
		return hasVersion;
	}

	public void setHasVersion(ArrayList<String> hasVersion) {
		this.hasVersion = hasVersion;
	}
	
}
