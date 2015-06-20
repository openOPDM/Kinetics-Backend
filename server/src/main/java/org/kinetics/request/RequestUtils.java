package org.kinetics.request;

import static com.lohika.server.core.Protocol.Arguments.EMAIL;
import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractGenericArgument;
import static org.kinetics.request.AuthKineticsRequestStrategy.LIST_INTEGER_TYPE;
import static org.kinetics.rest.Protocol.Arguments.PROJECT;

import java.util.List;

import org.kinetics.dao.project.Project;
import org.kinetics.dao.project.ProjectRepository;
import org.kinetics.exception.ProjectNotExistException;
import org.kinetics.rest.Protocol.Arguments;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;
import com.lohika.protocol.core.request.Argument;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.server.core.exception.InvalidArgumentValue;
import com.lohika.server.core.helpers.RequestDataExtractHelper;

public abstract class RequestUtils {

	/**
	 * Extracts Email argument and turns it to lower case
	 * 
	 * @param requestData
	 * @return
	 * @throws InvalidArgumentValue
	 *             if email is missing
	 */
	public static String extractEmailStringArgument(RequestFunction requestData) {
		String resultValue = requestData.getArguments().findByName(EMAIL)
				.getStringValue();
		if (!StringUtils.hasText(resultValue)) {
			throw new InvalidArgumentValue(EMAIL);
		}
		return resultValue.toLowerCase();
	}

	/**
	 * Extracts Email argument and turns it to lower case.
	 * 
	 * @param requestData
	 * @return null if email is missing
	 */
	public static String extractOptionalEmailStringArgument(
			RequestFunction requestData) {
		Argument argument = requestData.getArguments().findByName(EMAIL);
		if (argument == null || argument.getValue() == null) {
			return null;
		}
		return argument.getStringValue().toLowerCase();
	}

	public static Project extractProjectArgument(ProjectRepository repository,
			RequestFunction function) {
		return extractProjectByFieldName(repository, function, PROJECT);
	}

	public static Project extractProjectIdArgument(
			ProjectRepository repository, RequestFunction function) {
		return extractProjectByFieldName(repository, function, Arguments.ID);
	}

	private static Project extractProjectByFieldName(
			ProjectRepository repository, RequestFunction function, String param) {
		Integer projectId = RequestDataExtractHelper.extractArgument(function,
				param, Integer.class);

		Project project = repository.findOne(projectId);
		if (project == null) {
			throw new ProjectNotExistException();
		}
		return project;
	}

	// TODO: consider to migrate to service
	public static List<Project> extractProjectsArgument(
			ProjectRepository repository, RequestFunction function) {
		List<Integer> projectIds = extractGenericArgument(function, PROJECT,
				LIST_INTEGER_TYPE);
		if (projectIds.isEmpty()) {
			throw new InvalidArgumentValue(PROJECT);
		}
		List<Project> projects = Lists.newArrayList(repository
				.findAllByIdIn(projectIds));

		if (projects.isEmpty()) {
			throw new ProjectNotExistException();
		}
		return projects;
	}
}
