package org.kinetics.managers.testsession;

import static org.apache.commons.collections.CollectionUtils.transform;
import static org.kinetics.rest.Protocol.Arguments.TEST_SESSION;

import java.util.Collection;
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
import com.lohika.protocol.core.response.data.GenericResponseData;
import com.lohika.server.core.strategy.RequestDescriptor;

@Component
@RequestDescriptor(target = Managers.TEST_SESSION_MANAGER, method = GetAll.METHOD)
public class GetAll extends AuthKineticsRequestStrategy {

	static final String METHOD = "getAll";

	@Autowired
	private TestSessionRepository testRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {
		List<TestSession> tests = testRepo.findAllByUserAndProject(
				session.getUser(), session.getProject());

		// Workaround to remove RAWDATA as it can consume a lot of space
		transform((Collection<TestSession>) tests,
				ClearHeavyDataTransformer.instance());

		GenericResponseData<List<TestSession>> data = new GenericResponseData<List<TestSession>>(
				TEST_SESSION, tests);
		return ResponseFactory.makeSuccessDataResponse(data, requestData);
	}

}
