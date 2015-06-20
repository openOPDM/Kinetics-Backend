package org.kinetics.managers.extension;

import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractArgument;
import static org.kinetics.rest.Protocol.Arguments.ID;

import java.util.EnumSet;

import org.codehaus.jackson.type.TypeReference;
import org.kinetics.dao.extension.ExtensionMetaData;
import org.kinetics.dao.extension.ExtensionMetaDataRepository;
import org.kinetics.dao.extension.ExtensionProperty;

import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.server.core.exception.InvalidArgumentValue;

public final class ExtensionUtils {

	public static final TypeReference<EnumSet<ExtensionProperty>> PROP_ENUMSET_TYPE = new TypeReference<EnumSet<ExtensionProperty>>() {
	};

	private ExtensionUtils() {
	}

	static ExtensionMetaData extractMetaData(ExtensionMetaDataRepository repo,
			RequestFunction function) {
		final Integer id = extractArgument(function, ID, Integer.class);

		ExtensionMetaData metaData = repo.findOne(id);
		if (metaData == null) {
			throw new InvalidArgumentValue(ID);
		}
		return metaData;
	}

}
