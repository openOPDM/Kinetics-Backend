package org.kinetics.dao.extension;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.kinetics.dao.extension.ExtendedEntity.TEST_SESSION;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.user.User;
import org.kinetics.managers.shared.UTConsts;
import org.kinetics.rest.Protocol.Errors;

import com.lohika.server.core.exception.RestException;

public class TestExtensionService {

	private ExtensionMetaDataRepository metaDataRepo;

	private ExtensionDataRepository dataRepo;

	private ExtensionListNodeRepository listNodeRepo;

	private ExtensionService extensionService;

	@Before
	public void setUp() {
		metaDataRepo = createMock(ExtensionMetaDataRepository.class);
		dataRepo = createMock(ExtensionDataRepository.class);
		listNodeRepo = createMock(ExtensionListNodeRepository.class);

		extensionService = new ExtensionService();
		extensionService.setExtensionDataRepo(dataRepo);
		extensionService.setMetaDataRepo(metaDataRepo);
		extensionService.setListNodeRepo(listNodeRepo);

		new Session(new User(null, null, null, null), null);
	}

	@Test
	public void testPersistExtensionsMatched() {

		final Integer exId = UTConsts.DUMMY_ID;
		List<ExtensionData> extensions = Arrays.asList(new ExtensionData(exId,
				null));

		expect(metaDataRepo.findOne(eq(exId))).andReturn(
				new ExtensionMetaData());

		expect(dataRepo.save(extensions)).andReturn(null);

		replay(metaDataRepo, dataRepo, listNodeRepo);

		extensionService.persistExtensions(0, extensions, TEST_SESSION);

		verify(metaDataRepo, dataRepo, listNodeRepo);
	}

	@Test
	public void testPersistExtensionsNoSuchListNode() {

		final Integer exId = UTConsts.DUMMY_ID;
		List<ExtensionData> extensions = Arrays.asList(new ExtensionData(exId,
				null));

		expect(metaDataRepo.findOne(eq(exId))).andReturn(
				new ExtensionMetaData(TEST_SESSION, null, ExtensionType.LIST));

		expect(
				listNodeRepo.findOneByLabelAndMetaData(isNull(String.class),
						anyObject(ExtensionMetaData.class))).andReturn(null);

		replay(metaDataRepo, dataRepo, listNodeRepo);

		try {

			extensionService.persistExtensions(0, extensions, TEST_SESSION);
		} catch (RestException e) {
			assertEquals(Errors.EXTENSION_WRONG_LIST_VALUE, e.getError());
		}

		verify(metaDataRepo, dataRepo, listNodeRepo);
	}

}
