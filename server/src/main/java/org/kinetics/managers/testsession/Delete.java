package org.kinetics.managers.testsession;

import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractGenericArgument;
import static org.kinetics.rest.Protocol.Arguments.IDS;

import java.util.List;

import org.kinetics.dao.extension.ExtendedEntity;
import org.kinetics.dao.extension.ExtensionDataRepository;
import org.kinetics.dao.session.Session;
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

@Component
@RequestDescriptor(target = Managers.TEST_SESSION_MANAGER, method = Delete.METHOD)
@RequiredArguments(IDS)
public class Delete extends AuthKineticsRequestStrategy {

	static final String METHOD = "delete";

	@Autowired
	private TestSessionRepository testSessionRepo;

	@Autowired
	private ExtensionDataRepository extensionDataRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {
		List<Integer> ids = extractGenericArgument(requestData, IDS,
				LIST_STRING_TYPE);
		if (ids.isEmpty()) {
			throw new InvalidArgumentValue(IDS);
		}
		testSessionRepo.deleteAllByIdsAndProject(ids, session.getProject());
		// cleanup extension data
		extensionDataRepo.deleteByEntities(ExtendedEntity.TEST_SESSION, ids);

		return ResponseFactory.makeSuccessResponse(requestData);
	}

}
