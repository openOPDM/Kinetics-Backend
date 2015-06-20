package org.kinetics.managers.testsession;

import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractArgument;
import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractGenericArgument;
import static org.kinetics.managers.testsession.ModifyStatus.METHOD;
import static org.kinetics.rest.Protocol.Arguments.IDS;
import static org.kinetics.rest.Protocol.Arguments.VALID;

import java.util.List;

import org.kinetics.dao.session.Session;
import org.kinetics.dao.testsession.TestSession;
import org.kinetics.dao.testsession.TestSessionRepository;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol.Managers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.server.core.exception.InvalidArgumentValue;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequiredArguments;

//TODO: is it accessible by Patient also?
@Component
@RequestDescriptor(target = Managers.TEST_SESSION_MANAGER, method = METHOD)
@RequiredArguments({ IDS, VALID })
public class ModifyStatus extends AuthKineticsRequestStrategy {

	static final String METHOD = "modifyStatus";

	@Autowired
	private TestSessionRepository testSessionRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {
		List<Integer> ids = extractGenericArgument(requestData, IDS,
				LIST_INTEGER_TYPE);
		if (ids.isEmpty()) {
			throw new InvalidArgumentValue(IDS);
		}
		final Boolean isValid = extractArgument(requestData, VALID,
				Boolean.class);

		List<TestSession> sessions = testSessionRepo.findAllByIdInAndProject(
				ids, session.getProject());
		for (TestSession testSession : sessions) {
			testSession.setIsValid(isValid);
		}
		testSessionRepo.save(sessions);

		return ResponseFactory.makeSuccessResponse(requestData);
	}

}
