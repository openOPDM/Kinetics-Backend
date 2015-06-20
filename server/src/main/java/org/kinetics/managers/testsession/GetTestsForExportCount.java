package org.kinetics.managers.testsession;

import static org.kinetics.rest.Protocol.Arguments.DATE_FROM;
import static org.kinetics.rest.Protocol.Arguments.DATE_TO;

import org.joda.time.DateTime;
import org.kinetics.dao.authorization.PermissionsEnum;
import org.kinetics.dao.project.ProjectStatus;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.testsession.TestSessionRepository;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.request.RequestDataService;
import org.kinetics.rest.Protocol.Arguments;
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
@RequestDescriptor(target = Managers.TEST_SESSION_MANAGER, method = GetTestsForExportCount.METHOD)
@RequiredArguments({ DATE_FROM, DATE_TO })
@HasPermission(PermissionsEnum.Permission.EXPORT_TEST_DATA)
public class GetTestsForExportCount extends AuthKineticsRequestStrategy {

	static final String KB_TEST_PREFIX = "kb|";

	static final String METHOD = "getTestsForExportCount";

	@Autowired
	private TestSessionRepository testSessionRepo;
	@Autowired
	private RequestDataService requestDataService;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {
		DateTime[] dateTimes = requestDataService
				.extractDateFromTo(requestData);
		Long count = testSessionRepo
				.findAllByCreationDateBetweenAndTypeStartingWithCountAndStatus(
						dateTimes[0], dateTimes[1], KB_TEST_PREFIX,
						ProjectStatus.ACTIVE);

		GenericResponseData<Integer> data = new GenericResponseData<Integer>(
				Arguments.SIZE, count.intValue());

		return ResponseFactory.makeSuccessDataResponse(data, requestData);
	}
}
