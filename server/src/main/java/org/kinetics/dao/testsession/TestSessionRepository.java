package org.kinetics.dao.testsession;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.kinetics.dao.audit.AuditData;
import org.kinetics.dao.project.Project;
import org.kinetics.dao.project.ProjectStatus;
import org.kinetics.dao.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface TestSessionRepository extends
		PagingAndSortingRepository<TestSession, Integer> {

	List<TestSession> findAllByUserAndProject(User user, Project project);

	@Query("SELECT ts FROM TestSession ts WHERE ts.user.id = ?1 AND ts.project.id = ?2")
	List<TestSession> findAllByUserIdAndProjectId(Integer user, Integer project);

	TestSession findOneByIdAndProject(Integer id, Project project);

	TestSession findOneByIdAndUser(Integer id, User owner);
	
	@Query("SELECT ts FROM TestSession ts WHERE ts.id = ?1 AND ts.user IN ?2")
	TestSession findOneByIdAndUserIn(Integer id, Collection<User> users);

	@Query(name = "findTestByDate")
	List<AuditData> findAllByCreatedBetween(Date from, Date to);

	@Query("SELECT ts FROM TestSession ts WHERE ts.id IN ?1 AND ts.project = ?2")
	// TODO: workaround till
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=349477 is fixed!
	List<TestSession> findAllByIdInAndProject(Collection<Integer> ids,
			Project project);

	List<TestSession> findAllByProjectAndUserAndCreationDateBetween(Project project, User user,
			DateTime start, DateTime end);

	@Query("SELECT COUNT(ts) FROM TestSession ts WHERE ts.creationDate > ?1 AND ts.creationDate < ?2 AND ts.type LIKE CONCAT(?3, '%') AND ts.project.status = ?4")
	Long findAllByCreationDateBetweenAndTypeStartingWithCountAndStatus(
			DateTime start, DateTime end, String type, ProjectStatus status);

	@Query("SELECT ts FROM TestSession ts WHERE ts.creationDate > ?1 AND ts.creationDate < ?2 AND ts.type LIKE CONCAT(?3, '%') AND ts.project.status = ?4")
	List<TestSession> findAllByCreationDateBetweenAndTypeStartingWithAndStatus(
			DateTime start, DateTime end, String type, ProjectStatus status,
			Pageable pageable);

	@Transactional
	@Modifying
	@Query("DELETE FROM TestSession ts WHERE ts.id IN ?1 AND ts.project = ?2")
	void deleteAllByIdsAndProject(Collection<Integer> ids, Project project);

	@Transactional
	@Modifying
	@Query("DELETE FROM TestSession ts WHERE ts.user = ?1 AND ts.project IN ?2")
	void deleteAllByUserAndProjectIn(User user, Collection<Project> project);

	@Transactional
	@Modifying
	@Query("DELETE FROM TestSession ts WHERE ts.user IN ?1 AND ts.project = ?2")
	void deleteAllByUsersInAndProject(Collection<User> users, Project project);

}
