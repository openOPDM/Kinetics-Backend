package org.kinetics.managers.account;

import static com.lohika.server.core.Protocol.Arguments.EMAIL;
import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractGenericArgument;
import static org.kinetics.dao.authorization.PermissionsEnum.Permission.MANAGE_USER;

import java.util.List;

import org.kinetics.dao.project.ProjectRepository;
import org.kinetics.dao.session.Session;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol.Managers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.server.core.exception.InvalidArgumentValue;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequiredArguments;
import com.lohika.server.core.validator.HasPermission;

@Component
@RequestDescriptor(target = Managers.ACCOUNT_MANANGER, method = DeleteUserByEmail.METHOD)
@HasPermission(MANAGE_USER)
@RequiredArguments(EMAIL)
public class DeleteUserByEmail extends AuthKineticsRequestStrategy {

	static final String METHOD = "deleteUserByEmail";

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private ProjectRepository customerRepo;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		final List<String> emails = extractGenericArgument(requestData, EMAIL,
				LIST_STRING_TYPE);
		if (emails.isEmpty()) {
			throw new InvalidArgumentValue(EMAIL);
		}
		for (String email : emails) {
			User user = userRepo.findOneByEmail(email.toLowerCase());
			if (user == null) {
				continue;
			}
			// we do not allow to delete admin user
			if (user.getEmail().equals(session.getUser().getEmail())) {
				continue;
			}
			userRepo.delete(user);

			// TODO: consider to send email notification to user that he was
			// removed by Admin
		}
		return ResponseFactory.makeSuccessResponse(requestData);
	}

}
