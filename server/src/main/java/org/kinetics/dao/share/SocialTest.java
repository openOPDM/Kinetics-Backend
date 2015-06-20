package org.kinetics.dao.share;

import static java.util.UUID.randomUUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.eclipse.persistence.annotations.CascadeOnDelete;
import org.kinetics.dao.testsession.TestSession;

@Entity
@CascadeOnDelete
public class SocialTest {

	@JsonIgnore
	@Id
	@GeneratedValue
	private Integer id;

	@JoinColumn(unique = true, nullable = false)
	@OneToOne
	@CascadeOnDelete
	private TestSession testSession;

	@Column(updatable = false)
	private String token;

	public SocialTest() {
	}

	@PrePersist
	public void prePersist() {
		if (this.token == null) {
			this.token = randomUUID().toString();
		}
	}

	public SocialTest(TestSession testSession) {
		this.testSession = testSession;
	}

	public TestSession getTestSession() {
		return testSession;
	}

	public void setTestSession(TestSession testSession) {
		this.testSession = testSession;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Integer getId() {
		return id;
	}

	@Override
	public String toString() {
		return "SocialTest [id=" + id + ", testSession=" + testSession
				+ ", token=" + token + "]";
	}

}
