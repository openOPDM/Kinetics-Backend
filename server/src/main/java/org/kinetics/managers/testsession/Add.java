package org.kinetics.managers.testsession;

import static org.kinetics.managers.testsession.Add.METHOD;
import static org.kinetics.rest.Protocol.Arguments.ID;
import static org.kinetics.rest.Protocol.Arguments.TEST_SESSION;

import org.kinetics.dao.session.Session;
import org.kinetics.dao.testsession.TestSession;
import org.kinetics.dao.testsession.TestSessionService;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol.Managers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.protocol.core.response.data.GenericResponseData;
import com.lohika.server.core.helpers.RequestDataExtractHelper;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequiredArguments;

@Component
@RequestDescriptor(target = Managers.TEST_SESSION_MANAGER, method = METHOD)
@RequiredArguments(TEST_SESSION)
public class Add extends AuthKineticsRequestStrategy {

	static final String METHOD = "add";

	@Autowired
	TestSessionService testService;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		TestSession newTest = RequestDataExtractHelper.extractArgument(
				requestData, TEST_SESSION, TestSession.class);

		// fill server provided properties
		newTest.setUser(session.getUser());
		newTest.setProject(session.getProject());

		testService.persistTestAndExtension(newTest, session);

		GenericResponseData<Integer> responseData = new GenericResponseData<Integer>(
				ID, newTest.getId());
		return ResponseFactory.makeSuccessDataResponse(responseData,
				requestData);
	}

}
