package org.kinetics.managers.account;

import static com.lohika.server.core.Protocol.Arguments.EMAIL;
import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractArgument;
import static org.kinetics.rest.Protocol.Arguments.FIRST_NAME;
import static org.kinetics.rest.Protocol.Arguments.PASSWORD;
import static org.kinetics.rest.Protocol.Arguments.PROJECT;
import static org.kinetics.rest.Protocol.Arguments.SECOND_NAME;
import static org.kinetics.rest.Protocol.Arguments.USER;
import static org.kinetics.rest.Protocol.Errors.NO_SHARED_DATA;

import org.kinetics.dao.authorization.RoleRepository;
import org.kinetics.dao.authorization.RolesEnum;
import org.kinetics.dao.share.SharedTestRepository;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
import org.kinetics.dao.user.UserStatus;
import org.kinetics.request.RequestDataService;
import org.kinetics.rest.Protocol.Managers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.server.core.exception.RestException;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequestStrategy;
import com.lohika.server.core.strategy.RequiredArguments;

@Component
@RequestDescriptor(target = Managers.ACCOUNT_MANANGER, method = CreateShareUser.METHOD)
@RequiredArguments({ FIRST_NAME, SECOND_NAME, EMAIL, PASSWORD, USER, PROJECT })
public class CreateShareUser implements RequestStrategy {

	static final String METHOD = "createShareUser";

	@Autowired
	private UserService userService;
	@Autowired
	private RoleRepository roleRepo;
	@Autowired
	private SharedTestRepository sharedTestRepo;
	@Autowired
	private RequestDataService requestDataService;
	@Autowired
	private UserRepository userRepo;

	@Override
	public ResponseContainer execute(RequestFunction requestData) {

		User newUser = userService.buildCompleteUserFromRequest(requestData);
		newUser.addRole(roleRepo.findByName(RolesEnum.PATIENT.name()));
		newUser.setStatus(UserStatus.ACTIVE);

		final Integer ownerId = extractArgument(requestData, USER,
				Integer.class);

		// save first, then check for test, as it can be deleted till that point
		userRepo.save(newUser);

		if (sharedTestRepo.findOneByEmailAndOwnerAndProject(newUser.getEmail(),
				ownerId, newUser.getProjects().iterator().next().getId()) == null) {
			throw new RestException(NO_SHARED_DATA);
		}
		return ResponseFactory.makeSuccessResponse(requestData);
	}

}
