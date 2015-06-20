package org.kinetics.managers.testsession;

import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractArgument;
import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractGenericArgument;
import static org.kinetics.dao.authorization.PermissionsEnum.Permission.RUN_TEST_FOR_PATIENT;
import static org.kinetics.managers.testsession.AddForPatient.METHOD;
import static org.kinetics.rest.Protocol.Arguments.ID;
import static org.kinetics.rest.Protocol.Arguments.TEST_SESSION;

import org.codehaus.jackson.type.TypeReference;
import org.kinetics.dao.service.AnalystPatient;
import org.kinetics.dao.service.AnalystPatientRepository;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.testsession.TestSession;
import org.kinetics.dao.testsession.TestSessionService;
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
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequiredArguments;
import com.lohika.server.core.validator.HasPermission;

@Component
@RequestDescriptor(target = Managers.TEST_SESSION_MANAGER, method = METHOD)
@RequiredArguments({ TEST_SESSION, ID })
@HasPermission(RUN_TEST_FOR_PATIENT)
public class AddForPatient extends AuthKineticsRequestStrategy {

	static final String METHOD = "addForPatient";

	private static final TypeReference<TestSession> TEST_TYPE = new TypeReference<TestSession>() {
	};

	@Autowired
	TestSessionService testService;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	AnalystPatientRepository analystPatientRepo;

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

		// get Test bean
		TestSession newTest = extractGenericArgument(requestData, TEST_SESSION,
				TEST_TYPE);

		// fill server provided properties
		newTest.setUser(patient);
		newTest.setProject(session.getProject());

		testService.persistTestAndExtension(newTest, session);

		return ResponseFactory.makeSuccessResponse(requestData);
	}

}
