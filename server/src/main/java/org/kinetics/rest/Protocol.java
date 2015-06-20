package org.kinetics.rest;

import com.lohika.protocol.core.response.error.ServerError;

/**
 * Our common protocol constants
 * 
 * @author akaverin
 * 
 */
public interface Protocol {

	/**
	 * Logic operational units names
	 * 
	 * @author akaverin
	 * 
	 */
	public interface Managers {

		String ACCOUNT_MANANGER = "AccountManager";

		String PROJECT_MANANGER = "ProjectManager";

		String TEST_SESSION_MANAGER = "TestSessionManager";

		String TEST_CONFIG_MANAGER = "TestConfigManager";

		String EXTENSION_MANAGER = "ExtensionManager";

		String SHARING_MANAGER = "SharingManager";

		String AUDIT_MANAGER = "AuditManager";
	}

	/**
	 * Arguments used by specific manager
	 * 
	 * @author akaverin
	 * 
	 */
	public interface Arguments {

		String PASSWORD = "passHash";
		String NEW_PASSWORD = "newPassHash";
		String TOKEN = "token";

		String PROJECT = "project";
		String PROJECT_NAME = "projectName";

		String ROLE = "role";

		String SECOND_NAME = "secondName";
		String FIRST_NAME = "firstName";
		String GENDER = "gender";
		String BIRTHDAY = "birthday";

		String CONFIRMATION_CODE = "confirmationCode";

		String TEST_SESSION = "testSession";

		String ID = "id";
		String IDS = "ids";

		String USER = "user";

		String SORT_FIELD = "sortField";
		String SORT_ORDER = "sortOrder";
		String PAGE = "page";
		String SIZE = "size";
		String DISABLE = "disable";
		String VALID = "valid";

		String SEARCH_TOKEN = "searchToken";
		String SEARCH_DATA = "searchData";
		// TODO: add later, to define exact search
		// String SEARCH_OPTIONS = "searchOptions";

		String SETTINGS = "settings";

		String DATE_FROM = "dateFrom";
		String DATE_TO = "dateTo";

		// captcha
		String CHALLENGE = "challenge";
		String SOLUTION = "solution";

		// extensions
		String EXTENSION = "extension";
		String ENTITY = "entity";
		String NAME = "name";
		String TYPE = "type";
		String PROPERTIES = "properties";
		String FILTERS = "filters";
		String LIST_DATA = "listdata";

		String SHARE_DATA = "shareData";
		String AUDIT_DATA = "auditData";

		// social
		String MESSAGE = "message";
		String URL = "urlPath";
	}

	public interface Errors {

		ServerError USER_ALREADY_EXISTS = new ServerError(800,
				"User already registered");
		ServerError USER_DOESNT_EXIST = new ServerError(801,
				"User doesn't exist in the system");
		ServerError USER_IS_ACTIVE = new ServerError(802,
				"User is already activated");
		ServerError USER_NOT_ACTIVE = new ServerError(803,
				"User is not activated yet");
		ServerError CONFIRMATION_CODE_INVALID = new ServerError(804,
				"Wrong confirmation code");
		ServerError CREDENTIALS_INVALID = new ServerError(805,
				"Provided credentials are invalid");
		ServerError PROJECT_ALREADY_EXISTS = new ServerError(806,
				"Project with this name already registered");
		ServerError TEST_NOT_FOUND = new ServerError(807,
				"No test found for supplied id");
		ServerError USER_DISABLED = new ServerError(808,
				"User account is disabled. Please, contact administrator");
		ServerError CAPTCHA_INVALID = new ServerError(809,
				"Provided captcha is invalid");
		ServerError PROJECT_DOESNT_EXIST = new ServerError(810,
				"Provided Project doesn't exist");
		ServerError EMAIL_ALREADY_EXISTS = new ServerError(811,
				"Email address already exists in system");

		// Extension related errors
		ServerError EXTENSION_LIST_EMPTY = new ServerError(812,
				"Empty extension lists are not supported");
		ServerError EXTENSION_NAME_DUPLICATE = new ServerError(813,
				"Extension name provided already in use.");
		ServerError EXTENSION_NOT_EXIST = new ServerError(814,
				"Provided Extension doesn't exist.");
		ServerError EXTENSION_WRONG_LIST_VALUE = new ServerError(815,
				"Provided value doesn't match any list option.");
		ServerError EXTENSION_REQUIRED_MISSING = new ServerError(816,
				"Required extension is missing.");
		ServerError PROJECT_DISABLED = new ServerError(817,
				"Project account is disabled. Please, contact administrator");
		ServerError PROJECT_IS_ACTIVE = new ServerError(818,
				"Project is already activated");

		ServerError PROJECT_NOT_ASSIGNED = new ServerError(819,
				"User do not have any active assigned projects");

		ServerError USER_INVALID_ROLE = new ServerError(820,
				"User role is not suitable for operation");
		ServerError SITE_ADMIN_DELETE = new ServerError(821,
				"Cannot delete last active Site Admin");

		ServerError NO_SHARED_DATA = new ServerError(822,
				"Cannot access shared data.");

		ServerError TOKEN_IS_PRESENT = new ServerError(823,
				"Token is already generated for requested resource");
	}
}
