package org.bonitasoft.democlientv6;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.ApiAccessType;
import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.data.ArchivedDataInstance;
import org.bonitasoft.engine.bpm.data.ArchivedDataNotFoundException;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstancesSearchDescriptor;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.platform.LoginException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.util.APITypeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	/** SLF4J logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	/** Address of JEE application server where Bonita web app is deployed */
	private static final String BONITA_SERVER_HOSTNAME = "localhost";

	/** Listening port of JEE application server where Bonita web app is deployed */ 
	private static final String BONITA_SERVER_PORT = "8080";

	/** name of the Bonita web app */
	private static final String BONITA_WEBAPP_NAME = "bonita";

	/** Name of a process deployed */
	private static final String PROCESS_NAME = "process_name";

	public static void main(String[] args) throws Exception {

		// Create a session to Bonita Engine using HTTP channel
		APISession session = httpConnect();

		// Get a reference to processAPI using current session 
		ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(session);

		// Display currently archived process instances informations 
		displayArchivedProcessInstanceInformation(processAPI);
	}

	/**
	 * Search for archived process instance for process definition name define
	 * by @see {@link Main#PROCESS_NAME}. Display:
	 * <ul>
	 * <li>Archived process instance root process instance id (id of the top
	 * parent process instance, id might no longer match to an existing instance
	 * if moved to archive).</li>
	 * <li>Id of process instance associated with the archived process instance.
	 * Information about process instance are moved from "process instance" to
	 * "archived process instance" as soon as possible. Information might </li>
	 * </ul>
	 * 
	 * @param processAPI
	 * @throws SearchException
	 * @throws ArchivedDataNotFoundException
	 */
	private static void displayArchivedProcessInstanceInformation(
			ProcessAPI processAPI) throws SearchException,
			ArchivedDataNotFoundException {
		SearchOptions searchOptions = new SearchOptionsBuilder(0, 250).filter(
				ArchivedProcessInstancesSearchDescriptor.NAME, PROCESS_NAME)
				.done();
		SearchResult<ArchivedProcessInstance> archivedProcessInstanceResults = processAPI
				.searchArchivedProcessInstances(searchOptions);

		List<ArchivedProcessInstance> archives = archivedProcessInstanceResults
				.getResult();

		LOGGER.info("Found {} archived process instances for process {}",
				archives.size(), PROCESS_NAME);

		for (ArchivedProcessInstance archivedProcessInstance : archives) {
			long sourceObjectId = archivedProcessInstance.getSourceObjectId();
			long rootProcessInstanceId = archivedProcessInstance
					.getRootProcessInstanceId();

			LOGGER.info("Root process instance: {}", rootProcessInstanceId);
			LOGGER.info("Source object id: {}", sourceObjectId);
			LOGGER.info("Process name: {}", archivedProcessInstance.getName());

			ArchivedDataInstance di = processAPI
					.getArchivedProcessDataInstance("data_name", sourceObjectId);
			String valueToRetrieve = di.getValue().toString();
			LOGGER.info("value: {}", valueToRetrieve);
		}
	}

	private static APISession httpConnect() throws BonitaHomeNotSetException,
			ServerAPIException, UnknownAPITypeException, LoginException {

		// Create a Map to configure Bonita Client
		Map<String, String> apiTypeManagerParams = new HashMap<>();

		// Use HTTP connection to Bonita Engine API
		APITypeManager.setAPITypeAndParams(ApiAccessType.HTTP,
				apiTypeManagerParams);

		// URL for server (without web app name)
		apiTypeManagerParams.put("server.url", "http://"
				+ BONITA_SERVER_HOSTNAME + ":" + BONITA_SERVER_PORT);

		// Bonita web application name
		apiTypeManagerParams.put("application.name", BONITA_WEBAPP_NAME);

		// Get a reference to login API and create a session for user
		// walter.bates (this user exist in default organization available in
		// Bonita Studio test environment)
		LoginAPI loginAPI = TenantAPIAccessor.getLoginAPI();
		APISession session = loginAPI.login("walter.bates", "bpm");
		return session;
	}
}
