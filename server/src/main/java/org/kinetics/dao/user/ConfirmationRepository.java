package org.kinetics.dao.user;

import java.util.Collection;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface ConfirmationRepository extends
		CrudRepository<Confirmation, Integer> {

	Confirmation findOneByCode(String code);

	Collection<Confirmation> findByUser(User user);

	@Transactional
	@Modifying
	@Query("delete from Confirmation c where c.user = ?1")
	void deleteByUser(User user);

}
