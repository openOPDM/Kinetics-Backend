package org.kinetics.managers.share;

import static org.kinetics.rest.Protocol.Arguments.ID;
import static org.kinetics.rest.Protocol.Managers.SHARING_MANAGER;

import org.kinetics.dao.session.Session;
import org.kinetics.dao.share.SocialTestRepository;
import org.kinetics.dao.testsession.TestSession;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.request.RequestDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequiredArguments;

@Component
@RequestDescriptor(target = SHARING_MANAGER, method = DropToken.METHOD)
@RequiredArguments(ID)
public class DropToken extends AuthKineticsRequestStrategy {

	static final String METHOD = "dropToken";

	@Autowired
	private RequestDataService requestDataService;
	@Autowired
	private SocialTestRepository socialTestRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		final TestSession testSession = requestDataService.extractTestById(
				requestData, session);

		socialTestRepo.deleteByTestSession(testSession);

		return ResponseFactory.makeSuccessResponse(requestData);
	}

}
