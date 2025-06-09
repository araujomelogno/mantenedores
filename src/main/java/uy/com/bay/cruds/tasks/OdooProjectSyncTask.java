package uy.com.bay.cruds.tasks;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uy.com.bay.cruds.data.Proyecto;
import uy.com.bay.cruds.services.OdooService;
import uy.com.bay.cruds.services.ProyectoService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class OdooProjectSyncTask {

    private final OdooService odooService;
    private final ProyectoService proyectoService;

    public OdooProjectSyncTask(OdooService odooService, ProyectoService proyectoService) {
        this.odooService = odooService;
        this.proyectoService = proyectoService;
    }

    @Scheduled(cron = "0 0 * * * ?") // Runs every hour at the beginning of the hour
    // For testing, you might use a more frequent cron like "*/30 * * * * ?" (every 30 seconds)
    // @Scheduled(cron = "*/30 * * * * ?")
    public void syncOdooProjects() {
        System.out.println("Starting Odoo Project Sync Task...");

        List<Map<String, Object>> odooProjects = odooService.getOdooProjects();
        if (odooProjects.isEmpty()) {
            System.out.println("No projects fetched from Odoo. Sync task finished.");
            return;
        }

        List<Proyecto> existingProyectos = proyectoService.findAll();
        Set<String> existingOdooIds = existingProyectos.stream()
                                                     .map(Proyecto::getOdooId)
                                                     .filter(id -> id != null && !id.isEmpty())
                                                     .collect(Collectors.toSet());

        int newProjectsCount = 0;
        for (Map<String, Object> odooProjectMap : odooProjects) {
            // Assuming Odoo project map contains "id" as the Odoo ID and "name" as the project name.
            // These keys might need adjustment based on the actual data from OdooService.
            Object odooIdObj = odooProjectMap.get("id");
            String odooId = null;
            if (odooIdObj != null) {
                odooId = String.valueOf(odooIdObj); // Convert to String, ensure it's not null
            }

            if (odooId == null || odooId.trim().isEmpty()) {
                System.out.println("Skipping Odoo project with null or empty ID.");
                continue;
            }

            if (!existingOdooIds.contains(odooId)) {
                Proyecto newProyecto = new Proyecto();
                newProyecto.setOdooId(odooId);

                Object nameObj = odooProjectMap.get("name");
                if (nameObj != null) {
                    newProyecto.setName(String.valueOf(nameObj));
                } else {
                    newProyecto.setName("Default Name - ID: " + odooId); // Or handle as an error
                }

                // Map other fields as necessary from odooProjectMap to newProyecto
                // For example:
                // String description = (String) odooProjectMap.get("description");
                // newProyecto.setObs(description);

                proyectoService.save(newProyecto);
                newProjectsCount++;
                System.out.println("Saved new project: " + newProyecto.getName() + " (Odoo ID: " + odooId + ")");
            }
        }

        if (newProjectsCount > 0) {
            System.out.println("Odoo Project Sync Task finished. Added " + newProjectsCount + " new project(s).");
        } else {
            System.out.println("Odoo Project Sync Task finished. No new projects to add.");
        }
    }
}
