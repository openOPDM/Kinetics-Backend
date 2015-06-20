package org.kinetics.dao.user;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EntityResult;
import javax.persistence.Enumerated;
import javax.persistence.FieldResult;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedNativeQuery;
import javax.persistence.PrePersist;
import javax.persistence.SqlResultSetMapping;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.eclipse.persistence.annotations.CascadeOnDelete;
import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;
import org.eclipse.persistence.annotations.Converters;
import org.eclipse.persistence.annotations.Index;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.contrib.eclipselink.DateTimeConverter;
import org.joda.time.contrib.eclipselink.LocalDateConverter;
import org.kinetics.dao.TimestampEntity;
import org.kinetics.dao.TimestampListener;
import org.kinetics.dao.audit.AuditData;
import org.kinetics.dao.authorization.Role;
import org.kinetics.dao.project.Project;
import org.kinetics.util.secure.HashData;

/**
 * @author akaverin
 * 
 */
@Entity
@CascadeOnDelete
@EntityListeners(TimestampListener.class)
@SqlResultSetMapping(name = "stats", entities = @EntityResult(entityClass = AuditData.class, fields = {
		@FieldResult(name = "date", column = "date"),
		@FieldResult(name = "total", column = "total") }))
@Converters({
		@Converter(converterClass = LocalDateConverter.class, name = "localDateConverter"),
		@Converter(converterClass = DateTimeConverter.class, name = "dateTimeConverter") })
@NamedNativeQuery(name = "nativeFind", query = "SELECT u1.CREATIONDATE as 'date', (SELECT count(u2.CREATIONDATE) "
		+ "FROM USER u2 WHERE u2.CREATIONDATE <= u1.CREATIONDATE) as 'total' "
		+ "FROM USER u1 WHERE u1.CREATIONDATE BETWEEN ?1 AND ?2 GROUP BY u1.CREATIONDATE", resultSetMapping = "stats")
public class User implements TimestampEntity {

	private static final int DEFAULT_TEXT_LENGTH = 50;

	@Id
	@GeneratedValue
	private Integer id;

	@Index
	@Column(length = DEFAULT_TEXT_LENGTH)
	private String email;

	@Column(length = DEFAULT_TEXT_LENGTH)
	private String firstName;

	@Column(length = DEFAULT_TEXT_LENGTH)
	private String secondName;

	@Embedded
	@JsonIgnore
	private HashData hashData;

	@Column(updatable = false)
	@Convert("localDateConverter")
	private LocalDate creationDate;

	@Convert("dateTimeConverter")
	private DateTime timestamp;

	@Enumerated
	private UserStatus status;

	@Convert("localDateConverter")
	private LocalDate birthday;

	@Column(unique = true, length = DEFAULT_TEXT_LENGTH)
	private String UID;

	@Enumerated
	private Gender gender;

	@ManyToMany
	@CascadeOnDelete
	private Collection<Role> roles;

	@ManyToMany
	@CascadeOnDelete
	@JsonProperty("project")
	private Collection<Project> projects;

	public User() {
		roles = new HashSet<Role>();
		projects = new HashSet<Project>();
	}

	public User(String firstName, String secondName) {
		this();
		this.firstName = firstName;
		this.secondName = secondName;
	}

	public User(String email, String firstName, String secondName, HashData hash) {
		this();
		this.email = email;
		this.firstName = firstName;
		this.secondName = secondName;
		this.hashData = hash;
	}

	public User(User user) {
		this();
		this.email = user.getEmail();
		this.birthday = user.getBirthday();
		this.creationDate = user.getCreationDate();
		this.firstName = user.getFirstName();
		this.secondName = user.getSecondName();
		this.gender = user.getGender();
		this.hashData = user.getHashData();
		this.status = user.getStatus();
		this.timestamp = user.getTimestamp();
	}

	@PrePersist
	public void prePersist() {
		if (creationDate == null) {
			creationDate = new LocalDate();
		}
		if (UID == null) {
			UID = UUID.randomUUID().toString().toUpperCase();
		}
	}

	public Integer getId() {
		return id;
	}

	@JsonProperty
	public String getEmail() {
		return email;
	}

	@JsonIgnore
	public void setEmail(String email) {
		this.email = email;
	}

	public DateTime getTimestamp() {
		return timestamp;
	}

	@Override
	public void setTimestamp(DateTime timestamp) {
		this.timestamp = timestamp;
	}

	public LocalDate getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(LocalDate creationDate) {
		this.creationDate = creationDate;
	}

	public UserStatus getStatus() {
		return status;
	}

	public void setStatus(UserStatus status) {
		this.status = status;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getSecondName() {
		return secondName;
	}

	public void setSecondName(String secondName) {
		this.secondName = secondName;
	}

	public HashData getHashData() {
		return hashData;
	}

	public void setHashData(HashData hashData) {
		this.hashData = hashData;
	}

	public LocalDate getBirthday() {
		return birthday;
	}

	public void setBirthday(LocalDate birthday) {
		this.birthday = birthday;
	}

	public String getUID() {
		return UID;
	}

	public void setUID(String iD) {
		UID = iD;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public Collection<Role> getRoles() {
		return Collections.unmodifiableCollection(roles);
	}

	public void addRole(Role role) {
		this.roles.add(role);
	}

	public void removeRole(Role role) {
		this.roles.remove(role);
	}

	public void changeRole(Role role) {
		this.roles.clear();
		this.roles.add(role);
	}

	public Collection<Project> getProjects() {
		return Collections.unmodifiableCollection(projects);
	}

	public void addProject(Project project) {
		this.projects.add(project);
	}

	public void addProjects(Collection<Project> projects) {
		this.projects.addAll(projects);
	}

	public void changeProjects(Collection<Project> projects) {
		this.projects.clear();
		this.projects.addAll(projects);
	}

	public void removeProject(Project project) {
		this.projects.remove(project);
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", email=" + email + ", firstName="
				+ firstName + ", secondName=" + secondName + ", creationDate="
				+ creationDate + ", timestamp=" + timestamp + ", status="
				+ status + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		User user = (User) o;

		if (!UID.equals(user.UID))
			return false;
		if (!creationDate.equals(user.creationDate))
			return false;
		if (!id.equals(user.id))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = id.hashCode();
		result = 31 * result + creationDate.hashCode();
		result = 31 * result + UID.hashCode();
		return result;
	}
}
