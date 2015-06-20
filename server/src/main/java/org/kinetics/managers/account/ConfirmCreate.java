package org.kinetics.managers.account;

import static com.lohika.server.core.Protocol.Arguments.EMAIL;
import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractStringArgument;
import static org.kinetics.rest.Protocol.Arguments.CONFIRMATION_CODE;
import static org.kinetics.rest.Protocol.Managers.ACCOUNT_MANANGER;

import org.kinetics.dao.audit.EventService;
import org.kinetics.dao.audit.EventType;
import org.kinetics.dao.user.Confirmation;
import org.kinetics.dao.user.ConfirmationRepository;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
import org.kinetics.dao.user.UserStatus;
import org.kinetics.exception.UserNotExistException;
import org.kinetics.request.RequestUtils;
import org.kinetics.rest.Protocol.Errors;
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
@RequestDescriptor(target = ACCOUNT_MANANGER, method = ConfirmCreate.METHOD)
@RequiredArguments({ CONFIRMATION_CODE, EMAIL })
public class ConfirmCreate implements RequestStrategy {

	static final String METHOD = "confirmCreate";

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ConfirmationRepository confirmRepo;
	@Autowired
	private UserService userService;
	@Autowired
	private EventService eventService;
	
	@Override
	public ResponseContainer execute(RequestFunction requestData) {
		final String code = extractStringArgument(requestData,
				CONFIRMATION_CODE);
		final String email = RequestUtils
				.extractEmailStringArgument(requestData);

		// compare code with Confirmation table!
		Confirmation confirmation = confirmRepo.findOneByCode(code);
		if (confirmation == null) {
			throw new RestException(Errors.CONFIRMATION_CODE_INVALID);
		}

		User user = confirmation.getUser();
		if (user == null || !user.getEmail().equals(email)) {
			throw new UserNotExistException();
		}
		userService.validateStatus(user, UserStatus.WAITING_CONFIRMATION);

		// remove confirmations and update user status
		confirmRepo.deleteByUser(user);

		user.setStatus(UserStatus.ACTIVE);
		userRepository.save(user);
		
		eventService.newEvent(EventType.CONFIRM);

		return ResponseFactory.makeSuccessResponse(requestData);
	}

}
