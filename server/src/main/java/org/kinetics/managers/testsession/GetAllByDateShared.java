package org.kinetics.managers.testsession;

import static org.apache.commons.collections.CollectionUtils.transform;
import static org.kinetics.rest.Protocol.Arguments.DATE_FROM;
import static org.kinetics.rest.Protocol.Arguments.DATE_TO;
import static org.kinetics.rest.Protocol.Arguments.PROJECT;
import static org.kinetics.rest.Protocol.Arguments.USER;

import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;
import org.kinetics.dao.project.Project;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.share.SharedTestRepository;
import org.kinetics.dao.testsession.TestSession;
import org.kinetics.dao.testsession.TestSessionRepository;
import org.kinetics.dao.user.User;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.request.RequestDataService;
import org.kinetics.rest.Protocol.Arguments;
import org.kinetics.rest.Protocol.Errors;
import org.kinetics.rest.Protocol.Managers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.protocol.core.response.data.GenericResponseData;
import com.lohika.server.core.exception.RestException;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequiredArguments;

@Component
@RequestDescriptor(target = Managers.TEST_SESSION_MANAGER, method = GetAllByDateShared.METHOD)
@RequiredArguments({ DATE_FROM, DATE_TO, USER, PROJECT })
public class GetAllByDateShared extends AuthKineticsRequestStrategy {

	public static final String METHOD = "getAllByDateShared";

	@Autowired
	private TestSessionRepository testSessionRepo;
	@Autowired
	private RequestDataService requestDataService;
	@Autowired
	private SharedTestRepository sharedTestRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		final DateTime[] dateTimes = requestDataService
				.extractDateFromTo(requestData);

		User owner = requestDataService.extractUser(requestData, USER);
		Project project = requestDataService.extractProject(requestData,
				PROJECT);

		if (sharedTestRepo.findOneByEmailAndOwnerAndProject(session.getUser()
				.getEmail(), owner.getId(), project.getId()) == null) {
			throw new RestException(Errors.NO_SHARED_DATA);
		}

		List<TestSession> testSessions = testSessionRepo
				.findAllByProjectAndUserAndCreationDateBetween(project, owner,
						dateTimes[0], dateTimes[1]);

		// Workaround to remove RAWDATA as it can consume a lot of space
		transform((Collection<TestSession>) testSessions,
				ClearHeavyDataTransformer.instance());

		GenericResponseData<List<TestSession>> data = new GenericResponseData<List<TestSession>>(
				Arguments.TEST_SESSION, testSessions);

		return ResponseFactory.makeSuccessDataResponse(data, requestData);
	}
}
