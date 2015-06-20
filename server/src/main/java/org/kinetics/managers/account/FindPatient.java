package org.kinetics.managers.account;

import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractStringArgument;
import static org.kinetics.dao.authorization.PermissionsEnum.Permission.MANAGE_PATIENT;
import static org.kinetics.dao.user.UserStatus.DISABLED;
import static org.kinetics.managers.account.FindPatient.METHOD;
import static org.kinetics.rest.Protocol.Arguments.SEARCH_DATA;
import static org.kinetics.rest.Protocol.Arguments.SEARCH_TOKEN;

import java.util.List;

import org.kinetics.dao.authorization.Role;
import org.kinetics.dao.authorization.RoleRepository;
import org.kinetics.dao.authorization.RolesEnum;
import org.kinetics.dao.project.Project;
import org.kinetics.dao.service.AnalystPatientRepository;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol.Arguments;
import org.kinetics.rest.Protocol.Managers;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestDescriptor(target = Managers.ACCOUNT_MANANGER, method = METHOD)
@RequiredArguments({ SEARCH_TOKEN, SEARCH_DATA })
@HasPermission(MANAGE_PATIENT)
public class FindPatient extends AuthKineticsRequestStrategy {

	static final String METHOD = "findPatient";

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private RoleRepository roleRepo;

	@Autowired
	private AnalystPatientRepository analystPatientRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		final String searchData = extractStringArgument(requestData,
				SEARCH_DATA);
		final String searchToken = extractStringArgument(requestData,
				SEARCH_TOKEN).toLowerCase();
		try {
			SearchToken.valueOf(searchToken);
		} catch (IllegalArgumentException ex) {
			throw new InvalidArgumentValue(SEARCH_TOKEN);
		}

		Role patientRole = roleRepo.findByName(RolesEnum.PATIENT.name());
		List<Integer> patientIds = analystPatientRepo
				.findPatientIdsByAnalystAndProject(session.getUser(),
						session.getProject());
		patientIds.add(session.getUser().getId());

		List<User> result = performSearch(searchData, searchToken, patientRole,
				patientIds, session.getProject());

		GenericResponseData<List<User>> data = new GenericResponseData<List<User>>(
				Arguments.USER, result);
		return ResponseFactory.makeSuccessDataResponse(data, requestData);
	}

	private List<User> performSearch(final String searchData,
			final String searchToken, Role role, List<Integer> ids,
			Project customer) {

		SearchToken token = SearchToken.valueOf(searchToken);
		switch (token) {
		case summary:
			return userRepo
					.findAllByRoleAndEmailOrFirstNameOrSecondNameOrUIDAndCustomerAndExcludeStatusAndIds(
							searchData, role, customer, DISABLED, ids);

		case email:
			return userRepo
					.findAllByEmailAndRoleAndProjectAndExcludeStatusAndIds(
							searchData, role, customer, DISABLED, ids);

		case name:
			return userRepo
					.findAllByFirstNameOrSecondNameAndRoleAndCustomerAndExcludeStatusAndIds(
							searchData, role, customer, DISABLED, ids);

		case uid:
			return userRepo
					.findAllByUIDAndRoleAndCustomerAndExcludeStatusAndIds(
							searchData, role, customer, DISABLED, ids);

		case empty:
			return userRepo.findAllByRoleAndProjectAndExcludeStatusAndIds(
					role, customer, DISABLED, ids);

		default:
			throw new InvalidArgumentValue(SEARCH_TOKEN);
		}
	}

}
