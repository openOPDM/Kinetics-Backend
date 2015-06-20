package org.kinetics.managers.project;

import static org.kinetics.dao.project.ProjectStatus.ACTIVE;
import static org.kinetics.dao.project.ProjectStatus.DISABLED;

import org.kinetics.dao.project.Project;
import org.kinetics.rest.Protocol;

import com.lohika.server.core.exception.RestException;

public class ProjectStatusValidator {

	public static void isNotActive(Project customer) {
		if (ACTIVE.equals(customer.getStatus())) {
			throw new RestException(Protocol.Errors.PROJECT_IS_ACTIVE);
		}
	}

	public static void isNotDisabled(Project customer) {
		if (DISABLED.equals(customer.getStatus())) {
			throw new RestException(Protocol.Errors.PROJECT_DISABLED);
		}
	}
}
