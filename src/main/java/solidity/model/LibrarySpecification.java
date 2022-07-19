package solidity.model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class LibrarySpecification {
	
	public ArrayList<String> hasImports;
	public ArrayList<String> hasVersion;
	public ArrayList<UsingForSpecification> hasUsingFor;
	public ArrayList<AttributeSpecification> hasAttributes;
	public ArrayList<ModifierSpecification> hasModifier;
	public ArrayList<FunctionSpecification> hasFunctions;
	public ArrayList<EventSpecification> hasEvents;
	public boolean isAbstract;
	
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

}
