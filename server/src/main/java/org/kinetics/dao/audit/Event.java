package org.kinetics.dao.audit;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FieldResult;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.SqlResultSetMapping;

import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;
import org.joda.time.DateTime;
import org.joda.time.contrib.eclipselink.DateTimeConverter;

@Entity
@Converter(converterClass = DateTimeConverter.class, name = "dateTimeConverter")
@SqlResultSetMapping(name = "stats", entities = @EntityResult(entityClass = AuditData.class, fields = {
		@FieldResult(name = "date", column = "date"),
		@FieldResult(name = "total", column = "total") }))
@NamedQuery(name = "countByDate", query = "SELECT NEW org.kinetics.dao.audit.AuditData(CAST(e.timestamp DATE), COUNT(e.timestamp)) "
		+ "FROM Event e WHERE e.type = ?1 AND CAST(e.timestamp DATE) BETWEEN CAST(?2 DATE) AND CAST(?3 DATE) GROUP BY CAST(e.timestamp DATE)")
public class Event {

	@Id
	@GeneratedValue
	private Integer id;

	@Column(updatable = false)
	private EventType type;

	@Column(updatable = false)
	@Convert("dateTimeConverter")
	private DateTime timestamp;

	public Event() {
	}

	public Event(EventType type) {
		this.type = type;
	}

	public Event(EventType type, DateTime ts) {
		this.type = type;
		this.timestamp = ts;
	}

	@PrePersist
	public void prePersist() {
		if (timestamp == null) {
			timestamp = new DateTime();
		}
	}

	public Integer getId() {
		return id;
	}

	public EventType getType() {
		return type;
	}

	public DateTime getTimestamp() {
		return timestamp;
	}

	@Override
	public String toString() {
		return "Event [id=" + id + ", type=" + type + ", timestamp="
				+ timestamp + "]";
	}

}
