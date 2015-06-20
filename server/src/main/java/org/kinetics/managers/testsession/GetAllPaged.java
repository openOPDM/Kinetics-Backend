package org.kinetics.managers.testsession;

import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractArgument;
import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractOptionalStringArgument;
import static org.kinetics.rest.Protocol.Arguments.PAGE;
import static org.kinetics.rest.Protocol.Arguments.SIZE;
import static org.kinetics.rest.Protocol.Arguments.TEST_SESSION;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.testsession.TestSession;
import org.kinetics.dao.testsession.TestSessionRepository;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol.Arguments;
import org.kinetics.rest.Protocol.Managers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.protocol.core.response.data.GenericResponseData;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequiredArguments;

//TODO: provide args-> page #, items per page, sort?? or use default one...
@Component
@RequestDescriptor(target = Managers.TEST_SESSION_MANAGER, method = GetAllPaged.METHOD)
@RequiredArguments({ PAGE, SIZE })
public class GetAllPaged extends AuthKineticsRequestStrategy {

	static final String METHOD = "getAllPaged";

	@Autowired
	private TestSessionRepository testSessionRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {
		final Integer page = extractArgument(requestData, PAGE, Integer.class);
		final Integer size = extractArgument(requestData, SIZE, Integer.class);

		PageRequest pageRequest = buildPageRequest(requestData, page, size);
		List<TestSession> sessions = testSessionRepo.findAll(pageRequest)
				.getContent();

		// Workaround to remove RAWDATA as it can consume a lot of space
		// transform(sessions, new ClearRawDataTransformer());
		@SuppressWarnings("unchecked")
		Collection<TestSession> clearedSessions = CollectionUtils.collect(
				sessions, ClearHeavyDataTransformer.instance());

		GenericResponseData<Collection<TestSession>> data = new GenericResponseData<Collection<TestSession>>(
				TEST_SESSION, clearedSessions);

		return ResponseFactory.makeSuccessDataResponse(data, requestData);
	}

	PageRequest buildPageRequest(RequestFunction requestData,
			final Integer page, final Integer size) {

		// TODO: add check for incoming field names, so far = we allow all ones
		String sortField = extractOptionalStringArgument(requestData,
				Arguments.SORT_FIELD);
		final String sortType = extractOptionalStringArgument(requestData,
				Arguments.SORT_ORDER);
		if (StringUtils.isEmpty(sortField)) {
			return new PageRequest(page, size);
		}
		return new PageRequest(page, size, Direction.fromString(sortType),
				sortField);
	}

}
