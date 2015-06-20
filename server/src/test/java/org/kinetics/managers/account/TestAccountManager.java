package org.kinetics.managers.account;

import static com.lohika.server.core.Protocol.Arguments.EMAIL;
import static com.lohika.server.core.Protocol.Arguments.SESSION_TOKEN;
import static com.lohika.server.core.Protocol.Errors.INVALID_REQUEST_PARAMETER;
import static com.lohika.server.core.Protocol.Errors.USER_PERMISSION_INVALID;
import static com.lohika.server.core.test.TestUtils.NAME_PREFIX;
import static com.lohika.server.core.test.TestUtils.assertErrorResponse;
import static com.lohika.server.core.test.TestUtils.assertOkMessageResponse;
import static com.lohika.server.core.test.TestUtils.extractSingleGenericResponseData;
import static com.lohika.server.core.test.TestUtils.makeSession;
import static com.lohika.server.core.test.TestUtils.makeUniqueId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.kinetics.dao.user.UserStatus.WAITING_CONFIRMATION;
import static org.kinetics.managers.shared.UTConsts.DUMMY_EMAIL;
import static org.kinetics.rest.Protocol.Arguments.BIRTHDAY;
import static org.kinetics.rest.Protocol.Arguments.CONFIRMATION_CODE;
import static org.kinetics.rest.Protocol.Arguments.FIRST_NAME;
import static org.kinetics.rest.Protocol.Arguments.ID;
import static org.kinetics.rest.Protocol.Arguments.PASSWORD;
import static org.kinetics.rest.Protocol.Arguments.PROJECT;
import static org.kinetics.rest.Protocol.Arguments.SEARCH_DATA;
import static org.kinetics.rest.Protocol.Arguments.SEARCH_TOKEN;
import static org.kinetics.rest.Protocol.Arguments.SECOND_NAME;
import static org.kinetics.rest.Protocol.Arguments.TOKEN;
import static org.kinetics.rest.Protocol.Arguments.USER;
import static org.kinetics.rest.Protocol.Errors.NO_SHARED_DATA;
import static org.kinetics.rest.Protocol.Managers.ACCOUNT_MANANGER;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.codehaus.jackson.annotate.JsonProperty;
import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kinetics.dao.audit.EventRepository;
import org.kinetics.dao.audit.EventType;
import org.kinetics.dao.authorization.Role;
import org.kinetics.dao.authorization.RoleRepository;
import org.kinetics.dao.authorization.RolesEnum;
import org.kinetics.dao.project.Project;
import org.kinetics.dao.project.ProjectRepository;
import org.kinetics.dao.project.ProjectStatus;
import org.kinetics.dao.service.AnalystPatient;
import org.kinetics.dao.service.AnalystPatientRepository;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.session.SessionRepository;
import org.kinetics.dao.share.SharedTest;
import org.kinetics.dao.share.SharedTestRepository;
import org.kinetics.dao.testsession.TestSession;
import org.kinetics.dao.testsession.TestSessionRepository;
import org.kinetics.dao.user.Confirmation;
import org.kinetics.dao.user.ConfirmationRepository;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
import org.kinetics.dao.user.UserStatus;
import org.kinetics.managers.shared.UTConsts;
import org.kinetics.rest.Protocol;
import org.kinetics.rest.Protocol.Arguments;
import org.kinetics.rest.Protocol.Errors;
import org.kinetics.test.TransactionalRequestTest;
import org.kinetics.util.secure.HashUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.lohika.protocol.core.processor.RequestBuilder;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.Response;
import com.lohika.server.core.test.TestRequestExecuter;
import com.lohika.server.core.test.TestUtils;

public class TestAccountManager extends TransactionalRequestTest {

	@Autowired
	private UserRepository userRepo;
	@Autowired
	private RoleRepository roleRepo;
	@Autowired
	private ConfirmationRepository confirmRepo;
	@Autowired
	private SessionRepository sessionRepo;
	@Autowired
	private DummyUserManager userManager;
	@Autowired
	private AnalystPatientRepository analystPatientRepo;
	@Autowired
	private ProjectRepository projectRepo;
	@Autowired
	private TestSessionRepository testSessionRepo;
	@Autowired
	private SharedTestRepository sharedTestRepo;
	@Autowired
	private EventRepository eventRepo;

	private Project dummyProject;

	@Before
	public void setUp() {
		dummyProject = projectRepo.save(new Project(makeUniqueId("project")));
	}

	@Test
	public void testCreateUser() {

		long count = userRepo.count();

		// valid case
		Response response = createDummyAccount(executor, dummyProject.getId());
		// dummyProject2.getId());
		assertOkMessageResponse(response);

		assertEquals(count + 1, userRepo.count());
		assertEquals(1, confirmRepo.count());

		assertErrorResponse(
				createDummyAccount(makeUniqueId(), executor, (Integer[]) null),
				INVALID_REQUEST_PARAMETER);

		assertEquals(1, eventRepo.count());
		assertEquals(1, eventRepo.countByType(EventType.SIGNUP));
	}

