package org.kinetics.managers.audit;

import static com.lohika.server.core.Protocol.Arguments.SESSION_TOKEN;
import static com.lohika.server.core.test.TestUtils.extractSingleGenericResponseData;
import static org.junit.Assert.assertEquals;
import static org.kinetics.dao.audit.EventType.TOTAL_USERS;
import static org.kinetics.rest.Protocol.Arguments.DATE_FROM;
import static org.kinetics.rest.Protocol.Arguments.DATE_TO;
import static org.kinetics.rest.Protocol.Managers.AUDIT_MANAGER;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;
import org.kinetics.dao.audit.AuditData;
import org.kinetics.dao.audit.Event;
import org.kinetics.dao.audit.EventRepository;
import org.kinetics.dao.audit.EventService;
import org.kinetics.dao.audit.EventType;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.testsession.TestSession;
import org.kinetics.dao.testsession.TestSessionRepository;
import org.kinetics.rest.Protocol.Arguments;
import org.kinetics.test.AutorizedRequestTest;
import org.kinetics.test.DaoGenerator;
import org.springframework.beans.factory.annotation.Autowired;

import com.lohika.protocol.core.processor.RequestBuilder;
import com.lohika.protocol.core.request.RequestFunction;

public class TestAuditManager extends AutorizedRequestTest {

	private final static DateTimeFormatter _localDateFormat = ISODateTimeFormat
			.date();

	@Autowired
	private EventRepository eventRepo;
	@Autowired
	private EventService eventService;
	@Autowired
	private TestSessionRepository testSessionRepo;

	@Test
	public void testEventDAO() {

		DateTime dateTime = new DateTime();
		eventRepo.save(new Event(TOTAL_USERS));
		eventRepo.save(new Event(TOTAL_USERS));
		eventRepo.save(new Event(TOTAL_USERS, dateTime.minusDays(1)));
		eventRepo.save(new Event(TOTAL_USERS, dateTime.plusDays(1)));

		LocalDate date = dateTime.toLocalDate();

		List<AuditData> data = eventRepo.findByTypeAndStampBetween(TOTAL_USERS,
				date.toDateTimeAtStartOfDay().toDate(), date
						.toDateTimeAtStartOfDay().toDate());

		assertEquals(1, data.size());
		assertEquals(date, data.get(0).getDate());
		assertEquals(2, data.get(0).getTotal());
	}

	@Test
	public void testTestSessionDAO() {

		DateTime dateTime = new DateTime();

		List<TestSession> testSessions = DaoGenerator.generateTestSessions(
				activeSession, 4);
		testSessions.get(0).setCreationDate(dateTime.minusDays(1));
		testSessions.get(1).setCreationDate(dateTime.plusDays(1));
		testSessionRepo.save(testSessions);

		List<AuditData> data = testSessionRepo.findAllByCreatedBetween(
				dateTime.toDate(), dateTime.toDate());

		assertEquals(1, data.size());
		assertEquals(dateTime.toLocalDate(), data.get(0).getDate());
		assertEquals(2, data.get(0).getTotal());
	}

	@Test
	public void testGetAuditData() {

		Session adminSession = userManager.createActivatedSiteAdminAndLogin();

		LocalDate date = new LocalDate();

		RequestBuilder builder = new RequestBuilder(AUDIT_MANAGER,
				GetAuditData.METHOD)
				.addArg(SESSION_TOKEN, adminSession.getSessionToken())
				.addArg(Arguments.TYPE, EventType.SIGNUP)
				.addArg(DATE_FROM, _localDateFormat.print(date))
				.addArg(DATE_TO, _localDateFormat.print(date));

		@SuppressWarnings("unchecked")
		List<AuditData> data = extractSingleGenericResponseData(
				executor.execute(builder.buildFunction()), List.class);

		assertEquals(1, data.size());
		assertEquals(date, data.get(0).getDate());
		assertEquals(2, data.get(0).getTotal());
	}

	@Test
	public void testGetAuditEvents() {
		Session adminSession = userManager.createActivatedSiteAdminAndLogin();

		RequestFunction function = new RequestBuilder(AUDIT_MANAGER,
				GetAuditEvents.METHOD).addArg(SESSION_TOKEN,
				adminSession.getSessionToken()).buildFunction();

		@SuppressWarnings("unchecked")
		List<String> events = extractSingleGenericResponseData(
				executor.execute(function), List.class);

		assertEquals(EventType.values().length, events.size());
	}
}
