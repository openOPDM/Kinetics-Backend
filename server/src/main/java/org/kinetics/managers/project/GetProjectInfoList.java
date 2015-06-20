package org.kinetics.managers.project;

import static org.kinetics.managers.project.GetProjectInfoList.METHOD;

import java.util.List;

import org.kinetics.dao.project.Project;
import org.kinetics.dao.project.ProjectRepository;
import org.kinetics.rest.Protocol.Arguments;
import org.kinetics.rest.Protocol.Managers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.protocol.core.processor.ResponseFactory;
import com.lohika.protocol.core.request.RequestFunction;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.protocol.core.response.data.GenericResponseData;
import com.lohika.server.core.strategy.RequestDescriptor;
import com.lohika.server.core.strategy.RequestStrategy;

@Component
@RequestDescriptor(target = Managers.PROJECT_MANANGER, method = METHOD)
public class GetProjectInfoList implements RequestStrategy {

	static final String METHOD = "getProjectInfoList";
	@Autowired
	private ProjectRepository customerRepo;

	@Override
	public ResponseContainer execute(RequestFunction requestFunction) {

		List<Project> customers = (List<Project>) customerRepo.findAll();

		GenericResponseData<List<Project>> data = new GenericResponseData<List<Project>>(
				Arguments.PROJECT, customers);

		return ResponseFactory.makeSuccessDataResponse(data, requestFunction);
	}
}
