package solidity.model;

import java.util.ArrayList;

public class ModifierSpecification {
	
	public String hasModifierName;
	public String hasCode;
	private ModifierBehaviour modifierBehaviour;
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
