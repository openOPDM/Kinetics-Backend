package org.kinetics.managers.settings;

import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractGenericArgument;
import static org.kinetics.rest.Protocol.Arguments.SETTINGS;

import java.util.List;

import org.codehaus.jackson.type.TypeReference;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.settings.Setting;
import org.kinetics.dao.settings.SettingRepository;
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

@Component
@RequestDescriptor(target = Managers.TEST_CONFIG_MANAGER, method = ModifyGlobalSettings.METHOD)
@RequiredArguments(SETTINGS)
public class ModifyGlobalSettings extends AuthKineticsRequestStrategy {

	private static final TypeReference<List<Setting>> LIST_SETTING_TYPE = new TypeReference<List<Setting>>() {
	};

	static final String METHOD = "modifyGlobalSettings";

	@Autowired
	private SettingRepository settingRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {
		List<Setting> settings = extractGenericArgument(requestData, SETTINGS,
				LIST_SETTING_TYPE);
		if (settings.isEmpty()) {
			throw new InvalidArgumentValue(SETTINGS);
		}
		settingRepo.save(settings);

		return ResponseFactory.makeSuccessResponse(requestData);
	}

}
