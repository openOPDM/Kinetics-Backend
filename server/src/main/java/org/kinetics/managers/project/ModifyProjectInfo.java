package org.kinetics.managers.project;

import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractStringArgument;
import static org.kinetics.managers.project.ModifyProjectInfo.METHOD;
import static org.kinetics.request.RequestUtils.extractProjectIdArgument;
import static org.kinetics.rest.Protocol.Arguments.ID;
import static org.kinetics.rest.Protocol.Arguments.PROJECT_NAME;
import static org.kinetics.rest.Protocol.Errors.PROJECT_ALREADY_EXISTS;
import static org.kinetics.rest.Protocol.Managers.PROJECT_MANANGER;

import org.kinetics.dao.authorization.PermissionsEnum;
import org.kinetics.dao.project.Project;
import org.kinetics.dao.project.ProjectRepository;
import org.kinetics.dao.session.Session;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.server.core.exception.RestException;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequiredArguments;
import com.lohika.server.core.validator.HasPermission;

@Component
@RequestDescriptor(target = PROJECT_MANANGER, method = METHOD)
@RequiredArguments({ ID, PROJECT_NAME })
@HasPermission(PermissionsEnum.Permission.MANAGE_CUSTOMER)
public class ModifyProjectInfo extends AuthKineticsRequestStrategy {

	final static String METHOD = "modifyProjectInfo";
	@Autowired
	private ProjectRepository projectRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		Project project = extractProjectIdArgument(projectRepo, requestData);
		ProjectStatusValidator.isNotDisabled(project);

		final String customerName = extractStringArgument(requestData,
				Protocol.Arguments.PROJECT_NAME);
		if (projectRepo.findOneByName(customerName) != null) {
			throw new RestException(PROJECT_ALREADY_EXISTS);
		}

		project.setName(customerName);
		projectRepo.save(project);

		return ResponseFactory.makeSuccessResponse(requestData);
	}
}
