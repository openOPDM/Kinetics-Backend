package org.kinetics.managers.account;

import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractArgument;
import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractGenericArgument;
import static org.kinetics.dao.authorization.PermissionsEnum.Permission.MANAGE_USER;
import static org.kinetics.rest.Protocol.Arguments.DISABLE;
import static org.kinetics.rest.Protocol.Arguments.IDS;

import java.util.List;

import org.kinetics.dao.session.Session;
import org.kinetics.dao.session.SessionRepository;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
import org.kinetics.dao.user.UserStatus;
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
import com.lohika.server.core.validator.HasPermission;

@Component
@RequestDescriptor(target = Managers.ACCOUNT_MANANGER, method = ModifyUserStatus.METHOD)
@RequiredArguments({ DISABLE, IDS })
@HasPermission(MANAGE_USER)
public class ModifyUserStatus extends AuthKineticsRequestStrategy {

	static final String METHOD = "modifyUserStatus";

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private SessionRepository sessionRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {
		final Boolean disable = extractArgument(requestData, DISABLE,
				Boolean.class);
		final UserStatus status = disable ? UserStatus.DISABLED
				: UserStatus.ACTIVE;

		List<Integer> userIds = extractGenericArgument(requestData, IDS,
				LIST_INTEGER_TYPE);
		if (userIds.isEmpty()) {
			throw new InvalidArgumentValue(IDS);
		}
		Iterable<User> users = userRepo.findAllByIdIn(userIds);
		for (User user : users) {
			user.setStatus(status);
			// invalidate user sessions
			sessionRepo.deleteByUser(user);
		}
		// TODO: consider transaction?
		userRepo.save(users);

		return ResponseFactory.makeSuccessResponse(requestData);
	}

}
