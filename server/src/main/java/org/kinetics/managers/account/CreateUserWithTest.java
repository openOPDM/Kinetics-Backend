package org.kinetics.managers.account;

import static com.lohika.server.core.Protocol.Arguments.EMAIL;
import static org.kinetics.rest.Protocol.Arguments.FIRST_NAME;
import static org.kinetics.rest.Protocol.Arguments.PASSWORD;
import static org.kinetics.rest.Protocol.Arguments.PROJECT;
import static org.kinetics.rest.Protocol.Arguments.SECOND_NAME;
import static org.kinetics.rest.Protocol.Arguments.TEST_SESSION;

import org.kinetics.dao.audit.EventService;
import org.kinetics.dao.audit.EventType;
import org.kinetics.rest.Protocol.Managers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequestStrategy;
import com.lohika.server.core.strategy.RequiredArguments;

@Component
@RequestDescriptor(target = Managers.ACCOUNT_MANANGER, method = CreateUserWithTest.METHOD)
@RequiredArguments({ PROJECT, EMAIL, FIRST_NAME, SECOND_NAME, PASSWORD,
		TEST_SESSION })
public class CreateUserWithTest implements RequestStrategy {

	static final String METHOD = "createUserWithTest";

	@Autowired
	private UserService userService;
	@Autowired
	private EventService eventService;

	@Override
	public ResponseContainer execute(RequestFunction requestData) {
		userService.createUserWithTestFromRequest(requestData);		
		eventService.newEvent(EventType.TRY_SIGNUP);
		
		return ResponseFactory.makeSuccessResponse(requestData);
	}
}
