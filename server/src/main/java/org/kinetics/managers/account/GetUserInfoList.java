package org.kinetics.managers.account;

import static org.kinetics.dao.authorization.PermissionsEnum.Permission.MANAGE_USER;
import static org.kinetics.dao.user.UserStatus.WAITING_CONFIRMATION;
import static org.kinetics.managers.account.GetUserInfoList.METHOD;

import java.util.List;

import org.kinetics.dao.authorization.RoleRepository;
import org.kinetics.dao.authorization.RolesEnum;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol.Arguments;
import org.kinetics.rest.Protocol.Managers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.protocol.core.response.data.GenericResponseData;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.validator.HasPermission;

@Component
@RequestDescriptor(target = Managers.ACCOUNT_MANANGER, method = METHOD)
@HasPermission(MANAGE_USER)
public class GetUserInfoList extends AuthKineticsRequestStrategy {

	static final String METHOD = "getUserInfoList";
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private RoleRepository roleRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		List<User> users;

		if (session.getUser().getRoles()
				.contains(roleRepo.findByName(RolesEnum.SITE_ADMIN.name()))) {
			users = userRepo.findAllExcludeIdAndStatus(session.getUser()
					.getId(), WAITING_CONFIRMATION);
		} else {
			users = userRepo.findAllExcludeIdAndStatusAndProject(session
					.getUser().getId(), WAITING_CONFIRMATION, session
					.getProject());
		}

		GenericResponseData<List<User>> data = new GenericResponseData<List<User>>(
				Arguments.USER, users);

		return ResponseFactory.makeSuccessDataResponse(data, requestData);
	}

}
