package org.kinetics.managers.project;

import static org.kinetics.request.RequestUtils.extractProjectIdArgument;
import static org.kinetics.rest.Protocol.Arguments.ID;
import static org.kinetics.rest.Protocol.Managers.PROJECT_MANANGER;

import org.kinetics.dao.project.Project;
import org.kinetics.dao.project.ProjectRepository;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.session.SessionRepository;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequiredArguments;

@Component
@RequestDescriptor(target = PROJECT_MANANGER, method = SwitchProject.METHOD)
@RequiredArguments(ID)
public class SwitchProject extends AuthKineticsRequestStrategy {

	static final String METHOD = "switchProject";

	@Autowired
	private ProjectRepository projectRepo;

	@Autowired
	private SessionRepository sessionRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		Project newProject = extractProjectIdArgument(projectRepo, requestData);

		if (!session.getProject().equals(newProject)) {
			session.setProject(newProject);
			sessionRepo.save(session);
		}
		return ResponseFactory.makeSuccessResponse(requestData);
	}

}
