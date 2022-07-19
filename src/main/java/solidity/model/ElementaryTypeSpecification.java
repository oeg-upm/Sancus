package solidity.model;

public class ElementaryTypeSpecification {
	
	private ElementaryType elementaryType;
	
	public enum ElementaryType{ADDRESS, BOOL, STRING}
	
	public ElementaryType getElementaryType() {
		return elementaryType;
	}
	
	public void setElementaryType(ElementaryType elementaryType) {
		this.elementaryType = elementaryType;
	}
	
}
