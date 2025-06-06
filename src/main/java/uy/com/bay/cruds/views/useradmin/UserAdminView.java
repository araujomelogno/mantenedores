package uy.com.bay.cruds.views.useradmin;

import java.util.Optional;
import java.util.Set;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;

import jakarta.annotation.security.RolesAllowed;
import uy.com.bay.cruds.data.Role;
import uy.com.bay.cruds.data.User;
import uy.com.bay.cruds.services.UserService;

@PageTitle("Usuarios")
@Route("useradmin/:samplePersonID?/:action?(edit)")
@Menu(order = 2, icon = LineAwesomeIconUrl.COLUMNS_SOLID)
@RolesAllowed("ADMIN")
@Uses(Icon.class)
public class UserAdminView extends Div implements BeforeEnterObserver {

	private final String SAMPLEPERSON_ID = "samplePersonID";
	private final String SAMPLEPERSON_EDIT_ROUTE_TEMPLATE = "useradmin/%s/edit";

	private final Grid<User> grid = new Grid<>(User.class, false);

	private TextField userName;
	PasswordField password;
	private ComboBox<Role> roles;

	private final Button cancel = new Button("Cancel");
	private final Button save = new Button("Save");

	private final BeanValidationBinder<User> binder;

	private User user;

	private final UserService userService;

	public UserAdminView(UserService userService) {
		this.userService = userService;
		addClassNames("useradmin-view");

		roles = new ComboBox<>("Role");
		roles.setItems(Role.values());
		roles.setItemLabelGenerator(Role::name);

		// Create UI
		SplitLayout splitLayout = new SplitLayout();

		createGridLayout(splitLayout);
		createEditorLayout(splitLayout);

		add(splitLayout);

		// Configure Grid
		grid.addColumn("username").setAutoWidth(true);
		grid.addColumn(user -> {
		    Set<Role> userRoles = user.getRoles();
		    if (userRoles != null && !userRoles.isEmpty()) {
		        return userRoles.iterator().next().name();
		    }
		    return "";
		}).setHeader("Role").setAutoWidth(true);

		grid.setItems(query -> userService.list(VaadinSpringDataHelpers.toSpringPageRequest(query)).stream());
		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

		// when a row is selected or deselected, populate form
		grid.asSingleSelect().addValueChangeListener(event -> {
			if (event.getValue() != null) {
				UI.getCurrent().navigate(String.format(SAMPLEPERSON_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
			} else {
				clearForm();
				UI.getCurrent().navigate(UserAdminView.class);
			}
		});

		// Configure Form
		binder = new BeanValidationBinder<>(User.class);

		// Bind fields. This is where you'd define e.g. validation rules
		binder.forField(roles)
			    .withConverter(role -> role != null ? java.util.Set.of(role) : java.util.Collections.emptySet(),
			                   set -> set != null && !set.isEmpty() ? set.iterator().next() : null)
			    .bind(User::getRoles, User::setRoles);

		binder.bindInstanceFields(this);

		cancel.addClickListener(e -> {
			clearForm();
			refreshGrid();
		});

		save.addClickListener(e -> {
			try {
				if (this.user == null) {
					this.user = new User();
				}
				binder.writeBean(this.user);
				userService.save(this.user);
				clearForm();
				refreshGrid();
				Notification.show("Data updated");
				UI.getCurrent().navigate(UserAdminView.class);
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
		Optional<Long> samplePersonId = event.getRouteParameters().get(SAMPLEPERSON_ID).map(Long::parseLong);
		if (samplePersonId.isPresent()) {
			Optional<User> userFromBackend = userService.get(samplePersonId.get());
			if (userFromBackend.isPresent()) {
				populateForm(userFromBackend.get());
			} else {
				Notification.show(
						String.format("The requested samplePerson was not found, ID = %s", samplePersonId.get()), 3000,
						Notification.Position.BOTTOM_START);
				// when a row is selected but the data is no longer available,
				// refresh grid
				refreshGrid();
				event.forwardTo(UserAdminView.class);
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
		userName = new TextField("Usuario:");
		password = new PasswordField();
		formLayout.add(userName, password, roles);

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

	private void populateForm(User value) {
		this.user = value;
		binder.readBean(this.user);

	}
}
