package org.kinetics.managers.project;

import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractArgument;
import static org.kinetics.managers.project.DeleteProject.METHOD;
import static org.kinetics.rest.Protocol.Arguments.ID;
import static org.kinetics.rest.Protocol.Managers.PROJECT_MANANGER;

import org.kinetics.dao.authorization.PermissionsEnum;
import org.kinetics.dao.project.ProjectRepository;
import org.kinetics.dao.session.Session;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.server.core.exception.InvalidArgumentValue;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequiredArguments;
import com.lohika.server.core.validator.HasPermission;

@Component
@RequestDescriptor(target = PROJECT_MANANGER, method = METHOD)
@RequiredArguments(ID)
@HasPermission(PermissionsEnum.Permission.MANAGE_CUSTOMER)
public class DeleteProject extends AuthKineticsRequestStrategy {

	final static String METHOD = "deleteProject";
	@Autowired
	private ProjectRepository customerRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		final Integer customerId = extractArgument(requestData, ID,
				Integer.class);
		if (customerId == null) {
			throw new InvalidArgumentValue(ID);
		}
		customerRepo.delete(customerId);
		return ResponseFactory.makeSuccessResponse(requestData);
	}
}
