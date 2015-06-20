package org.kinetics.managers.account;

import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractArgument;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.kinetics.dao.authorization.PermissionsEnum.Permission.MANAGE_PATIENT;
import static org.kinetics.managers.account.ModifyPatientInfo.METHOD;
import static org.kinetics.request.RequestUtils.extractOptionalEmailStringArgument;
import static org.kinetics.rest.Protocol.Arguments.ID;
import static org.kinetics.rest.Protocol.Errors.EMAIL_ALREADY_EXISTS;

import org.kinetics.dao.service.AnalystPatient;
import org.kinetics.dao.service.AnalystPatientRepository;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
import org.kinetics.exception.UserNotExistException;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol.Managers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.server.core.exception.InvalidArgumentValue;
import com.lohika.server.core.exception.MissingPermissionException;
import com.lohika.server.core.exception.RestException;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequiredArguments;
import com.lohika.server.core.validator.HasPermission;

@Component
@RequestDescriptor(target = Managers.ACCOUNT_MANANGER, method = METHOD)
@RequiredArguments(ID)
@HasPermission(MANAGE_PATIENT)
public class ModifyPatientInfo extends AuthKineticsRequestStrategy {

	static final String METHOD = "modifyPatientInfo";

	@Autowired
	private UserRepository userRepo;
	@Autowired
	private AnalystPatientRepository analystPatientRepo;
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

		boolean isDirty = false;
		final String email = extractOptionalEmailStringArgument(requestData);
		if (isEmpty(email)) {
			patient.setEmail(null);
			isDirty = true;
			
		} else{
			User user = userRepo.findOneByEmail(email);
			if (user != null && !user.getId().equals(patientId)) {
				throw new RestException(EMAIL_ALREADY_EXISTS);
			}
			patient.setEmail(email);
			isDirty = true;			
		}
		if (userService.updateUserOptionalData(patient, requestData) || isDirty) {
			userRepo.save(patient);
		}
		return ResponseFactory.makeSuccessResponse(requestData);
	}

}
