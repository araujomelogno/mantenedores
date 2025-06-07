package uy.com.bay.cruds.views.encuestadores;

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
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import jakarta.annotation.security.PermitAll;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.vaadin.lineawesome.LineAwesomeIconUrl;
import uy.com.bay.cruds.data.Encuestador;
import uy.com.bay.cruds.services.EncuestadorService;

@PageTitle("Encuestadores")
@Route("surveyors/:encuestadorID?/:action?(edit)")
@Menu(order = 1, icon = LineAwesomeIconUrl.USER_ALT_SOLID)
@PermitAll
public class EncuestadoresView extends Div implements BeforeEnterObserver {

    private final String ENCUESTADOR_ID = "encuestadorID";
    private final String ENCUESTADOR_EDIT_ROUTE_TEMPLATE = "surveyors/%s/edit";

    private final Grid<Encuestador> grid = new Grid<>(Encuestador.class, false);

    private TextField firstName;
    private TextField lastName;
    private TextField ci;

    private Button addButton;
    private TextField firstNameFilter;
    private TextField lastNameFilter;
    private TextField ciFilter;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private final BeanValidationBinder<Encuestador> binder;

    private Encuestador encuestador;

    private final EncuestadorService encuestadorService;

    public EncuestadoresView(EncuestadorService encuestadorService) {
        this.encuestadorService = encuestadorService;
        addClassNames("encuestadores-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        addButton = new Button("Agregar Encuestador");
        // addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY); // Estilo opcional
        addButton.addClickListener(e -> {
            clearForm();
            this.encuestador = new Encuestador();
            binder.readBean(this.encuestador);
            // Opcionalmente, forzar que el panel de edición sea visible y tenga el foco
            // UI.getCurrent().navigate(EncuestadoresView.class); // Si quieres limpiar la URL también
        });

        firstNameFilter = new TextField();
        firstNameFilter.setPlaceholder("Nombre...");
        firstNameFilter.setClearButtonVisible(true);
        firstNameFilter.addValueChangeListener(e -> refreshGrid()); // Asume que refreshGrid() llama a dataProvider.refreshAll()

        lastNameFilter = new TextField();
        lastNameFilter.setPlaceholder("Apellido...");
        lastNameFilter.setClearButtonVisible(true);
        lastNameFilter.addValueChangeListener(e -> refreshGrid());

        ciFilter = new TextField();
        ciFilter.setPlaceholder("CI...");
        ciFilter.setClearButtonVisible(true);
        ciFilter.addValueChangeListener(e -> refreshGrid());

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("firstName").setHeader("Nombre").setAutoWidth(true);
        grid.addColumn("lastName").setHeader("Apellido").setAutoWidth(true);
        grid.addColumn("ci").setHeader("CI").setAutoWidth(true);

        grid.setItems(query -> {
            String fnameFilter = firstNameFilter.getValue() != null ? firstNameFilter.getValue().trim().toLowerCase() : "";
            String lnameFilter = lastNameFilter.getValue() != null ? lastNameFilter.getValue().trim().toLowerCase() : "";
            String ciValFilter = ciFilter.getValue() != null ? ciFilter.getValue().trim().toLowerCase() : "";

            // Obtener el stream del servicio
            java.util.stream.Stream<Encuestador> stream = encuestadorService.list(VaadinSpringDataHelpers.toSpringPageRequest(query)).stream();

            // Aplicar filtros si hay texto en los campos de filtro
            if (!fnameFilter.isEmpty()) {
                stream = stream.filter(enc -> enc.getFirstName() != null && enc.getFirstName().toLowerCase().contains(fnameFilter));
            }
            if (!lnameFilter.isEmpty()) {
                stream = stream.filter(enc -> enc.getLastName() != null && enc.getLastName().toLowerCase().contains(lnameFilter));
            }
            if (!ciValFilter.isEmpty()) {
                stream = stream.filter(enc -> enc.getCi() != null && enc.getCi().toLowerCase().contains(ciValFilter));
            }
            return stream;
        });
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(ENCUESTADOR_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(EncuestadoresView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Encuestador.class);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.encuestador == null) {
                    this.encuestador = new Encuestador();
                }
                binder.writeBean(this.encuestador);
                encuestadorService.save(this.encuestador);
                clearForm();
                refreshGrid();
                Notification.show("Data updated");
                UI.getCurrent().navigate(EncuestadoresView.class);
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
        Optional<Long> encuestadorId = event.getRouteParameters().get(ENCUESTADOR_ID).map(Long::parseLong);
        if (encuestadorId.isPresent()) {
            Optional<Encuestador> encuestadorFromBackend = encuestadorService.get(encuestadorId.get());
            if (encuestadorFromBackend.isPresent()) {
                populateForm(encuestadorFromBackend.get());
            } else {
                Notification.show(
                        String.format("The requested encuestador was not found, ID = %s", encuestadorId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(EncuestadoresView.class);
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
        firstName = new TextField("First Name");
        lastName = new TextField("Last Name");
        ci = new TextField("Ci");
        formLayout.add(firstName, lastName, ci);

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

        HorizontalLayout topBar = new HorizontalLayout();
        topBar.setWidthFull();
        // topBar.setSpacing(true); // Opcional para espaciado
        topBar.add(firstNameFilter, lastNameFilter, ciFilter, addButton);
        // Para alinear el botón a la derecha (más avanzado, podría requerir un Div espaciador o CSS)
        // Ejemplo simple para empujar el botón:
        // Div spacer = new Div();
        // spacer.getStyle().set("flex-grow", "1");
        // topBar.add(firstNameFilter, lastNameFilter, ciFilter, spacer, addButton);
        // O simplemente dejarlos en orden.

        wrapper.add(topBar); // Añadir topBar al wrapper ANTES del grid
        wrapper.add(grid);
        splitLayout.addToPrimary(wrapper);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(Encuestador value) {
        this.encuestador = value;
        binder.readBean(this.encuestador);

    }
}
