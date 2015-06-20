package org.kinetics.managers.project;

import static org.kinetics.rest.Protocol.Arguments.IDS;

import java.util.Collection;
import java.util.List;

import org.kinetics.dao.project.Project;
import org.kinetics.dao.project.ProjectRepository;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.session.SessionRepository;
import org.kinetics.dao.testsession.TestSessionRepository;
import org.kinetics.dao.user.UserRepository;
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

@Component
@RequestDescriptor(target = Managers.PROJECT_MANANGER, method = ChangeProjects.METHOD)
@RequiredArguments(IDS)
public class ChangeProjects extends AuthKineticsRequestStrategy {

	static final String METHOD = "changeProjects";

	@Autowired
	private ProjectRepository projectRepo;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private UserService userService;

	@Autowired
	private TestSessionRepository testSessionRepo;

	@Autowired
	private SessionRepository sessionRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		List<Integer> ids = RequestDataExtractHelper.extractGenericArgument(
				requestData, IDS, LIST_INTEGER_TYPE);

		Collection<Project> newProjects = projectRepo.findAllByIdIn(ids);
		if (newProjects.isEmpty()) {
			throw new InvalidArgumentValue(IDS);
		}
		userService.changeUserProjects(session.getUser(), newProjects);

		return ResponseFactory.makeSuccessResponse(requestData);
	}
}
