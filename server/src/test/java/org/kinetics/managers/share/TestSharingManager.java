package org.kinetics.managers.share;

import static com.lohika.server.core.Protocol.Arguments.EMAIL;
import static com.lohika.server.core.Protocol.Arguments.SESSION_TOKEN;
import static com.lohika.server.core.test.TestUtils.assertErrorResponse;
import static com.lohika.server.core.test.TestUtils.assertOkMessageResponse;
import static com.lohika.server.core.test.TestUtils.extractSingleGenericResponseData;
import static com.lohika.server.core.test.TestUtils.makeUniqueId;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.kinetics.rest.Protocol.Arguments.DATE_FROM;
import static org.kinetics.rest.Protocol.Arguments.DATE_TO;
import static org.kinetics.rest.Protocol.Arguments.ID;
import static org.kinetics.rest.Protocol.Arguments.PROJECT;
import static org.kinetics.rest.Protocol.Arguments.TOKEN;
import static org.kinetics.rest.Protocol.Arguments.USER;
import static org.kinetics.rest.Protocol.Errors.TEST_NOT_FOUND;
import static org.kinetics.rest.Protocol.Managers.SHARING_MANAGER;
import static org.kinetics.test.DaoGenerator.generateTestSessions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Assert;
import org.junit.Test;
import org.kinetics.dao.project.ProjectRepository;
import org.kinetics.dao.project.ProjectStatus;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.share.SharedTest;
import org.kinetics.dao.share.SharedTestRepository;
import org.kinetics.dao.share.SocialTest;
import org.kinetics.dao.share.SocialTestRepository;
import org.kinetics.dao.testsession.TestSession;
import org.kinetics.dao.testsession.TestSessionRepository;
import org.kinetics.dao.user.User;
import org.kinetics.managers.share.GetAllShareInfo.MyCollaboratorData;
import org.kinetics.managers.share.GetAllShareInfo.OtherTestRoomData;
import org.kinetics.managers.testsession.GetAllByDateShared;
import org.kinetics.managers.testsession.GetAllShared;
import org.kinetics.managers.testsession.GetDetails;
import org.kinetics.managers.testsession.GetDetailsByToken;
import org.kinetics.rest.Protocol.Arguments;
import org.kinetics.rest.Protocol.Errors;
import org.kinetics.rest.Protocol.Managers;
import org.kinetics.test.AutorizedRequestTest;
import org.springframework.beans.factory.annotation.Autowired;

import com.lohika.protocol.core.processor.RequestBuilder;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.server.core.test.TestUtils;

public class TestSharingManager extends AutorizedRequestTest {

	private final static DateTimeFormatter _localDateFormat = ISODateTimeFormat
			.date();

	@Autowired
	private TestSessionRepository testSessionRepo;
	@Autowired
	private SharedTestRepository sharedTestRepo;
	@Autowired
	private ProjectRepository projectRepo;
	@Autowired
	private SocialTestRepository socialTestRepo;

