package uy.com.bay.cruds.services;

import uy.com.bay.cruds.config.OdooConfig;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// java.util.Vector is not directly used in the provided code, so I'm omitting it for now.
// If it's needed by a dependency or a future version, it can be added.

@Service
public class OdooService {

    private static final Logger logger = LoggerFactory.getLogger(OdooService.class);
    private final OdooConfig odooConfig;
    private XmlRpcClient objectClient; // For model operations
    private XmlRpcClient commonClient; // For authentication

    public OdooService(OdooConfig odooConfig) {
        this.odooConfig = odooConfig;
        try {
            XmlRpcClientConfigImpl commonConfig = new XmlRpcClientConfigImpl();
            commonConfig.setServerURL(new URL(odooConfig.getUrl() + "/xmlrpc/2/common"));
            commonClient = new XmlRpcClient();
            commonClient.setConfig(commonConfig);
            // It's good practice to set connection and read timeouts
            // commonConfig.setConnectionTimeout(60 * 1000); // 60 seconds
            // commonConfig.setReplyTimeout(60 * 1000);     // 60 seconds


            XmlRpcClientConfigImpl objectConfig = new XmlRpcClientConfigImpl();
            objectConfig.setServerURL(new URL(odooConfig.getUrl() + "/xmlrpc/2/object"));
            objectClient = new XmlRpcClient();
            objectClient.setConfig(objectConfig);
            // It's good practice to set connection and read timeouts
            // objectConfig.setConnectionTimeout(60 * 1000); // 60 seconds
            // objectConfig.setReplyTimeout(60 * 1000);     // 60 seconds


        } catch (MalformedURLException e) {
            logger.error("Malformed Odoo URL: {}", odooConfig.getUrl(), e);
            // Consider how to handle this state - perhaps the service should not be usable.
            // For now, subsequent calls will likely fail if clients are null.
            throw new RuntimeException("Error initializing Odoo XML-RPC client: Invalid URL", e);
        }
    }

    private Integer authenticate() throws XmlRpcException {
        if (commonClient == null) {
            logger.error("Odoo common client not initialized. Cannot authenticate.");
            throw new IllegalStateException("Odoo common client not initialized.");
        }
        Object result = commonClient.execute("authenticate", Arrays.asList(
                odooConfig.getDb(),
                odooConfig.getUsername(),
                odooConfig.getPassword(),
                Collections.emptyMap()
        ));
        if (result instanceof Integer) {
            Integer uid = (Integer) result;
            if (uid != 0) { // Odoo returns 0 or false for failed login, uid > 0 for success
                 logger.info("Successfully authenticated with Odoo. UID: {}", uid);
                return uid;
            }
        }
        logger.error("Odoo authentication failed. Result: {}. Check credentials, DB name, and Odoo URL.", result);
        return null; // Or throw a specific authentication exception
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getOdooProjects() {
        if (objectClient == null) {
            logger.error("Odoo object client not initialized. Cannot fetch projects.");
            return Collections.emptyList();
        }
        try {
            Integer uid = authenticate();
            if (uid == null) {
                logger.error("Cannot fetch projects: Authentication failed.");
                return Collections.emptyList();
            }

            List<String> fieldsToFetch = Arrays.asList("id", "name");
            List<Object> domain = Collections.emptyList(); // Fetch all projects

            HashMap<String, Object> keywordArgs = new HashMap<>();
            keywordArgs.put("fields", fieldsToFetch);
            // keywordArgs.put("limit", 10); // Optional: for pagination
            // keywordArgs.put("offset", 0); // Optional: for pagination

            Object[] params = new Object[]{
                    odooConfig.getDb(),
                    uid,
                    odooConfig.getPassword(),
                    "project.project", // Odoo model name for projects
                    "search_read",     // Method to call
                    Collections.singletonList(domain),
                    keywordArgs
            };

            logger.info("Executing Odoo search_read on 'project.project' with fields: {}", fieldsToFetch);
            Object[] projectsRaw = (Object[]) objectClient.execute("execute_kw", params);

            List<Map<String, Object>> projectsList = new ArrayList<>();
            for (Object projectObj : projectsRaw) {
                if (projectObj instanceof Map) {
                    projectsList.add((Map<String, Object>) projectObj);
                } else {
                    logger.warn("Received an object that is not a Map from Odoo: {}", projectObj);
                }
            }
            logger.info("Successfully fetched {} projects from Odoo.", projectsList.size());
            return projectsList;

        } catch (XmlRpcException e) {
            logger.error("XmlRpcException while fetching projects from Odoo: {}. Check Odoo XML-RPC endpoint and network.", e.getMessage(), e);
        } catch (ClassCastException e) {
            logger.error("ClassCastException while processing Odoo response. Unexpected data structure: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected exception while fetching projects from Odoo: {}", e.getMessage(), e);
        }
        return Collections.emptyList(); // Return empty list in case of any error
    }
}
