package org.kinetics.managers.share;

import static com.lohika.server.core.Protocol.Arguments.EMAIL;
import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractGenericArgument;

import java.util.List;

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
@RequestDescriptor(target = Managers.SHARING_MANAGER, method = RemoveEmail.METHOD)
@RequiredArguments(EMAIL)
public class RemoveEmail extends AuthKineticsRequestStrategy {

	static final String METHOD = "removeEmail";

	@Autowired
	private SharedTestRepository sharedTestRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		List<String> emails = extractGenericArgument(requestData, EMAIL,
				LIST_STRING_TYPE);

		sharedTestRepo.deleteByEmailInAndOwnerAndProject(emails,
				session.getUser(), session.getProject());

		return ResponseFactory.makeSuccessResponse(requestData);
	}

}
