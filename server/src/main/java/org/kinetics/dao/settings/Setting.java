package org.kinetics.dao.settings;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Setting {

	@Id
	private String name;

	private String value;

	public Setting() {
	}

	public Setting(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "Setting [name=" + name + ", value=" + value + "]";
	}

}
