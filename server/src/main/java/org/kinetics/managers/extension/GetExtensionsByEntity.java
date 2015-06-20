package org.kinetics.managers.extension;

import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractArgument;
import static org.kinetics.dao.authorization.PermissionsEnum.Permission.GET_EXTENSIONS;
import static org.kinetics.rest.Protocol.Arguments.ENTITY;
import static org.kinetics.rest.Protocol.Managers.EXTENSION_MANAGER;

import java.util.List;

import org.kinetics.dao.extension.ExtendedEntity;
import org.kinetics.dao.extension.ExtensionMetaData;
import org.kinetics.dao.extension.ExtensionMetaDataRepository;
import org.kinetics.dao.extension.ExtensionService;
import org.kinetics.dao.session.Session;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol.Arguments;
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
@RequestDescriptor(target = EXTENSION_MANAGER, method = GetExtensionsByEntity.METHOD)
@RequiredArguments(ENTITY)
@HasPermission(GET_EXTENSIONS)
public class GetExtensionsByEntity extends AuthKineticsRequestStrategy {

	static final String METHOD = "getExtensionsByEntity";

	@Autowired
	private ExtensionMetaDataRepository metaDataRepo;

	@Autowired
	private ExtensionService extensionService;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {
		final ExtendedEntity entity = extractArgument(requestData, ENTITY,
				ExtendedEntity.class);

		List<ExtensionMetaData> metaDatas = metaDataRepo
				.findAllByEntity(entity);

		extensionService.populateListLabels(metaDatas);

		GenericResponseData<List<ExtensionMetaData>> data = new GenericResponseData<List<ExtensionMetaData>>(
				Arguments.EXTENSION, metaDatas);

		return ResponseFactory.makeSuccessDataResponse(data, requestData);
	}

}
