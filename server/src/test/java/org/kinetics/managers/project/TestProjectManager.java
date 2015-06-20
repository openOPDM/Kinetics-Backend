package org.kinetics.managers.project;

import static com.lohika.server.core.Protocol.Arguments.EMAIL;
import static com.lohika.server.core.Protocol.Arguments.SESSION_TOKEN;
import static com.lohika.server.core.test.TestUtils.assertErrorResponse;
import static com.lohika.server.core.test.TestUtils.assertOkMessageResponse;
import static com.lohika.server.core.test.TestUtils.extractSingleGenericResponseData;
import static com.lohika.server.core.test.TestUtils.makeUniqueId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.kinetics.rest.Protocol.Arguments.ID;
import static org.kinetics.rest.Protocol.Arguments.IDS;
import static org.kinetics.rest.Protocol.Arguments.PROJECT;
import static org.kinetics.rest.Protocol.Arguments.PROJECT_NAME;
import static org.kinetics.rest.Protocol.Errors.PROJECT_IS_ACTIVE;
import static org.kinetics.rest.Protocol.Managers.ACCOUNT_MANANGER;
import static org.kinetics.rest.Protocol.Managers.PROJECT_MANANGER;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.kinetics.dao.project.Project;
import org.kinetics.dao.project.ProjectRepository;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.session.SessionRepository;
import org.kinetics.dao.testsession.TestSession;
import org.kinetics.dao.testsession.TestSessionRepository;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
import org.kinetics.managers.account.DummyUserManager;
import org.kinetics.managers.account.Login;
import org.kinetics.managers.shared.UTConsts;
import org.kinetics.rest.Protocol;
import org.kinetics.rest.Protocol.Arguments;
import org.kinetics.test.TransactionalRequestTest;
import org.springframework.beans.factory.annotation.Autowired;

import com.lohika.protocol.core.processor.RequestBuilder;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.Response;
import com.lohika.server.core.test.TestRequestExecuter;
import com.lohika.server.core.test.TestUtils;

public class TestProjectManager extends TransactionalRequestTest {

	@Autowired
	private ProjectRepository projectRepo;

	@Autowired
	private SessionRepository sessionRepo;

	@Autowired
	private DummyUserManager userManager;

	// private Project dummyProject;

	@Autowired
	private TestSessionRepository testSessionRepo;

	@Autowired
	private UserRepository userRepo;

	@Test
	public void testCreateProject() {
		Session siteAdminSession = userManager
				.createActivatedSiteAdminAndLogin();

		long count = projectRepo.count();

		Integer id = extractSingleGenericResponseData(
				createDummyProject(executor, siteAdminSession), Integer.class);

		assertEquals(count + 1, projectRepo.count());
		assertTrue(projectRepo.exists(id));
	}

