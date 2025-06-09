package uy.com.bay.cruds.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;


// Imports for XmlRpcClient, MalformedURLException etc. if we were to mock them
import uy.com.bay.cruds.config.OdooConfig;
import java.net.MalformedURLException; // Keep for existing test
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // Use lenient strictness to avoid UnnecessaryStubbingException for now
public class OdooServiceTest {

    @Mock
    private OdooConfig odooConfigMock;

    // OdooService instance will be created directly in tests for now,
    // as injecting mocks for its internal XmlRpcClients is complex without refactoring.
    private OdooService odooService;

    @BeforeEach
    void setUp() {
        // No default stubbings here to avoid UnnecessaryStubbingException.
        // Specific mocks will be set in each test method.
    }

    @Test
    void getOdooProjects_whenOdooUrlIsMalformed_thenThrowsRuntimeExceptionDuringConstruction() {
        // Specific mock for this test case
        when(odooConfigMock.getUrl()).thenReturn("bad_url-causes_malformed_url_exception");
        // No need to mock getDb, getUsername, getPassword as the constructor should fail before using them.

        Exception exception = assertThrows(RuntimeException.class, () -> {
            odooService = new OdooService(odooConfigMock);
        });
        assertTrue(exception.getMessage().contains("Error initializing Odoo XML-RPC client: Invalid URL"));
    }

    @Test
    void getOdooProjects_whenConfigIsValidButConnectionFails_returnsEmptyList() {
        // Setup mocks required for this test case for OdooService constructor to pass
        when(odooConfigMock.getUrl()).thenReturn("http://valid-but-likely-unreachable-url.com");
        // Mocks for getDb, getUsername, getPassword are needed if the constructor or authenticate() is called.
        // The new OdooService implementation calls these in authenticate() if the URL is valid.
        when(odooConfigMock.getDb()).thenReturn("test_db");
        when(odooConfigMock.getUsername()).thenReturn("test_user");
        when(odooConfigMock.getPassword()).thenReturn("test_pass");

        odooService = new OdooService(odooConfigMock); // Constructor should pass

        List<Map<String, Object>> projects = odooService.getOdooProjects();
        assertNotNull(projects, "Project list should not be null, even on failure.");
        assertTrue(projects.isEmpty(), "Expected empty list when Odoo connection or authentication fails");
    }

    // To truly test the successful data transformation logic within getOdooProjects,
    // OdooService would need to be refactored to allow injection of mocked XmlRpcClient
    // instances (e.g., via constructor injection or setters).
    // Without that, testing the success path of XML-RPC calls at a unit level
    // would require more advanced mocking tools like PowerMockito or a test-specific
    // subclassing approach, which are beyond the scope of this current setup.
    // The current tests focus on construction-time errors (bad URL) and runtime
    // connection/authentication failures leading to an empty project list.
    // Full validation of Odoo interaction logic will rely on integration testing (Step 6)
    // or manual testing with actual Odoo credentials.
}
