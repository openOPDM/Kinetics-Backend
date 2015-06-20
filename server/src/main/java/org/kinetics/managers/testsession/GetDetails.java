package org.kinetics.managers.testsession;

import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractArgument;
import static java.util.Arrays.asList;
import static org.kinetics.rest.Protocol.Arguments.ID;

import java.util.List;

import org.kinetics.dao.extension.ExtendedEntity;
import org.kinetics.dao.extension.ExtensionData;
import org.kinetics.dao.extension.ExtensionDataRepository;
import org.kinetics.dao.service.AnalystPatientRepository;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.share.SharedTestRepository;
import org.kinetics.dao.testsession.TestSession;
import org.kinetics.dao.testsession.TestSessionRepository;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserStatus;
import org.kinetics.exception.TestSessionNotFoundException;
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

@Component
@RequestDescriptor(target = Managers.TEST_SESSION_MANAGER, method = GetDetails.METHOD)
@RequiredArguments(ID)
public class GetDetails extends AuthKineticsRequestStrategy {

	public static final String METHOD = "getDetails";

	@Autowired
	private TestSessionRepository testRepo;
	@Autowired
	private ExtensionDataRepository extensionDataRepo;
	@Autowired
	private AnalystPatientRepository analystPatientRepo;
	@Autowired
	private SharedTestRepository sharedTestRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {
		final Integer id = extractArgument(requestData, ID, Integer.class);

		TestSession test = obtainTestSession(id, session);
		if (test == null) {
			throw new TestSessionNotFoundException();
		}

		List<ExtensionData> extensions = extensionDataRepo.findAllByEntity(
				ExtendedEntity.TEST_SESSION, id);
		test.setExtension(extensions);

		GenericResponseData<TestSession> data = new GenericResponseData<TestSession>(
				Arguments.TEST_SESSION, test);
		return ResponseFactory.makeSuccessDataResponse(data, requestData);
	}

	private TestSession obtainTestSession(Integer id, Session session) {
		// #1 simple case - check test by OWNER
		TestSession testSession = testRepo.findOneByIdAndUser(id,
				session.getUser());
		if (testSession != null) {
			return testSession;
		}

		// #2 check access via Analyst:
		List<User> patients = analystPatientRepo
				.findAllByStatusAndAnalystAndProject(session.getUser(),
						session.getProject(),
						asList(UserStatus.WAITING_PASS, UserStatus.ACTIVE));
		for (User patient : patients) {
			testSession = testRepo.findOneByIdAndUser(id, patient);
			if (testSession != null) {
				return testSession;
			}
		}
		// #3 Case for Shared Test Room
		List<User> owners = sharedTestRepo
				.findAllOwnersByEmailAndActiveProjects(session.getUser()
						.getEmail());
		if (!owners.isEmpty()) {
			return testRepo.findOneByIdAndUserIn(id, owners);
		}
		return null;
	}

}
