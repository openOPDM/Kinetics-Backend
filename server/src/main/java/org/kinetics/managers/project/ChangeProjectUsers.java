package org.kinetics.managers.project;

import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractGenericArgument;
import static org.kinetics.request.RequestUtils.extractProjectArgument;
import static org.kinetics.rest.Protocol.Arguments.IDS;
import static org.kinetics.rest.Protocol.Arguments.PROJECT;

import java.util.Collections;
import java.util.List;

import org.kinetics.dao.authorization.PermissionsEnum;
import org.kinetics.dao.project.Project;
import org.kinetics.dao.project.ProjectRepository;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
import org.kinetics.managers.account.UserService;
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
@RequestDescriptor(target = Managers.PROJECT_MANANGER, method = ChangeProjectUsers.METHOD)
@RequiredArguments({ PROJECT, IDS })
@HasPermission(PermissionsEnum.Permission.MANAGE_SITE_ADMIN)
public class ChangeProjectUsers extends AuthKineticsRequestStrategy {

	static final String METHOD = "changeProjectUsers";

	@Autowired
	private ProjectRepository projectRepo;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private UserService userService;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		Project project = extractProjectArgument(projectRepo, requestData);
		List<Integer> userIds = extractGenericArgument(requestData, IDS,
				LIST_INTEGER_TYPE);
		if (userIds.isEmpty()) {
			List<User> empty = Collections.emptyList();
			userService.changeProjectUsers(project, empty);
		} else {
			List<User> newUsers = userRepo.findAllByIdIn(userIds);
			userService.changeProjectUsers(project, newUsers);
		}
		return ResponseFactory.makeSuccessResponse(requestData);
	}
}
