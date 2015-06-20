package org.kinetics.dao.user;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.joda.time.LocalDate;
import org.kinetics.dao.audit.AuditData;
import org.kinetics.dao.authorization.Role;
import org.kinetics.dao.project.Project;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface UserRepository extends CrudRepository<User, Integer> {

	User findOneByEmail(String email);

	@Query("SELECT u.email FROM User u WHERE u.email IN ?1 AND ?2 MEMBER OF u.roles")
	List<String> findEmailsByEmailInAndRole(Collection<String> emails, Role role);

	@Query("SELECT u FROM User u WHERE u.email IN ?1")
	List<User> findAllByEmailIn(Collection<String> emails);

	List<User> findAllByProjects(Project customer);

	List<User> findAllByIdInAndProjects(Collection<Integer> ids, Project Project);

	@Query(name = "nativeFind")
	List<AuditData> findTotalCreatedBetween(Date from, Date to);

	// TODO: why it works here but not for native call?
	List<User> findAllByCreationDateBetween(LocalDate from, LocalDate to);

	// TODO: workaround till
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=349477 is fixed!
	@Query("SELECT u FROM User u WHERE u.id IN ?1")
	List<User> findAllByIdIn(Collection<Integer> ids);

	@Query("SELECT u FROM User u WHERE u.id <> ?1 AND u.status <> ?2")
	List<User> findAllExcludeIdAndStatus(Integer id, UserStatus status);

	@Query("SELECT u FROM User u WHERE u.id <> ?1 AND u.status <> ?2 AND ?3 MEMBER OF u.projects")
	List<User> findAllExcludeIdAndStatusAndProject(Integer id,
			UserStatus status, Project project);

	@Query("SELECT COUNT(u) FROM User u WHERE ?1 MEMBER OF u.roles AND u.status = ?2 AND u.id <> ?3")
	long countAllByRoleAndStatusAndExcludeId(Role role, UserStatus status,
			Integer id);

	@Query("SELECT u FROM User u WHERE ?1 MEMBER OF u.roles AND ?2 MEMBER OF u.projects AND u.status <> ?3 AND u.id NOT IN ?4")
	List<User> findAllByRoleAndProjectAndExcludeStatusAndIds(Role role,
			Project project, UserStatus status, Collection<Integer> ids);

	@Query("SELECT u FROM User u WHERE "
			+ "LOWER(u.email) LIKE CONCAT('%', LOWER(?1), '%') AND ?2 MEMBER OF u.projects AND u.status <> ?3")
	List<User> findAllByEmailAndProjectAndExcludeStatus(String token,
			Project project, UserStatus status);

	@Query("SELECT u FROM User u WHERE "
			+ "LOWER(u.email) LIKE CONCAT('%', LOWER(?1), '%') AND u.status <> ?2")
	List<User> findAllByEmailAndExcludeStatus(String token, UserStatus status);

	@Query("SELECT u FROM User u WHERE "
			+ "LOWER(u.email) LIKE CONCAT('%', LOWER(?1), '%')"
			+ "AND ?2 MEMBER OF u.roles AND ?3 MEMBER OF u.projects AND  u.status <> ?4 AND u.id NOT IN ?5")
	List<User> findAllByEmailAndRoleAndProjectAndExcludeStatusAndIds(
			String token, Role role, Project project, UserStatus status,
			Collection<Integer> ids);

	@Query("SELECT u FROM User u WHERE "
			+ "(LOWER(u.firstName) LIKE CONCAT('%', LOWER(?1), '%') OR "
			+ "LOWER(u.secondName) LIKE CONCAT('%', LOWER(?1), '%')) AND ?2 MEMBER OF u.projects AND u.status <> ?3")
	List<User> findAllByFirstNameOrSecondNameAndCustomerAndExcludeStatus(
			String token, Project customer, UserStatus status);

	@Query("SELECT u FROM User u WHERE "
			+ "(LOWER(u.firstName) LIKE CONCAT('%', LOWER(?1), '%') OR "
			+ "LOWER(u.secondName) LIKE CONCAT('%', LOWER(?1), '%')) AND u.status <> ?2")
	List<User> findAllByFirstNameOrSecondNameAndExcludeStatus(String token,
			UserStatus status);

	@Query("SELECT u FROM User u WHERE "
			+ "(LOWER(u.firstName) LIKE CONCAT('%', LOWER(?1), '%') OR "
			+ "LOWER(u.secondName) LIKE CONCAT('%', LOWER(?1), '%')) "
			+ "AND ?2 MEMBER OF u.roles AND ?3 MEMBER OF u.projects AND u.status <> ?4 AND u.id NOT IN ?5")
	List<User> findAllByFirstNameOrSecondNameAndRoleAndCustomerAndExcludeStatusAndIds(
			String token, Role role, Project customer, UserStatus status,
			Collection<Integer> ids);

	@Query("SELECT u FROM User u WHERE LOWER(u.UID) LIKE CONCAT('%', LOWER(?1), '%') "
			+ "AND ?2 MEMBER OF u.projects AND u.status <> ?3")
	List<User> findAllByUIDAndCustomerAndExcludeStatus(String token,
			Project customer, UserStatus status);

	@Query("SELECT u FROM User u WHERE LOWER(u.UID) LIKE CONCAT('%', LOWER(?1), '%') "
			+ " AND u.status <> ?2")
	List<User> findAllByUIDAndExcludeStatus(String token, UserStatus status);

	@Query("SELECT u FROM User u WHERE LOWER(u.UID) LIKE CONCAT('%', LOWER(?1), '%') "
			+ "AND ?2 MEMBER OF u.roles AND ?3 MEMBER OF u.projects AND u.status <> ?4 AND u.id NOT IN ?5")
	List<User> findAllByUIDAndRoleAndCustomerAndExcludeStatusAndIds(
			String token, Role role, Project project, UserStatus status,
			Collection<Integer> ids);

	@Query("SELECT u FROM User u WHERE "
			+ "(LOWER(u.UID) LIKE CONCAT('%', LOWER(?1), '%') OR "
			+ "LOWER(u.email) like CONCAT('%', LOWER(?1), '%') OR "
			+ "LOWER(u.firstName) LIKE CONCAT('%', LOWER(?1), '%') OR"
			+ " LOWER(u.secondName) LIKE CONCAT('%', LOWER(?1), '%'))  AND ?2 MEMBER OF u.projects  AND u.status <> ?3")
	List<User> findAllByEmailOrFirstNameOrSecondNameOrUIDAndCustomerAndExcludeStatus(
			String token, Project project, UserStatus status);

	@Query("SELECT u FROM User u WHERE "
			+ "(LOWER(u.UID) LIKE CONCAT('%', LOWER(?1), '%') OR "
			+ "LOWER(u.email) like CONCAT('%', LOWER(?1), '%') OR "
			+ "LOWER(u.firstName) LIKE CONCAT('%', LOWER(?1), '%') OR"
			+ " LOWER(u.secondName) LIKE CONCAT('%', LOWER(?1), '%'))  AND u.status <> ?2")
	List<User> findAllByEmailOrFirstNameOrSecondNameOrUIDAndExcludeStatus(
			String token, UserStatus status);

	@Query("SELECT u FROM User u WHERE "
			+ "(LOWER(u.UID) LIKE CONCAT('%', LOWER(?1), '%') OR "
			+ "LOWER(u.email) like CONCAT('%', LOWER(?1), '%') OR "
			+ "LOWER(u.firstName) LIKE CONCAT('%', LOWER(?1), '%') OR "
			+ "LOWER(u.secondName) LIKE CONCAT('%', LOWER(?1), '%')) "
			+ "AND ?2 MEMBER OF u.roles AND ?3 MEMBER OF u.projects AND u.status <> ?4 AND u.id NOT IN ?5")
	List<User> findAllByRoleAndEmailOrFirstNameOrSecondNameOrUIDAndCustomerAndExcludeStatusAndIds(
			String token, Role role, Project project, UserStatus status,
			Collection<Integer> ids);

}
