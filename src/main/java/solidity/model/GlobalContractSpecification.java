package solidity.model;

import java.util.ArrayList;

public class GlobalContractSpecification {
	
	public String id;
	public String description;
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
