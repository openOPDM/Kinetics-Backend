package org.kinetics.managers.testsession;

import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractArgument;
import static org.apache.commons.collections.CollectionUtils.transform;
import static org.kinetics.rest.Protocol.Arguments.PROJECT;
import static org.kinetics.rest.Protocol.Arguments.USER;

import java.util.Collection;
import java.util.List;

import org.kinetics.dao.session.Session;
import org.kinetics.dao.share.SharedTestRepository;
import org.kinetics.dao.testsession.TestSession;
import org.kinetics.dao.testsession.TestSessionRepository;
import org.kinetics.request.AuthKineticsRequestStrategy;
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
@RequestDescriptor(target = Managers.TEST_SESSION_MANAGER, method = GetAllShared.METHOD)
@RequiredArguments({ PROJECT, USER })
public class GetAllShared extends AuthKineticsRequestStrategy {

	public static final String METHOD = "getAllShared";
	@Autowired
	private TestSessionRepository testSessionRepo;
	@Autowired
	private SharedTestRepository sharedTestRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		final Integer ownerId = extractArgument(requestData, USER,
				Integer.class);
		final Integer projectId = extractArgument(requestData, PROJECT,
				Integer.class);

		if (sharedTestRepo.findOneByEmailAndOwnerAndProject(session.getUser()
				.getEmail(), ownerId, projectId) == null) {
			throw new RestException(Errors.NO_SHARED_DATA);
		}
		List<TestSession> testSessions = testSessionRepo
				.findAllByUserIdAndProjectId(ownerId, projectId);
		// Workaround to remove RAWDATA as it can consume a lot of space
		transform((Collection<TestSession>) testSessions,
				ClearHeavyDataTransformer.instance());

		GenericResponseData<List<TestSession>> data = new GenericResponseData<List<TestSession>>(
				Arguments.TEST_SESSION, testSessions);

		return ResponseFactory.makeSuccessDataResponse(data, requestData);
	}
}
