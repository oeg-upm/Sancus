package solidity.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ArrayDimensionSpecification {

    @JsonProperty("index")
    public short index;
    @JsonProperty("length")
    public short lenght;
	
	public short getIndex() {
		return index;
	}
	public void setIndex(short index) {
		this.index = index;
	}
	public short getLenght() {
		return lenght;
	}
	public void setLenght(short lenght) {
		this.lenght = lenght;
	}
	
}
