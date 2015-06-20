package org.kinetics.dao.authorization;

import java.util.Collection;
import java.util.HashSet;

import org.kinetics.dao.session.Session;
import org.kinetics.dao.session.SessionProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lohika.server.core.validator.AuthorizationProvider;

@Component
public class AuthorizationProviderImpl implements
		AuthorizationProvider<Collection<String>> {

	@Autowired
	private SessionProviderImpl sessionProvider;

	@Override
	public Collection<String> getPermissions(String sessionToken) {

		Session session = sessionProvider.find(sessionToken);

		Collection<String> permissionsHashSet = new HashSet<String>();

		for (Role role : session.getUser().getRoles()) {
			RolesEnum roleEnum = RolesEnum.valueOf(role.getName());
			for (PermissionsEnum permissionsEnum : PermissionsEnum.values()) {
				if (permissionsEnum.getRoles().contains(roleEnum)) {
					permissionsHashSet.add(permissionsEnum.getName());
				}
			}
		}

		return permissionsHashSet;
	}
}