	@Test
	public void testAddEmail() {
		Session siteAdminSession = userManager
				.createActivatedSiteAdminAndLogin();

		List<String> emails = Arrays.asList(makeUniqueId(), makeUniqueId(),
				makeUniqueId());

		RequestBuilder builder = new RequestBuilder(SHARING_MANAGER,
				AddEmail.METHOD).addArg(SESSION_TOKEN,
				activeSession.getSessionToken()).addArg(EMAIL, emails);

		assertOkMessageResponse(executor.execute(builder.buildFunction()));
		assertEquals(emails.size(), sharedTestRepo.count());

		builder.setArg(EMAIL, Arrays.asList(emails.get(0), activeSession
				.getUser().getEmail(), siteAdminSession.getUser().getEmail()));

		assertOkMessageResponse(executor.execute(builder.buildFunction()));
		// nothing should be added
		assertEquals(emails.size(), sharedTestRepo.count());

		builder.setArg(EMAIL, emails);

		assertOkMessageResponse(executor.execute(builder.buildFunction()));
		assertEquals(emails.size(), sharedTestRepo.count());

		User registeredUser = userManager.createActivatedUser(makeUniqueId());
		builder.setArg(EMAIL, Arrays.asList(registeredUser.getEmail()));

		assertOkMessageResponse(executor.execute(builder.buildFunction()));
		// add 1 more
		assertEquals(emails.size() + 1, sharedTestRepo.count());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetAllShareInfo() {
		Session anotherUser = userManager.createActivatedAnalystAndLogin();

		// share:
		RequestBuilder builder = new RequestBuilder(SHARING_MANAGER,
				AddEmail.METHOD).addArg(SESSION_TOKEN,
				activeSession.getSessionToken()).addArg(EMAIL,
				asList(anotherUser.getUser().getEmail(), makeUniqueId()));

		assertOkMessageResponse(executor.execute(builder.buildFunction()));

		builder.setArg(SESSION_TOKEN, anotherUser.getSessionToken()).setArg(
				EMAIL, asList(activeSession.getUser().getEmail()));

		assertOkMessageResponse(executor.execute(builder.buildFunction()));
		assertEquals(3, sharedTestRepo.count());

		// do test checks:
		RequestFunction function = new RequestBuilder(SHARING_MANAGER,
				GetAllShareInfo.METHOD).addArg(SESSION_TOKEN,
				activeSession.getSessionToken()).buildFunction();

		Map<String, Object> sharedData = extractSingleGenericResponseData(
				executor.execute(function), Map.class);

		assertEquals(3, sharedData.size());
		// my test room shared with 2 users - existing and non-existing
		assertEquals(2,
				((List<MyCollaboratorData>) sharedData
						.get(GetAllShareInfo.SHARE_MINE)).size());

		OtherTestRoomData otherTestRoomData = ((List<OtherTestRoomData>) sharedData
				.get(GetAllShareInfo.SHARE_OTHERS)).get(0);
		assertEquals(anotherUser.getUser().getEmail(),
				otherTestRoomData.getOwnerEmail());
		assertEquals(anotherUser.getProject().getId(),
				otherTestRoomData.getProjectId());
		assertEquals(anotherUser.getUser().getId(),
				otherTestRoomData.getUserId());

		// check disabled projects
		userManager.getProject().setStatus(ProjectStatus.DISABLED);
		projectRepo.save(userManager.getProject());

		sharedData = extractSingleGenericResponseData(
				executor.execute(function), Map.class);

		assertEquals(3, sharedData.size());
		assertEquals(2,
				((List<MyCollaboratorData>) sharedData
						.get(GetAllShareInfo.SHARE_MINE)).size());
		assertEquals(0,
				((List<OtherTestRoomData>) sharedData
						.get(GetAllShareInfo.SHARE_OTHERS)).size());

		TestSession testSession = generateTestSession();

		builder = new RequestBuilder(SHARING_MANAGER, GenerateToken.METHOD)
				.addArg(SESSION_TOKEN, activeSession.getSessionToken()).addArg(
						ID, testSession.getId());

		executor.execute(builder.buildFunction());

		sharedData = extractSingleGenericResponseData(
				executor.execute(function), Map.class);

		assertEquals(3, sharedData.size());
		assertEquals(2,
				((List<MyCollaboratorData>) sharedData
						.get(GetAllShareInfo.SHARE_MINE)).size());
		assertEquals(0,
				((List<OtherTestRoomData>) sharedData
						.get(GetAllShareInfo.SHARE_OTHERS)).size());
		assertEquals(1,
				((List<SocialTest>) sharedData.get(GetAllShareInfo.SOCIAL))
						.size());
		assertEquals(testSession,
				((List<SocialTest>) sharedData.get(GetAllShareInfo.SOCIAL))
						.get(0).getTestSession());
	}

	@Test
	public void testGetAllShared() {
		TestSession testSession = generateTestSession();
		Session sharedUser = userManager.createActivatedAnalystAndLogin();

		RequestFunction function = new RequestBuilder(
				Managers.TEST_SESSION_MANAGER, GetAllShared.METHOD)
				.addArg(SESSION_TOKEN, sharedUser.getSessionToken())
				.addArg(USER, activeSession.getUser().getId())
				.addArg(PROJECT, userManager.getProject().getId())
				.buildFunction();

		TestUtils.assertErrorResponse(executor.execute(function),
				Errors.NO_SHARED_DATA);

		sharedTestRepo.save(new SharedTest(activeSession.getUser(), userManager
				.getProject(), sharedUser.getUser().getEmail()));

		TestSession result = (TestSession) extractSingleGenericResponseData(
				executor.execute(function), List.class).get(0);

		assertEquals(testSession, result);
	}

	@Test
	public void testRemoveEmail() {

		List<String> emails = asList(makeUniqueId(), makeUniqueId(),
				makeUniqueId());

		// share:
		RequestFunction function = new RequestBuilder(SHARING_MANAGER,
				AddEmail.METHOD)
				.addArg(SESSION_TOKEN, activeSession.getSessionToken())
				.addArg(EMAIL, emails).buildFunction();

		assertOkMessageResponse(executor.execute(function));
		assertEquals(emails.size(), sharedTestRepo.count());

		function = new RequestBuilder(SHARING_MANAGER, RemoveEmail.METHOD)
				.addArg(SESSION_TOKEN, activeSession.getSessionToken())
				.addArg(EMAIL, emails).buildFunction();

		assertOkMessageResponse(executor.execute(function));
		assertEquals(0, sharedTestRepo.count());
	}

	@Test
	public void testLeaveTests() {
		Session shareUserSession = userManager.createActivatedAnalystAndLogin();

		// share with 2 random users
		List<String> emails = Arrays.asList(shareUserSession.getUser()
				.getEmail(), makeUniqueId());

		RequestFunction function = new RequestBuilder(SHARING_MANAGER,
				AddEmail.METHOD)
				.addArg(SESSION_TOKEN, activeSession.getSessionToken())
				.addArg(EMAIL, emails).buildFunction();

		assertOkMessageResponse(executor.execute(function));

		assertEquals(emails.size(), sharedTestRepo.count());

		function = new RequestBuilder(SHARING_MANAGER, LeaveTests.METHOD)
				.addArg(SESSION_TOKEN, shareUserSession.getSessionToken())
				.addArg(USER, activeSession.getUser().getId())
				.addArg(PROJECT, activeSession.getProject().getId())
				.buildFunction();

		assertOkMessageResponse(executor.execute(function));
		assertEquals(1, sharedTestRepo.count());
		Assert.assertFalse(sharedTestRepo.findAll().iterator().next()
				.getEmail().equals(shareUserSession.getUser().getEmail()));
	}

	@Test
	public void testCheckToken() {

		TestSession testSession = generateTestSession();

		RequestFunction function = new RequestBuilder(SHARING_MANAGER,
				CheckToken.METHOD)
				.addArg(SESSION_TOKEN, activeSession.getSessionToken())
				.addArg(ID, testSession.getId()).buildFunction();

		assertOkMessageResponse(executor.execute(function));

		SocialTest socialTest = socialTestRepo
				.save(new SocialTest(testSession));

		String token = extractSingleGenericResponseData(
				executor.execute(function), String.class);
		assertEquals(socialTest.getToken(), token);
	}

	@Test
	public void testGenerateToken() {

		TestSession testSession = generateTestSession();

		RequestFunction function = new RequestBuilder(SHARING_MANAGER,
				GenerateToken.METHOD)
				.addArg(SESSION_TOKEN, activeSession.getSessionToken())
				.addArg(ID, testSession.getId()).buildFunction();

		String token = extractSingleGenericResponseData(
				executor.execute(function), String.class);

		String token2 = extractSingleGenericResponseData(
				executor.execute(function), String.class);

		assertEquals(token, token2);

		assertEquals(1, socialTestRepo.count());
	}

	@Test
	public void testDropToken() {

		TestSession testSession = generateTestSession();
		socialTestRepo.save(new SocialTest(testSession));

		RequestFunction function = new RequestBuilder(SHARING_MANAGER,
				DropToken.METHOD)
				.addArg(SESSION_TOKEN, activeSession.getSessionToken())
				.addArg(ID, testSession.getId()).buildFunction();

		assertOkMessageResponse(executor.execute(function));

		assertEquals(0, socialTestRepo.count());
	}

	@Test
	public void testGetDetailsByToken() {

		TestSession testSession = generateTestSession();
		SocialTest socialTest = socialTestRepo
				.save(new SocialTest(testSession));

		RequestFunction function = new RequestBuilder(
				Managers.TEST_SESSION_MANAGER, GetDetailsByToken.METHOD)
				.addArg(TOKEN, socialTest.getToken()).buildFunction();

		TestSession result = extractSingleGenericResponseData(
				executor.execute(function), TestSession.class);

		assertEquals(testSession, result);

		testSessionRepo.delete(testSession);

		assertErrorResponse(executor.execute(function), Errors.TEST_NOT_FOUND);
	}

	@Test
	public void testGetDetailsForShared() {

		Session anotherUser = userManager.createActivatedAnalystAndLogin();

		// share:
		RequestBuilder builder = new RequestBuilder(SHARING_MANAGER,
				AddEmail.METHOD).addArg(SESSION_TOKEN,
				activeSession.getSessionToken()).addArg(EMAIL,
				asList(anotherUser.getUser().getEmail(), makeUniqueId()));

		assertOkMessageResponse(executor.execute(builder.buildFunction()));

		TestSession testSession = generateTestSession();

		RequestFunction function = new RequestBuilder(
				Managers.TEST_SESSION_MANAGER, GetDetails.METHOD)
				.addArg(SESSION_TOKEN, anotherUser.getSessionToken())
				.addArg(ID, testSession.getId()).buildFunction();

		TestSession result = extractSingleGenericResponseData(
				executor.execute(function), TestSession.class);

		assertEquals(testSession, result);

		sharedTestRepo.deleteByEmailAndOwnerAndProject(anotherUser.getUser()
				.getEmail(), activeSession.getUser().getId(), activeSession
				.getProject().getId());

		assertErrorResponse(executor.execute(function), TEST_NOT_FOUND);
	}

	@Test
	public void testShareByMail() {

		RequestFunction function = new RequestBuilder(SHARING_MANAGER,
				ShareByMail.METHOD)
				.addArg(SESSION_TOKEN, activeSession.getSessionToken())
				.addArg(EMAIL,
						asList(makeUniqueId("email"), makeUniqueId("email")))
				.addArg(Arguments.MESSAGE, makeUniqueId("message"))
				.addArg(Arguments.URL, makeUniqueId("url")).buildFunction();

		assertOkMessageResponse(executor.execute(function));
	}

	@Test
	public void testGetAllByDateShared() {

		Session anotherUser = userManager.createActivatedAnalystAndLogin();

		// share:
		RequestBuilder builder = new RequestBuilder(SHARING_MANAGER,
				AddEmail.METHOD).addArg(SESSION_TOKEN,
				activeSession.getSessionToken()).addArg(EMAIL,
				asList(anotherUser.getUser().getEmail(), makeUniqueId()));

		assertOkMessageResponse(executor.execute(builder.buildFunction()));

		generateTestSession();

		LocalDate date = new LocalDate();

		RequestFunction function = new RequestBuilder(
				Managers.TEST_SESSION_MANAGER, GetAllByDateShared.METHOD)
				.addArg(SESSION_TOKEN, anotherUser.getSessionToken())
				.addArg(USER, activeSession.getUser().getId())
				.addArg(PROJECT, activeSession.getProject().getId())
				.addArg(DATE_FROM, _localDateFormat.print(date))
				.addArg(DATE_TO, _localDateFormat.print(date.plusDays(1)))
				.buildFunction();

		@SuppressWarnings("unchecked")
		List<TestSession> result = extractSingleGenericResponseData(
				executor.execute(function), List.class);

		assertEquals(1, result.size());
	}

	private TestSession generateTestSession() {
		TestSession testSession = generateTestSessions(activeSession, 1).get(0);
		testSessionRepo.save(testSession);
		return testSession;
	}

}
