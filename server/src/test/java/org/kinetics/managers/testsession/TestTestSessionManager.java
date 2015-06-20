package org.kinetics.managers.testsession;

import static com.lohika.server.core.Protocol.Arguments.SESSION_TOKEN;
import static com.lohika.server.core.Protocol.Errors.USER_PERMISSION_INVALID;
import static com.lohika.server.core.test.TestUtils.NAME_PREFIX;
import static com.lohika.server.core.test.TestUtils.assertErrorResponse;
import static com.lohika.server.core.test.TestUtils.assertOkMessageResponse;
import static com.lohika.server.core.test.TestUtils.extractSingleGenericResponseData;
import static com.lohika.server.core.test.TestUtils.makeUniqueId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.kinetics.rest.Protocol.Arguments.ID;
import static org.kinetics.rest.Protocol.Arguments.IDS;
import static org.kinetics.rest.Protocol.Arguments.TEST_SESSION;
import static org.kinetics.rest.Protocol.Managers.TEST_SESSION_MANAGER;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.kinetics.dao.extension.ExtendedEntity;
import org.kinetics.dao.extension.ExtensionData;
import org.kinetics.dao.extension.ExtensionDataRepository;
import org.kinetics.dao.extension.ExtensionMetaData;
import org.kinetics.dao.extension.ExtensionMetaDataRepository;
import org.kinetics.dao.extension.ExtensionType;
import org.kinetics.dao.service.AnalystPatient;
import org.kinetics.dao.service.AnalystPatientRepository;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.testsession.TestSession;
import org.kinetics.dao.testsession.TestSessionRepository;
import org.kinetics.managers.account.TestAccountManager;
import org.kinetics.managers.shared.UTConsts;
import org.kinetics.managers.testsession.GetTestsForExport.ExportTestSession;
import org.kinetics.rest.Protocol.Arguments;
import org.kinetics.test.AutorizedRequestTest;
import org.kinetics.test.DaoGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.lohika.protocol.core.processor.RequestBuilder;
import com.lohika.protocol.core.request.Argument;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.Response;
import com.lohika.server.core.test.TestUtils;

public class TestTestSessionManager extends AutorizedRequestTest {

	@Autowired
	private TestSessionRepository testRepo;

	@Autowired
	private AnalystPatientRepository analystPatientRepo;

	@Autowired
	private ExtensionMetaDataRepository metaDataRepo;

	@Autowired
	private ExtensionDataRepository extensionDataRepo;

	@Test
	public void testAdd() {
		RequestFunction function = new RequestBuilder(TEST_SESSION_MANAGER,
				Add.METHOD)
				.addArg(SESSION_TOKEN, activeSession.getSessionToken())
				.addArg(TEST_SESSION, generateTestSessions(1, false).get(0))
				.buildFunction();

		Response response = executor.execute(function);
		Integer testId = TestUtils.extractSingleGenericResponseData(response,
				Integer.class);
		assertNotNull(testRepo.findOne(testId));
		assertEquals(1, testRepo.count());
	}

	@Test
	@Transactional
	public void testAddWithExtension() {

		TestSession testSession = generateTestSessions(1, false).get(0);

		final String extName = makeUniqueId(NAME_PREFIX);
		final Integer exId = UTConsts.DUMMY_ID;
		// hack to resolve issue with empty metaData
		ExtensionData mock = new ExtensionData() {
			@Override
			public Integer getIdFromMetaData() {
				return exId;
			}

			@Override
			public String getNameFromMetaData() {
				return extName;
			}
		};
		mock.setValue(makeUniqueId());

		testSession.setExtension(Arrays.asList(mock));

		RequestFunction function = new RequestBuilder(TEST_SESSION_MANAGER,
				Add.METHOD)
				.addArg(SESSION_TOKEN, activeSession.getSessionToken())
				.addArg(TEST_SESSION, testSession).buildFunction();

		// add metadata
		metaDataRepo.save(new ExtensionMetaData(exId,
				ExtendedEntity.TEST_SESSION, extName, ExtensionType.TEXT));
		Integer testId = TestUtils.extractSingleGenericResponseData(
				executor.execute(function), Integer.class);
		assertNotNull(testRepo.findOne(testId));
		assertEquals(1, extensionDataRepo.count());
	}

	@Test
	public void testGetAll() throws JsonGenerationException,
			JsonMappingException, IOException {
		testAdd();
		TestSession test = testRepo.findAll().iterator().next();
		assertNotNull(test);

		RequestFunction function = new RequestBuilder(TEST_SESSION_MANAGER,
				GetAll.METHOD).addArg(SESSION_TOKEN,
				activeSession.getSessionToken()).buildFunction();

		Response response = executor.execute(function);
		@SuppressWarnings("unchecked")
		List<TestSession> tests = TestUtils.extractSingleGenericResponseData(
				response, List.class);

		assertEquals(1, tests.size());
		assertEquals(test, tests.get(0));
		// check for clean expensive data
		Assert.assertNull(test.getRawData());
	}

