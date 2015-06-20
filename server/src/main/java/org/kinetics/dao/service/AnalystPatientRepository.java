package org.kinetics.dao.service;

import java.util.Collection;
import java.util.List;

import org.kinetics.dao.project.Project;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface AnalystPatientRepository extends
		CrudRepository<AnalystPatient, Integer> {

	@Query("SELECT ap.patient FROM AnalystPatient ap WHERE ap.analyst=?1 AND ap.project = ?2 AND "
			+ "ap.patient.status IN ?3")
	List<User> findAllByStatusAndAnalystAndProject(User analyst,
			Project project, Collection<UserStatus> status);

	@Query("SELECT ap.patient.id FROM AnalystPatient ap WHERE ap.analyst=?1 AND ap.project = ?2")
	List<Integer> findPatientIdsByAnalystAndProject(User analyst,
			Project project);

	AnalystPatient findByAnalystAndPatientAndProject(User analyst,
			User patient, Project project);

	List<AnalystPatient> findByAnalystAndPatientInAndProject(User analyst,
			List<User> patient, Project project);

	@Transactional
	@Modifying
	@Query("DELETE FROM AnalystPatient ap WHERE ap.patient = ?1 AND ap.project IN ?2")
	void deleteAllByPatientAndProjects(User patient,
			Collection<Project> projects);

	@Transactional
	@Modifying
	@Query("DELETE FROM AnalystPatient ap WHERE ap.patient IN ?1 AND ap.project = ?2")
	void deleteAllByPatientsAndProject(Collection<User> patients,
			Project project);

}