	@Test
	public void testCreateUserWithTest() {
		long count = userRepo.count();

		RequestFunction function = new RequestBuilder(ACCOUNT_MANANGER,
				CreateUserWithTest.METHOD)
				.addArg(FIRST_NAME, makeUniqueId(NAME_PREFIX))
				.addArg(SECOND_NAME, makeUniqueId(NAME_PREFIX))
				.addArg(EMAIL, DUMMY_EMAIL)
				.addArg(PROJECT, Arrays.asList(dummyProject.getId()))
				.addArg(PASSWORD, UTConsts.DUMMY_PASS)
				.addArg(Arguments.TEST_SESSION,
						new TestSession(1, makeUniqueId("raw"),
								makeUniqueId("type"), null, null) {

							@Override
							@JsonProperty
							public Integer getUserId() {
								return null;
							}

							@Override
							@JsonProperty
							public String getUserFirstName() {
								return null;
							}

							@Override
							@JsonProperty
							public String getUserSecondName() {
								return null;
							}

						}).buildFunction();

		assertOkMessageResponse(executor.execute(function));

		assertEquals(count + 1, userRepo.count());
		assertEquals(1, testSessionRepo.count());

		assertErrorResponse(executor.execute(function),
				Errors.USER_ALREADY_EXISTS);

		assertEquals(1, eventRepo.count());
		assertEquals(1, eventRepo.countByType(EventType.TRY_SIGNUP));
	}

	// @Test
	public void testCreateUserCaptcha() {

		// TODO: consider...

		// RequestFunction function = new RequestBuilder(ACCOUNT_MANANGER,
		// CreateUser.METHOD)
		// .addArg(Arguments.FIRST_NAME, makeUniqueId(NAME_PREFIX))
		// .addArg(SECOND_NAME, makeUniqueId(NAME_PREFIX))
		// .addArg(EMAIL, UTConsts.DUMMY_EMAIL)
		// .addArg(Arguments.PASSWORD, UTConsts.DUMMY_PASS)
		// .addArg(Arguments.CHALLENGE, "12345")
		// .addArg(Arguments.SOLUTION, "solution string").buildFunction();

	}

	@Test
	public void testConfirmCreate() {
		// create account
		createDummyAccount(executor, dummyProject.getId());

		// #1 - valid case
		Confirmation confirmation = confirmRepo
				.findByUser(userRepo.findOneByEmail(UTConsts.DUMMY_EMAIL))
				.iterator().next();

		Response success = confirmDummyUser(confirmation, executor);
		assertOkMessageResponse(success);

		assertEquals(0, confirmRepo.count());

		assertEquals(2, eventRepo.count());
		assertEquals(1, eventRepo.countByType(EventType.SIGNUP));
		assertEquals(1, eventRepo.countByType(EventType.CONFIRM));
	}

	@Test
	public void testConfirmCreateDirty() {
		RequestBuilder builder = new RequestBuilder(ACCOUNT_MANANGER,
				ConfirmCreate.METHOD).addArg(EMAIL, UTConsts.DUMMY_EMAIL)
				.addArg(CONFIRMATION_CODE, "xxxxxx");
		RequestFunction function = builder.buildFunction();

		// create account
		testCreateUser();

		// #2 - bad confirmation code
		Response errorResponse = executor.execute(function);
		assertErrorResponse(errorResponse, Errors.CONFIRMATION_CODE_INVALID);
	}

	@Test
	public void testAuthenticate() {
		User user = userManager.createActivatedUser(DUMMY_EMAIL, dummyProject);

		RequestBuilder builder = new RequestBuilder(ACCOUNT_MANANGER,
				Authenticate.METHOD).addArg(EMAIL, user.getEmail()).addArg(
				PASSWORD, UTConsts.DUMMY_PASS);

		Response response = executor.execute(builder.buildFunction());
		@SuppressWarnings("unchecked")
		List<Project> projects = extractSingleGenericResponseData(response,
				List.class);

		assertEquals(1, projects.size());
		assertEquals(dummyProject.getId(), projects.get(0).getId());

		dummyProject.setStatus(ProjectStatus.DISABLED);
		projectRepo.save(dummyProject);

		assertErrorResponse(executor.execute(builder.buildFunction()),
				Errors.PROJECT_NOT_ASSIGNED);

		Session session = userManager.createActivatedSiteAdminAndLogin();
		builder.setArg(EMAIL, session.getUser().getEmail()).setArg(PASSWORD,
				UTConsts.DUMMY_PASS);

		assertOkMessageResponse(executor.execute(builder.buildFunction()));
	}

	@Test
	public void testLogin() {
		testConfirmCreate();

		String token = loginDummyUser(DUMMY_EMAIL, dummyProject.getId(),
				executor);

		Assert.assertNotNull(token);
		assertEquals(1, sessionRepo.count());
	}

