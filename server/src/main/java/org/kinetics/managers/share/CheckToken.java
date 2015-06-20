package org.kinetics.managers.share;

import static org.kinetics.rest.Protocol.Arguments.ID;

import org.kinetics.dao.session.Session;
import org.kinetics.dao.share.SocialTest;
import org.kinetics.dao.share.SocialTestRepository;
import org.kinetics.dao.testsession.TestSession;
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
@RequestDescriptor(target = Managers.SHARING_MANAGER, method = CheckToken.METHOD)
@RequiredArguments(ID)
public class CheckToken extends AuthKineticsRequestStrategy {

	static final String METHOD = "checkToken";
	@Autowired
	private SocialTestRepository socialTestRepo;
	@Autowired
	private RequestDataService requestDataService;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		final TestSession testSession = requestDataService.extractTestById(
				requestData, session);

		SocialTest socialTest = socialTestRepo
				.findOneByTestSession(testSession);
		if (socialTest == null) {
			return ResponseFactory.makeSuccessResponse(requestData);
		}
		GenericResponseData<String> data = new GenericResponseData<String>(
				Arguments.TOKEN, socialTest.getToken());
		return ResponseFactory.makeSuccessDataResponse(data, requestData);
	}

}
