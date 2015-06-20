package org.kinetics.managers.extension;

import static org.kinetics.dao.authorization.PermissionsEnum.Permission.MANAGE_EXTENSIONS;
import static org.kinetics.rest.Protocol.Arguments.ENTITY;
import static org.kinetics.rest.Protocol.Arguments.LIST_DATA;
import static org.kinetics.rest.Protocol.Arguments.NAME;
import static org.kinetics.rest.Protocol.Managers.EXTENSION_MANAGER;

import java.util.List;

import org.kinetics.dao.extension.ExtensionService;
import org.kinetics.dao.session.Session;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol.Errors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.server.core.exception.RestException;
import com.lohika.server.core.helpers.RequestDataExtractHelper;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequiredArguments;
import com.lohika.server.core.validator.HasPermission;

@Component
@RequestDescriptor(target = EXTENSION_MANAGER, method = AddListExtension.METHOD)
@HasPermission(MANAGE_EXTENSIONS)
@RequiredArguments({ ENTITY, NAME, LIST_DATA })
public class AddListExtension extends AuthKineticsRequestStrategy {

	static final String METHOD = "addListExtension";

	@Autowired
	private ExtensionService extensionService;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		List<String> listItems = RequestDataExtractHelper
				.extractGenericArgument(requestData, LIST_DATA,
						LIST_STRING_TYPE);
		if (listItems.isEmpty()) {
			throw new RestException(Errors.EXTENSION_LIST_EMPTY);
		}
		extensionService.persistListExtension(requestData, listItems);

		return ResponseFactory.makeSuccessResponse(requestData);
	}

}
