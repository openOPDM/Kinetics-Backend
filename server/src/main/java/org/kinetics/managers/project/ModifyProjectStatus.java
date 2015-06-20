package org.kinetics.managers.project;

import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractArgument;
import static org.kinetics.dao.authorization.PermissionsEnum.Permission.MANAGE_CUSTOMER;
import static org.kinetics.request.RequestUtils.extractProjectIdArgument;
import static org.kinetics.rest.Protocol.Arguments.DISABLE;
import static org.kinetics.rest.Protocol.Arguments.ID;

import org.kinetics.dao.project.Project;
import org.kinetics.dao.project.ProjectRepository;
import org.kinetics.dao.project.ProjectStatus;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.session.SessionRepository;
import org.kinetics.dao.user.UserRepository;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequiredArguments;
import com.lohika.server.core.validator.HasPermission;

@Component
@RequestDescriptor(target = Protocol.Managers.PROJECT_MANANGER, method = ModifyProjectStatus.METHOD)
@RequiredArguments({ DISABLE, ID })
@HasPermission(MANAGE_CUSTOMER)
public class ModifyProjectStatus extends AuthKineticsRequestStrategy {

	static final String METHOD = "modifyProjectStatus";
	@Autowired
	private ProjectRepository customerRepo;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private SessionRepository sessionRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {
		final Boolean disable = extractArgument(requestData, DISABLE,
				Boolean.class);
		final ProjectStatus status = disable ? ProjectStatus.DISABLED
				: ProjectStatus.ACTIVE;

		Project project = extractProjectIdArgument(customerRepo, requestData);
		if (status.equals(ProjectStatus.ACTIVE)) {
			ProjectStatusValidator.isNotActive(project);
		}
		project.setStatus(status);
		sessionRepo.deleteByProject(project);
		customerRepo.save(project);
		return ResponseFactory.makeSuccessResponse(requestData);
	}
}
