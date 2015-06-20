package org.kinetics.managers.account;

import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractStringArgument;
import static org.kinetics.rest.Protocol.Arguments.NEW_PASSWORD;
import static org.kinetics.rest.Protocol.Arguments.PASSWORD;

import org.kinetics.dao.session.Session;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol.Errors;
import org.kinetics.rest.Protocol.Managers;
import org.kinetics.util.secure.HashData;
import org.kinetics.util.secure.HashUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.server.core.exception.RestException;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequiredArguments;

@Component
@RequestDescriptor(target = Managers.ACCOUNT_MANANGER, method = ModifyPassword.METHOD)
@RequiredArguments({ PASSWORD, NEW_PASSWORD })
public class ModifyPassword extends AuthKineticsRequestStrategy {

	static final String METHOD = "ModifyPassword";

	@Autowired
	private UserRepository userRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {
		final String pass = extractStringArgument(requestData, PASSWORD);
		final String newPass = extractStringArgument(requestData, NEW_PASSWORD);

		User currentUser = session.getUser();
		if (!HashUtils.isValid(pass, currentUser.getHashData())) {
			throw new RestException(Errors.CREDENTIALS_INVALID);
		}
		HashData hashData = HashUtils.generate(newPass);
		currentUser.setHashData(hashData);
		userRepo.save(currentUser);

		return ResponseFactory.makeSuccessResponse(requestData);
	}

}
