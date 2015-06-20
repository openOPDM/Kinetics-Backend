package org.kinetics.managers.account;

import static org.kinetics.dao.authorization.PermissionsEnum.Permission.MANAGE_USER;
import static org.kinetics.managers.account.GetRoleList.METHOD;

import java.util.List;

import org.kinetics.dao.authorization.Role;
import org.kinetics.dao.authorization.RoleRepository;
import org.kinetics.dao.session.Session;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.protocol.core.response.data.GenericResponseData;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.validator.HasPermission;

@Component
@RequestDescriptor(target = Protocol.Managers.ACCOUNT_MANANGER, method = METHOD)
@HasPermission(MANAGE_USER)
public class GetRoleList extends AuthKineticsRequestStrategy {

	static final String METHOD = "getRoleList";
	@Autowired
	private RoleRepository roleRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		List<Role> roles = (List<Role>) roleRepo.findAll();

		GenericResponseData<List<Role>> data = new GenericResponseData<List<Role>>(
				Protocol.Arguments.ROLE, roles);

		return ResponseFactory.makeSuccessDataResponse(data, requestData);
	}
}
