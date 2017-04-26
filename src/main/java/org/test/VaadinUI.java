package org.test;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.WebListener;
import javax.servlet.annotation.WebServlet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.context.ContextLoaderListener;

import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.spring.annotation.EnableVaadin;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.server.SpringVaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SpringUI
public class VaadinUI extends UI {

	private static final String GRID_SORT_PREFERENCE_SEPARATOR = ":";

	private static final String GRID_SORT_PREFERENCE_NEXT_SEPERATOR = ">";

	@Autowired
	private CustomerRepository repo;

	@Autowired
	private CustomerEditor editor;

	final Grid<Customer> grid;

	final TextField filter;

	private final Button addNewBtn;

	@WebServlet(value = "/*", asyncSupported = true)
	public static class Servlet extends SpringVaadinServlet {
	}

	@WebListener
	public static class MyContextLoaderListener extends ContextLoaderListener {
	}

	@Configuration
	@EnableVaadin
	public static class MyConfiguration {
	}

	public VaadinUI() {
		this.grid = new Grid<>(Customer.class);
		this.filter = new TextField();
		this.addNewBtn = new Button("New customer", FontAwesome.PLUS);
	}

	@Override
	protected void init(VaadinRequest request) {
		// build layout
		HorizontalLayout actions = new HorizontalLayout(filter, addNewBtn);
		VerticalLayout mainLayout = new VerticalLayout(actions, editor);
		mainLayout.addComponentsAndExpand(grid);
		setContent(mainLayout);

		filter.setPlaceholder("Filter by last name");

		// Hook logic to components

		// Replace listing with filtered content when user changes filter
		filter.setValueChangeMode(ValueChangeMode.LAZY);
		filter.addValueChangeListener(e -> listCustomers(e.getValue()));

		// Connect selected Customer to editor or hide if none is selected
		grid.asSingleSelect().addValueChangeListener(e -> {
			editor.editCustomer(e.getValue());
		});

		// Instantiate and edit new Customer the new button is clicked
		addNewBtn.addClickListener(e -> editor.editCustomer(new Customer("", "")));

		// Listen changes made by the editor, refresh data from backend
		editor.setChangeHandler(() -> {
			editor.setVisible(false);
			listCustomers(filter.getValue());
		});

		// Initialize listing
		listCustomers(null);
	}

	// tag::listCustomers[]
	void listCustomers(String filterText) {

		// repo.findAll(query -> repo.find(new PageRequest(query.get

		grid.setDataProvider((sortOrders, offset, limit) -> fromGridQuery(sortOrders, offset, limit).stream(),
				() -> (int) repo.count());
		// if (StringUtils.isEmpty(filterText)) {
		// grid.setItems(repo.findAll());
		// }
		// else {
		// grid.setItems(repo.findByLastNameStartsWithIgnoreCase(filterText));
		// }
	}
	// end::listCustomers[]

	private List<Customer> fromGridQuery(List<QuerySortOrder> sortOrders, int offset, int limit) {
		final int pageSize = limit;
		int startPage = (int) Math.floor((double) offset / pageSize);
		int endPage = (int) Math.floor((double) (offset + pageSize - 1) / pageSize);

		if (startPage != endPage) {
			// if (!sortOrders.isEmpty()) {
			// QuerySortOrder sortOrder = sortOrders.get(0);
			// if (sortOrder.getDirection() == SortDirection.ASCENDING) {
			// repo.findAll(new PageRequest(startPage, pageSize, Direction.ASC,
			// sortOrder.getSorted())).getContent();
			// } else {
			// return new PageRequest(startPage, pageSize, Direction.DESC,
			// sortOrder.getSorted());
			// }
			// }
			// else {
			List<Customer> page0 = repo.findAll(new PageRequest(startPage, pageSize)).getContent();
			page0 = page0.subList(offset % pageSize, page0.size());
			List<Customer> page1 = repo.findAll(new PageRequest(endPage, pageSize)).getContent();
			page1 = page1.subList(0, limit - page0.size());
			List<Customer> result = new ArrayList<Customer>(page0);
			result.addAll(page1);
			return result;
			// }
		} else {
			// if (!sortOrders.isEmpty()) {
			// QuerySortOrder sortOrder = sortOrders.get(0);
			// if (sortOrder.getDirection() == SortDirection.ASCENDING) {
			// requests.add(new PageRequest(startPage, pageSize, Direction.ASC,
			// sortOrder.getSorted()));
			// } else {
			// requests.add(new PageRequest(startPage, pageSize, Direction.DESC,
			// sortOrder.getSorted()));
			// }
			// }
			// else {
			return repo.findAll(new PageRequest(startPage, pageSize)).getContent();
			// }
		}

	}

}
