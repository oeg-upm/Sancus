package solidity.model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class ImplementationSpecification {

    @JsonProperty("contractName")
    public String hasContractName;

    @JsonUnwrapped
    public ContractSpecification contractSpecification;

	public LibrarySpecification librarySpecification;
	public InterfaceSpecification interfaceSpecification;
    public StructSpecification structSpecification;

    @JsonProperty("@id")
    public String id;

	public String getHasContractName() {
		return hasContractName;
	}

	public void setHasContractName(String hasContractName) {
		this.hasContractName = hasContractName;
	}
	
	public LibrarySpecification getLibrarySpecification() {
		return librarySpecification;
	}

	public void setLibrarySpecification(LibrarySpecification librarySpecification) {
		this.librarySpecification = librarySpecification;
	}

	public InterfaceSpecification getInterfaceSpecification() {
		return interfaceSpecification;
	}

	public void setInterfaceSpecification(InterfaceSpecification interfaceSpecification) {
		this.interfaceSpecification = interfaceSpecification;
	}

//	public ArrayList<String> getHasImports() {
//		return hasImports;
//	}
//
//	public void setHasImports(ArrayList<String> hasImports) {
//		this.hasImports = hasImports;
//	}

//	public String getVersion() {
//		return version;
//	}
//
//	public void setVersion(String version) {
//		this.version = version;
//	}

	public ContractSpecification getContractSpecification() {
		return contractSpecification;
	}

	public void setContractSpecification(ContractSpecification contractSpecification) {
		this.contractSpecification = contractSpecification;
	}

    @JsonProperty(value="hasStructDefinition")
    @JsonInclude(Include.NON_DEFAULT)
	public StructSpecification getStructSpecification() {
		return structSpecification;
	}

	public void setStructSpecification(StructSpecification structSpecification) {
		this.structSpecification = structSpecification;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
}
