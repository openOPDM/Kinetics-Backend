package org.kinetics.managers.account;

import static com.google.common.collect.Sets.newHashSet;
import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractOptionalArgument;
import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractOptionalGenericArgument;
import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractOptionalStringArgument;
import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractStringArgument;
import static org.kinetics.request.AuthKineticsRequestStrategy.LIST_INTEGER_TYPE;
import static org.kinetics.request.RequestUtils.extractEmailStringArgument;
import static org.kinetics.request.RequestUtils.extractProjectsArgument;
import static org.kinetics.rest.Protocol.Arguments.FIRST_NAME;
import static org.kinetics.rest.Protocol.Arguments.PASSWORD;
import static org.kinetics.rest.Protocol.Arguments.PROJECT;
import static org.kinetics.rest.Protocol.Arguments.SECOND_NAME;
import static org.kinetics.rest.Protocol.Arguments.TEST_SESSION;
import static org.kinetics.rest.Protocol.Errors.CREDENTIALS_INVALID;
import static org.kinetics.rest.Protocol.Errors.USER_ALREADY_EXISTS;
import static org.kinetics.rest.Protocol.Errors.USER_DISABLED;
import static org.kinetics.rest.Protocol.Errors.USER_IS_ACTIVE;
import static org.kinetics.rest.Protocol.Errors.USER_NOT_ACTIVE;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.kinetics.dao.audit.AuditData;
import org.kinetics.dao.authorization.Role;
import org.kinetics.dao.authorization.RoleRepository;
import org.kinetics.dao.authorization.RolesEnum;
import org.kinetics.dao.project.Project;
import org.kinetics.dao.project.ProjectRepository;
import org.kinetics.dao.service.AnalystPatientRepository;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.session.SessionRepository;
import org.kinetics.dao.share.SharedTestRepository;
import org.kinetics.dao.testsession.TestSession;
import org.kinetics.dao.testsession.TestSessionRepository;
import org.kinetics.dao.user.Confirmation;
import org.kinetics.dao.user.ConfirmationRepository;
import org.kinetics.dao.user.Gender;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
import org.kinetics.dao.user.UserStatus;
import org.kinetics.rest.Protocol.Arguments;
import org.kinetics.rest.Protocol.Errors;
import org.kinetics.service.RoleService;
import org.kinetics.util.ConfirmationCodeGenerator;
import org.kinetics.util.mail.MailService;
import org.kinetics.util.secure.HashUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.server.core.exception.RestException;
import com.lohika.server.core.helpers.RequestDataExtractHelper;

@Service
@Transactional(readOnly = true)
public class UserService {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(UserService.class);

	@Autowired
	private TestSessionRepository testRepo;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private ProjectRepository projectRepo;
	@Autowired
	private ConfirmationRepository confirmRepo;
	@Autowired
	private MailService mailService;
	@Autowired
	private AnalystPatientRepository analystPatientRepo;
	@Autowired
	private TestSessionRepository testSessionRepo;
	@Autowired
	private SessionRepository sessionRepo;
	@Autowired
	private RoleRepository roleRepo;
	@Autowired
	private RoleService roleService;
	@Autowired
	private SharedTestRepository sharedTestRepo;

	@Transactional
	public void createUserWithTestFromRequest(RequestFunction requestData) {
		User user = buildCompleteUserFromRequest(requestData);
		user.addRole(roleService.getPatientRole());
		userRepo.save(user);
		sendConfirmation(user, null);

		final TestSession newTest = RequestDataExtractHelper.extractArgument(
				requestData, TEST_SESSION, TestSession.class);

		newTest.setUser(user);
		// TODO: select first available project, but we should consider that to
		// do if have multiple...
		newTest.setProject(user.getProjects().iterator().next());
		testRepo.save(newTest);
	}

	/**
	 * Performs check for User existence and also adds Project entities
	 * 
	 * @param requestData
	 * @return
	 */
	User buildCompleteUserFromRequest(RequestFunction requestData) {
		checkUserExistence(requestData);
		User user = buildUserWithCredentialsFromRequest(requestData);

		user.addProjects(extractProjectsArgument(projectRepo, requestData));

		return user;
	}

