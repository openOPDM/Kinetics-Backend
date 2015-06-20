package org.kinetics.managers.account;

import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractStringArgument;
import static org.kinetics.dao.authorization.PermissionsEnum.Permission.MANAGE_USER;
import static org.kinetics.dao.user.UserStatus.WAITING_CONFIRMATION;
import static org.kinetics.managers.account.FindUser.METHOD;
import static org.kinetics.rest.Protocol.Arguments.SEARCH_DATA;
import static org.kinetics.rest.Protocol.Arguments.SEARCH_TOKEN;

import java.util.List;

import org.kinetics.dao.project.Project;
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
@HasPermission(MANAGE_USER)
public class FindUser extends AuthKineticsRequestStrategy {

	static final String METHOD = "findUser";

	@Autowired
	private UserRepository userRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		final String searchData = extractStringArgument(requestData,
				SEARCH_DATA);
		final String searchToken = extractStringArgument(requestData,
				SEARCH_TOKEN).toLowerCase();

		// we have SiteAdmin
		List<User> result;
		if (session.getProject() == null) {
			result = performSearchForSiteAdmin(searchData, searchToken);
		} else {
			result = performSearch(searchData, searchToken,
					session.getProject());
		}
		result.remove(session.getUser());

		GenericResponseData<List<User>> data = new GenericResponseData<List<User>>(
				Arguments.USER, result);
		return ResponseFactory.makeSuccessDataResponse(data, requestData);
	}

	private List<User> performSearchForSiteAdmin(String searchData,
			String searchToken) {
		SearchToken token = SearchToken.valueOf(searchToken);
		switch (token) {
		case summary:
			return userRepo
					.findAllByEmailOrFirstNameOrSecondNameOrUIDAndExcludeStatus(
							searchData, WAITING_CONFIRMATION);

		case email:
			return userRepo.findAllByEmailAndExcludeStatus(searchData,
					WAITING_CONFIRMATION);

		case name:
			return userRepo.findAllByFirstNameOrSecondNameAndExcludeStatus(
					searchData, WAITING_CONFIRMATION);

		case uid:
			return userRepo.findAllByUIDAndExcludeStatus(searchData,
					WAITING_CONFIRMATION);

		default:
			throw new InvalidArgumentValue(SEARCH_TOKEN);
		}
	}

	private List<User> performSearch(final String searchData,
			final String searchToken, Project customer) {

		SearchToken token = SearchToken.valueOf(searchToken);
		switch (token) {
		case summary:
			return userRepo
					.findAllByEmailOrFirstNameOrSecondNameOrUIDAndCustomerAndExcludeStatus(
							searchData, customer, WAITING_CONFIRMATION);

		case email:
			return userRepo.findAllByEmailAndProjectAndExcludeStatus(
					searchData, customer, WAITING_CONFIRMATION);

		case name:
			return userRepo
					.findAllByFirstNameOrSecondNameAndCustomerAndExcludeStatus(
							searchData, customer, WAITING_CONFIRMATION);

		case uid:
			return userRepo.findAllByUIDAndCustomerAndExcludeStatus(searchData,
					customer, WAITING_CONFIRMATION);

		default:
			throw new InvalidArgumentValue(SEARCH_TOKEN);
		}
	}

}
