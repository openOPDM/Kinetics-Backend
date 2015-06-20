package org.kinetics.dao;

import javax.persistence.PrePersist;

import org.joda.time.DateTime;

public class TimestampListener {

	@PrePersist
	public void persist(TimestampEntity entity) {
		entity.setTimestamp(new DateTime());
	}

}