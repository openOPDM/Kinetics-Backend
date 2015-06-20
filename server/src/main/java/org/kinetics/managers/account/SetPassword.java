package org.kinetics.managers.account;

import static com.lohika.server.core.Protocol.Arguments.EMAIL;
import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractStringArgument;
import static org.kinetics.rest.Protocol.Arguments.PASSWORD;
import static org.kinetics.rest.Protocol.Arguments.TOKEN;

import org.kinetics.dao.user.Confirmation;
import org.kinetics.dao.user.ConfirmationRepository;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
import org.kinetics.dao.user.UserStatus;
import org.kinetics.exception.UserNotExistException;
import org.kinetics.request.RequestUtils;
import org.kinetics.rest.Protocol.Managers;
import org.kinetics.util.secure.HashUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.server.core.exception.InvalidArgumentValue;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequestStrategy;
import com.lohika.server.core.strategy.RequiredArguments;

@Component
@RequestDescriptor(target = Managers.ACCOUNT_MANANGER, method = SetPassword.METHOD)
@RequiredArguments({ EMAIL, TOKEN, PASSWORD })
public class SetPassword implements RequestStrategy {

	static final String METHOD = "setPassword";

	private static final int TOKEN_TIMEOUT = 1000 * 60 * 15;

	@Autowired
	private ConfirmationRepository confirmRepo;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private UserService userService;

	@Override
	public ResponseContainer execute(RequestFunction requestData) {

		final String email = RequestUtils
				.extractEmailStringArgument(requestData);

		final String newPassword = extractStringArgument(requestData, PASSWORD);

		final String token = extractStringArgument(requestData, TOKEN);
		Confirmation confirmation = confirmRepo.findOneByCode(token);
		if (confirmation == null) {
			throw new InvalidArgumentValue(TOKEN);
		}
		// check expiration
		if (confirmation.getTimestamp() != null
				&& System.currentTimeMillis()
						- confirmation.getTimestamp().toDate().getTime() > TOKEN_TIMEOUT) {
			throw new InvalidArgumentValue(TOKEN);
		}

		User user = confirmation.getUser();
		if (user == null || !user.getEmail().equals(email)) {
			throw new UserNotExistException();
		}
		userService.validateStatus(user, UserStatus.ACTIVE,
				UserStatus.WAITING_PASS);

		// initial password setup
		if (UserStatus.WAITING_PASS.equals(user.getStatus())) {
			user.setStatus(UserStatus.ACTIVE);
		}

		// remove other tokens for this user
		confirmRepo.deleteByUser(user);

		// actual pass change
		user.setHashData(HashUtils.generate(newPassword));
		userRepo.save(user);

		return ResponseFactory.makeSuccessResponse(requestData);
	}
}
