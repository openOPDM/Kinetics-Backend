package org.kinetics.managers.testsession;

import static org.apache.commons.collections.CollectionUtils.transform;
import static org.kinetics.rest.Protocol.Arguments.DATE_FROM;
import static org.kinetics.rest.Protocol.Arguments.DATE_TO;

import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.testsession.TestSession;
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

@Component
@RequestDescriptor(target = Managers.TEST_SESSION_MANAGER, method = GetAllByDate.METHOD)
@RequiredArguments({ DATE_FROM, DATE_TO })
public class GetAllByDate extends AuthKineticsRequestStrategy {

	static final String METHOD = "getAllByDate";

	@Autowired
	private TestSessionRepository testSessionRepo;
	@Autowired
	private RequestDataService requestDataService;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		final DateTime[] dateTimes = requestDataService
				.extractDateFromTo(requestData);

		List<TestSession> testSessions = testSessionRepo
				.findAllByProjectAndUserAndCreationDateBetween(
						session.getProject(), session.getUser(), dateTimes[0],
						dateTimes[1]);

		// Workaround to remove RAWDATA as it can consume a lot of space
		transform((Collection<TestSession>) testSessions,
				ClearHeavyDataTransformer.instance());

		GenericResponseData<List<TestSession>> data = new GenericResponseData<List<TestSession>>(
				Arguments.TEST_SESSION, testSessions);

		return ResponseFactory.makeSuccessDataResponse(data, requestData);
	}
}