	@Test
	public void testGetAllByUser() {
		// create by usual DummyUser
		testAdd();

		Session analystSession = userManager.createActivatedAnalystAndLogin();

		analystPatientRepo.save(new AnalystPatient(analystSession.getUser(),
				activeSession.getUser(), activeSession.getProject()));

		RequestFunction function = new RequestBuilder(TEST_SESSION_MANAGER,
				GetAllForUser.METHOD)
				.addArg(SESSION_TOKEN, analystSession.getSessionToken())
				.addArg(Arguments.ID, activeSession.getUser().getId())
				.buildFunction();
		@SuppressWarnings("unchecked")
		List<TestSession> list = TestUtils.extractSingleGenericResponseData(
				executor.execute(function), List.class);

		assertEquals(1, list.size());
	}

	@Test
	public void testGetDetails() {
		testAdd();
		TestSession test = testRepo.findAll().iterator().next();
		assertNotNull(test);

		// #1 case - direct ownership
		RequestBuilder builder = new RequestBuilder(TEST_SESSION_MANAGER,
				GetDetails.METHOD).addArg(SESSION_TOKEN,
				activeSession.getSessionToken()).addArg(
				new Argument(Arguments.ID, test.getId()));

		Response response = executor.execute(builder.buildFunction());
		TestSession resultTest = extractSingleGenericResponseData(response,
				TestSession.class);

		assertEquals(test, resultTest);
		assertNotNull(test.getRawData());

		// #2 case - via Analyst
		Session analystSession = userManager.createActivatedAnalystAndLogin();

		analystPatientRepo.save(new AnalystPatient(analystSession.getUser(),
				activeSession.getUser(), activeSession.getProject()));

		builder.setArg(SESSION_TOKEN, analystSession.getSessionToken());

		response = executor.execute(builder.buildFunction());
		resultTest = extractSingleGenericResponseData(response,
				TestSession.class);

		assertEquals(test, resultTest);
	}

