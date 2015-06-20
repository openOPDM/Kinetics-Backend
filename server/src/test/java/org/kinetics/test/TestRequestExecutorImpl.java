package org.kinetics.test;

import org.kinetics.rest.MainPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.request.RequestContainer;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.server.core.test.AbsTestRequestExecutor;

@Component
public class TestRequestExecutorImpl extends AbsTestRequestExecutor {

	@Autowired
	private MainPoint mainPoint;

	@Override
	protected ResponseContainer executeLogic(RequestContainer container) {
		return mainPoint.execute(container).getBody();
	}

}