	@Test
	public void testLoginDirty() {
		// #1 - no such user
		Response errorResponse = loginDummyUserInternal(DUMMY_EMAIL,
				dummyProject.getId(), executor);
		assertErrorResponse(errorResponse, Errors.USER_DOESNT_EXIST);

		// #2 - no project
		User user = userManager.createActivatedUser(DUMMY_EMAIL);
		errorResponse = loginDummyUserInternal(user.getEmail(), null, executor);
		assertErrorResponse(errorResponse, INVALID_REQUEST_PARAMETER);

		// #3 - disabled project
		dummyProject.setStatus(ProjectStatus.DISABLED);
		// projectRepo.save(dummyProject);

		errorResponse = loginDummyUserInternal(user.getEmail(),
				dummyProject.getId(), executor);
		assertErrorResponse(errorResponse, Errors.PROJECT_DISABLED);

		dummyProject.setStatus(ProjectStatus.ACTIVE);

		// #4 - disabled user
		user.setStatus(UserStatus.DISABLED);

		errorResponse = loginDummyUserInternal(user.getEmail(),
				dummyProject.getId(), executor);
		assertErrorResponse(errorResponse, Errors.USER_DISABLED);

		// #5 - no password activation
		user.setHashData(null);
		user.setStatus(UserStatus.WAITING_PASS);

		errorResponse = loginDummyUserInternal(user.getEmail(),
				dummyProject.getId(), executor);
		assertErrorResponse(errorResponse, Errors.CREDENTIALS_INVALID);
	}

	@Test
	public void testSiteAdminLogin() {
		Session siteAdminSession = userManager
				.createActivatedSiteAdminAndLogin();
		assertNotNull(siteAdminSession);
		assertEquals(1, sessionRepo.count());
	}

	@Test
	public void testLogout() {
		testConfirmCreate();

		String token = loginDummyUser(DUMMY_EMAIL, dummyProject.getId(),
				executor);

		RequestFunction function = new RequestBuilder(ACCOUNT_MANANGER,
				Logout.METHOD).addArg(TestUtils.makeSession(token))
				.buildFunction();
		Response response = executor.execute(function);
		assertOkMessageResponse(response);

		assertEquals(0, sessionRepo.count());
	}

	@Test
	public void testResetPassword() {
		RequestFunction function = new RequestBuilder(ACCOUNT_MANANGER,
				ResetPassword.METHOD).addArg(EMAIL, UTConsts.DUMMY_EMAIL)
				.buildFunction();

		Response response = executor.execute(function);
		assertErrorResponse(response, Errors.USER_DOESNT_EXIST);

		User user = userManager.createActivatedUser(UTConsts.DUMMY_EMAIL);

		response = executor.execute(function);

		Collection<Confirmation> confirmations = confirmRepo.findByUser(user);
		assertEquals(1, confirmations.size());
		assertEquals(user.getEmail(), confirmations.iterator().next().getUser()
				.getEmail());
	}

	@Test
	public void testSetPassword() {
		// simulate flow
		testResetPassword();

		Confirmation confirmation = confirmRepo.findAll().iterator().next();
		assertNotNull(confirmation.getTimestamp());

		RequestFunction function = new RequestBuilder(ACCOUNT_MANANGER,
				SetPassword.METHOD).addArg(EMAIL, UTConsts.DUMMY_EMAIL)
				.addArg(PASSWORD, makeUniqueId())
				.addArg(TOKEN, confirmation.getCode()).buildFunction();

		assertOkMessageResponse(executor.execute(function));

		assertEquals(0, confirmRepo.count());

		assertErrorResponse(executor.execute(function),
				INVALID_REQUEST_PARAMETER);
	}

	@Test
	public void testDeleteUser() {
		long count = userRepo.count();

		Session session = userManager.createActivatedUserAndLogin();

		Response response = deleteUser(executor, session);
		assertOkMessageResponse(response);

		assertEquals(count, userRepo.count());

		// check dependent entities
		assertEquals(0, sessionRepo.count());
		assertEquals(0, confirmRepo.count());

		// check admin deletion
		session = userManager.createActivatedSiteAdminAndLogin();
		assertOkMessageResponse(deleteUser(executor, session));

		userRepo.deleteAll();

		session = userManager.createActivatedSiteAdminAndLogin();
		assertErrorResponse(deleteUser(executor, session),
				Errors.SITE_ADMIN_DELETE);
	}

	@Test
	public void testGetUserInfo() {
		Session session = userManager.createActivatedUserAndLogin();
		RequestFunction function = new RequestBuilder(ACCOUNT_MANANGER,
				GetUserInfo.METHOD).addArg(SESSION_TOKEN,
				session.getSessionToken()).buildFunction();

		Response response = executor.execute(function);
		User user = TestUtils.extractSingleGenericResponseData(response,
				User.class);

		Assert.assertNotNull(user);
		assertEquals(session.getUser().getId(), user.getId());
	}

	@Test
	public void testGetUserInfoList() {
		Session userSession = userManager.createActivatedUserAndLogin();

		RequestFunction function = new RequestBuilder(ACCOUNT_MANANGER,
				GetUserInfoList.METHOD).addArg(SESSION_TOKEN,
				userSession.getSessionToken()).buildFunction();
		assertErrorResponse(executor.execute(function), USER_PERMISSION_INVALID);

		Session adminSession = userManager.createActivatedSiteAdminAndLogin();

		function = new RequestBuilder(ACCOUNT_MANANGER, GetUserInfoList.METHOD)
				.addArg(SESSION_TOKEN, adminSession.getSessionToken())
				.buildFunction();
		Response response = executor.execute(function);
		@SuppressWarnings("unchecked")
		List<User> users = TestUtils.extractSingleGenericResponseData(response,
				List.class);

		assertEquals(
				userRepo.findAllExcludeIdAndStatus(
						adminSession.getUser().getId(), WAITING_CONFIRMATION)
						.size(), users.size());
	}

