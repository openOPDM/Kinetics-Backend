package org.kinetics.managers.account;

import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.lohika.server.core.Protocol.Arguments.EMAIL;
import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractStringArgument;
import static org.kinetics.request.RequestUtils.extractEmailStringArgument;
import static org.kinetics.rest.Protocol.Arguments.PASSWORD;
import static org.kinetics.rest.Protocol.Arguments.PROJECT;

import java.util.List;

import org.kinetics.dao.authorization.RoleRepository;
import org.kinetics.dao.authorization.RolesEnum;
import org.kinetics.dao.project.Project;
import org.kinetics.dao.project.ProjectRepository;
import org.kinetics.dao.project.ProjectStatus;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
import org.kinetics.exception.UserNotExistException;
import org.kinetics.rest.Protocol.Errors;
import org.kinetics.rest.Protocol.Managers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.protocol.core.response.data.GenericResponseData;
import com.lohika.server.core.exception.RestException;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequestStrategy;
import com.lohika.server.core.strategy.RequiredArguments;

@Component
@RequestDescriptor(target = Managers.ACCOUNT_MANANGER, method = Authenticate.METHOD)
@RequiredArguments({ EMAIL, PASSWORD })
public class Authenticate implements RequestStrategy {

	static final String METHOD = "authenticate";

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private ProjectRepository projectRepo;

	@Autowired
	private RoleRepository roleRepo;

	@Autowired
	private UserService userService;

	@Override
	public ResponseContainer execute(RequestFunction request) {

		final String email = extractEmailStringArgument(request);

		User user = userRepo.findOneByEmail(email);
		if (user == null) {
			throw new UserNotExistException();
		}
		userService
				.authenticate(user, extractStringArgument(request, PASSWORD));

		if (user.getRoles().contains(
				roleRepo.findByName(RolesEnum.SITE_ADMIN.name()))) {
			return ResponseFactory.makeSuccessResponse(request);
		}

		List<Project> activeProjects = newArrayListWithCapacity(user
				.getProjects().size());
		for (Project project : user.getProjects()) {
			if (ProjectStatus.ACTIVE.equals(project.getStatus())) {
				activeProjects.add(project);
			}
		}

		if (activeProjects.isEmpty()) {
			throw new RestException(Errors.PROJECT_NOT_ASSIGNED);
		}
		GenericResponseData<List<Project>> data = new GenericResponseData<List<Project>>(
				PROJECT, activeProjects);

		return ResponseFactory.makeSuccessDataResponse(data, request);
	}

}
