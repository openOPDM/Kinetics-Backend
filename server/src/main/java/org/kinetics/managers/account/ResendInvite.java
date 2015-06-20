package org.kinetics.managers.account;

import static com.lohika.server.core.Protocol.Arguments.EMAIL;
import static org.kinetics.request.RequestUtils.extractEmailStringArgument;

import org.kinetics.dao.authorization.PermissionsEnum;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
import org.kinetics.dao.user.UserStatus;
import org.kinetics.exception.UserNotExistException;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol.Managers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequiredArguments;
import com.lohika.server.core.validator.HasPermission;

@Component
@RequestDescriptor(target = Managers.ACCOUNT_MANANGER, method = ResendInvite.METHOD)
@RequiredArguments(EMAIL)
@HasPermission(PermissionsEnum.Permission.MANAGE_USER)
public class ResendInvite extends AuthKineticsRequestStrategy {

	static final String METHOD = "resendInvite";

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private UserService userService;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		final String email = extractEmailStringArgument(requestData);

		User user = userRepo.findOneByEmail(email);
		if (user == null) {
			throw new UserNotExistException();
		}
		userService.validateStatus(user, UserStatus.WAITING_PASS);
		userService.sendUserInvite(user);
		return ResponseFactory.makeSuccessResponse(requestData);
	}

}
