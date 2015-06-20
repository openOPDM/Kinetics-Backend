package org.kinetics.managers.project;

import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractStringArgument;
import static org.kinetics.managers.project.CreateProject.METHOD;
import static org.kinetics.rest.Protocol.Arguments.PROJECT_NAME;
import static org.kinetics.rest.Protocol.Errors.PROJECT_ALREADY_EXISTS;
import static org.kinetics.rest.Protocol.Managers.PROJECT_MANANGER;

import org.kinetics.dao.authorization.PermissionsEnum;
import org.kinetics.dao.project.Project;
import org.kinetics.dao.project.ProjectRepository;
import org.kinetics.dao.session.Session;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol.Arguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.protocol.core.response.data.GenericResponseData;
import com.lohika.server.core.exception.RestException;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequiredArguments;
import com.lohika.server.core.validator.HasPermission;

@Component
@RequestDescriptor(target = PROJECT_MANANGER, method = METHOD)
@RequiredArguments(PROJECT_NAME)
@HasPermission(PermissionsEnum.Permission.MANAGE_CUSTOMER)
public class CreateProject extends AuthKineticsRequestStrategy {

	final static String METHOD = "createProject";

	@Autowired
	private ProjectRepository projectRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		final String customerName = extractStringArgument(requestData,
				PROJECT_NAME);

		if (projectRepo.findOneByName(customerName) != null) {
			throw new RestException(PROJECT_ALREADY_EXISTS);
		}
		Project project = projectRepo.save(new Project(customerName));

		GenericResponseData<Integer> data = new GenericResponseData<Integer>(
				Arguments.ID, project.getId());

		return ResponseFactory.makeSuccessDataResponse(data, requestData);
	}

}
