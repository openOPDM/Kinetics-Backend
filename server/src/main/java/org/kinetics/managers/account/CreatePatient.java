package org.kinetics.managers.account;

import static org.kinetics.dao.authorization.PermissionsEnum.Permission.MANAGE_PATIENT;
import static org.kinetics.managers.account.CreatePatient.METHOD;
import static org.kinetics.request.RequestUtils.extractOptionalEmailStringArgument;
import static org.kinetics.rest.Protocol.Arguments.FIRST_NAME;
import static org.kinetics.rest.Protocol.Arguments.SECOND_NAME;
import static org.kinetics.rest.Protocol.Errors.USER_ALREADY_EXISTS;
import static org.kinetics.rest.Protocol.Managers.ACCOUNT_MANANGER;

import org.kinetics.dao.authorization.RoleRepository;
import org.kinetics.dao.authorization.RolesEnum;
import org.kinetics.dao.service.AnalystPatient;
import org.kinetics.dao.service.AnalystPatientRepository;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
import org.kinetics.dao.user.UserStatus;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol.Errors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.server.core.exception.RestException;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequiredArguments;
import com.lohika.server.core.validator.HasPermission;

@Component
@RequestDescriptor(target = ACCOUNT_MANANGER, method = METHOD)
@RequiredArguments({ FIRST_NAME, SECOND_NAME })
@HasPermission(MANAGE_PATIENT)
public class CreatePatient extends AuthKineticsRequestStrategy {

	static final String METHOD = "createPatient";

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private AnalystPatientRepository analystPatientRepo;

	@Autowired
	private RoleRepository roleRepo;

	@Autowired
	private UserService userService;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		String email = extractOptionalEmailStringArgument(requestData);
		if (email != null && userRepo.findOneByEmail(email) != null) {
			addExistingPatient(session, email);

		} else {
			createNewPatient(requestData, session, email);
		}

		return ResponseFactory.makeSuccessResponse(requestData);
	}

	private void addExistingPatient(Session session, String email) {
		User patient = userRepo.findOneByEmail(email);

		// check for analyst-patient connection
		if (analystPatientRepo.findByAnalystAndPatientAndProject(
				session.getUser(), patient, session.getProject()) != null) {
			throw new RestException(USER_ALREADY_EXISTS);
		}
		// check for PATIENT role
		if (!patient.getRoles().contains(
				roleRepo.findByName(RolesEnum.PATIENT.name()))) {
			throw new RestException(Errors.SITE_ADMIN_DELETE);
		}
		analystPatientRepo.save(new AnalystPatient(session.getUser(), patient,
				session.getProject()));

		patient.addProject(session.getProject());
		userRepo.save(patient);
	}

	private void createNewPatient(RequestFunction requestData, Session session,
			String email) {
		User patient = userService.buildUserFromRequest(requestData);

		patient.setEmail(email);
		patient.addRole(roleRepo.findByName(RolesEnum.PATIENT.name()));
		patient.addProject(session.getProject());
		patient.setStatus(UserStatus.WAITING_PASS);
		userRepo.save(patient);

		analystPatientRepo.save(new AnalystPatient(session.getUser(), patient,
				session.getProject()));

		if (email != null) {
			userService.sendPatientInvite(patient);
		}
	}

}
