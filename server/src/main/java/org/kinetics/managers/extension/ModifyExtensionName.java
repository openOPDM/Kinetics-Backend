package org.kinetics.managers.extension;

import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractStringArgument;
import static org.kinetics.dao.authorization.PermissionsEnum.Permission.MANAGE_EXTENSIONS;
import static org.kinetics.rest.Protocol.Arguments.ID;
import static org.kinetics.rest.Protocol.Arguments.NAME;
import static org.kinetics.rest.Protocol.Managers.EXTENSION_MANAGER;

import org.kinetics.dao.extension.ExtensionMetaData;
import org.kinetics.dao.extension.ExtensionMetaDataRepository;
import org.kinetics.dao.session.Session;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol.Errors;
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
@RequestDescriptor(target = EXTENSION_MANAGER, method = ModifyExtensionName.METHOD)
@RequiredArguments({ ID, NAME })
@HasPermission(MANAGE_EXTENSIONS)
public class ModifyExtensionName extends AuthKineticsRequestStrategy {

	static final String METHOD = "modifyExtensionName";

	@Autowired
	private ExtensionMetaDataRepository metaDataRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		ExtensionMetaData metaData = ExtensionUtils.extractMetaData(
				metaDataRepo, requestData);

		final String newName = extractStringArgument(requestData, NAME);

		if (metaData.getName().equals(newName)) {
			return ResponseFactory.makeSuccessResponse(requestData);
		}

		// check if such name + entity combination already used!!!
		if (metaDataRepo.findOneByNameAndEntity(newName, metaData.getEntity()) != null) {
			throw new RestException(Errors.EXTENSION_NAME_DUPLICATE);
		}
		metaData.setName(newName);
		metaDataRepo.save(metaData);

		return ResponseFactory.makeSuccessResponse(requestData);
	}

}
