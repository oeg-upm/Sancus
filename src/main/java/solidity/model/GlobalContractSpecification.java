package solidity.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class GlobalContractSpecification {

    @JsonProperty("@id")
    public String id;

    @JsonProperty("description")
    public String description;

    @JsonProperty("version")
    public String version;

	public ArrayList<ImplementationSpecification> globalContract;

	public ArrayList<ImplementationSpecification> getGlobalContract() {
		return globalContract;
	}

	public void setGlobalContract(ArrayList<ImplementationSpecification> globalContract) {
		this.globalContract = globalContract;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
}
