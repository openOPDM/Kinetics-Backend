package org.kinetics.dao.audit;

import java.util.List;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventService {

	@Autowired
	private EventRepository eventRepo;

	public void newEvent(EventType type) {
		eventRepo.save(new Event(type));
	}

	public List<AuditData> findAllByTypeAndCreatedBetween(EventType type,
			LocalDate from, LocalDate to) {
		return eventRepo.findByTypeAndStampBetween(type, from
				.toDateTimeAtStartOfDay().toDate(), to.toDateTimeAtStartOfDay()
				.toDate());
	}

}
