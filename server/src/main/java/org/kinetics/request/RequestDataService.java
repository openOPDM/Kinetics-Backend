package org.kinetics.request;

import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractArgument;
import static org.kinetics.rest.Protocol.Arguments.DATE_FROM;
import static org.kinetics.rest.Protocol.Arguments.DATE_TO;
import static org.kinetics.rest.Protocol.Arguments.ID;

import org.joda.time.DateTime;
import org.kinetics.dao.project.Project;
import org.kinetics.dao.project.ProjectRepository;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.testsession.TestSession;
import org.kinetics.dao.testsession.TestSessionRepository;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
import org.kinetics.exception.ProjectNotExistException;
import org.kinetics.exception.TestSessionNotFoundException;
import org.kinetics.exception.UserNotExistException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.server.core.exception.InvalidArgumentValue;

/**
 * Generic service for Request data extraction and validation against DAO layer
 * 
 * @author akaverin
 * 
 */
@Service
public class RequestDataService {

	@Autowired
	private TestSessionRepository testSessionRepo;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private ProjectRepository projectRepo;

	/**
	 * Extract {@link TestSession} from request, parameter comes as Integer
	 * named ID
	 * 
	 * @param requestData
	 * @param session
	 *            to perform search in current project only
	 * @return instance loaded from DAO layer
	 */
	public TestSession extractTestById(RequestFunction requestData,
			Session session) {
		final Integer id = extractArgument(requestData, ID, Integer.class);

		TestSession testSession = testSessionRepo.findOneByIdAndProject(id,
				session.getProject());
		if (testSession == null) {
			throw new TestSessionNotFoundException();
		}
		return testSession;
	}

	/**
	 * Extract {@link TestSession} from request, parameter comes as Integer
	 * named ID
	 * 
	 * @param requestData
	 * 
	 * @return instance loaded from DAO layer
	 */
	public TestSession extractTestById(RequestFunction requestData) {
		final Integer id = extractArgument(requestData, ID, Integer.class);

		TestSession testSession = testSessionRepo.findOne(id);
		if (testSession == null) {
			throw new TestSessionNotFoundException();
		}
		return testSession;
	}

	/**
	 * Extract {@link DateTime} from request
	 * 
	 * @param requestData
	 * @return array of {@link DateTime} objects with from set to 0, and to - 1
	 *         positions
	 */
	public DateTime[] extractDateFromTo(RequestFunction requestData) {
		final DateTime from = extractArgument(requestData, DATE_FROM,
				DateTime.class);
		final DateTime to = extractArgument(requestData, DATE_TO,
				DateTime.class);

		if (from.isAfter(to)) {
			throw new InvalidArgumentValue(DATE_FROM);
		}
		return new DateTime[] { from, to };
	}

	/**
	 * Extracts {@link User} from request
	 * 
	 * @param requestData
	 *            to be parsed
	 * @param argName
	 *            name to be used for search
	 * @return
	 */
	public User extractUser(RequestFunction requestData, String argName) {
		final Integer id = extractArgument(requestData, argName, Integer.class);

		User user = userRepo.findOne(id);
		if (user == null) {
			throw new UserNotExistException();
		}
		return user;
	}

	/**
	 * Extracts {@link Project} from request
	 * 
	 * @param requestData
	 *            to be parsed
	 * @param argName
	 *            name to be used for search
	 * @return
	 */
	public Project extractProject(RequestFunction function, String argName) {
		Integer projectId = extractArgument(function, argName, Integer.class);

		Project project = projectRepo.findOne(projectId);
		if (project == null) {
			throw new ProjectNotExistException();
		}
		return project;
	}

}
