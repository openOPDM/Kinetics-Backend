package org.kinetics.managers.extension;

import static org.kinetics.dao.authorization.PermissionsEnum.Permission.MANAGE_EXTENSIONS;
import static org.kinetics.rest.Protocol.Arguments.ID;
import static org.kinetics.rest.Protocol.Arguments.LIST_DATA;

import java.util.ArrayList;
import java.util.List;

import org.kinetics.dao.extension.ExtensionListNode;
import org.kinetics.dao.extension.ExtensionListNodeRepository;
import org.kinetics.dao.extension.ExtensionMetaData;
import org.kinetics.dao.extension.ExtensionMetaDataRepository;
import org.kinetics.dao.extension.ExtensionType;
import org.kinetics.dao.session.Session;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol.Errors;
import org.kinetics.rest.Protocol.Managers;
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
@RequestDescriptor(target = Managers.EXTENSION_MANAGER, method = ModifyListExtension.METHOD)
@HasPermission(MANAGE_EXTENSIONS)
@RequiredArguments({ ID, LIST_DATA })
public class ModifyListExtension extends AuthKineticsRequestStrategy {

	static final String METHOD = "modifyListExtension";

	@Autowired
	private ExtensionMetaDataRepository metaDataRepo;

	@Autowired
	private ExtensionListNodeRepository nodeRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		ExtensionMetaData metaData = ExtensionUtils.extractMetaData(
				metaDataRepo, requestData);

		if (!ExtensionType.LIST.equals(metaData.getType())) {
			throw new RestException(null);
		}
		List<String> listItems = RequestDataExtractHelper
				.extractGenericArgument(requestData, LIST_DATA,
						LIST_STRING_TYPE);
		if (listItems.isEmpty()) {
			throw new RestException(Errors.EXTENSION_LIST_EMPTY);
		}
		// clean old values
		nodeRepo.deleteByMetaData(metaData);

		List<ExtensionListNode> nodes = new ArrayList<ExtensionListNode>(
				listItems.size());
		for (String item : listItems) {
			nodes.add(new ExtensionListNode(item, metaData));
		}
		nodeRepo.save(nodes);

		return ResponseFactory.makeSuccessResponse(requestData);
	}

}