	@Test
	public void testDeleteProject() {
		Session siteAdminSession = userManager
				.createActivatedSiteAdminAndLogin();

		long count = projectRepo.count();
		Integer id = extractSingleGenericResponseData(
				createDummyProject(executor, siteAdminSession), Integer.class);

		RequestBuilder builder = new RequestBuilder(PROJECT_MANANGER,
				DeleteProject.METHOD).addArg(SESSION_TOKEN,
				siteAdminSession.getSessionToken()).addArg(ID, id);

		assertOkMessageResponse(executor.execute(builder.buildFunction()));

		assertEquals(count, projectRepo.count());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetProjectInfoList() {
		RequestFunction function = new RequestBuilder(PROJECT_MANANGER,
				GetProjectInfoList.METHOD).buildFunction();

		Response response = executor.execute(function);

		List<Project> customersResponse = TestUtils
				.extractSingleGenericResponseData(response, List.class);

		List<Project> customerRepoAll = (List<Project>) projectRepo.findAll();

		assertEquals(customerRepoAll.size(), customersResponse.size());
	}

	@Test
	public void testModifyProjectStatus() {
		Session siteAdminSession = userManager
				.createActivatedSiteAdminAndLogin();
		final Integer id = extractSingleGenericResponseData(
				createDummyProject(executor, siteAdminSession), Integer.class);
		Project project = projectRepo.findOne(id);

		RequestBuilder builder = new RequestBuilder(PROJECT_MANANGER,
				ModifyProjectStatus.METHOD)
				.addArg(SESSION_TOKEN, siteAdminSession.getSessionToken())
				.addArg(ID, project.getId())
				.addArg(Protocol.Arguments.DISABLE, false);

		assertErrorResponse(executor.execute(builder.buildFunction()),
				PROJECT_IS_ACTIVE);

		// login to another project and tokens invalidation
		User patient = userManager.createActivatedUser(UTConsts.DUMMY_EMAIL);

		sessionRepo.save(new Session(patient, patient.getProjects().iterator()
				.next()));

		patient.addProject(project);
		userRepo.save(patient);

		assertEquals(1, sessionRepo.findAllByUser(patient).size());

		builder.setArg(Protocol.Arguments.DISABLE, true);
		assertOkMessageResponse(executor.execute(builder.buildFunction()));

		assertEquals(1, sessionRepo.findAllByUser(patient).size());

		RequestFunction function = new RequestBuilder(ACCOUNT_MANANGER,
				Login.METHOD).addArg(EMAIL, patient.getEmail())
				.addArg(Protocol.Arguments.PASSWORD, UTConsts.DUMMY_PASS)
				.addArg(PROJECT, project.getId()).buildFunction();

		assertErrorResponse(executor.execute(function),
				Protocol.Errors.PROJECT_DISABLED);

		builder.setArg(Protocol.Arguments.DISABLE, false);
		assertOkMessageResponse(executor.execute(builder.buildFunction()));
		String token = TestUtils.extractSingleGenericResponseData(
				executor.execute(function), String.class);
		Assert.assertNotNull(token);
	}

	@Test
	public void testModifyProjectInfo() {
		Session siteAdminSession = userManager
				.createActivatedSiteAdminAndLogin();
		final Integer id = extractSingleGenericResponseData(
				createDummyProject(executor, siteAdminSession), Integer.class);
		Project project = projectRepo.findOne(id);
		String customerName = "newDummyName";

		RequestBuilder builder = new RequestBuilder(PROJECT_MANANGER,
				ModifyProjectInfo.METHOD)
				.addArg(SESSION_TOKEN, siteAdminSession.getSessionToken())
				.addArg(ID, project.getId()).addArg(PROJECT_NAME, customerName);

		assertOkMessageResponse(executor.execute(builder.buildFunction()));

		assertEquals(customerName, projectRepo.findOne(project.getId())
				.getName());
	}

	@Test
	public void testChangeProjects() {
		Session session = userManager.createActivatedUserAndLogin();

		List<Project> projects = (List<Project>) projectRepo.save(Arrays
				.asList(new Project(makeUniqueId()),
						new Project(makeUniqueId())));

		RequestBuilder builder = new RequestBuilder(PROJECT_MANANGER,
				ChangeProjects.METHOD).addArg(SESSION_TOKEN,
				session.getSessionToken()).addArg(
				IDS,
				Arrays.asList(projects.get(0).getId(), projects.get(1).getId(),
						session.getProject().getId()));

		assertOkMessageResponse(executor.execute(builder.buildFunction()));

		assertEquals(3, session.getUser().getProjects().size());

		// generate some data
		testSessionRepo.save(new TestSession(0, null, makeUniqueId(), session
				.getUser(), session.getProject()));
		testSessionRepo.save(new TestSession(0, null, makeUniqueId(), session
				.getUser(), projects.get(0)));

		builder.setArg(IDS, Arrays.asList(session.getProject().getId()));
		assertOkMessageResponse(executor.execute(builder.buildFunction()));

		assertEquals(1, session.getUser().getProjects().size());
		assertEquals(1, testSessionRepo.count());

		// TODO add check for data cleanup!
	}

	@Test
	public void testChangeUserProjects() {
		Session adminSession = userManager.createActivatedSiteAdminAndLogin();

		User user = userManager.createActivatedUser(UTConsts.DUMMY_EMAIL);

		Project project = projectRepo.save(new Project(makeUniqueId()));

		RequestFunction function = new RequestBuilder(PROJECT_MANANGER,
				ChangeUserProjects.METHOD)
				.addArg(SESSION_TOKEN, adminSession.getSessionToken())
				.addArg(Arguments.USER, user.getId())
				.addArg(IDS, Arrays.asList(project.getId())).buildFunction();

		assertOkMessageResponse(executor.execute(function));

		assertEquals(1, user.getProjects().size());
		assertTrue(user.getProjects().contains(project));
		assertFalse(user.getProjects().contains(adminSession.getProject()));
	}

	@Test
	public void testChangeProjectUsers() {
		Session adminSession = userManager.createActivatedSiteAdminAndLogin();

		User user = userManager.createActivatedUser(makeUniqueId());
		User user2 = userManager.createActivatedUser(makeUniqueId());

		Project project = projectRepo.save(new Project(makeUniqueId()));

		RequestBuilder builder = new RequestBuilder(PROJECT_MANANGER,
				ChangeProjectUsers.METHOD)
				.addArg(SESSION_TOKEN, adminSession.getSessionToken())
				.addArg(PROJECT, project.getId())
				.addArg(IDS, Arrays.asList(user.getId(), user2.getId()));

		assertOkMessageResponse(executor.execute(builder.buildFunction()));

		assertTrue(user.getProjects().contains(project));
		assertTrue(user2.getProjects().contains(project));
		assertEquals(2, userRepo.findAllByProjects(project).size());

		builder.setArg(IDS, Arrays.asList(user.getId()));
		assertOkMessageResponse(executor.execute(builder.buildFunction()));

		assertTrue(user.getProjects().contains(project));
		assertFalse(user2.getProjects().contains(project));

		assertEquals(1, userRepo.findAllByProjects(project).size());

		builder.setArg(IDS, Collections.emptyList());
		assertOkMessageResponse(executor.execute(builder.buildFunction()));

		assertEquals(0, userRepo.findAllByProjects(project).size());

		// TODO: add clean up tests
	}

	@Test
	public void testSwitchProject() {
		Session session = userManager.createActivatedUserAndLogin();

		Project project = projectRepo.save(new Project(makeUniqueId()));

		RequestFunction function = new RequestBuilder(PROJECT_MANANGER,
				SwitchProject.METHOD)
				.addArg(SESSION_TOKEN, session.getSessionToken())
				.addArg(ID, project.getId()).buildFunction();

		assertOkMessageResponse(executor.execute(function));
		assertEquals(session.getProject(), project);
	}

	@Test
	public void testGetUserInfoById() {
		Session adminSession = userManager.createActivatedSiteAdminAndLogin();
		Session session = userManager.createActivatedUserAndLogin();

		RequestFunction function = new RequestBuilder(PROJECT_MANANGER,
				GetProjectInfoById.METHOD)
				.addArg(SESSION_TOKEN, adminSession.getSessionToken())
				.addArg(ID, session.getProject().getId()).buildFunction();

		Project project = extractSingleGenericResponseData(
				executor.execute(function), Project.class);

		assertEquals(session.getProject(), project);
	}

	static Response createDummyProject(TestRequestExecuter executor,
			Session session) {
		RequestFunction function = new RequestBuilder(PROJECT_MANANGER,
				CreateProject.METHOD)
				.addArg(SESSION_TOKEN, session.getSessionToken())
				.addArg(PROJECT_NAME, makeUniqueId("project")).buildFunction();

		return executor.execute(function);
	}
}
