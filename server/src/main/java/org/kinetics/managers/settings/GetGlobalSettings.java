package org.kinetics.managers.settings;

import static org.kinetics.managers.settings.GetGlobalSettings.METHOD;

import java.util.List;

import org.kinetics.dao.session.Session;
import org.kinetics.dao.settings.Setting;
import org.kinetics.dao.settings.SettingRepository;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol.Arguments;
import org.kinetics.rest.Protocol.Managers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.protocol.core.response.data.GenericResponseData;
import com.lohika.server.core.strategy.RequestDescriptor;

@Component
@RequestDescriptor(target = Managers.TEST_CONFIG_MANAGER, method = METHOD)
public class GetGlobalSettings extends AuthKineticsRequestStrategy {

	static final String METHOD = "getGlobalSettings";

	@Autowired
	private SettingRepository settingRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {
		List<Setting> settings = (List<Setting>) settingRepo.findAll();

		GenericResponseData<List<Setting>> data = new GenericResponseData<List<Setting>>(
				Arguments.SETTINGS, settings);

		return ResponseFactory.makeSuccessDataResponse(data, requestData);
	}

}