	User buildCompleteUserFromRequestNoPass(RequestFunction requestData) {
		String email = checkUserExistence(requestData);

		User user = buildUserFromRequest(requestData);
		user.setEmail(email);
		// case for SiteAdmin
		List<Integer> projectIds = extractOptionalGenericArgument(requestData,
				PROJECT, LIST_INTEGER_TYPE);
		if (projectIds != null && !projectIds.isEmpty()) {
			user.addProjects(extractProjectsArgument(projectRepo, requestData));
		}
		return user;
	}

	/**
	 * Basic user information with EMAIL and PASS, no check for existence
	 * 
	 * @param requestData
	 * @return
	 */
	User buildUserWithCredentialsFromRequest(RequestFunction requestData) {
		User user = buildUserFromRequest(requestData);

		final String email = extractEmailStringArgument(requestData);
		user.setEmail(email);
		final String pass = extractStringArgument(requestData, PASSWORD);
		user.setHashData(HashUtils.generate(pass));

		return user;
	}

	/**
	 * Basic information without credentials
	 * 
	 * @param requestData
	 * @return
	 */
	User buildUserFromRequest(RequestFunction requestData) {
		final String firstName = extractStringArgument(requestData, FIRST_NAME);
		final String secondName = extractStringArgument(requestData,
				SECOND_NAME);

		User user = new User(firstName, secondName);
		user.setStatus(UserStatus.WAITING_CONFIRMATION);

		// optional fields
		String genderVal = extractOptionalStringArgument(requestData,
				Arguments.GENDER);
		if (genderVal != null) {
			Gender gender = Gender.valueOf(genderVal.toUpperCase());
			user.setGender(gender);
		}
		LocalDate birthDay = extractOptionalArgument(requestData,
				Arguments.BIRTHDAY, LocalDate.class);
		if (birthDay != null) {
			user.setBirthday(birthDay);
		}
		return user;
	}

	/**
	 * 
	 * @param user
	 * @param request
	 * @return
	 */
	boolean updateUserOptionalData(User user, RequestFunction request) {
		final String firstName = extractOptionalStringArgument(request,
				Arguments.FIRST_NAME);
		final String secondName = extractOptionalStringArgument(request,
				Arguments.SECOND_NAME);
		String genderVal = extractOptionalStringArgument(request,
				Arguments.GENDER);
		LocalDate birthDay = extractOptionalArgument(request,
				Arguments.BIRTHDAY, LocalDate.class);

		if (firstName == null && secondName == null && genderVal == null
				&& birthDay == null) {
			return false;
		}
		if (firstName != null) {
			user.setFirstName(firstName);
		}
		if (secondName != null) {
			user.setSecondName(secondName);
		}
		if (genderVal != null) {
			Gender gender = Gender.valueOf(genderVal.toUpperCase());
			user.setGender(gender);
		}
		if (birthDay != null) {
			user.setBirthday(birthDay);
		}
		return true;
	}

	void sendConfirmation(User user, String token) {
		String newCode = getNewConfirmationCode(user);
		try {
			mailService.sendConfirmationCode(user, newCode, token);
		} catch (MailException e) {
			LOGGER.error("Failed to send confirmation", e);
		}
	}

	void sendPatientInvite(User patient) {
		String newCode = getNewConfirmationCode(patient);
		try {
			mailService.sendPatientInvitation(patient, newCode);
		} catch (MailException e) {
			LOGGER.error("Failed to send patient invitation", e);
		}
	}

	void sendUserInvite(User user) {
		String newCode = getNewConfirmationCode(user);
		try {
			mailService.sendUserInvitation(user, newCode);
		} catch (MailException e) {
			LOGGER.error("Failed to send user invitation", e);
		}
	}

