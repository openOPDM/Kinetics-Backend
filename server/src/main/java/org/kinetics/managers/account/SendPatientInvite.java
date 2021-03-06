package org.kinetics.managers.account;

import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractArgument;
import static org.kinetics.dao.authorization.PermissionsEnum.Permission.MANAGE_PATIENT;
import static org.kinetics.managers.account.SendPatientInvite.METHOD;
import static org.kinetics.rest.Protocol.Arguments.ID;

import org.kinetics.dao.service.AnalystPatient;
import org.kinetics.dao.service.AnalystPatientRepository;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
import org.kinetics.exception.UserNotExistException;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.server.core.exception.InvalidArgumentValue;
import com.lohika.server.core.exception.MissingPermissionException;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequiredArguments;
import com.lohika.server.core.validator.HasPermission;

@Component
@RequestDescriptor(target = Protocol.Managers.ACCOUNT_MANANGER, method = METHOD)
@RequiredArguments(ID)
@HasPermission(MANAGE_PATIENT)
public class SendPatientInvite extends AuthKineticsRequestStrategy {

	static final String METHOD = "sendPatientInvite";

	@Autowired
	AnalystPatientRepository analystPatientRepo;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private UserService userService;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		final Integer patientId = extractArgument(requestData, ID,
				Integer.class);
		if (patientId == null) {
			throw new InvalidArgumentValue(ID);
		}
		User patient = userRepo.findOne(patientId);
		if (patient == null) {
			throw new UserNotExistException();
		}
		AnalystPatient analystPatient = analystPatientRepo
				.findByAnalystAndPatientAndProject(session.getUser(), patient,
						session.getProject());
		if (analystPatient == null) {
			throw new MissingPermissionException();
		}
		userService.sendPatientInvite(patient);

		return ResponseFactory.makeSuccessResponse(requestData);
	}
}
