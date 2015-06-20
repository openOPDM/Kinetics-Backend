package org.kinetics.dao.service;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.eclipse.persistence.annotations.CascadeOnDelete;
import org.kinetics.dao.project.Project;
import org.kinetics.dao.user.User;

@Entity
@CascadeOnDelete
public class AnalystPatient {

	@Id
	@GeneratedValue
	private Integer id;

	@OneToOne
	@CascadeOnDelete
	private User analyst;

	@OneToOne
	@CascadeOnDelete
	private User patient;

	@OneToOne
	@CascadeOnDelete
	private Project project;

	public AnalystPatient() {
	}

	public AnalystPatient(User analyst, User patient, Project project) {
		this.analyst = analyst;
		this.patient = patient;
		this.project = project;
	}

	public Integer getId() {
		return id;
	}

	public User getAnalyst() {
		return analyst;
	}

	public User getPatient() {
		return patient;
	}

	public Project getProject() {
		return project;
	}

	@Override
	public String toString() {
		return "AnalystPatient{" + "id=" + id + ", analyst=" + analyst
				+ ", patient=" + patient + '}';
	}
}
