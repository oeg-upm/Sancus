package sancus.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Entity
@Table(name="contract")
public class ValidSmartContract implements Serializable, Comparable<ValidSmartContract>{
	
	@Transient
	private static final long serialVersionUID = 1L;
	
	@Id
	@NotEmpty
	private String address;
	@NotNull
	private boolean valid;
	
	public ValidSmartContract() {

	}
	
	public ValidSmartContract(String address, boolean valid) {
		this.address = address;
		this.valid = valid;
	}

	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	@Override
	public int compareTo(ValidSmartContract o) {
		return this.getAddress().compareTo(o.getAddress());
	}
	
}
