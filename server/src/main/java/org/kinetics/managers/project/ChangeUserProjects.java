package org.kinetics.managers.project;

import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractGenericArgument;
import static org.kinetics.rest.Protocol.Arguments.IDS;
import static org.kinetics.rest.Protocol.Arguments.USER;

import java.util.Collection;
import java.util.List;

import org.kinetics.dao.authorization.PermissionsEnum;
import org.kinetics.dao.project.Project;
import org.kinetics.dao.project.ProjectRepository;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
import org.kinetics.exception.UserNotExistException;
import org.kinetics.managers.account.UserService;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol.Managers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.server.core.exception.InvalidArgumentValue;
import com.lohika.server.core.helpers.RequestDataExtractHelper;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequiredArguments;
import com.lohika.server.core.validator.HasPermission;

@Component
@RequestDescriptor(target = Managers.PROJECT_MANANGER, method = ChangeUserProjects.METHOD)
@RequiredArguments({ USER, IDS })
@HasPermission(PermissionsEnum.Permission.MANAGE_SITE_ADMIN)
public class ChangeUserProjects extends AuthKineticsRequestStrategy {

	static final String METHOD = "changeUserProjects";

	@Autowired
	private ProjectRepository projectRepo;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private UserService userService;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		Integer userId = RequestDataExtractHelper.extractArgument(requestData,
				USER, Integer.class);
		User user = userRepo.findOne(userId);
		if (user == null) {
			throw new UserNotExistException();
		}
		final List<Integer> ids = extractGenericArgument(requestData, IDS,
				LIST_INTEGER_TYPE);
		Collection<Project> newProjects = projectRepo.findAllByIdIn(ids);
		if (newProjects.isEmpty()) {
			throw new InvalidArgumentValue(IDS);
		}
		userService.changeUserProjects(user, newProjects);

		return ResponseFactory.makeSuccessResponse(requestData);
	}

}
