package org.kinetics.dao.session;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;

import org.eclipse.persistence.annotations.CascadeOnDelete;
import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;
import org.joda.time.DateTime;
import org.joda.time.contrib.eclipselink.DateTimeConverter;
import org.kinetics.dao.TimestampEntity;
import org.kinetics.dao.TimestampListener;
import org.kinetics.dao.project.Project;
import org.kinetics.dao.user.User;

import com.lohika.server.core.strategy.SessionEntity;

@Entity
@CascadeOnDelete
@EntityListeners(TimestampListener.class)
@Converter(converterClass = DateTimeConverter.class, name = "dateTimeConverter")
public class Session implements TimestampEntity, SessionEntity {

	@Id
	private String sessionToken;

	// TODO: consider ManyToOne once we implement sessions cleanup
	@OneToOne
	@CascadeOnDelete
	private User user;

	@OneToOne
	@CascadeOnDelete
	private Project project;

	@Convert("dateTimeConverter")
	private DateTime timestamp;

	public Session() {
	}

	// TODO: workaround solution, for some reason @UUIDGenerator doesn't seem to
	// work
	@PrePersist
	public void prePersist() {
		if (sessionToken == null) {
			sessionToken = UUID.randomUUID().toString();
		}
	}

	public Session(User user, Project project) {
		this.user = user;
		this.project = project;
	}

	@Override
	public Date getTimestamp() {
		return timestamp.toDate();
	}

	@Override
	public void setTimestamp(DateTime date) {
		timestamp = date;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getSessionToken() {
		return sessionToken;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	@Override
	public String toString() {
		return "Session [sessionToken=" + sessionToken + ", user=" + user
				+ ", timestamp=" + timestamp + "]";
	}

}
