package org.kinetics.managers.share;

import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractArgument;
import static org.kinetics.rest.Protocol.Arguments.PROJECT;
import static org.kinetics.rest.Protocol.Arguments.USER;

import org.kinetics.dao.session.Session;
import org.kinetics.dao.share.SharedTestRepository;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol.Managers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequiredArguments;

@Component
@RequestDescriptor(target = Managers.SHARING_MANAGER, method = LeaveTests.METHOD)
@RequiredArguments({ PROJECT, USER })
public class LeaveTests extends AuthKineticsRequestStrategy {

	static final String METHOD = "leaveTests";

	@Autowired
	private SharedTestRepository sharedTestRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		Integer userId = extractArgument(requestData, USER, Integer.class);
		Integer projectId = extractArgument(requestData, PROJECT, Integer.class);

		// TODO: do we need any validation?
		sharedTestRepo.deleteByEmailAndOwnerAndProject(session.getUser()
				.getEmail(), userId, projectId);

		return ResponseFactory.makeSuccessResponse(requestData);
	}
}
