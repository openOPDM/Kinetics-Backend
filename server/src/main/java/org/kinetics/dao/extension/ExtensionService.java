package org.kinetics.dao.extension;

import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractArgument;
import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractOptionalGenericArgument;
import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractStringArgument;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.kinetics.rest.Protocol.Arguments.ENTITY;
import static org.kinetics.rest.Protocol.Arguments.NAME;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.codehaus.jackson.type.TypeReference;
import org.kinetics.managers.extension.ExtensionUtils;
import org.kinetics.rest.Protocol.Arguments;
import org.kinetics.rest.Protocol.Errors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.server.core.exception.RestException;

@Service
@Transactional(readOnly = true)
public class ExtensionService {

	static final TypeReference<Set<ExtensionMetaFilter>> FILTER_SET_TYPE = new TypeReference<Set<ExtensionMetaFilter>>() {
	};

	@Autowired
	private ExtensionMetaDataRepository metaDataRepo;

	@Autowired
	private ExtensionDataRepository extensionDataRepo;

	@Autowired
	private ExtensionListNodeRepository listNodeRepo;

	@Transactional
	public ExtensionMetaData persistExtension(RequestFunction requestData,
			ExtensionType type) {

		ExtendedEntity entity = extractArgument(requestData, ENTITY,
				ExtendedEntity.class);

		String name = extractStringArgument(requestData, NAME);
		if (metaDataRepo.findOneByNameAndEntity(name, entity) != null) {
			throw new RestException(Errors.EXTENSION_NAME_DUPLICATE);
		}

		ExtensionMetaData metaData = new ExtensionMetaData(entity, name, type);

		EnumSet<ExtensionProperty> properties = extractOptionalGenericArgument(
				requestData, Arguments.PROPERTIES,
				ExtensionUtils.PROP_ENUMSET_TYPE);

		if (properties != null) {
			metaData.setProperties(properties);
		}

		Set<ExtensionMetaFilter> filters = extractOptionalGenericArgument(
				requestData, Arguments.FILTERS, FILTER_SET_TYPE);

		if (properties != null) {
			metaData.setFilters(filters);
		}

		return metaDataRepo.save(metaData);
	}

	@Transactional
	public void persistExtensions(Integer entityId,
			Collection<ExtensionData> extensions, ExtendedEntity entity) {

		if (extensions == null || extensions.isEmpty()) {
			return;
		}

		Iterator<ExtensionData> iterator = extensions.iterator();
		while (iterator.hasNext()) {
			ExtensionData extension = iterator.next();
			ExtensionMetaData metaData = metaDataRepo.findOne(extension
					.getMetaId());

			if (metaData != null) {
				// validate list options
				if (ExtensionType.LIST == metaData.getType()) {
					if (listNodeRepo.findOneByLabelAndMetaData(
							extension.getValue(), metaData) == null) {
						throw new RestException(
								Errors.EXTENSION_WRONG_LIST_VALUE);
					}
				}
				// setup references
				extension.setMetaData(metaData);
				extension.setEntityId(entityId);
			} else {
				iterator.remove();
			}
		}

		extensionDataRepo.save(extensions);
	}

	@Transactional
	public void persistListExtension(RequestFunction requestData,
			List<String> listItems) {
		ExtensionMetaData metaData = persistExtension(requestData,
				ExtensionType.LIST);

		List<ExtensionListNode> nodes = new ArrayList<ExtensionListNode>(
				listItems.size());
		for (String item : listItems) {
			if (isEmpty(item)) {
				continue;
			}
			nodes.add(new ExtensionListNode(item.trim(), metaData));
		}
		listNodeRepo.save(nodes);
	}

	public void populateListLabels(List<ExtensionMetaData> metaDatas) {
		for (ExtensionMetaData metaData : metaDatas) {
			if (ExtensionType.LIST.equals(metaData.getType())) {
				// fetch list...
				metaData.setList(listNodeRepo.findAllLabelsByMetaData(metaData));
			}
		}
	}

	void setMetaDataRepo(ExtensionMetaDataRepository metaDataRepo) {
		this.metaDataRepo = metaDataRepo;
	}

	void setExtensionDataRepo(ExtensionDataRepository extensionDataRepo) {
		this.extensionDataRepo = extensionDataRepo;
	}

	void setListNodeRepo(ExtensionListNodeRepository listNodeRepo) {
		this.listNodeRepo = listNodeRepo;
	}

}
