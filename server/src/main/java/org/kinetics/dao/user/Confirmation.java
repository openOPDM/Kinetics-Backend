package org.kinetics.dao.user;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.eclipse.persistence.annotations.CascadeOnDelete;
import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;
import org.joda.time.DateTime;
import org.joda.time.contrib.eclipselink.DateTimeConverter;

/**
 * @author akaverin
 * 
 */
@Entity
@CascadeOnDelete
@Converter(converterClass = DateTimeConverter.class, name = "dateTimeConverter")
public class Confirmation {

	@Id
	@GeneratedValue
	private Integer id;

	@ManyToOne
	private User user;

	private String code;

	/**
	 * Used only for time based confirmations
	 */
	@Convert("dateTimeConverter")
	private DateTime timestamp;

	public Confirmation() {
	}

	public Confirmation(User user, String code) {
		this.user = user;
		this.code = code;
	}

	public Confirmation(User user, String code, DateTime ts) {
		this(user, code);
		this.timestamp = ts;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Integer getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public DateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(DateTime timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "Confirmation [id=" + id + ", user=" + user + ", code=" + code
				+ ", timestamp=" + timestamp + "]";
	}

}
