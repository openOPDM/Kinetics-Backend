package org.kinetics.managers.extension;

import static com.lohika.server.core.Protocol.Arguments.SESSION_TOKEN;
import static com.lohika.server.core.test.TestUtils.NAME_PREFIX;
import static com.lohika.server.core.test.TestUtils.assertErrorResponse;
import static com.lohika.server.core.test.TestUtils.assertOkMessageResponse;
import static com.lohika.server.core.test.TestUtils.makeUniqueId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.kinetics.rest.Protocol.Arguments.ENTITY;
import static org.kinetics.rest.Protocol.Arguments.FILTERS;
import static org.kinetics.rest.Protocol.Arguments.ID;
import static org.kinetics.rest.Protocol.Arguments.IDS;
import static org.kinetics.rest.Protocol.Arguments.LIST_DATA;
import static org.kinetics.rest.Protocol.Arguments.NAME;
import static org.kinetics.rest.Protocol.Arguments.PROPERTIES;
import static org.kinetics.rest.Protocol.Arguments.TYPE;
import static org.kinetics.rest.Protocol.Managers.EXTENSION_MANAGER;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kinetics.dao.extension.ExtendedEntity;
import org.kinetics.dao.extension.ExtensionDataRepository;
import org.kinetics.dao.extension.ExtensionListNode;
import org.kinetics.dao.extension.ExtensionListNodeRepository;
import org.kinetics.dao.extension.ExtensionMetaData;
import org.kinetics.dao.extension.ExtensionMetaDataRepository;
import org.kinetics.dao.extension.ExtensionMetaFilter;
import org.kinetics.dao.extension.ExtensionProperty;
import org.kinetics.dao.extension.ExtensionType;
import org.kinetics.dao.project.ProjectRepository;
import org.kinetics.dao.session.Session;
import org.kinetics.managers.shared.UTConsts;
import org.kinetics.rest.Protocol.Errors;
import org.kinetics.test.AutorizedRequestTest;
import org.springframework.beans.factory.annotation.Autowired;

import com.lohika.protocol.core.processor.RequestBuilder;
import com.lohika.protocol.core.response.Response;
import com.lohika.server.core.Protocol;
import com.lohika.server.core.test.TestUtils;

public class TestExtensionManager extends AutorizedRequestTest {

	@Autowired
	ExtensionMetaDataRepository metaDataRepo;

	@Autowired
	ExtensionDataRepository dataRepo;

	@Autowired
	ProjectRepository customerRepo;

	@Autowired
	ExtensionListNodeRepository nodeRepo;

	private Session adminSession;

	@Before
	public void setUp() {
		adminSession = userManager.createActivatedSiteAdminAndLogin();
	}

	@Test
	public void testAdd() {
		assertEquals(0, getExtensionMetaDataList().size());

		Response response = executeAdd(adminSession);

		assertOkMessageResponse(response);

		assertEquals(1, getExtensionMetaDataList().size());
	}

	@Test
	public void testGetExtensionsByEntity() {
		testAdd();

		Response response = executor.execute(new RequestBuilder(
				EXTENSION_MANAGER, GetExtensionsByEntity.METHOD)
				.addArg(ENTITY, ExtendedEntity.TEST_SESSION)
				.addArg(SESSION_TOKEN, adminSession.getSessionToken())
				.buildFunction());

		@SuppressWarnings("unchecked")
		List<ExtensionMetaData> datas = TestUtils
				.extractSingleGenericResponseData(response, List.class);

		assertEquals(1, datas.size());
		assertEquals(ExtendedEntity.TEST_SESSION, datas.get(0).getEntity());
	}

	@Test
	public void testGetExtensionsByEntityForList() {
		testAddListExtension();

		Response response = executor.execute(new RequestBuilder(
				EXTENSION_MANAGER, GetExtensionsByEntity.METHOD)
				.addArg(ENTITY, ExtendedEntity.TEST_SESSION)
				.addArg(SESSION_TOKEN, adminSession.getSessionToken())
				.buildFunction());

		assertListExtensions(response);
	}

	@Test
	public void testGetAllExtensionsForList() {
		testAddListExtension();

		Response response = executor.execute(new RequestBuilder(
				EXTENSION_MANAGER, GetAllExtensions.METHOD).addArg(
				SESSION_TOKEN, adminSession.getSessionToken()).buildFunction());

		assertListExtensions(response);
	}

	@Test
	public void testGetAllExtensions() {
		testAdd();

		Response response = executor.execute(new RequestBuilder(
				EXTENSION_MANAGER, GetAllExtensions.METHOD).addArg(
				SESSION_TOKEN, adminSession.getSessionToken()).buildFunction());

		@SuppressWarnings("unchecked")
		List<ExtensionMetaData> datas = TestUtils
				.extractSingleGenericResponseData(response, List.class);

		assertEquals(1, datas.size());
	}

	@Test
	public void testDelete() {
		testAddListExtension();

		ExtensionMetaData data = getExtensionMetaDataList().get(0);

		long count = metaDataRepo.count();

		assertOkMessageResponse(executor.execute(new RequestBuilder(
				EXTENSION_MANAGER, DeleteExtension.METHOD)
				.addArg(SESSION_TOKEN, adminSession.getSessionToken())
				.addArg(IDS, Arrays.asList(data.getId())).buildFunction()));

		assertEquals(count - 1, metaDataRepo.count());
	}

