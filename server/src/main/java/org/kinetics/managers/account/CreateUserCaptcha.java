package org.kinetics.managers.account;

import static com.lohika.server.core.Protocol.Arguments.EMAIL;
import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractOptionalStringArgument;
import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractStringArgument;
import static org.kinetics.managers.account.CreateUserCaptcha.METHOD;
import static org.kinetics.rest.Protocol.Arguments.CHALLENGE;
import static org.kinetics.rest.Protocol.Arguments.FIRST_NAME;
import static org.kinetics.rest.Protocol.Arguments.PASSWORD;
import static org.kinetics.rest.Protocol.Arguments.PROJECT;
import static org.kinetics.rest.Protocol.Arguments.SECOND_NAME;
import static org.kinetics.rest.Protocol.Arguments.SOLUTION;
import static org.kinetics.rest.Protocol.Arguments.TOKEN;
import static org.kinetics.rest.Protocol.Managers.ACCOUNT_MANANGER;

import javax.servlet.http.HttpServletRequest;

import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

import org.kinetics.dao.audit.EventService;
import org.kinetics.dao.audit.EventType;
import org.kinetics.dao.authorization.RoleRepository;
import org.kinetics.dao.authorization.RolesEnum;
import org.kinetics.dao.user.User;
import org.kinetics.dao.user.UserRepository;
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
@RequestDescriptor(target = ACCOUNT_MANANGER, method = METHOD)
@RequiredArguments({ PROJECT, EMAIL, FIRST_NAME, SECOND_NAME, PASSWORD,
		CHALLENGE, SOLUTION })
public class CreateUserCaptcha implements RequestStrategy {

	static final String METHOD = "createUserCaptcha";

	@Autowired
	private UserRepository userRepo;
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private UserService userService;
	@Autowired
	private RoleRepository roleRepo;
	@Autowired
	private EventService eventService;

	private static final String KEY = "6LdRDd8SAAAAAJQQ2QrNygHqFucWo-CsgfzqO9ju";

	private ReCaptchaImpl reCaptcha;

	public CreateUserCaptcha() {
		reCaptcha = new ReCaptchaImpl();
		reCaptcha.setPrivateKey(KEY);
	}

	@Override
	public ResponseContainer execute(RequestFunction requestData) {
		User user = userService.buildCompleteUserFromRequest(requestData);

		validateReCaptcha(requestData);

		user.addRole(roleRepo.findByName(RolesEnum.PATIENT.name()));
		userRepo.save(user);
		userService.sendConfirmation(user,
				extractOptionalStringArgument(requestData, TOKEN));

		eventService.newEvent(EventType.SIGNUP);

		return ResponseFactory.makeSuccessResponse(requestData);
	}

	private void validateReCaptcha(RequestFunction requestData) {
		String remoteAddr = request.getRemoteAddr();

		final String challenge = extractStringArgument(requestData, CHALLENGE);
		final String response = extractStringArgument(requestData, SOLUTION);

		ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(remoteAddr,
				challenge, response);

		if (!reCaptchaResponse.isValid()) {
			throw new RestException(Errors.CAPTCHA_INVALID);
		}
	}

	/**
	 * For UT mocking
	 * 
	 * @param reCaptcha
	 */
	void setReCaptcha(ReCaptchaImpl reCaptcha) {
		this.reCaptcha = reCaptcha;
	}

}
