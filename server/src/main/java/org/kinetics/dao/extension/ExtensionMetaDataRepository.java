package org.kinetics.dao.extension;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface ExtensionMetaDataRepository extends
		CrudRepository<ExtensionMetaData, Integer> {

	List<ExtensionMetaData> findAllByEntity(ExtendedEntity entity);

	ExtensionMetaData findOneByNameAndEntity(String name, ExtendedEntity entity);

	@Transactional
	@Modifying
	@Query("DELETE FROM ExtensionMetaData e WHERE e.id IN ?1")
	void deleteById(Collection<Integer> ids);

}
