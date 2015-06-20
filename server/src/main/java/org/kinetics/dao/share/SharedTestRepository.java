package org.kinetics.dao.share;

import java.util.Collection;
import java.util.List;

import org.kinetics.dao.project.Project;
import org.kinetics.dao.user.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface SharedTestRepository extends
		CrudRepository<SharedTest, Integer> {

	@Query("SELECT st.email FROM SharedTest st WHERE st.owner = ?1 AND st.project = ?2")
	List<String> findEmailsByOwnerAndProject(User owner, Project project);

	@Query("SELECT st FROM SharedTest st WHERE st.email = ?1 AND st.owner.id = ?2 AND st.project.id = ?3")
	SharedTest findOneByEmailAndOwnerAndProject(String email, Integer owner,
			Integer projectId);

	@Query("SELECT st FROM SharedTest st WHERE st.email = ?1 AND "
			+ "st.project.status = org.kinetics.dao.project.ProjectStatus.ACTIVE")
	List<SharedTest> findAllByEmailAndActiveProjects(String email);
	
	@Query("SELECT st.owner FROM SharedTest st WHERE st.email = ?1 AND "
			+ "st.project.status = org.kinetics.dao.project.ProjectStatus.ACTIVE")
	List<User> findAllOwnersByEmailAndActiveProjects(String email);

	@Transactional
	@Modifying
	@Query("DELETE FROM SharedTest st WHERE st.owner = ?1 AND st.project IN ?2")
	void deleteByUserAndProjectsIn(User user, Collection<Project> projects);
	
	@Transactional
	@Modifying
	@Query("DELETE FROM SharedTest st WHERE st.owner IN ?1 AND st.project = ?2")
	void deleteByUsersInAndProject(Collection<User> users, Project project);

	@Transactional
	@Modifying
	@Query("DELETE FROM SharedTest st WHERE st.email = ?1 AND st.owner.id = ?2 AND st.project.id = ?3")
	void deleteByEmailAndOwnerAndProject(String email, Integer userId,
			Integer projectId);

	@Transactional
	@Modifying
	@Query("DELETE FROM SharedTest st WHERE st.email IN ?1 AND st.owner = ?2 AND st.project = ?3")
	void deleteByEmailInAndOwnerAndProject(Collection<String> email,
			User owner, Project project);

}
