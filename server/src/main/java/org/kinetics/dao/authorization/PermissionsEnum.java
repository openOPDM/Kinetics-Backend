package org.kinetics.dao.authorization;

import java.util.EnumSet;
import java.util.Set;

public enum PermissionsEnum {
    MANAGE_PROFILE(Permission.MANAGE_PROFILE,
            EnumSet.allOf(RolesEnum.class)),
    MANAGE_USER(Permission.MANAGE_USER,
            EnumSet.of(RolesEnum.SITE_ADMIN)),
    GET_USER_DATA(Permission.GET_USER_DATA,
            EnumSet.of(RolesEnum.ANALYST)),
    MANAGE_ADMINISTRATOR(Permission.MANAGE_ADMINISTRATOR,
            EnumSet.of(RolesEnum.SITE_ADMIN)),
    MANAGE_CUSTOMER(Permission.MANAGE_CUSTOMER,
            EnumSet.of(RolesEnum.SITE_ADMIN)),
    MANAGE_SITE_ADMIN(Permission.MANAGE_SITE_ADMIN,
            EnumSet.of(RolesEnum.SITE_ADMIN)),
    MANAGE_PATIENT(Permission.MANAGE_PATIENT,
            EnumSet.of(RolesEnum.ANALYST)),
    RUN_TEST_FOR_PATIENT(Permission.RUN_TEST_FOR_PATIENT,
            EnumSet.of(RolesEnum.ANALYST)),
	GET_EXTENSIONS(Permission.GET_EXTENSIONS,
            EnumSet.allOf(RolesEnum.class)),
	MANAGE_EXTENSIONS(Permission.MANAGE_EXTENSIONS,
            EnumSet.of(RolesEnum.SITE_ADMIN)),
    SEND_INVITATION_TO_PATIENT(Permission.SEND_INVITATION_TO_PATIENT,
            EnumSet.of(RolesEnum.ANALYST)),
    EXPORT_TEST_DATA(Permission.EXPORT_TEST_DATA, EnumSet.of(RolesEnum.SITE_ADMIN)),
    AUDIT_DATA(Permission.AUDIT_DATA, EnumSet.of(RolesEnum.SITE_ADMIN));

	private final Set<RolesEnum> roles;
	private final String name;

	private PermissionsEnum(String name, Set<RolesEnum> roles) {
		this.name = name;
		this.roles = roles;
	}

	public Set<RolesEnum> getRoles() {
		return roles;
	}

	public String getName() {
		return name;
	}

	public static final class Permission {
        public static final String MANAGE_PROFILE = "MANAGE_PROFILE";
		public static final String MANAGE_USER = "MANAGE_USER";
		public static final String GET_USER_DATA = "GET_USER_DATA";
		public static final String MANAGE_ADMINISTRATOR = "MANAGE_ADMINISTRATOR";
		public static final String MANAGE_SITE_ADMIN = "MANAGE_SITE_ADMIN";
		public static final String MANAGE_CUSTOMER = "MANAGE_CUSTOMER";
		public static final String MANAGE_PATIENT = "MANAGE_PATIENT";
		public static final String RUN_TEST_FOR_PATIENT = "RUN_TEST_FOR_PATIENT";
		public static final String SEND_INVITATION_TO_PATIENT = "SEND_INVITATION";
		public static final String GET_EXTENSIONS = "GET_EXTENSIONS";
		public static final String MANAGE_EXTENSIONS = "MANAGE_EXTENSIONS";
		public static final String EXPORT_TEST_DATA = "EXPORT_TEST_DATA";
		public static final String AUDIT_DATA = "AUDIT_DATA";
	}
}
