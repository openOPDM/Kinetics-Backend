package org.kinetics.managers.account;

import static java.util.Arrays.asList;
import static org.kinetics.dao.authorization.PermissionsEnum.Permission.MANAGE_PATIENT;

import java.util.List;

import org.kinetics.dao.service.AnalystPatientRepository;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserStatus;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.protocol.core.response.data.GenericResponseData;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.validator.HasPermission;

@Component
@RequestDescriptor(target = Protocol.Managers.ACCOUNT_MANANGER, method = GetPatients.METHOD)
@HasPermission(MANAGE_PATIENT)
public class GetPatients extends AuthKineticsRequestStrategy {

	static final String METHOD = "getPatients";

	@Autowired
	private AnalystPatientRepository analystPatientRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {
		User analyst = session.getUser();

		List<User> patients = analystPatientRepo
				.findAllByStatusAndAnalystAndProject(analyst,
						session.getProject(),
						asList(UserStatus.WAITING_PASS, UserStatus.ACTIVE));

		GenericResponseData<List<User>> data = new GenericResponseData<List<User>>(
				Protocol.Arguments.USER, patients);
		return ResponseFactory.makeSuccessDataResponse(data, requestData);
	}
}
