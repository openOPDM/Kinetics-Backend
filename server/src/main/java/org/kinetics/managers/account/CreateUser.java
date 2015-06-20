package org.kinetics.managers.account;

import static com.lohika.server.core.Protocol.Arguments.EMAIL;
import static org.kinetics.managers.account.CreateUser.METHOD;
import static org.kinetics.rest.Protocol.Arguments.FIRST_NAME;
import static org.kinetics.rest.Protocol.Arguments.PASSWORD;
import static org.kinetics.rest.Protocol.Arguments.PROJECT;
import static org.kinetics.rest.Protocol.Arguments.SECOND_NAME;
import static org.kinetics.rest.Protocol.Managers.ACCOUNT_MANANGER;

import org.kinetics.dao.audit.EventService;
import org.kinetics.dao.audit.EventType;
import org.kinetics.dao.authorization.RoleRepository;
import org.kinetics.dao.authorization.RolesEnum;
import org.kinetics.dao.project.ProjectRepository;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequestStrategy;
import com.lohika.server.core.strategy.RequiredArguments;

@Component
@RequestDescriptor(target = ACCOUNT_MANANGER, method = METHOD)
@RequiredArguments({ PROJECT, EMAIL, FIRST_NAME, SECOND_NAME, PASSWORD })
public class CreateUser implements RequestStrategy {

	static final String METHOD = "createUser";

	@Autowired
	private UserRepository userRepo;
	@Autowired
	private ProjectRepository projectRepo;
	@Autowired
	private UserService userService;
	@Autowired
	private RoleRepository roleRepo;
	@Autowired
	private EventService eventService;

	@Override
	public ResponseContainer execute(RequestFunction requestData) {
		User user = userService.buildCompleteUserFromRequest(requestData);
		user.addRole(roleRepo.findByName(RolesEnum.PATIENT.name()));
		userRepo.save(user);
		userService.sendConfirmation(user, null);

		eventService.newEvent(EventType.SIGNUP);

		return ResponseFactory.makeSuccessResponse(requestData);
	}

}
