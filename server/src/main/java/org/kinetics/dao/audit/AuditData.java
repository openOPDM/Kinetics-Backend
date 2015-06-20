package org.kinetics.dao.audit;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;
import org.joda.time.LocalDate;
import org.joda.time.contrib.eclipselink.LocalDateConverter;
import org.kinetics.dao.audit.AuditData.UserStatsId;

@Entity
@IdClass(UserStatsId.class)
@Converter(converterClass = LocalDateConverter.class, name = "localDateConverter")
public class AuditData {

	static class UserStatsId {
		LocalDate date;
		long total;
	}

	@Id
	@Convert("localDateConverter")
	private LocalDate date;

	@Id
	private long total;

	public AuditData() {
	}

	public AuditData(Date date, Long total) {
		this.date = new LocalDate(date);
		this.total = total;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	@Override
	public String toString() {
		return "UserStats [date=" + date + ", total=" + total + "]";
	}

}
