package org.kinetics.dao.audit;

import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface EventRepository extends CrudRepository<Event, Integer> {

	@Query("SELECT COUNT(e) FROM Event e WHERE e.type = ?1")
	long countByType(EventType type);

	@Query("SELECT COUNT(e.timestamp) FROM Event e WHERE e.type = ?1 AND e.timestamp BETWEEN ?2 AND ?3 GROUP BY e.timestamp")
	long countEventByTypeAndStampBetween(EventType type, DateTime start,
			DateTime end);

	// TODO: maybe it should be rewritten via Criteria API
	@Query(name = "countByDate")
	List<AuditData> findByTypeAndStampBetween(EventType type, Date start,
			Date end);

}
