package org.kinetics.dao.authorization;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface RoleRepository extends CrudRepository<Role, Integer> {

	Role findByName(String name);

}
