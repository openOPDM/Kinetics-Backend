package org.kinetics.dao.share;

import java.util.List;

import org.kinetics.dao.project.Project;
import org.kinetics.dao.testsession.TestSession;
import org.kinetics.dao.user.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface SocialTestRepository extends
		CrudRepository<SocialTest, Integer> {

	@Query("SELECT st FROM SocialTest st WHERE st.testSession.user = ?1 AND st.testSession.project = ?2")
	List<SocialTest> findAllByOwnerAndProject(User owner, Project project);

	Long countByTestSession(TestSession testSession);

	SocialTest findOneByTestSession(TestSession testSession);

	SocialTest findOneByToken(String token);

	@Transactional
	@Modifying
	@Query("DELETE FROM SocialTest st WHERE st.testSession = ?1")
	void deleteByTestSession(TestSession testSession);

}
