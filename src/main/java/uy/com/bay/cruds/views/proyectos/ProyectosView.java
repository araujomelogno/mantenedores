package uy.com.bay.cruds.views.proyectos;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import jakarta.annotation.security.PermitAll;
import java.util.Optional;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import uy.com.bay.cruds.data.Proyecto;
import uy.com.bay.cruds.services.ProyectoService;

@PageTitle("Proyectos")
@Route("/:proyectoID?/:action?(edit)")
@Menu(order = 0, icon = LineAwesomeIconUrl.BRIEFCASE_SOLID)
@RouteAlias("")
@PermitAll
public class ProyectosView extends Div implements BeforeEnterObserver {

    private final String PROYECTO_ID = "proyectoID";
    private final String PROYECTO_EDIT_ROUTE_TEMPLATE = "/%s/edit";

    private final Grid<Proyecto> grid = new Grid<>(Proyecto.class, false);

    private TextField name;
    private TextField alchemerId;
    private TextField doobloId;
    private TextField odooId;
    private TextField obs;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private final BeanValidationBinder<Proyecto> binder;

    private Proyecto proyecto;

    private final ProyectoService proyectoService;

    public ProyectosView(ProyectoService proyectoService) {
        this.proyectoService = proyectoService;
        addClassNames("proyectos-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("name").setAutoWidth(true);
        grid.addColumn("alchemerId").setAutoWidth(true);
        grid.addColumn("doobloId").setAutoWidth(true);
        grid.addColumn("odooId").setAutoWidth(true);
        grid.addColumn("obs").setAutoWidth(true);
        grid.setItems(query -> proyectoService.list(VaadinSpringDataHelpers.toSpringPageRequest(query)).stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(PROYECTO_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(ProyectosView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Proyecto.class);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.proyecto == null) {
                    this.proyecto = new Proyecto();
                }
                binder.writeBean(this.proyecto);
                proyectoService.save(this.proyecto);
                clearForm();
                refreshGrid();
                Notification.show("Data updated");
                UI.getCurrent().navigate(ProyectosView.class);
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show(
                        "Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Failed to update the data. Check again that all values are valid");
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> proyectoId = event.getRouteParameters().get(PROYECTO_ID).map(Long::parseLong);
        if (proyectoId.isPresent()) {
            Optional<Proyecto> proyectoFromBackend = proyectoService.get(proyectoId.get());
            if (proyectoFromBackend.isPresent()) {
                populateForm(proyectoFromBackend.get());
            } else {
                Notification.show(String.format("The requested proyecto was not found, ID = %s", proyectoId.get()),
                        3000, Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(ProyectosView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        name = new TextField("Name");
        alchemerId = new TextField("Alchemer Id");
        doobloId = new TextField("Dooblo Id");
        odooId = new TextField("Odoo Id");
        obs = new TextField("Obs");
        formLayout.add(name, alchemerId, doobloId, odooId, obs);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(Proyecto value) {
        this.proyecto = value;
        binder.readBean(this.proyecto);

    }
}