	@Test
	public void testModifyUserInfo() {
		Session session = userManager.createActivatedUserAndLogin();

		String newSurname = makeUniqueId("surname");
		LocalDate newBirth = new LocalDate();

		RequestBuilder builder = new RequestBuilder(ACCOUNT_MANANGER,
				ModifyUserInfo.METHOD)
				.addArg(SESSION_TOKEN, session.getSessionToken())
				.addArg(BIRTHDAY, newBirth)
				.addArg(Arguments.SECOND_NAME, newSurname);

		assertOkMessageResponse(executor.execute(builder.buildFunction()));

		User user = userRepo.findOne(session.getUser().getId());
		assertEquals(newBirth, user.getBirthday());
		assertEquals(newSurname, user.getSecondName());
	}

	@Test
	public void testModifyPassword() {
		Session session = userManager.createActivatedUserAndLogin();

		String newPass = "123";

		RequestFunction function = new RequestBuilder(ACCOUNT_MANANGER,
				ModifyPassword.METHOD)
				.addArg(SESSION_TOKEN, session.getSessionToken())
				.addArg(Arguments.PASSWORD, newPass)
				.addArg(Arguments.NEW_PASSWORD, newPass).buildFunction();
		TestUtils.assertErrorResponse(executor.execute(function),
				Errors.CREDENTIALS_INVALID);

		function = new RequestBuilder(ACCOUNT_MANANGER, ModifyPassword.METHOD)
				.addArg(SESSION_TOKEN, session.getSessionToken())
				.addArg(Arguments.PASSWORD, UTConsts.DUMMY_PASS)
				.addArg(Arguments.NEW_PASSWORD, newPass).buildFunction();

		assertOkMessageResponse(executor.execute(function));
		Assert.assertTrue(HashUtils.isValid(newPass, session.getUser()
				.getHashData()));
	}

	@Test
	public void testModifyUserStatus() {
		User user = userManager.createActivatedUser(UTConsts.DUMMY_EMAIL);
		User user2 = userManager.createActivatedUser(makeUniqueId("email"));

		Session adminSession = userManager.createActivatedSiteAdminAndLogin();

		RequestBuilder builder = new RequestBuilder(ACCOUNT_MANANGER,
				ModifyUserStatus.METHOD)
				.addArg(SESSION_TOKEN, adminSession.getSessionToken())
				.addArg(Arguments.IDS,
						Arrays.asList(user.getId(), user2.getId()))
				.addArg(Arguments.DISABLE, true);
		assertOkMessageResponse(executor.execute(builder.buildFunction()));

		assertErrorResponse(
				loginDummyUserInternal(DUMMY_EMAIL, dummyProject.getId(),
						executor), Errors.USER_DISABLED);

		builder.setArg(Arguments.DISABLE, false);
		assertOkMessageResponse(executor.execute(builder.buildFunction()));

		Assert.assertNotNull(loginDummyUser(DUMMY_EMAIL, dummyProject.getId(),
				executor));
	}

