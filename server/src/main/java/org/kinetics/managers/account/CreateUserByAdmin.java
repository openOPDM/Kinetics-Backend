package org.kinetics.managers.account;

import static com.lohika.server.core.Protocol.Arguments.EMAIL;
import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractStringArgument;
import static org.kinetics.rest.Protocol.Arguments.FIRST_NAME;
import static org.kinetics.rest.Protocol.Arguments.ROLE;
import static org.kinetics.rest.Protocol.Arguments.SECOND_NAME;

import org.kinetics.dao.authorization.PermissionsEnum;
import org.kinetics.dao.authorization.Role;
import org.kinetics.dao.authorization.RoleRepository;
import org.kinetics.dao.authorization.RolesEnum;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
import org.kinetics.dao.user.UserStatus;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol.Arguments;
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
@RequestDescriptor(target = Managers.ACCOUNT_MANANGER, method = CreateUserByAdmin.METHOD)
@RequiredArguments({ EMAIL, FIRST_NAME, SECOND_NAME, ROLE })
@HasPermission(PermissionsEnum.Permission.MANAGE_USER)
public class CreateUserByAdmin extends AuthKineticsRequestStrategy {

	static final String METHOD = "createUserByAdmin";

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private RoleRepository roleRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		User user = userService.buildCompleteUserFromRequestNoPass(requestData);
		user.setStatus(UserStatus.WAITING_PASS);

		final String roleName = extractStringArgument(requestData, ROLE);
		Role role = roleRepo.findByName(roleName);
		if (role == null) {
			throw new InvalidArgumentValue(ROLE);
		}
		user.addRole(role);
		if (!RolesEnum.SITE_ADMIN.name().equals(roleName)
				&& user.getProjects().isEmpty()) {
			throw new InvalidArgumentValue(Arguments.PROJECT);
		}

		userRepo.save(user);
		userService.sendUserInvite(user);

		return ResponseFactory.makeSuccessResponse(requestData);
	}
}
