package org.kinetics.managers.extension;

import static com.lohika.protocol.core.processor.ResponseFactory.makeSuccessDataResponse;
import static org.kinetics.dao.authorization.PermissionsEnum.Permission.GET_EXTENSIONS;
import static org.kinetics.managers.extension.GetAllExtensions.METHOD;
import static org.kinetics.rest.Protocol.Arguments.EXTENSION;

import java.util.List;

import org.kinetics.dao.extension.ExtensionMetaData;
import org.kinetics.dao.extension.ExtensionMetaDataRepository;
import org.kinetics.dao.extension.ExtensionService;
import org.kinetics.dao.session.Session;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol.Managers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.protocol.core.response.data.GenericResponseData;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.validator.HasPermission;

@Component
@RequestDescriptor(target = Managers.EXTENSION_MANAGER, method = METHOD)
@HasPermission(GET_EXTENSIONS)
public class GetAllExtensions extends AuthKineticsRequestStrategy {

	static final String METHOD = "getAllExtensions";

	@Autowired
	private ExtensionMetaDataRepository metaDataRepo;

	@Autowired
	private ExtensionService extensionService;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		List<ExtensionMetaData> metaDatas = (List<ExtensionMetaData>) metaDataRepo
				.findAll();

		extensionService.populateListLabels(metaDatas);

		GenericResponseData<List<ExtensionMetaData>> data = new GenericResponseData<List<ExtensionMetaData>>(
				EXTENSION, metaDatas);

		return makeSuccessDataResponse(data, requestData);
	}
}
