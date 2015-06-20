package org.kinetics.managers.audit;

import static org.kinetics.rest.Protocol.Arguments.TYPE;

import java.util.ArrayList;
import java.util.List;

import org.kinetics.dao.audit.EventType;
import org.kinetics.dao.authorization.PermissionsEnum;
import org.kinetics.dao.session.Session;
import org.kinetics.request.AuthKineticsRequestStrategy;
import org.kinetics.rest.Protocol.Managers;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.protocol.core.response.data.GenericResponseData;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.validator.HasPermission;

@Component
@RequestDescriptor(target = Managers.AUDIT_MANAGER, method = GetAuditEvents.METHOD)
@HasPermission(PermissionsEnum.Permission.AUDIT_DATA)
public class GetAuditEvents extends AuthKineticsRequestStrategy {

	static final String METHOD = "getAuditEvents";

	@Override
	protected ResponseContainer processAuthenticatedRequest(
			RequestFunction requestData, Session session) {

		EventType[] eventEnums = EventType.values();
		List<String> events = new ArrayList<String>(eventEnums.length);
		for (EventType type : eventEnums) {
			events.add(type.name());
		}
		GenericResponseData<List<String>> data = new GenericResponseData<List<String>>(
				TYPE, events);

		return ResponseFactory.makeSuccessDataResponse(data, requestData);
	}

}
