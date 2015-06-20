package org.kinetics.rest.json;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.springframework.stereotype.Service;

@Service
public class KineticsObjectMapper extends ObjectMapper {

	public KineticsObjectMapper() {
		// to make sure LocalDate is converted in JS Date object
		configure(Feature.WRITE_DATES_AS_TIMESTAMPS, false);
	}

}
