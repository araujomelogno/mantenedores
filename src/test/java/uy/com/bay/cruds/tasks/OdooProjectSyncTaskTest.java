package uy.com.bay.cruds.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uy.com.bay.cruds.data.Proyecto;
import uy.com.bay.cruds.services.OdooService;
import uy.com.bay.cruds.services.ProyectoService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OdooProjectSyncTaskTest {

    @Mock
    private OdooService odooService;

    @Mock
    private ProyectoService proyectoService;

    @InjectMocks
    private OdooProjectSyncTask odooProjectSyncTask;

    private List<Map<String, Object>> odooProjectsList;
    private List<Proyecto> existingProyectosList;

    @BeforeEach
    void setUp() {
        odooProjectsList = new ArrayList<>();
        existingProyectosList = new ArrayList<>();
    }

    @Test
    void syncOdooProjects_whenNoOdooProjects_thenNoNewProjectsSaved() {
        when(odooService.getOdooProjects()).thenReturn(Collections.emptyList());

        odooProjectSyncTask.syncOdooProjects();

        verify(proyectoService, never()).save(any(Proyecto.class));
    }

    @Test
    void syncOdooProjects_whenNewProjectsFromOdoo_thenNewProjectsAreSaved() {
        Map<String, Object> newOdooProject = new HashMap<>();
        newOdooProject.put("id", "odoo123");
        newOdooProject.put("name", "New Odoo Project");
        odooProjectsList.add(newOdooProject);

        when(odooService.getOdooProjects()).thenReturn(odooProjectsList);
        when(proyectoService.findAll()).thenReturn(Collections.emptyList());
        // Mock the save operation to return the saved entity, if needed by other logic not present here
        when(proyectoService.save(any(Proyecto.class))).thenAnswer(invocation -> invocation.getArgument(0));


        odooProjectSyncTask.syncOdooProjects();

        verify(proyectoService, times(1)).save(argThat(p -> "odoo123".equals(p.getOdooId()) && "New Odoo Project".equals(p.getName())));
    }

    @Test
    void syncOdooProjects_whenSomeNewAndSomeExistingProjects_thenOnlyNewAreSaved() {
        // Existing project
        Proyecto existingProyecto = new Proyecto();
        existingProyecto.setOdooId("odooExisting");
        existingProyecto.setName("Existing Project");
        existingProyectosList.add(existingProyecto);

        Map<String, Object> odooProjectExisting = new HashMap<>();
        odooProjectExisting.put("id", "odooExisting");
        odooProjectExisting.put("name", "Existing Project From Odoo"); // Name might differ
        odooProjectsList.add(odooProjectExisting);

        Map<String, Object> odooProjectNew = new HashMap<>();
        odooProjectNew.put("id", "odooNew123");
        odooProjectNew.put("name", "Another New Project");
        odooProjectsList.add(odooProjectNew);

        when(odooService.getOdooProjects()).thenReturn(odooProjectsList);
        when(proyectoService.findAll()).thenReturn(existingProyectosList);
        when(proyectoService.save(any(Proyecto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        odooProjectSyncTask.syncOdooProjects();

        verify(proyectoService, times(1)).save(any(Proyecto.class));
        verify(proyectoService, times(1)).save(argThat(p -> "odooNew123".equals(p.getOdooId())));
        verify(proyectoService, never()).save(argThat(p -> "odooExisting".equals(p.getOdooId())));
    }


    @Test
    void syncOdooProjects_whenExistingProjectsFromOdoo_thenNoNewProjectsAreSaved() {
        Map<String, Object> existingOdooProject = new HashMap<>();
        existingOdooProject.put("id", "odoo456");
        existingOdooProject.put("name", "Existing Odoo Project");
        odooProjectsList.add(existingOdooProject);

        Proyecto existingLocalProyecto = new Proyecto();
        existingLocalProyecto.setOdooId("odoo456");
        existingLocalProyecto.setName("Existing Local Project");
        existingProyectosList.add(existingLocalProyecto);

        when(odooService.getOdooProjects()).thenReturn(odooProjectsList);
        when(proyectoService.findAll()).thenReturn(existingProyectosList);

        odooProjectSyncTask.syncOdooProjects();

        verify(proyectoService, never()).save(any(Proyecto.class));
    }

    @Test
    void syncOdooProjects_whenOdooProjectHasNullId_thenProjectIsSkipped() {
        Map<String, Object> projectWithNullId = new HashMap<>();
        projectWithNullId.put("id", null);
        projectWithNullId.put("name", "Project With Null ID");
        odooProjectsList.add(projectWithNullId);

        when(odooService.getOdooProjects()).thenReturn(odooProjectsList);
        // No need to mock findAll or save if it's skipped before that

        odooProjectSyncTask.syncOdooProjects();

        verify(proyectoService, never()).save(any(Proyecto.class));
    }

    @Test
    void syncOdooProjects_whenOdooProjectHasEmptyId_thenProjectIsSkipped() {
        Map<String, Object> projectWithEmptyId = new HashMap<>();
        projectWithEmptyId.put("id", "");
        projectWithEmptyId.put("name", "Project With Empty ID");
        odooProjectsList.add(projectWithEmptyId);

        when(odooService.getOdooProjects()).thenReturn(odooProjectsList);

        odooProjectSyncTask.syncOdooProjects();

        verify(proyectoService, never()).save(any(Proyecto.class));
    }

}
