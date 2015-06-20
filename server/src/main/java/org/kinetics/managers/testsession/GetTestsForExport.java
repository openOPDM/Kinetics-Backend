package org.kinetics.managers.testsession;

import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractArgument;
import static org.kinetics.rest.Protocol.Arguments.DATE_FROM;
import static org.kinetics.rest.Protocol.Arguments.DATE_TO;
import static org.kinetics.rest.Protocol.Arguments.PAGE;
import static org.kinetics.rest.Protocol.Arguments.SIZE;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.joda.time.DateTime;
import org.kinetics.dao.authorization.PermissionsEnum;
import org.kinetics.dao.extension.ExtendedEntity;
import org.kinetics.dao.extension.ExtensionData;
import org.kinetics.dao.extension.ExtensionDataRepository;
import org.kinetics.dao.project.ProjectStatus;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.testsession.TestSession;
import org.kinetics.dao.testsession.TestSessionRepository;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.request.RequestDataService;
import org.kinetics.rest.Protocol.Arguments;
import org.kinetics.rest.Protocol.Managers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.protocol.core.response.data.GenericResponseData;
import com.lohika.server.core.exception.InvalidArgumentValue;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequiredArguments;
import com.lohika.server.core.validator.HasPermission;

@Component
@RequestDescriptor(target = Managers.TEST_SESSION_MANAGER, method = GetTestsForExport.METHOD)
@RequiredArguments({ DATE_FROM, DATE_TO, PAGE, SIZE })
@HasPermission(PermissionsEnum.Permission.EXPORT_TEST_DATA)
public class GetTestsForExport extends AuthKineticsRequestStrategy {

	static final String KB_TEST_PREFIX = "kb|";

	static final String METHOD = "getTestsForExport";

	@Autowired
	private TestSessionRepository testSessionRepo;
	@Autowired
	private ExtensionDataRepository extensionDataRepo;
	@Autowired
	private RequestDataService requestDataService;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		final DateTime[] dateTimes = requestDataService
				.extractDateFromTo(requestData);
		final Integer page = extractArgument(requestData, PAGE, Integer.class);
		final Integer size = extractArgument(requestData, SIZE, Integer.class);

		if (page < 0) {
			throw new InvalidArgumentValue(PAGE);
		}
		if (size <= 0) {
			throw new InvalidArgumentValue(SIZE);
		}
		PageRequest pageRequest = new PageRequest(page, size,
				Sort.Direction.ASC, "creationDate");

		List<TestSession> sessions = testSessionRepo
				.findAllByCreationDateBetweenAndTypeStartingWithAndStatus(
						dateTimes[0], dateTimes[1], KB_TEST_PREFIX,
						ProjectStatus.ACTIVE, pageRequest);

		for (TestSession testSession : sessions) {
			List<ExtensionData> extensions = extensionDataRepo.findAllByEntity(
					ExtendedEntity.TEST_SESSION, testSession.getId());
			testSession.setExtension(extensions);
		}

		@SuppressWarnings("unchecked")
		List<ExportTestSession> exportList = (List<ExportTestSession>) CollectionUtils
				.collect(sessions, new Transformer() {
					@Override
					public Object transform(Object input) {
						return new ExportTestSession((TestSession) input);
					}
				}, new ArrayList<ExportTestSession>(sessions.size()));

		GenericResponseData<List<ExportTestSession>> data = new GenericResponseData<List<ExportTestSession>>(
				Arguments.TEST_SESSION, exportList);

		return ResponseFactory.makeSuccessDataResponse(data, requestData);
	}

	@JsonSerialize(include = Inclusion.NON_NULL)
	static final class ExportTestSession {

		private final TestSession testSession;

		public ExportTestSession(TestSession testSession) {
			this.testSession = testSession;
		}

		public DateTime getCreationDate() {
			return testSession.getCreationDate();
		}

		public String getType() {
			return testSession.getType();
		}

		public String getRawData() {
			return testSession.getRawData();
		}

		public Double getScore() {
			return testSession.getScore();
		}

		public Boolean getIsValid() {
			return testSession.getIsValid();
		}

		public Integer getId() {
			return testSession.getId();
		}

		public String getNotes() {
			return testSession.getNotes();
		}

		public List<ExtensionData> getExtension() {
			return testSession.getExtension();
		}

		public String getFirstName() {
			return testSession.getUser().getFirstName();
		}

		public String getSecondName() {
			return testSession.getUser().getSecondName();
		}

	}
}
