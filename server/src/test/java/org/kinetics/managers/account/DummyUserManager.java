package org.kinetics.managers.account;

import static com.lohika.server.core.test.TestUtils.makeUniqueId;
import static org.kinetics.managers.shared.UTConsts.DUMMY_EMAIL;

import javax.annotation.PostConstruct;

import org.kinetics.dao.authorization.RoleRepository;
import org.kinetics.dao.authorization.RolesEnum;
import org.kinetics.dao.project.Project;
import org.kinetics.dao.project.ProjectRepository;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.session.SessionRepository;
import org.kinetics.dao.user.Confirmation;
import org.kinetics.dao.user.ConfirmationRepository;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.server.core.test.TestRequestExecuter;
import com.lohika.server.core.test.TestUtils;

@Component
public class DummyUserManager {

	@Autowired
	private TestRequestExecuter executor;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private RoleRepository roleRepo;
	@Autowired
	private ConfirmationRepository confirmRepo;
	@Autowired
	private SessionRepository sessionRepo;
	@Autowired
	private ProjectRepository projectRepo;

	private Project project;

	@PostConstruct
	void init() {
		project = projectRepo.save(new Project(makeUniqueId()));
	}

	public Project getProject() {
		return project;
	}

	public User createActivatedUser(String email) {
		return createActivatedUser(email, project);
	}

	public User createActivatedUser(String email, Project project) {
		TestAccountManager.createDummyAccount(email, executor, project.getId());

		User user = userRepo.findOneByEmail(email);
		Confirmation confirmation = confirmRepo.findByUser(user).iterator()
				.next();

		TestAccountManager.confirmDummyUser(confirmation, executor);
		return user;
	}

	public Session createActivatedUserAndLogin() {
		User user = createActivatedUser(DUMMY_EMAIL);
		return sessionRepo.findOne(TestAccountManager.loginDummyUser(
				DUMMY_EMAIL, user.getProjects().iterator().next().getId(),
				executor));
	}

	public Session createActivatedUserAndLogin(Project project) {
		User user = createActivatedUser(DUMMY_EMAIL, project);
		return sessionRepo.findOne(TestAccountManager.loginDummyUser(
				DUMMY_EMAIL, user.getProjects().iterator().next().getId(),
				executor));
	}

	public Session createActivatedSiteAdminAndLogin() {
		String email = TestUtils.makeUniqueId("site_admin");
		User user = createActivatedUser(email);

		user.changeRole(roleRepo.findByName(RolesEnum.SITE_ADMIN.name()));
		userRepo.save(user);

		return sessionRepo.findOne(TestAccountManager.loginDummyUser(email,
				null, executor));
	}

	public Session createActivatedAnalystAndLogin() {
		// change role
		String email = TestUtils.makeUniqueId("analyst");
		User user = createActivatedUser(email);
		user.addRole(roleRepo.findByName(RolesEnum.ANALYST.name()));
		userRepo.save(user);

		return sessionRepo.findOne(TestAccountManager.loginDummyUser(email,
				user.getProjects().iterator().next().getId(), executor));
	}

}
