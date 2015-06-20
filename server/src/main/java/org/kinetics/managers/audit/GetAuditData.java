package org.kinetics.managers.audit;

import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractArgument;
import static java.util.Collections.emptyList;
import static org.kinetics.rest.Protocol.Arguments.AUDIT_DATA;
import static org.kinetics.rest.Protocol.Arguments.DATE_FROM;
import static org.kinetics.rest.Protocol.Arguments.DATE_TO;
import static org.kinetics.rest.Protocol.Arguments.TYPE;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.kinetics.dao.audit.AuditData;
import org.kinetics.dao.audit.EventService;
import org.kinetics.dao.audit.EventType;
import org.kinetics.dao.authorization.PermissionsEnum;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.testsession.TestSessionService;
import org.kinetics.managers.account.UserService;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.request.RequestDataService;
import org.kinetics.rest.Protocol.Managers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.protocol.core.response.data.GenericResponseData;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequiredArguments;
import com.lohika.server.core.validator.HasPermission;

@Component
@RequestDescriptor(target = Managers.AUDIT_MANAGER, method = GetAuditData.METHOD)
@HasPermission(PermissionsEnum.Permission.AUDIT_DATA)
@RequiredArguments({ TYPE, DATE_FROM, DATE_TO })
public class GetAuditData extends AuthKineticsRequestStrategy {

	static final String METHOD = "getAuditData";

	@Autowired
	private UserService userService;
	@Autowired
	private EventService eventService;
	@Autowired
	private RequestDataService requestDataService;
	@Autowired
	private TestSessionService testSessionService;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		final EventType type = extractArgument(requestData, TYPE,
				EventType.class);
		DateTime[] dates = requestDataService.extractDateFromTo(requestData);

		List<AuditData> auditData = buildAuditData(type, dates);

		GenericResponseData<List<AuditData>> data = new GenericResponseData<List<AuditData>>(
				AUDIT_DATA, auditData);

		return ResponseFactory.makeSuccessDataResponse(data, requestData);
	}

	private List<AuditData> buildAuditData(final EventType type,
			DateTime[] dates) {
		LocalDate from = dates[0].toLocalDate();
		LocalDate to = dates[1].toLocalDate();

		switch (type) {
		case SIGNUP:
		case CONFIRM:
		case TRY_SIGNUP:
			return eventService.findAllByTypeAndCreatedBetween(type, from, to);

		case TOTAL_USERS:
			return userService.findTotalCreatedBetween(from, to);

		case TEST_EXECUTION:
			return testSessionService.findByCreateBetween(from, to);
		}
		return emptyList();
	}

}
