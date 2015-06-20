package org.kinetics.dao;

import static org.kinetics.dao.DaoConsts.ANALYST_MAIL;
import static org.kinetics.dao.DaoConsts.ANALYST_NAME;
import static org.kinetics.dao.DaoConsts.EXTENSION_LIST;
import static org.kinetics.dao.DaoConsts.EXTENSION_PRIORITY_LIST;
import static org.kinetics.dao.DaoConsts.PROJECT_1;
import static org.kinetics.dao.DaoConsts.PROJECT_2;
import static org.kinetics.dao.DaoConsts.SITE_ADMIN_MAIL;
import static org.kinetics.dao.DaoConsts.SITE_ADMIN_NAME;
import static org.kinetics.dao.DaoConsts.WEB_CLIENT_MAIL;
import static org.kinetics.dao.DaoConsts.WEB_CLIENT_NAME;
import static org.kinetics.dao.DaoConsts.iOS_CLIENT_MAIL;
import static org.kinetics.dao.DaoConsts.iOS_CLIENT_NAME;
import static org.kinetics.dao.extension.ExtendedEntity.TEST_SESSION;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.kinetics.dao.authorization.Role;
import org.kinetics.dao.authorization.RoleRepository;
import org.kinetics.dao.authorization.RolesEnum;
import org.kinetics.dao.extension.ExtensionListNode;
import org.kinetics.dao.extension.ExtensionListNodeRepository;
import org.kinetics.dao.extension.ExtensionMetaData;
import org.kinetics.dao.extension.ExtensionMetaDataRepository;
import org.kinetics.dao.extension.ExtensionProperty;
import org.kinetics.dao.extension.ExtensionType;
import org.kinetics.dao.project.Project;
import org.kinetics.dao.project.ProjectRepository;
import org.kinetics.dao.service.AnalystPatient;
import org.kinetics.dao.service.AnalystPatientRepository;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
import org.kinetics.dao.user.UserStatus;
import org.kinetics.util.secure.HashUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class DefaultDataBuilder {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(DefaultDataBuilder.class);

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private RoleRepository roleRepo;

	@Autowired
	private AnalystPatientRepository analystPatientRepo;

	@Autowired
	private ProjectRepository projectRepo;

	@Autowired
	private ExtensionMetaDataRepository metaDataRepo;

	@Autowired
	private ExtensionListNodeRepository listNodeRepo;

	public DefaultDataBuilder() {
	}

	@PostConstruct
	private void buildDataSets() {
		try {
			Properties properties = new Properties();
			InputStream in = getClass().getResourceAsStream("/site.properties");
			properties.load(in);
			in.close();

			// roles needs to be built in any way
			buildDefaultRoles();
			buildSiteAdminAccount();

			if (properties.getProperty("site.defaultData").equals("true")) {
				LOGGER.info("Deploying default data...");

				// TODO: drop later
				buildFakeUsersForClients();

				// TODO: drop later
				buildExampleExtensions();

				// TODO: drop later
				buildDataForSeleniumTests();
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to load site.properties file", e);
		}
	}

	private void buildDataForSeleniumTests() {

		if (projectRepo.findOneByName(DaoConsts.QA_PROJECT) != null) {
			return;
		}
		Project testProject = new Project(DaoConsts.QA_PROJECT);
		projectRepo.save(testProject);

		List<User> users = Lists.newArrayList();

		Role patientRole = roleRepo.findByName(RolesEnum.PATIENT.name());

		User user = new User("patient-test@example.com", "", "",
				HashUtils.generate("patient"));
		user.addRole(patientRole);
		users.add(user);

		user = new User("analyst-test@example.com", "", "",
				HashUtils.generate("123"));
		user.addRole(roleRepo.findByName(RolesEnum.ANALYST.name()));
		users.add(user);

		user = new User("active_inactive@example.com", "", "",
				HashUtils.generate("123"));
		user.addRole(patientRole);
		users.add(user);

		user = new User("sp@example.com", "", "", HashUtils.generate("123"));
		user.addRole(patientRole);
		users.add(user);

		user = new User("uset1@example.com", "user1", "",
				HashUtils.generate("123"));
		user.addRole(patientRole);
		users.add(user);

		user = new User("patientCoupleSites@example.com", "", "",
				HashUtils.generate("123"));
		user.addRole(patientRole);
		users.add(user);

		for (User currentUser : users) {
			currentUser.setStatus(UserStatus.ACTIVE);
			currentUser.addProject(testProject);
		}
		userRepo.save(users);
	}

	private void buildExampleExtensions() {
		LOGGER.info("Deploying default extensions...");

		// check existence
		if (metaDataRepo.findOneByNameAndEntity(EXTENSION_LIST, TEST_SESSION) == null) {
			// build list extension
			ExtensionMetaData metaData = new ExtensionMetaData(TEST_SESSION,
					EXTENSION_LIST, ExtensionType.LIST);
			metaDataRepo.save(metaData);

			listNodeRepo.save(Arrays.asList(new ExtensionListNode(
					"San Francisco", metaData), new ExtensionListNode(
					"Vancouver", metaData), new ExtensionListNode("New York",
					metaData)));
		}

		if (metaDataRepo.findOneByNameAndEntity(DaoConsts.EXTENSION_VISIT,
				TEST_SESSION) == null) {
			// build numeric extension
			metaDataRepo.save(new ExtensionMetaData(TEST_SESSION,
					DaoConsts.EXTENSION_VISIT, ExtensionType.NUMERIC));
		}
		// check existence
		if (metaDataRepo.findOneByNameAndEntity(EXTENSION_PRIORITY_LIST,
				TEST_SESSION) == null) {

			// build priority list extension
			ExtensionMetaData metaData = new ExtensionMetaData(TEST_SESSION,
					EXTENSION_PRIORITY_LIST, ExtensionType.LIST);
			metaDataRepo.save(metaData);

			listNodeRepo.save(Arrays.asList(new ExtensionListNode("Highest",
					metaData), new ExtensionListNode("High", metaData),
					new ExtensionListNode("Normal", metaData),
					new ExtensionListNode("low", metaData),
					new ExtensionListNode("Lowest", metaData)));
		}

		if (metaDataRepo.findOneByNameAndEntity(
				DaoConsts.EXTENSION_REQUIRED_NUMBER, TEST_SESSION) == null) {
			// build required numeric extension
			ExtensionMetaData numMetaData = new ExtensionMetaData(TEST_SESSION,
					DaoConsts.EXTENSION_REQUIRED_NUMBER, ExtensionType.NUMERIC);
			numMetaData.setProperties(EnumSet.of(ExtensionProperty.REQUIRED,
					ExtensionProperty.SHOW_IN_GRID));
			metaDataRepo.save(numMetaData);
		}

		if (metaDataRepo.findOneByNameAndEntity(DaoConsts.EXTENSION_TEXT,
				TEST_SESSION) == null) {
			// build text extension
			metaDataRepo.save(new ExtensionMetaData(TEST_SESSION,
					DaoConsts.EXTENSION_TEXT, ExtensionType.TEXT));
		}
	}

	private void buildSiteAdminAccount() {
		LOGGER.info("Deploying default site admin account...");
		buildUserAccount(null, SITE_ADMIN_MAIL, SITE_ADMIN_NAME,
				roleRepo.findByName(RolesEnum.SITE_ADMIN.name()));
	}

	private void buildFakeUsersForClients() {
		LOGGER.info("Deploying default users for clients...");
		Project avanirProject = buildProject(PROJECT_1);
		Project kineticsProject = buildProject(PROJECT_2);

		buildUserAccount(avanirProject, WEB_CLIENT_MAIL, WEB_CLIENT_NAME,
				roleRepo.findByName(RolesEnum.PATIENT.name()));
		buildUserAccount(kineticsProject, iOS_CLIENT_MAIL, iOS_CLIENT_NAME,
				roleRepo.findByName(RolesEnum.PATIENT.name()));
		buildUserAccount(avanirProject, ANALYST_MAIL, ANALYST_NAME,
				roleRepo.findByName(RolesEnum.ANALYST.name()));

		// add 2nd project to analyst
		User analyst = userRepo.findOneByEmail(ANALYST_MAIL);
		if (!analyst.getProjects().contains(kineticsProject)) {
			analyst.addProject(kineticsProject);
			userRepo.save(analyst);
		}

		buildAnalystPatientAssociation(avanirProject, ANALYST_MAIL,
				WEB_CLIENT_MAIL);
		buildAnalystPatientAssociation(kineticsProject, ANALYST_MAIL,
				iOS_CLIENT_MAIL);
	}

	private Project buildProject(String projectName) {
		Project customer = projectRepo.findOneByName(projectName);
		if (customer == null) {
			return projectRepo.save(new Project(projectName));
		}
		return customer;
	}

	private void buildUserAccount(Project project, String email, String name,
			Role role) {
		User newUser = userRepo.findOneByEmail(email);
		if (newUser != null) {
			return;
		}
		newUser = new User(email, name, name, HashUtils.generate(name));
		newUser.setStatus(UserStatus.ACTIVE);
		newUser.addRole(role);
		if (project != null) {
			newUser.addProject(project);
		}
		userRepo.save(newUser);
	}

	private void buildAnalystPatientAssociation(Project project,
			String analystEmail, String... patientEmails) {
		User analyst = userRepo.findOneByEmail(analystEmail);
		List<AnalystPatient> analystPatients = new ArrayList<AnalystPatient>();
		for (String patientEmail : patientEmails) {
			User patient = userRepo.findOneByEmail(patientEmail);
			if (analystPatientRepo.findByAnalystAndPatientAndProject(analyst,
					patient, project) == null) {
				analystPatients.add(new AnalystPatient(analyst, patient,
						project));
			}
		}
		analystPatientRepo.save(analystPatients);
	}

	private void buildDefaultRoles() {
		LOGGER.info("Deploying default roles...");
		for (RolesEnum role : RolesEnum.values()) {
			buildRole(role.name());
		}
	}

	private void buildRole(String name) {
		Role role = roleRepo.findByName(name);
		if (role == null) {
			role = new Role(name);
			roleRepo.save(role);
		}
	}

}
