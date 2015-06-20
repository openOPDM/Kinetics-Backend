package org.kinetics.dao.project;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.eclipse.persistence.annotations.CascadeOnDelete;
import org.kinetics.dao.user.User;

@Entity
@CascadeOnDelete
public class Project {

	@Id
	@GeneratedValue
	private Integer id;

	@Column(unique = true)
	private String name;

	@Enumerated
	private ProjectStatus status;

	@JsonIgnore
	@ManyToMany(mappedBy = "projects")
	private Collection<User> users;

	public Project() {
	}

	public Project(String name) {
		this.name = name;
		this.status = ProjectStatus.ACTIVE;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ProjectStatus getStatus() {
		return status;
	}

	public void setStatus(ProjectStatus status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "Project [id=" + id + ", name=" + name + ", status=" + status
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Project other = (Project) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

}
