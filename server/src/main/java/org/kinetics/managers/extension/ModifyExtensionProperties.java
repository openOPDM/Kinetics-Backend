package org.kinetics.managers.extension;

import static org.kinetics.dao.authorization.PermissionsEnum.Permission.MANAGE_EXTENSIONS;
import static org.kinetics.managers.extension.ExtensionUtils.PROP_ENUMSET_TYPE;
import static org.kinetics.rest.Protocol.Arguments.ID;
import static org.kinetics.rest.Protocol.Arguments.PROPERTIES;
import static org.kinetics.rest.Protocol.Managers.EXTENSION_MANAGER;

import java.util.EnumSet;

import org.kinetics.dao.extension.ExtensionMetaData;
import org.kinetics.dao.extension.ExtensionMetaDataRepository;
import org.kinetics.dao.extension.ExtensionProperty;
import org.kinetics.dao.session.Session;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.server.core.helpers.RequestDataExtractHelper;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequiredArguments;
import com.lohika.server.core.validator.HasPermission;

@Component
@RequestDescriptor(target = EXTENSION_MANAGER, method = ModifyExtensionProperties.METHOD)
@HasPermission(MANAGE_EXTENSIONS)
@RequiredArguments({ ID, PROPERTIES })
public class ModifyExtensionProperties extends AuthKineticsRequestStrategy {

	static final String METHOD = "modifyExtensionProperties";

	@Autowired
	private ExtensionMetaDataRepository metaDataRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {
		ExtensionMetaData metaData = ExtensionUtils.extractMetaData(
				metaDataRepo, requestData);

		EnumSet<ExtensionProperty> newProperties = RequestDataExtractHelper
				.extractGenericArgument(requestData, PROPERTIES,
						PROP_ENUMSET_TYPE);

		metaData.setProperties(newProperties);
		metaDataRepo.save(metaData);

		return ResponseFactory.makeSuccessResponse(requestData);
	}

}
