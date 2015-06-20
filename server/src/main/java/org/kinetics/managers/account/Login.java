package org.kinetics.managers.account;

import static com.lohika.server.core.Protocol.Arguments.EMAIL;
import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractStringArgument;
import static org.kinetics.managers.account.Login.METHOD;
import static org.kinetics.request.RequestUtils.extractEmailStringArgument;
import static org.kinetics.request.RequestUtils.extractProjectArgument;
import static org.kinetics.rest.Protocol.Arguments.PASSWORD;
import static org.kinetics.rest.Protocol.Arguments.PROJECT;
import static org.kinetics.rest.Protocol.Managers.ACCOUNT_MANANGER;

import org.kinetics.dao.authorization.RoleRepository;
import org.kinetics.dao.authorization.RolesEnum;
import org.kinetics.dao.project.Project;
import org.kinetics.dao.project.ProjectRepository;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.session.SessionRepository;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
import org.kinetics.exception.UserNotExistException;
import org.kinetics.managers.project.ProjectStatusValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.protocol.core.response.data.GenericResponseData;
import com.lohika.server.core.Protocol;
import com.lohika.server.core.exception.InvalidArgumentValue;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequestStrategy;
import com.lohika.server.core.strategy.RequiredArguments;

@Component
@RequestDescriptor(target = ACCOUNT_MANANGER, method = METHOD)
@RequiredArguments({ EMAIL, PASSWORD })
public class Login implements RequestStrategy {

	public static final String METHOD = "login";

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private SessionRepository sessionRepo;

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
		if (user.getRoles().contains(
				roleRepo.findByName(RolesEnum.SITE_ADMIN.name()))) {

			return loginAsSiteAdmin(user, request);
		}

		return login(user, request);
	}

	private ResponseContainer login(User user, RequestFunction request) {

		if (request.getArguments().findByName(PROJECT) == null) {
			throw new InvalidArgumentValue(PROJECT);
		}
		final Project project = extractProjectArgument(projectRepo, request);

		userService
				.authenticate(user, extractStringArgument(request, PASSWORD));
		ProjectStatusValidator.isNotDisabled(project);

		return buildSessionResponse(user, project, request);
	}

	private ResponseContainer loginAsSiteAdmin(User user,
			RequestFunction request) {

		userService
				.authenticate(user, extractStringArgument(request, PASSWORD));

		return buildSessionResponse(user, null, request);
	}

	private ResponseContainer buildSessionResponse(User user,
			final Project project, RequestFunction request) {
		// create new session and return it
		Session session = sessionRepo.save(new Session(user, project));

		GenericResponseData<String> responseData = new GenericResponseData<String>(
				Protocol.Arguments.SESSION_TOKEN, session.getSessionToken());
		return ResponseFactory.makeSuccessDataResponse(responseData, request);
	}

}
