package org.kinetics.managers.account;

import static com.lohika.server.core.Protocol.Arguments.EMAIL;
import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractOptionalStringArgument;
import static org.kinetics.request.RequestUtils.extractEmailStringArgument;
import static org.kinetics.rest.Protocol.Arguments.TOKEN;

import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
import org.kinetics.dao.user.UserStatus;
import org.kinetics.exception.UserNotExistException;
import org.kinetics.rest.Protocol.Managers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequestStrategy;
import com.lohika.server.core.strategy.RequiredArguments;

@Component
@RequestDescriptor(target = Managers.ACCOUNT_MANANGER, method = ResendConfirmation.METHOD)
@RequiredArguments(EMAIL)
public class ResendConfirmation implements RequestStrategy {

	static final String METHOD = "resendConfirmation";

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private UserService userService;

	@Override
	public ResponseContainer execute(RequestFunction requestData) {

		final String email = extractEmailStringArgument(requestData);

		User user = userRepo.findOneByEmail(email);
		if (user == null) {
			throw new UserNotExistException();
		}
		userService.validateStatus(user, UserStatus.WAITING_CONFIRMATION);
		userService.sendConfirmation(user,
				extractOptionalStringArgument(requestData, TOKEN));

		return ResponseFactory.makeSuccessResponse(requestData);
	}

}
