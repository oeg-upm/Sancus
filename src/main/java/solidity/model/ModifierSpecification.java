package solidity.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class ModifierSpecification {

    @JsonProperty("modifierName")
    public String hasModifierName;

    @JsonProperty("modifierCode")
    public String hasCode;

    @JsonProperty("hasModifierBehaviour")
    private ModifierBehaviour modifierBehaviour;

    @JsonProperty("hasModifierArguments")
    public ArrayList<ParameterSpecification> hasModifierArguments;

	public enum ModifierBehaviour{VIRTUAL, OVERRIDE}

	public String getHasModifierName() {
		return hasModifierName;
	}

	public void setHasModifierName(String hasModifierName) {
		this.hasModifierName = hasModifierName;
	}

	public String getHasCode() {
		return hasCode;
	}

	public void setHasCode(String hasCode) {
		this.hasCode = hasCode;
	}

	public ModifierBehaviour getModifierBehaviour() {
		return modifierBehaviour;
	}

	public void setModifierBehaviour(ModifierBehaviour modifierBehaviour) {
		this.modifierBehaviour = modifierBehaviour;
	}

	public ArrayList<ParameterSpecification> getHasModifierArguments() {
		return hasModifierArguments;
	}

	public void setHasModifierArguments(ArrayList<ParameterSpecification> hasModifierArguments) {
		this.hasModifierArguments = hasModifierArguments;
	}
	
}
