package org.kinetics.dao.testsession;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FieldResult;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.eclipse.persistence.annotations.CascadeOnDelete;
import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;
import org.joda.time.DateTime;
import org.joda.time.contrib.eclipselink.DateTimeConverter;
import org.kinetics.dao.audit.AuditData;
import org.kinetics.dao.extension.ExtensionData;
import org.kinetics.dao.project.Project;
import org.kinetics.dao.user.User;

/**
 * Entity representing Test data
 * 
 * @author akaverin
 * 
 */
@Entity
@JsonSerialize(include = Inclusion.NON_NULL)
@CascadeOnDelete
@Converter(converterClass = DateTimeConverter.class, name = "dateTimeConverter")
@SqlResultSetMapping(name = "stats", entities = @EntityResult(entityClass = AuditData.class, fields = {
		@FieldResult(name = "date", column = "date"),
		@FieldResult(name = "total", column = "total") }))
@NamedQuery(name = "findTestByDate", query = "SELECT NEW org.kinetics.dao.audit.AuditData(CAST(t.creationDate DATE), COUNT(t.creationDate)) "
		+ "FROM TestSession t WHERE CAST(t.creationDate DATE) BETWEEN CAST(?1 DATE) AND CAST(?2 DATE) GROUP BY CAST(t.creationDate DATE)")
public class TestSession {

	private static final int DEFAULT_TYPE_LENGTH = 50;

	@Id
	@GeneratedValue
	private Integer id;

	@ManyToOne(optional = false)
	@JsonIgnore
	private User user;

	@ManyToOne(optional = false)
	@JsonIgnore
	private Project project;

	@Convert("dateTimeConverter")
	private DateTime creationDate;

	@Column(length = DEFAULT_TYPE_LENGTH)
	private String type;

	@Lob
	private String rawData;

	private Double score;

	private Boolean isValid = false;

	@Lob
	private String notes;

	/**
	 * For JSON only
	 */
	@Transient
	List<ExtensionData> extension;

	public TestSession() {
	}

	public TestSession(double score, String rawData, String type, User user,
			Project project) {
		this.score = score;
		this.rawData = rawData;
		this.type = type;
		this.user = user;
		this.project = project;
	}

	@PrePersist
	void prePersist() {
		if (creationDate == null) {
			creationDate = new DateTime();
		}
	}

	public User getUser() {
		return user;
	}

	@JsonProperty
	public Integer getUserId() {
		return user.getId();
	}

	@JsonProperty
	public String getUserFirstName() {
		return user.getFirstName();
	}

	@JsonProperty
	public String getUserSecondName() {
		return user.getSecondName();
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	@JsonIgnore
	public void setUserId(Integer id) {
	}

	@JsonIgnore
	public void setUserFirstName(String value) {
	}

	@JsonIgnore
	public void setUserSecondName(String value) {
	}

	public void setUser(User user) {
		this.user = user;
	}

	public DateTime getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(DateTime creationDate) {
		this.creationDate = creationDate;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRawData() {
		return rawData;
	}

	public void setRawData(String rawData) {
		this.rawData = rawData;
	}

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public Boolean getIsValid() {
		return isValid;
	}

	public void setIsValid(Boolean isValid) {
		this.isValid = isValid;
	}

	public Integer getId() {
		return id;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public List<ExtensionData> getExtension() {
		return extension;
	}

	public void setExtension(List<ExtensionData> extensionData) {
		this.extension = extensionData;
	}

	@Override
	public String toString() {
		return "Test [id=" + id + ", user=" + user + ", creationDate="
				+ creationDate + ", type=" + type + ", rawData=" + rawData
				+ ", score=" + score + ", isValid=" + isValid + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TestSession other = (TestSession) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

}