	@Transactional
	public void changeUserProjects(User user, Collection<Project> projects) {

		Set<Project> newProjects = newHashSet(projects);
		Set<Project> removedProjects = newHashSet(user.getProjects());
		removedProjects.removeAll(newProjects);
		if (!removedProjects.isEmpty()) {
			// If some projects are removed -> perform data clean up there
			analystPatientRepo.deleteAllByPatientAndProjects(user,
					removedProjects);
			testSessionRepo.deleteAllByUserAndProjectIn(user, removedProjects);
			sessionRepo.deleteByUserAndProjectsIn(user, removedProjects);
			sharedTestRepo.deleteByUserAndProjectsIn(user, removedProjects);
		}
		user.changeProjects(newProjects);
		userRepo.save(user);
	}

	@Transactional
	public void changeProjectUsers(Project project, Collection<User> users) {
		Set<User> newUsers = newHashSet(users);
		Set<User> currentUsers = newHashSet(userRepo.findAllByProjects(project));
		Set<User> removedUsers = newHashSet(currentUsers);
		Set<User> addedUsers = newHashSet(newUsers);

		// define delta
		removedUsers.removeAll(newUsers);
		addedUsers.removeAll(currentUsers);

		if (!removedUsers.isEmpty()) {
			analystPatientRepo.deleteAllByPatientsAndProject(removedUsers,
					project);
			testSessionRepo.deleteAllByUsersInAndProject(removedUsers, project);
			sessionRepo.deleteByUsersInAndProject(removedUsers, project);
			sharedTestRepo.deleteByUsersInAndProject(removedUsers, project);

			for (User user : removedUsers) {
				user.removeProject(project);
			}
			userRepo.save(removedUsers);
		}

		if (!addedUsers.isEmpty()) {
			for (User user : addedUsers) {
				user.addProject(project);
			}
			userRepo.save(addedUsers);
		}
	}

	public List<AuditData> findTotalCreatedBetween(LocalDate from, LocalDate to) {
		return userRepo.findTotalCreatedBetween(from.toDateTimeAtStartOfDay()
				.toDate(), to.toDateTimeAtStartOfDay().toDate());
	}

	public void authenticate(User user, String password) {

		validateStatus(user, UserStatus.ACTIVE);
		if (!HashUtils.isValid(password, user.getHashData())) {
			throw new RestException(Errors.CREDENTIALS_INVALID);
		}
	}

	public void validateStatus(User user, UserStatus validStatus) {
		if (validStatus.equals(user.getStatus())) {
			return;
		}
		assertStatus(user.getStatus());
	}

	public void validateStatus(User user, UserStatus validStatus1,
			UserStatus validStatus2) {
		if (user.getStatus().equals(validStatus1)
				|| user.getStatus().equals(validStatus2)) {
			return;
		}
		assertStatus(user.getStatus());
	}

	@Transactional
	public void deleteUser(Session session) {
		Role siteAdminRole = roleRepo.findByName(RolesEnum.SITE_ADMIN.name());
		if (session.getUser().getRoles().contains(siteAdminRole)) {
			if (0 == userRepo
					.countAllByRoleAndStatusAndExcludeId(siteAdminRole,
							UserStatus.ACTIVE, session.getUser().getId())) {
				throw new RestException(Errors.SITE_ADMIN_DELETE);
			}
		}
		userRepo.delete(session.getUser());
	}

	@Transactional
	private String getNewConfirmationCode(User user) {
		String newCode;
		do {
			newCode = ConfirmationCodeGenerator.generate();

		} while (confirmRepo.findOneByCode(newCode) != null);

		confirmRepo.save(new Confirmation(user, newCode));
		return newCode;
	}

	private String checkUserExistence(RequestFunction requestData) {
		final String email = extractEmailStringArgument(requestData);
		if (userRepo.findOneByEmail(email) != null) {
			throw new RestException(USER_ALREADY_EXISTS);
		}
		return email;
	}

	private void assertStatus(UserStatus status) {
		switch (status) {
		case ACTIVE:
			throw new RestException(USER_IS_ACTIVE);

		case WAITING_CONFIRMATION:
			throw new RestException(USER_NOT_ACTIVE);

		case WAITING_PASS:
			throw new RestException(CREDENTIALS_INVALID);

		case DISABLED:
			throw new RestException(USER_DISABLED);
		}
	}

}
