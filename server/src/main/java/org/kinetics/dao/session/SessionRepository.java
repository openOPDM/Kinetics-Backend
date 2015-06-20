package org.kinetics.dao.session;

import java.util.Collection;

import org.kinetics.dao.project.Project;
import org.kinetics.dao.user.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface SessionRepository extends CrudRepository<Session, String> {

	Collection<Session> findAllByUser(User user);

	@Transactional
	@Modifying
	@Query("DELETE FROM Session s where s.user = ?1")
	void deleteByUser(User user);

	@Transactional
	@Modifying
	@Query("DELETE FROM Session s where s.project = ?1")
	void deleteByProject(Project project);

	@Transactional
	@Modifying
	@Query("DELETE FROM Session s WHERE s.user = ?1 AND s.project IN ?2")
	void deleteByUserAndProjectsIn(User user, Collection<Project> projects);

	@Transactional
	@Modifying
	@Query("DELETE FROM Session s WHERE s.user IN ?1 AND s.project = ?2")
	void deleteByUsersInAndProject(Collection<User> users, Project project);

}