	@Test
	public void testGetDetailsWithExtension() {

		// generate data:
		TestSession testSession = generateTestSessions(1, true).get(0);

		final String extName = makeUniqueId(NAME_PREFIX);
		buildMetaData(extName, testSession);

		RequestFunction function = new RequestBuilder(TEST_SESSION_MANAGER,
				GetDetails.METHOD)
				.addArg(SESSION_TOKEN, activeSession.getSessionToken())
				.addArg(new Argument(Arguments.ID, testSession.getId()))
				.buildFunction();

		Response response = executor.execute(function);
		TestSession resultTest = extractSingleGenericResponseData(response,
				TestSession.class);

		assertEquals(testSession, resultTest);

		assertNotNull(resultTest.getExtension());
		assertEquals(1, resultTest.getExtension().size());
		assertEquals(extName, resultTest.getExtension().get(0)
				.getNameFromMetaData());
		Assert.assertNull(resultTest.getExtension().get(0).getName());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetAllPaged() {
		generateTestSessions(10, true);

		RequestBuilder builder = new RequestBuilder(TEST_SESSION_MANAGER,
				GetAllPaged.METHOD)
				.addArg(SESSION_TOKEN, activeSession.getSessionToken())
				.addArg(Arguments.PAGE, 0).addArg(Arguments.SIZE, 3);
		Response response = executor.execute(builder.buildFunction());

		Collection<TestSession> testSessions = TestUtils
				.extractSingleGenericResponseData(response, Collection.class);

		assertEquals(3, testSessions.size());

		List<TestSession> list = new ArrayList<TestSession>(testSessions);

		Assert.assertTrue(list.get(0).getId().compareTo(list.get(1).getId()) < 0);

		response = executor.execute(builder.addArg(Arguments.SORT_FIELD, "id")
				.addArg(Arguments.SORT_ORDER, "DESC").buildFunction());

		testSessions = TestUtils.extractSingleGenericResponseData(response,
				Collection.class);
		assertEquals(3, testSessions.size());

		list = new ArrayList<TestSession>(testSessions);

		Assert.assertTrue(list.get(0).getId().compareTo(list.get(1).getId()) > 0);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDelete() {
		List<TestSession> sessions = generateTestSessions(10, true);

		buildMetaData(makeUniqueId(), sessions.get(0));

		List<Integer> ids = (List<Integer>) CollectionUtils.collect(sessions,
				new Sessions2IdTransformer(),
				new ArrayList<Integer>(sessions.size()));

		RequestFunction function = new RequestBuilder(TEST_SESSION_MANAGER,
				Delete.METHOD)
				.addArg(SESSION_TOKEN, activeSession.getSessionToken())
				.addArg(Arguments.IDS, ids).buildFunction();
		Response response = executor.execute(function);
		assertOkMessageResponse(response);

		assertEquals(0, testRepo.count());
		assertEquals(0, extensionDataRepo.count());
	}

	@Test
	public void testModifyStatus() {
		List<TestSession> sessions = generateTestSessions(2, true);

		@SuppressWarnings("unchecked")
		List<Integer> ids = (List<Integer>) CollectionUtils.collect(sessions,
				new Sessions2IdTransformer(),
				new ArrayList<Integer>(sessions.size()));

		RequestFunction function = new RequestBuilder(TEST_SESSION_MANAGER,
				ModifyStatus.METHOD)
				.addArg(SESSION_TOKEN, activeSession.getSessionToken())
				.addArg(IDS, ids).addArg(Arguments.VALID, true).buildFunction();
		assertOkMessageResponse(executor.execute(function));

		Assert.assertTrue(testRepo.findOne(ids.get(0)).getIsValid());
	}

	@Test
	public void testCascadeRemoval() {
		testAdd();

		assertOkMessageResponse(TestAccountManager.deleteUser(executor,
				activeSession));

		assertEquals(0, (int) testRepo.count());
	}

	@Test
	public void testGetAllByDate() {

		List<TestSession> testSessions = generateTestSessions(5, true);

		DateTime now = new DateTime();

		for (TestSession session : testSessions) {
			session.setCreationDate(now);
		}
		testRepo.save(testSessions);

		testSessions.get(0).setCreationDate(now.minusHours(1));
		testRepo.save(testSessions.get(0));

		RequestFunction function = new RequestBuilder(TEST_SESSION_MANAGER,
				GetAllByDate.METHOD)
				.addArg(SESSION_TOKEN, activeSession.getSessionToken())
				.addArg(Arguments.DATE_FROM, now.minusMinutes(1))
				.addArg(Arguments.DATE_TO, now.plusMinutes(1)).buildFunction();

		@SuppressWarnings("unchecked")
		List<TestSession> result = TestUtils.extractSingleGenericResponseData(
				executor.execute(function), List.class);

		assertEquals(4, result.size());
		// check for clean expensive data
		Assert.assertNull(result.get(0).getRawData());
	}

	@Test
	public void testAddForPatient() {
		Session analystSession = userManager.createActivatedAnalystAndLogin();

		RequestFunction function = new RequestBuilder(TEST_SESSION_MANAGER,
				AddForPatient.METHOD)
				.addArg(SESSION_TOKEN, analystSession.getSessionToken())
				.addArg(new Argument(Arguments.TEST_SESSION,
						generateTestSessions(1, false).get(0)))
				.addArg(ID, activeSession.getUser().getId()).buildFunction();

		assertErrorResponse(executor.execute(function), USER_PERMISSION_INVALID);

		analystPatientRepo.save(new AnalystPatient(analystSession.getUser(),
				activeSession.getUser(), analystSession.getProject()));

		assertOkMessageResponse(executor.execute(function));

		assertEquals(1, testRepo.count());
	}

	@Test
	public void testGetTestsForExport() {
		Session siteAdminSession = userManager
				.createActivatedSiteAdminAndLogin();

		List<TestSession> testSessions = generateTestSessions(5, true);

		DateTime now = new DateTime();

		for (TestSession session : testSessions) {
			session.setCreationDate(now);
		}
		testSessions.get(1).setType(GetTestsForExport.KB_TEST_PREFIX);
		testSessions.get(2).setType(GetTestsForExport.KB_TEST_PREFIX);
		testRepo.save(testSessions);

		testSessions.get(0).setCreationDate(now.minusHours(1));
		testRepo.save(testSessions.get(0));

		RequestFunction function = new RequestBuilder(TEST_SESSION_MANAGER,
				GetTestsForExportCount.METHOD)
				.addArg(SESSION_TOKEN, siteAdminSession.getSessionToken())
				.addArg(Arguments.DATE_FROM, now.minusMinutes(1))
				.addArg(Arguments.DATE_TO, now.plusMinutes(1)).buildFunction();
		assertEquals(
				2,
				(int) TestUtils.extractSingleGenericResponseData(
						executor.execute(function), Integer.class));

		function = new RequestBuilder(TEST_SESSION_MANAGER,
				GetTestsForExport.METHOD)
				.addArg(SESSION_TOKEN, siteAdminSession.getSessionToken())
				.addArg(Arguments.DATE_FROM, now.minusMinutes(1))
				.addArg(Arguments.DATE_TO, now.plusMinutes(1))
				.addArg(Arguments.PAGE, 0).addArg(Arguments.SIZE, 5)
				.buildFunction();

		@SuppressWarnings("unchecked")
		List<ExportTestSession> result = TestUtils
				.extractSingleGenericResponseData(executor.execute(function),
						List.class);

		assertEquals(2, result.size());
		assertNotNull(result.get(0).getRawData());
	}

	private List<TestSession> generateTestSessions(int size, boolean persist) {
		List<TestSession> itemsToSave = DaoGenerator.generateTestSessions(
				activeSession, size);
		if (persist) {
			testRepo.save(itemsToSave);
		}
		return itemsToSave;
	}

	private static final class Sessions2IdTransformer implements Transformer {
		@Override
		public Object transform(Object input) {
			return ((TestSession) input).getId();
		}
	}

	private void buildMetaData(final String extName, TestSession testSession) {
		ExtensionMetaData metaData = new ExtensionMetaData(
				ExtendedEntity.TEST_SESSION, extName, ExtensionType.TEXT);
		metaDataRepo.save(metaData);
		extensionDataRepo.save(new ExtensionData(metaData, testSession.getId(),
				makeUniqueId()));
	}
}
