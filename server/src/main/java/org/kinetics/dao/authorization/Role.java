package org.kinetics.dao.authorization;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import org.kinetics.dao.user.User;

@Entity
public class Role {

	private static final int DEFAULT_TEXT_LENGTH = 50;

	@Id
	@GeneratedValue
	private Integer id;

	@Column(unique = true, length = DEFAULT_TEXT_LENGTH)
	private String name;

	@ManyToMany(mappedBy = "roles")
	private Collection<User> users;

	public Role(String name) {
		this.name = name;
	}

	public Role() {
	}

	public String getName() {
		return name;
	}

	public Integer getId() {
		return id;
	}

	@Override
	public String toString() {
		return "Role{" + "id=" + id + ", name='" + name + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Role role = (Role) o;

		if (!id.equals(role.id))
			return false;
		if (!name.equals(role.name))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = id.hashCode();
		result = 31 * result + name.hashCode();
		return result;
	}
}
