package org.kinetics.managers.share;

import static com.lohika.server.core.Protocol.Arguments.EMAIL;
import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractGenericArgument;
import static com.lohika.server.core.helpers.RequestDataExtractHelper.extractStringArgument;
import static org.kinetics.rest.Protocol.Arguments.MESSAGE;
import static org.kinetics.rest.Protocol.Arguments.URL;

import java.util.List;

import org.kinetics.dao.session.Session;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol.Managers;
import org.kinetics.util.mail.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequiredArguments;

@Component
@RequestDescriptor(target = Managers.SHARING_MANAGER, method = ShareByMail.METHOD)
@RequiredArguments({ EMAIL, MESSAGE, URL })
public class ShareByMail extends AuthKineticsRequestStrategy {

	static final String METHOD = "shareByMail";

	@Autowired
	private MailService mailService;

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		final List<String> emails = extractGenericArgument(requestData, EMAIL,
				LIST_STRING_TYPE);

		final String message = extractStringArgument(requestData, MESSAGE);
		final String url = extractStringArgument(requestData, URL);

		mailService.sendMessage(emails, message, url);

		return ResponseFactory.makeSuccessResponse(requestData);
	}

}
