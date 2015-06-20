package org.kinetics.managers.testsession;

import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractStringArgument;
import static org.kinetics.rest.Protocol.Arguments.TEST_SESSION;
import static org.kinetics.rest.Protocol.Arguments.TOKEN;
import static org.kinetics.rest.Protocol.Managers.TEST_SESSION_MANAGER;

import org.kinetics.dao.share.SocialTest;
import org.kinetics.dao.share.SocialTestRepository;
import org.kinetics.dao.testsession.TestSession;
import org.kinetics.exception.TestSessionNotFoundException;
import org.kinetics.request.RequestDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.protocol.core.response.data.GenericResponseData;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequestStrategy;
import com.lohika.server.core.strategy.RequiredArguments;

@Component
@RequestDescriptor(target = TEST_SESSION_MANAGER, method = GetDetailsByToken.METHOD)
@RequiredArguments(TOKEN)
public class GetDetailsByToken implements RequestStrategy {

	public static final String METHOD = "getDetailsByToken";

	@Autowired
	private RequestDataService requestDataService;
	@Autowired
	private SocialTestRepository socialTestRepo;

	@Override
	public ResponseContainer execute(RequestFunction requestData) {

		final String token = extractStringArgument(requestData, TOKEN);

		SocialTest socialTest = socialTestRepo.findOneByToken(token);
		if (socialTest == null) {
			throw new TestSessionNotFoundException();
		}
		GenericResponseData<TestSession> data = new GenericResponseData<TestSession>(
				TEST_SESSION, socialTest.getTestSession());

		return ResponseFactory.makeSuccessDataResponse(data, requestData);
	}

}
