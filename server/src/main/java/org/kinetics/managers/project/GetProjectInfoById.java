package org.kinetics.managers.project;

import static org.kinetics.request.RequestUtils.extractProjectIdArgument;
import static org.kinetics.rest.Protocol.Arguments.ID;

import org.kinetics.dao.authorization.PermissionsEnum;
import org.kinetics.dao.project.Project;
import org.kinetics.dao.project.ProjectRepository;
import org.kinetics.dao.session.Session;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol.Arguments;
import org.kinetics.rest.Protocol.Managers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.protocol.core.response.data.GenericResponseData;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequiredArguments;
import com.lohika.server.core.validator.HasPermission;

@Component
@RequestDescriptor(target = Managers.PROJECT_MANANGER, method = GetProjectInfoById.METHOD)
@RequiredArguments(ID)
@HasPermission(PermissionsEnum.Permission.MANAGE_CUSTOMER)
public class GetProjectInfoById extends AuthKineticsRequestStrategy {

	static final String METHOD = "getProjectInfoById";

	@Autowired
	private ProjectRepository projectRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		Project project = extractProjectIdArgument(projectRepo, requestData);

		GenericResponseData<Project> data = new GenericResponseData<Project>(
				Arguments.PROJECT, project);

		return ResponseFactory.makeSuccessDataResponse(data, requestData);
	}
}
