package org.kinetics.managers.extension;

import static org.kinetics.dao.authorization.PermissionsEnum.Permission.MANAGE_EXTENSIONS;
import static org.kinetics.rest.Protocol.Arguments.IDS;
import static org.kinetics.rest.Protocol.Managers.EXTENSION_MANAGER;

import java.util.List;

import org.kinetics.dao.extension.ExtensionMetaDataRepository;
import org.kinetics.dao.session.Session;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.server.core.exception.InvalidArgumentValue;
import com.lohika.server.core.helpers.RequestDataExtractHelper;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequiredArguments;
import com.lohika.server.core.validator.HasPermission;

@Component
@RequestDescriptor(target = EXTENSION_MANAGER, method = DeleteExtension.METHOD)
@HasPermission(MANAGE_EXTENSIONS)
@RequiredArguments(IDS)
public class DeleteExtension extends AuthKineticsRequestStrategy {

	static final String METHOD = "deleteExtension";
	@Autowired
	private ExtensionMetaDataRepository metaDataRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		List<Integer> ids = RequestDataExtractHelper.extractGenericArgument(
				requestData, IDS, LIST_INTEGER_TYPE);
		if (ids.isEmpty()) {
			throw new InvalidArgumentValue(IDS);
		}

		metaDataRepo.deleteById(ids);

		return ResponseFactory.makeSuccessResponse(requestData);
	}

}
