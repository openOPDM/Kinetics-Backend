package org.kinetics.managers.account;

import static com.lohika.server.core.Protocol.Arguments.EMAIL;
import static org.kinetics.managers.account.ResetPassword.METHOD;
import static org.kinetics.request.RequestUtils.extractEmailStringArgument;
import static org.kinetics.rest.Protocol.Managers.ACCOUNT_MANANGER;

import java.util.UUID;

import org.joda.time.DateTime;
import org.kinetics.dao.user.Confirmation;
import org.kinetics.dao.user.ConfirmationRepository;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
import org.kinetics.dao.user.UserStatus;
import org.kinetics.exception.UserNotExistException;
import org.kinetics.util.mail.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequestStrategy;
import com.lohika.server.core.strategy.RequiredArguments;

/**
 * NOTE In production (after POC) avoid sending new passwords via email, as
 * plain mails can be sniffed. Replace this logic with temporarily tokens
 * (http://crackstation.net/hashing-security.htm)
 * 
 * @author akaverin
 * 
 */
@Component
@RequestDescriptor(target = ACCOUNT_MANANGER, method = METHOD)
@RequiredArguments(EMAIL)
public class ResetPassword implements RequestStrategy {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ResetPassword.class);

	static final String METHOD = "resetPassword";

	@Autowired
	private UserRepository userRepo;
	@Autowired
	private MailService mailService;
	@Autowired
	private ConfirmationRepository confirmationRepo;
	@Autowired
	private UserService userService;

	@Override
	public ResponseContainer execute(RequestFunction requestData) {
		final String email = extractEmailStringArgument(requestData);

		User user = userRepo.findOneByEmail(email);
		if (user == null) {
			throw new UserNotExistException();
		}
		userService.validateStatus(user, UserStatus.ACTIVE,
				UserStatus.WAITING_PASS);

		Confirmation confirmation = new Confirmation(user, UUID.randomUUID()
				.toString(), new DateTime());

		confirmationRepo.save(confirmation);
		try {
			mailService.sendPasswordToken(user, confirmation.getCode());
		} catch (MailException e) {
			LOGGER.error("Failed to send new password", e);
		}
		return ResponseFactory.makeSuccessResponse(requestData);
	}

}
