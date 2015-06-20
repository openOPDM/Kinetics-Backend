package org.kinetics.managers.account;

import static com.lohika.server.core.Protocol.Arguments.EMAIL;
import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractStringArgument;
import static org.kinetics.managers.account.ConfirmPatientProfile.METHOD;
import static org.kinetics.request.RequestUtils.extractEmailStringArgument;
import static org.kinetics.rest.Protocol.Arguments.CONFIRMATION_CODE;
import static org.kinetics.rest.Protocol.Arguments.PASSWORD;
import static org.kinetics.rest.Protocol.Managers.ACCOUNT_MANANGER;

import org.kinetics.dao.authorization.RoleRepository;
import org.kinetics.dao.user.Confirmation;
import org.kinetics.dao.user.ConfirmationRepository;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
import org.kinetics.dao.user.UserStatus;
import org.kinetics.exception.UserNotExistException;
import org.kinetics.rest.Protocol;
import org.kinetics.util.secure.HashUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.server.core.exception.RestException;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequestStrategy;
import com.lohika.server.core.strategy.RequiredArguments;

@Component
@RequestDescriptor(target = ACCOUNT_MANANGER, method = METHOD)
@RequiredArguments({ CONFIRMATION_CODE, PASSWORD, EMAIL })
public class ConfirmPatientProfile implements RequestStrategy {

	static final String METHOD = "confirmPatientProfile";
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ConfirmationRepository confirmRepo;
	@Autowired
	private RoleRepository roleRepo;
	@Autowired
	private UserService userService;

	@Override
	public ResponseContainer execute(RequestFunction requestData) {
		final String code = extractStringArgument(requestData,
				CONFIRMATION_CODE);
		final String email = extractEmailStringArgument(requestData);

		Confirmation confirmation = confirmRepo.findOneByCode(code);
		if (confirmation == null) {
			throw new RestException(Protocol.Errors.CONFIRMATION_CODE_INVALID);
		}

		User user = confirmation.getUser();
		if (user == null || !user.getEmail().equals(email)) {
			throw new UserNotExistException();
		}
		userService.validateStatus(user, UserStatus.WAITING_PASS);

		final String pass = extractStringArgument(requestData, PASSWORD);
		user.setHashData(HashUtils.generate(pass));

		userService.updateUserOptionalData(user, requestData);
		user.setStatus(UserStatus.ACTIVE);

		userRepository.save(user);
		confirmRepo.deleteByUser(user);

		return ResponseFactory.makeSuccessResponse(requestData);
	}
}
