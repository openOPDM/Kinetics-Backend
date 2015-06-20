package org.kinetics.managers.account;

import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractGenericArgument;
import static org.kinetics.dao.authorization.PermissionsEnum.Permission.MANAGE_USER;
import static org.kinetics.managers.account.GetUserInfoById.METHOD;
import static org.kinetics.rest.Protocol.Arguments.IDS;
import static org.kinetics.rest.Protocol.Arguments.USER;

import java.util.List;

import org.kinetics.dao.session.Session;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol.Managers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.protocol.core.response.data.GenericResponseData;
import com.lohika.server.core.exception.InvalidArgumentValue;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequiredArguments;
import com.lohika.server.core.validator.HasPermission;

@Component
@RequestDescriptor(target = Managers.ACCOUNT_MANANGER, method = METHOD)
@HasPermission(MANAGE_USER)
@RequiredArguments(IDS)
public class GetUserInfoById extends AuthKineticsRequestStrategy {

	static final String METHOD = "getUserInfoById";

	@Autowired
	private UserRepository userRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {
		List<Integer> ids = extractGenericArgument(requestData, IDS,
				LIST_INTEGER_TYPE);
		if (ids.isEmpty()) {
			throw new InvalidArgumentValue(IDS);
		}
		List<User> users;
		if (session.getProject() == null) {
			// site admin case
			users = (List<User>) userRepo.findAll(ids);
		} else {
			users = userRepo
					.findAllByIdInAndProjects(ids, session.getProject());
		}
		GenericResponseData<List<User>> data = new GenericResponseData<List<User>>(
				USER, users);
		return ResponseFactory.makeSuccessDataResponse(data, requestData);
	}

}
