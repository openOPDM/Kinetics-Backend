package org.kinetics.dao.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.persistence.logging.AbstractSessionLog;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.logging.SessionLogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EclipseLinkLogAdapter extends AbstractSessionLog implements
		SessionLog {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(EclipseLinkLogAdapter.class);

	public void log(SessionLogEntry sessionLogEntry) {
		switch (sessionLogEntry.getLevel()) {
		case OFF:
			// No logging
			return;

		case SEVERE:
			LOGGER.error(buildMessage(sessionLogEntry));
			break;

		case WARNING:
			LOGGER.warn(buildMessage(sessionLogEntry));
			break;

		case INFO:
			LOGGER.info(buildMessage(sessionLogEntry));
			break;

		default:
			LOGGER.debug(buildMessage(sessionLogEntry));
		}
	}

	private String buildMessage(SessionLogEntry entry) {
		StringWriter writer = new StringWriter();

		writer.write(getSupplementDetailString(entry));
		if (entry.hasMessage()) {
			writer.write(formatMessage(entry));
		}
		if (entry.hasException()) {
			if (shouldLogExceptionStackTrace()) {
				entry.getException().printStackTrace(new PrintWriter(writer));
			} else {
				writer.write(entry.getException().toString());
			}
		}
		writer.flush();

		return writer.toString();
	}
}