	@Test
	public void testResendConfirmation() {
		RequestFunction function = new RequestBuilder(ACCOUNT_MANANGER,
				ResendConfirmation.METHOD).addArg(EMAIL, UTConsts.DUMMY_EMAIL)
				.addArg(TOKEN, makeUniqueId()).buildFunction();
		assertErrorResponse(executor.execute(function),
				Errors.USER_DOESNT_EXIST);

		testCreateUser();

		assertOkMessageResponse(executor.execute(function));

		assertEquals(2, confirmRepo.count());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFindUser() {

		Session adminSession = userManager.createActivatedSiteAdminAndLogin();
		User user = userManager.createActivatedUser(DUMMY_EMAIL);

		// case #1 - by full name
		RequestBuilder builder = new RequestBuilder(ACCOUNT_MANANGER,
				FindUser.METHOD)
				.addArg(SESSION_TOKEN, adminSession.getSessionToken())
				.addArg(SEARCH_TOKEN, SearchToken.email)
				.addArg(SEARCH_DATA, user.getEmail().toUpperCase());

		List<User> users = extractSingleGenericResponseData(
				executor.execute(builder.buildFunction()), List.class);

		assertEquals(1, users.size());
		assertEquals(user.getEmail(), users.get(0).getEmail());

		// case #2 - partial email
		builder.setArg(SEARCH_DATA,
				DUMMY_EMAIL.substring(0, DUMMY_EMAIL.length() - 3)
						.toUpperCase());

		users = extractSingleGenericResponseData(
				executor.execute(builder.buildFunction()), List.class);

		assertEquals(1, users.size());
		assertEquals(user.getEmail(), users.get(0).getEmail());

		// case #3 Summary
		builder.setArg(SEARCH_TOKEN, SearchToken.summary);
		users = extractSingleGenericResponseData(
				executor.execute(builder.buildFunction()), List.class);

		assertEquals(1, users.size());
		assertEquals(user.getEmail(), users.get(0).getEmail());

		// case #4 - name
		builder.setArg(SEARCH_TOKEN, SearchToken.name).setArg(SEARCH_DATA,
				user.getFirstName().toUpperCase());
		users = extractSingleGenericResponseData(
				executor.execute(builder.buildFunction()), List.class);

		assertEquals(1, users.size());
		assertEquals(user.getEmail(), users.get(0).getEmail());

		// case #5 - exclude non activated user
		user.setStatus(WAITING_CONFIRMATION);
		userRepo.save(user);

		users = extractSingleGenericResponseData(
				executor.execute(builder.buildFunction()), List.class);
		assertEquals(0, users.size());
	}

	@Test
	public void testDeleteUserByEmail() {
		Session session = userManager.createActivatedUserAndLogin();

		RequestBuilder builder = new RequestBuilder(ACCOUNT_MANANGER,
				DeleteUserByEmail.METHOD).addArg(SESSION_TOKEN,
				session.getSessionToken()).addArg(EMAIL,
				Arrays.asList(session.getUser().getEmail()));

		assertErrorResponse(executor.execute(builder.buildFunction()),
				USER_PERMISSION_INVALID);

		Session adminSession = userManager.createActivatedSiteAdminAndLogin();

		builder.setArg(SESSION_TOKEN, adminSession.getSessionToken());

		assertOkMessageResponse(executor.execute(builder.buildFunction()));

		userRepo.exists(session.getUser().getId());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetUserInfoById() {
		Session session = userManager.createActivatedUserAndLogin();

		RequestBuilder builder = new RequestBuilder(ACCOUNT_MANANGER,
				GetUserInfoById.METHOD).addArg(SESSION_TOKEN,
				session.getSessionToken()).addArg(Arguments.IDS,
				Arrays.asList(session.getUser().getId()));

		assertErrorResponse(executor.execute(builder.buildFunction()),
				USER_PERMISSION_INVALID);

		Session adminSession = userManager.createActivatedSiteAdminAndLogin();

		builder.setArg(SESSION_TOKEN, adminSession.getSessionToken());

		List<User> result = extractSingleGenericResponseData(
				executor.execute(builder.buildFunction()), List.class);

		assertEquals(1, result.size());
		assertEquals(session.getUser(), result.get(0));

		Session siteAdminSession = userManager
				.createActivatedSiteAdminAndLogin();

		builder.setArg(SESSION_TOKEN, siteAdminSession.getSessionToken());

		result = extractSingleGenericResponseData(
				executor.execute(builder.buildFunction()), List.class);

		assertEquals(1, result.size());
		assertEquals(session.getUser(), result.get(0));
	}

	@Test
	public void testAssignPatient() {

		long count = analystPatientRepo.count();

		User patient = userManager.createActivatedUser(UTConsts.DUMMY_EMAIL);

		Session analystSession = userManager.createActivatedAnalystAndLogin();

		RequestBuilder builder = new RequestBuilder(ACCOUNT_MANANGER,
				AssignPatient.METHOD).addArg(SESSION_TOKEN,
				analystSession.getSessionToken()).addArg(Arguments.IDS,
				Arrays.asList(patient.getId()));
		assertOkMessageResponse(executor.execute(builder.buildFunction()));

		assertEquals(count + 1, analystPatientRepo.count());

		builder.setMethod(UnassignPatient.METHOD);
		assertOkMessageResponse(executor.execute(builder.buildFunction()));

		assertEquals(count, analystPatientRepo.count());

		Session adminSession = userManager.createActivatedSiteAdminAndLogin();
		builder.setArg(SESSION_TOKEN, adminSession.getSessionToken());
		assertErrorResponse(executor.execute(builder.buildFunction()),
				USER_PERMISSION_INVALID);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetPatients() {
		User patient = userManager.createActivatedUser(UTConsts.DUMMY_EMAIL);
		Session analystSession = userManager.createActivatedAnalystAndLogin();

		RequestBuilder builder = new RequestBuilder(ACCOUNT_MANANGER,
				AssignPatient.METHOD).addArg(SESSION_TOKEN,
				analystSession.getSessionToken()).addArg(Arguments.IDS,
				Arrays.asList(patient.getId()));
		assertOkMessageResponse(executor.execute(builder.buildFunction()));

		builder = new RequestBuilder(ACCOUNT_MANANGER, GetPatients.METHOD)
				.addArg(SESSION_TOKEN, analystSession.getSessionToken());

		List<User> patients = extractSingleGenericResponseData(
				executor.execute(builder.buildFunction()), List.class);

		assertEquals(1, patients.size());
		assertEquals(patient.getEmail(), patients.get(0).getEmail());

		Session adminSession = userManager.createActivatedSiteAdminAndLogin();
		builder.setArg(SESSION_TOKEN, adminSession.getSessionToken());
		assertErrorResponse(executor.execute(builder.buildFunction()),
				USER_PERMISSION_INVALID);
	}

	@Test
	public void testCreatePatient() {
		Session analystSession = userManager.createActivatedAnalystAndLogin();

		long userCount = userRepo.count();
		long analystPatientCount = analystPatientRepo.count();

		RequestBuilder builder = new RequestBuilder(ACCOUNT_MANANGER,
				CreatePatient.METHOD)
				.addArg(SESSION_TOKEN, analystSession.getSessionToken())
				.addArg(Arguments.FIRST_NAME, makeUniqueId(NAME_PREFIX))
				.addArg(Arguments.SECOND_NAME, makeUniqueId(NAME_PREFIX))
				.addArg(EMAIL, null);

		assertOkMessageResponse(executor.execute(builder.buildFunction()));

		// Test case for getPatients
		@SuppressWarnings("unchecked")
		List<User> patients = extractSingleGenericResponseData(
				executor.execute(new RequestBuilder(ACCOUNT_MANANGER,
						GetPatients.METHOD).addArg(SESSION_TOKEN,
						analystSession.getSessionToken()).buildFunction()),
				List.class);
		assertEquals(1, patients.size());

		assertEquals(userCount + 1, userRepo.count());
		assertEquals(analystPatientCount + 1, analystPatientRepo.count());

		builder.setArg(EMAIL, UTConsts.DUMMY_EMAIL);

		assertOkMessageResponse(executor.execute(builder.buildFunction()));

		assertEquals(userCount + 2, userRepo.count());
		assertEquals(analystPatientCount + 2, analystPatientRepo.count());

		assertErrorResponse(executor.execute(builder.buildFunction()),
				Errors.USER_ALREADY_EXISTS);

		Session adminSession = userManager.createActivatedSiteAdminAndLogin();
		builder.setArg(SESSION_TOKEN, adminSession.getSessionToken());
		assertErrorResponse(executor.execute(builder.buildFunction()),
				USER_PERMISSION_INVALID);

		// try to login via patient
		assertErrorResponse(
				loginDummyUserInternal(UTConsts.DUMMY_EMAIL,
						dummyProject.getId(), executor),
				Errors.CREDENTIALS_INVALID);
	}

	@Test
	public void testModifyPatientInfo() {
		Session analystSession = userManager.createActivatedAnalystAndLogin();

		RequestBuilder builder = new RequestBuilder(ACCOUNT_MANANGER,
				CreatePatient.METHOD)
				.addArg(SESSION_TOKEN, analystSession.getSessionToken())
				.addArg(Arguments.FIRST_NAME, makeUniqueId(NAME_PREFIX))
				.addArg(Arguments.SECOND_NAME, makeUniqueId(NAME_PREFIX))
				.setArg(EMAIL, UTConsts.DUMMY_EMAIL);
		assertOkMessageResponse(executor.execute(builder.buildFunction()));

		User patient = userRepo.findOneByEmail(UTConsts.DUMMY_EMAIL);
		assertNotNull(patient);

		String newSurname = makeUniqueId("surname");
		String newEmail = "newEmail@example.com".toLowerCase();
		LocalDate newBirth = new LocalDate();

		builder = new RequestBuilder(ACCOUNT_MANANGER, ModifyPatientInfo.METHOD)
				.addArg(SESSION_TOKEN, analystSession.getSessionToken())
				.addArg(ID, patient.getId()).addArg(SECOND_NAME, newSurname)
				.setArg(BIRTHDAY, newBirth).setArg(EMAIL, newEmail);
		assertOkMessageResponse(executor.execute(builder.buildFunction()));

		User user = userRepo.findOne(patient.getId());
		assertEquals(newBirth, user.getBirthday());
		assertEquals(newSurname, user.getSecondName());
		assertEquals(newEmail, user.getEmail());

		builder.setArg(EMAIL, analystSession.getUser().getEmail());
		assertErrorResponse(executor.execute(builder.buildFunction()),
				Errors.EMAIL_ALREADY_EXISTS);

		Session adminSession = userManager.createActivatedSiteAdminAndLogin();
		builder.setArg(SESSION_TOKEN, adminSession.getSessionToken());
		assertErrorResponse(executor.execute(builder.buildFunction()),
				USER_PERMISSION_INVALID);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFindPatient() {

		Session analystSession = userManager.createActivatedAnalystAndLogin();
		User patient = userManager.createActivatedUser(DUMMY_EMAIL);

		// case #1 - partial email
		RequestBuilder builder = new RequestBuilder(ACCOUNT_MANANGER,
				FindPatient.METHOD)
				.addArg(SESSION_TOKEN, analystSession.getSessionToken())
				.addArg(SEARCH_TOKEN, SearchToken.email)
				.addArg(SEARCH_DATA,
						patient.getEmail()
								.substring(0, DUMMY_EMAIL.length() - 3)
								.toUpperCase());

		List<User> users = extractSingleGenericResponseData(
				executor.execute(builder.buildFunction()), List.class);

		assertEquals(1, users.size());
		assertEquals(patient.getEmail(), users.get(0).getEmail());

		// case #2 - name
		builder.setArg(SEARCH_TOKEN, SearchToken.name).setArg(SEARCH_DATA,
				patient.getFirstName().toUpperCase());
		users = extractSingleGenericResponseData(
				executor.execute(builder.buildFunction()), List.class);

		assertEquals(1, users.size());
		assertEquals(patient.getEmail(), users.get(0).getEmail());

		// case #3 - summary token
		builder.setArg(SEARCH_TOKEN, SearchToken.summary).setArg(SEARCH_DATA,
				patient.getFirstName().toUpperCase());
		users = extractSingleGenericResponseData(
				executor.execute(builder.buildFunction()), List.class);

		assertEquals(1, users.size());
		assertEquals(patient.getEmail(), users.get(0).getEmail());

		// case #4 - exclude assigned patients
		analystPatientRepo.save(new AnalystPatient(analystSession.getUser(),
				patient, analystSession.getProject()));
		users = extractSingleGenericResponseData(
				executor.execute(builder.buildFunction()), List.class);
		assertEquals(0, users.size());

		// case #5 - invalid permission
		Session adminSession = userManager.createActivatedSiteAdminAndLogin();
		builder.setArg(SESSION_TOKEN, adminSession.getSessionToken());
		assertErrorResponse(executor.execute(builder.buildFunction()),
				USER_PERMISSION_INVALID);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetRoleList() {
		Session userSession = userManager.createActivatedUserAndLogin();
		Session adminSession = userManager.createActivatedSiteAdminAndLogin();

		RequestBuilder builder = new RequestBuilder(ACCOUNT_MANANGER,
				GetRoleList.METHOD).addArg(SESSION_TOKEN,
				userSession.getSessionToken());
		assertErrorResponse(executor.execute(builder.buildFunction()),
				USER_PERMISSION_INVALID);

		builder.setArg(SESSION_TOKEN, adminSession.getSessionToken());
		List<Role> rolesResponse = extractSingleGenericResponseData(
				executor.execute(builder.buildFunction()), List.class);
		List<Role> roleRepoAll = (List<Role>) roleRepo.findAll();

		assertEquals(roleRepoAll.size(), rolesResponse.size());
	}

	@Test
	public void testGetPatientInfoById() {
		User patient = userManager.createActivatedUserAndLogin().getUser();
		Session analystSession = userManager.createActivatedAnalystAndLogin();

		RequestBuilder builder = new RequestBuilder(ACCOUNT_MANANGER,
				AssignPatient.METHOD).addArg(SESSION_TOKEN,
				analystSession.getSessionToken()).addArg(Arguments.IDS,
				Arrays.asList(patient.getId()));
		assertOkMessageResponse(executor.execute(builder.buildFunction()));

		builder = new RequestBuilder(ACCOUNT_MANANGER,
				GetPatientInfoById.METHOD).addArg(ID, patient.getId()).addArg(
				SESSION_TOKEN, analystSession.getSessionToken());

		User patientOutput = extractSingleGenericResponseData(
				executor.execute(builder.buildFunction()), User.class);

		assertEquals(patient, patientOutput);

		Session adminSession = userManager.createActivatedSiteAdminAndLogin();
		builder.setArg(SESSION_TOKEN, adminSession.getSessionToken());
		assertErrorResponse(executor.execute(builder.buildFunction()),
				USER_PERMISSION_INVALID);

	}

	@Test
	public void testPatientInvitation() {
		Session analystSession = userManager.createActivatedAnalystAndLogin();

		RequestBuilder builder = new RequestBuilder(ACCOUNT_MANANGER,
				CreatePatient.METHOD)
				.addArg(SESSION_TOKEN, analystSession.getSessionToken())
				.addArg(Arguments.FIRST_NAME, makeUniqueId(NAME_PREFIX))
				.setArg(EMAIL, UTConsts.DUMMY_EMAIL)
				.addArg(Arguments.SECOND_NAME, makeUniqueId(NAME_PREFIX));
		assertOkMessageResponse(executor.execute(builder.buildFunction()));

		User patient = userRepo.findOneByEmail(UTConsts.DUMMY_EMAIL);
		assertNotNull(patient);

		long confirmCount = confirmRepo.count();

		builder = new RequestBuilder(ACCOUNT_MANANGER, SendPatientInvite.METHOD)
				.addArg(SESSION_TOKEN, analystSession.getSessionToken())
				.addArg(ID, patient.getId());
		assertOkMessageResponse(executor.execute(builder.buildFunction()));
		assertEquals(confirmCount + 1, confirmRepo.count());

		Confirmation confirmation = ((Vector<Confirmation>) confirmRepo
				.findByUser(patient)).get(0);
		assertNotNull(confirmation);

		builder = new RequestBuilder(ACCOUNT_MANANGER,
				GetPatientInfoByConfCode.METHOD).addArg(CONFIRMATION_CODE,
				confirmation.getCode()).addArg(EMAIL,
				confirmation.getUser().getEmail());
		User patientOutput = extractSingleGenericResponseData(
				executor.execute(builder.buildFunction()), User.class);
		assertEquals(patient, patientOutput);

		builder = new RequestBuilder(ACCOUNT_MANANGER,
				ConfirmPatientProfile.METHOD)
				.addArg(CONFIRMATION_CODE, confirmation.getCode())
				.addArg(PASSWORD, UTConsts.DUMMY_PASS)
				.addArg(EMAIL, confirmation.getUser().getEmail());
		assertOkMessageResponse(executor.execute(builder.buildFunction()));
		assertEquals(0, confirmRepo.count());
	}

	@Test
	public void testCreateUserByAdmin() {
		Session siteAdminSession = userManager
				.createActivatedSiteAdminAndLogin();

		final long count = userRepo.count();

		String newAdminEmail = makeUniqueId();

		RequestBuilder builder = new RequestBuilder(ACCOUNT_MANANGER,
				CreateUserByAdmin.METHOD).addArg(FIRST_NAME, makeUniqueId())
				.addArg(SESSION_TOKEN, siteAdminSession.getSessionToken())
				.addArg(SECOND_NAME, makeUniqueId())
				.addArg(Arguments.ROLE, RolesEnum.SITE_ADMIN.name())
				.addArg(EMAIL, newAdminEmail);

		assertOkMessageResponse(executor.execute(builder.buildFunction()));

		assertEquals(count + 1, userRepo.count());
		assertEquals(RolesEnum.SITE_ADMIN.name(),
				userRepo.findOneByEmail(newAdminEmail).getRoles().iterator()
						.next().getName());

		builder.setArg(Arguments.ROLE, RolesEnum.ANALYST.name()).setArg(EMAIL,
				UTConsts.DUMMY_EMAIL);

		assertErrorResponse(executor.execute(builder.buildFunction()),
				INVALID_REQUEST_PARAMETER);

		builder.setArg(PROJECT, Arrays.asList(dummyProject.getId()));

		assertOkMessageResponse(executor.execute(builder.buildFunction()));

		assertEquals(count + 2, userRepo.count());
		assertEquals(RolesEnum.ANALYST.name(),
				userRepo.findOneByEmail(UTConsts.DUMMY_EMAIL).getRoles()
						.iterator().next().getName());
	}

	@Test
	public void testResendInvite() {
		Session siteAdminSession = userManager
				.createActivatedSiteAdminAndLogin();

		User user = userManager.createActivatedUser(UTConsts.DUMMY_EMAIL);
		user.setStatus(UserStatus.WAITING_PASS);
		userRepo.save(user);

		RequestFunction function = new RequestBuilder(ACCOUNT_MANANGER,
				ResendInvite.METHOD)
				.addArg(SESSION_TOKEN, siteAdminSession.getSessionToken())
				.addArg(EMAIL, user.getEmail()).buildFunction();

		assertOkMessageResponse(executor.execute(function));
	}

	@Test
	public void testCreateShareUser() {
		Session testOwner = userManager.createActivatedUserAndLogin();

		final long count = userRepo.count();

		String mail = makeUniqueId(NAME_PREFIX);

		RequestBuilder builder = new RequestBuilder(ACCOUNT_MANANGER,
				CreateShareUser.METHOD).addArg(FIRST_NAME, mail)
				.addArg(SECOND_NAME, mail).addArg(EMAIL, mail)
				.addArg(PROJECT, Arrays.asList(dummyProject.getId()))
				.addArg(PASSWORD, UTConsts.DUMMY_PASS)
				.addArg(BIRTHDAY, new LocalDate().toDate())
				.addArg(USER, testOwner.getUser().getId());

		assertErrorResponse(executor.execute(builder.buildFunction()),
				NO_SHARED_DATA);
		assertEquals(count + 1, userRepo.count());

		mail = makeUniqueId(NAME_PREFIX);
		builder.setArg(EMAIL, mail);
		sharedTestRepo.save(new SharedTest(testOwner.getUser(), dummyProject,
				mail));

		assertOkMessageResponse(executor.execute(builder.buildFunction()));

		assertEquals(count + 2, userRepo.count());
		User user = userRepo.findOneByEmail(mail);
		assertEquals(UserStatus.ACTIVE, user.getStatus());
	}

	static Response createDummyAccount(String email,
			TestRequestExecuter executor, Integer... projectIds) {
		RequestFunction function = new RequestBuilder(ACCOUNT_MANANGER,
				CreateUser.METHOD)
				.addArg(FIRST_NAME, makeUniqueId(NAME_PREFIX))
				.addArg(SECOND_NAME, makeUniqueId(NAME_PREFIX))
				.addArg(EMAIL, email).addArg(PROJECT, projectIds)
				.addArg(PASSWORD, UTConsts.DUMMY_PASS)
				.addArg(BIRTHDAY, new LocalDate().toDate()).buildFunction();

		return executor.execute(function);
	}

	static Response createDummyAccount(TestRequestExecuter executor,
			Integer... customerId) {
		return createDummyAccount(UTConsts.DUMMY_EMAIL, executor, customerId);
	}

	static String loginDummyUser(String email, Integer customerId,
			TestRequestExecuter executor) {
		Response response = loginDummyUserInternal(email, customerId, executor);
		String token = TestUtils.extractSingleGenericResponseData(response,
				String.class);
		return token;
	}

	private static Response loginDummyUserInternal(String email,
			Integer customerId, TestRequestExecuter executor) {
		RequestFunction function = new RequestBuilder(ACCOUNT_MANANGER,
				Login.METHOD).addArg(EMAIL, email)
				.addArg(Protocol.Arguments.PASSWORD, UTConsts.DUMMY_PASS)
				.addArg(PROJECT, customerId).buildFunction();

		return executor.execute(function);
	}

	static Response confirmDummyUser(Confirmation confirmation,
			TestRequestExecuter executor) {
		RequestFunction function = new RequestBuilder(ACCOUNT_MANANGER,
				ConfirmCreate.METHOD)
				.addArg(EMAIL, confirmation.getUser().getEmail())
				.addArg(CONFIRMATION_CODE, confirmation.getCode())
				.buildFunction();

		return executor.execute(function);
	}

	public static Response deleteUser(TestRequestExecuter executor,
			Session session) {
		RequestFunction function = new RequestBuilder(ACCOUNT_MANANGER,
				DeleteUser.METHOD).addArg(
				makeSession(session.getSessionToken())).buildFunction();
		return executor.execute(function);
	}

}
