package org.kinetics.managers.settings;

import static com.lohika.server.core.Protocol.Arguments.SESSION_TOKEN;
import static com.lohika.server.core.test.TestUtils.assertOkMessageResponse;
import static com.lohika.server.core.test.TestUtils.extractSingleGenericResponseData;
import static com.lohika.server.core.test.TestUtils.makeUniqueId;
import static org.kinetics.rest.Protocol.Managers.TEST_CONFIG_MANAGER;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.kinetics.dao.settings.Setting;
import org.kinetics.dao.settings.SettingRepository;
import org.kinetics.rest.Protocol.Arguments;
import org.kinetics.test.AutorizedRequestTest;
import org.springframework.beans.factory.annotation.Autowired;

import com.lohika.protocol.core.processor.RequestBuilder;
import com.lohika.protocol.core.request.RequestFunction;

public class TestTestConfigManager extends AutorizedRequestTest {

	@Autowired
	private SettingRepository settingRepo;

	private Setting DUMMY_SETTING = new Setting("dummy", "dummyValue");

	@Test
	public void testGetGlobalSettings() {
		settingRepo.save(DUMMY_SETTING);

		RequestFunction function = new RequestBuilder(TEST_CONFIG_MANAGER,
				GetGlobalSettings.METHOD).addArg(SESSION_TOKEN,
				activeSession.getSessionToken()).buildFunction();
		@SuppressWarnings("unchecked")
		List<Setting> settings = extractSingleGenericResponseData(
				executor.execute(function), List.class);

		Assert.assertFalse(settings.isEmpty());
		for (Setting setting : settings) {
			if (setting.getName().equals(DUMMY_SETTING.getName())) {
				Assert.assertEquals(DUMMY_SETTING.getValue(),
						setting.getValue());
				return;
			}
		}
		Assert.fail("Dummy setting not found!!");
	}

	@Test
	public void testModifyGlobalSettings() {
		RequestFunction function = new RequestBuilder(TEST_CONFIG_MANAGER,
				ModifyGlobalSettings.METHOD)
				.addArg(SESSION_TOKEN, activeSession.getSessionToken())
				.addArg(Arguments.SETTINGS,
						Arrays.asList(new Setting(DUMMY_SETTING.getName(),
								makeUniqueId()))).buildFunction();

		assertOkMessageResponse(executor.execute(function));

		Assert.assertTrue(!DUMMY_SETTING.getValue().equals(
				settingRepo.findOne(DUMMY_SETTING.getName()).getValue()));
	}

}
