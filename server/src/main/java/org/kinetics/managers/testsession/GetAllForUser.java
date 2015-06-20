package org.kinetics.managers.testsession;

import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractArgument;
import static org.apache.commons.collections.CollectionUtils.transform;
import static org.kinetics.dao.authorization.PermissionsEnum.Permission.GET_USER_DATA;
import static org.kinetics.rest.Protocol.Arguments.ID;

import java.util.Collection;
import java.util.List;

import org.kinetics.dao.service.AnalystPatientRepository;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.testsession.TestSession;
import org.kinetics.dao.testsession.TestSessionRepository;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
import org.kinetics.exception.UserNotExistException;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol.Arguments;
import org.kinetics.rest.Protocol.Managers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.protocol.core.response.data.GenericResponseData;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequiredArguments;
import com.lohika.server.core.validator.HasPermission;

@Component
@HasPermission(GET_USER_DATA)
@RequestDescriptor(target = Managers.TEST_SESSION_MANAGER, method = GetAllForUser.METHOD)
@RequiredArguments(ID)
public class GetAllForUser extends AuthKineticsRequestStrategy {

	static final String METHOD = "getAllForUser";

	@Autowired
	private TestSessionRepository testSessionRepo;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private AnalystPatientRepository analystPatientRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {
		final Integer id = extractArgument(requestData, ID, Integer.class);
		User patient = userRepo.findOne(id);
		if (patient == null) {
			throw new UserNotExistException();
		}
		if (analystPatientRepo.findByAnalystAndPatientAndProject(
				session.getUser(), patient, session.getProject()) == null) {
			throw new UserNotExistException();
		}
		List<TestSession> testSessions = testSessionRepo
				.findAllByUserAndProject(patient, session.getProject());

		// Workaround to remove RAWDATA as it can consume a lot of space
		transform((Collection<TestSession>) testSessions,
				ClearHeavyDataTransformer.instance());

		GenericResponseData<List<TestSession>> data = new GenericResponseData<List<TestSession>>(
				Arguments.TEST_SESSION, testSessions);

		return ResponseFactory.makeSuccessDataResponse(data, requestData);
	}

}
