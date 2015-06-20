package org.kinetics.dao.extension;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface ExtensionDataRepository extends
		CrudRepository<ExtensionData, Integer> {

	@Query("SELECT em FROM ExtensionData em WHERE em.metaData.entity = ?1 AND em.entityId = ?2")
	List<ExtensionData> findAllByEntity(ExtendedEntity entity, Integer entityId);

	@Query("SELECT em FROM ExtensionData em WHERE em.metaData.entity = ?1 AND em.entityId = ?2 "
			+ "AND ?3 MEMBER OF em.metaData.filters")
	List<ExtensionData> findAllByEntityAndFilter(ExtendedEntity entity,
			Integer entityId, ExtensionMetaFilter metaFilter);

	@Transactional
	@Modifying
	@Query("DELETE FROM ExtensionData em WHERE em.metaData.entity = ?1 AND em.entityId IN ?2")
	void deleteByEntities(ExtendedEntity entity, Collection<Integer> ids);

}
