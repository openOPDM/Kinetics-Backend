package org.kinetics.rest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lohika.protocol.core.request.RequestContainer;
import com.lohika.protocol.core.response.ResponseContainer;
import com.lohika.server.core.RequestExecutor;

@Controller
@RequestMapping("/rest/mainpoint")
public final class MainPoint {

	@Autowired
	private RequestExecutor executor;

	/**
	 * Server status check helper method
	 * 
	 * @return
	 */
	@RequestMapping(value = "/info", method = RequestMethod.GET, produces = TEXT_PLAIN_VALUE)
	@ResponseBody
	public String getInfo() {
		return "Server UP and running!\nCurrent server version: "
				+ getClass().getPackage().getImplementationVersion();
	}

	@RequestMapping(value = "/execute", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<ResponseContainer> execute(
			@RequestBody RequestContainer container) {
		HttpHeaders headers = new HttpHeaders();
		headers.setCacheControl("no-cache");

		// TODO: add no version method
		return new ResponseEntity<ResponseContainer>(executor.execute("",
				container.getRequest().getFunction()), headers, HttpStatus.OK);
	}

}
