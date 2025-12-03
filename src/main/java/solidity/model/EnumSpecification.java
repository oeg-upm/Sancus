package solidity.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class EnumSpecification {

    @JsonProperty("option")
    public ArrayList<Object> options;

	public ArrayList<Object> getOptions() {
		return options;
	}

	public void setOptions(ArrayList<Object> options) {
		this.options = options;
	}

}
