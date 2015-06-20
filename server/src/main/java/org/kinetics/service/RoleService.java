package org.kinetics.service;

import org.kinetics.dao.authorization.Role;
import org.kinetics.dao.authorization.RoleRepository;
import org.kinetics.dao.authorization.RolesEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

	@Autowired
	private RoleRepository roleRepo;

	/**
	 * 
	 * @return {@link Role} representing SiteAdmin
	 */
	public Role getSiteAdminRole() {
		return roleRepo.findByName(RolesEnum.SITE_ADMIN.name());
	}

	public Role getPatientRole() {
		return roleRepo.findByName(RolesEnum.PATIENT.name());
	}

}
