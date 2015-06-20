package org.kinetics.dao.share;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.eclipse.persistence.annotations.CascadeOnDelete;
import org.kinetics.dao.project.Project;
import org.kinetics.dao.user.User;

@Entity
@CascadeOnDelete
public class SharedTest {

	@Id
	@GeneratedValue
	private Integer id;

	@OneToOne
	@CascadeOnDelete
	@JoinColumn(name = "USER_ID")
	private User owner;

	@OneToOne
	@CascadeOnDelete
	private Project project;

	private String email;

	public SharedTest() {
	}

	public SharedTest(User owner, Project project, String email) {
		this.owner = owner;
		this.project = project;
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Integer getId() {
		return id;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	@Override
	public String toString() {
		return "SharedTest [id=" + id + ", project=" + project + ", email="
				+ email + "]";
	}

}