	@Test
	public void testModifyName() {
		executeAdd(adminSession);
		executeAdd(adminSession);

		List<ExtensionMetaData> metaDatas = getExtensionMetaDataList();

		String newName = makeUniqueId();

		RequestBuilder builder = new RequestBuilder(EXTENSION_MANAGER,
				ModifyExtensionName.METHOD)
				.addArg(SESSION_TOKEN, adminSession.getSessionToken())
				.addArg(ID, metaDatas.get(0).getId()).addArg(NAME, newName);

		assertOkMessageResponse(executor.execute(builder.buildFunction()));

		assertEquals(newName, metaDataRepo.findOne(metaDatas.get(0).getId())
				.getName());

		// attempt to modify again
		assertErrorResponse(executor.execute(builder.setArg(ID,
				metaDatas.get(1).getId()).buildFunction()),
				Errors.EXTENSION_NAME_DUPLICATE);
	}

	@Test
	public void testModifyProperties() {
		testAdd();

		ExtensionMetaData data = getExtensionMetaDataList().get(0);

		List<String> emptyList = Collections.emptyList();
		RequestBuilder builder = new RequestBuilder(EXTENSION_MANAGER,
				ModifyExtensionProperties.METHOD)
				.addArg(SESSION_TOKEN, adminSession.getSessionToken())
				.addArg(PROPERTIES, emptyList).addArg(ID, data.getId());

		assertOkMessageResponse(executor.execute(builder.buildFunction()));

		assertEquals(0, data.getProperties().size());

		builder.setArg(PROPERTIES, Arrays.asList(ExtensionProperty.REQUIRED,
				ExtensionProperty.SHOW_IN_GRID));

		assertOkMessageResponse(executor.execute(builder.buildFunction()));

		assertEquals(2, data.getProperties().size());
		Assert.assertTrue(data.getProperties().contains(
				ExtensionProperty.REQUIRED));
	}

	@Test
	public void testAddListExtension() {

		RequestBuilder builder = new RequestBuilder(EXTENSION_MANAGER,
				AddListExtension.METHOD)
				.addArg(SESSION_TOKEN, adminSession.getSessionToken())
				.addArg(ENTITY, ExtendedEntity.TEST_SESSION)
				.addArg(NAME, makeUniqueId())
				.addArg(LIST_DATA, Collections.emptyList());

		TestUtils.assertErrorResponse(
				executor.execute(builder.buildFunction()),
				Errors.EXTENSION_LIST_EMPTY);

		String node1 = makeUniqueId();
		String node2 = makeUniqueId();

		assertOkMessageResponse(executor.execute(builder.setArg(LIST_DATA,
				Arrays.asList(node1, node2, "", " aa ")).buildFunction()));

		List<ExtensionMetaData> metaDatas = getExtensionMetaDataList();
		assertEquals(1, metaDatas.size());
		assertEquals(3, nodeRepo.findAllLabelsByMetaData(metaDatas.get(0))
				.size());

		for (ExtensionListNode listNode : nodeRepo.findAll()) {
			assertEquals(listNode.getLabel().trim().length(), listNode
					.getLabel().length());
		}
	}

	@Test
	public void testModifyListExtension() {
		testAddListExtension();

		ExtensionMetaData metaData = getExtensionMetaDataList().get(0);

		String newNode = makeUniqueId();

		assertOkMessageResponse(executor.execute(new RequestBuilder(
				EXTENSION_MANAGER, ModifyListExtension.METHOD)
				.addArg(SESSION_TOKEN, adminSession.getSessionToken())
				.addArg(ID, metaData.getId())
				.addArg(LIST_DATA, Arrays.asList(newNode)).buildFunction()));

		assertEquals(1, nodeRepo.findAllLabelsByMetaData(metaData).size());
		assertEquals(newNode, nodeRepo.findAllLabelsByMetaData(metaData).get(0));
	}

	private Response executeAdd(Session session) {
		return executor.execute(new RequestBuilder(EXTENSION_MANAGER,
				AddExtension.METHOD)
				.addArg(Protocol.Arguments.SESSION_TOKEN,
						session.getSessionToken())
				.addArg(ENTITY, ExtendedEntity.TEST_SESSION)
				.addArg(NAME, makeUniqueId(NAME_PREFIX))
				.addArg(TYPE, ExtensionType.TEXT)
				.addArg(PROPERTIES,
						new String[] { ExtensionProperty.REQUIRED.name() })
				.addArg(FILTERS, createDummyFilters()).buildFunction());
	}

	private static Set<ExtensionMetaFilter> createDummyFilters() {
		Set<ExtensionMetaFilter> filters = new HashSet<ExtensionMetaFilter>();
		ExtensionMetaFilter filter = new ExtensionMetaFilter(
				UTConsts.DUMMY_FILTER_NAME, UTConsts.DUMMY_FILTER_DATA);
		filters.add(filter);
		return filters;
	}

	private static void assertListExtensions(Response response) {
		@SuppressWarnings("unchecked")
		List<ExtensionMetaData> datas = TestUtils
				.extractSingleGenericResponseData(response, List.class);

		assertEquals(1, datas.size());
		ExtensionMetaData metaData = datas.get(0);
		assertEquals(ExtendedEntity.TEST_SESSION, metaData.getEntity());
		assertEquals(ExtensionType.LIST, metaData.getType());
		assertNotNull(metaData.getList());
		assertEquals(3, metaData.getList().size());
	}

	private List<ExtensionMetaData> getExtensionMetaDataList() {
		return (List<ExtensionMetaData>) metaDataRepo.findAll();
	}

}
