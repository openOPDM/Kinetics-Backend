package org.kinetics.managers.account;

import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractGenericArgument;
import static org.kinetics.dao.authorization.PermissionsEnum.Permission.MANAGE_PATIENT;
import static org.kinetics.rest.Protocol.Arguments.IDS;

import java.util.ArrayList;
import java.util.List;

import org.kinetics.dao.service.AnalystPatient;
import org.kinetics.dao.service.AnalystPatientRepository;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
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
import com.lohika.server.core.validator.HasPermission;

@Component
@RequestDescriptor(target = Managers.ACCOUNT_MANANGER, method = AssignPatient.METHOD)
@RequiredArguments(IDS)
@HasPermission(MANAGE_PATIENT)
public class AssignPatient extends AuthKineticsRequestStrategy {

	static final String METHOD = "assignPatient";

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private AnalystPatientRepository analystPatientRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		List<Integer> patientIds = extractGenericArgument(requestData, IDS,
				LIST_INTEGER_TYPE);
		if (patientIds.isEmpty()) {
			throw new InvalidArgumentValue(IDS);
		}

		User analyst = session.getUser();
		List<User> patients = userRepo.findAllByIdInAndProjects(patientIds,
				session.getProject());
		List<AnalystPatient> analystPatientList = new ArrayList<AnalystPatient>();
		for (User patient : patients) {
			analystPatientList.add(new AnalystPatient(analyst, patient, session
					.getProject()));
		}
		analystPatientRepo.save(analystPatientList);

		return ResponseFactory.makeSuccessResponse(requestData);
	}
}
