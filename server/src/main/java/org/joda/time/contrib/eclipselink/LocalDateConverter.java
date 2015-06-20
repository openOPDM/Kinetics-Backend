package org.joda.time.contrib.eclipselink;

import java.util.Date;

import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.mappings.foundation.AbstractDirectMapping;
import org.eclipse.persistence.sessions.Session;
import org.joda.time.LocalDate;

/**
 * Persist LocalDate via EclipseLink
 * 
 * @author georgi.knox
 * 
 */
public class LocalDateConverter implements Converter {

	private static final long serialVersionUID = 5814355322927041505L;

	public Object convertDataValueToObjectValue(Object dataValue,
			Session session) {
		if (dataValue == null) {
			return null;
		}
		if (dataValue instanceof Date) {
			return new LocalDate(dataValue);
		}
		throw new IllegalStateException(
				"Converstion exception, value is not of LocalDate type.");
	}

	public Object convertObjectValueToDataValue(Object objectValue, Session arg1) {

		if (objectValue == null) {
			return null;
		}
		if (objectValue instanceof LocalDate) {
			LocalDate localDate = (LocalDate) objectValue;
			return localDate.toDateTimeAtStartOfDay().toDate();
		}
		throw new IllegalStateException(
				"Converstion exception, value is not of java.util.Date type.");
	}

	public void initialize(DatabaseMapping mapping, Session arg1) {
		((AbstractDirectMapping) mapping)
				.setFieldClassification(java.sql.Date.class);

	}

	public boolean isMutable() {
		return false;
	}

}
