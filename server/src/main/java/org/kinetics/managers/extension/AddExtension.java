package org.kinetics.managers.extension;

import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractArgument;
import static org.kinetics.dao.authorization.PermissionsEnum.Permission.MANAGE_EXTENSIONS;
import static org.kinetics.rest.Protocol.Arguments.ENTITY;
import static org.kinetics.rest.Protocol.Arguments.NAME;
import static org.kinetics.rest.Protocol.Arguments.TYPE;

import org.kinetics.dao.extension.ExtensionService;
import org.kinetics.dao.extension.ExtensionType;
import org.kinetics.dao.session.Session;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol.Managers;
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
@RequestDescriptor(target = Managers.EXTENSION_MANAGER, method = AddExtension.METHOD)
@HasPermission(MANAGE_EXTENSIONS)
@RequiredArguments({ ENTITY, TYPE, NAME })
public class AddExtension extends AuthKineticsRequestStrategy {

	static final String METHOD = "addExtension";

	@Autowired
	private ExtensionService extensionService;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		ExtensionType type = extractArgument(requestData, TYPE,
				ExtensionType.class);

		if (ExtensionType.LIST.equals(type)) {
			throw new InvalidArgumentValue(TYPE);
		}
		extensionService.persistExtension(requestData, type);

		return ResponseFactory.makeSuccessResponse(requestData);
	}
}
