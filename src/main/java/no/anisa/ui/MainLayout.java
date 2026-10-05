package no.anisa.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.theme.lumo.Lumo;

import no.anisa.ui.cpu.CpuSchedulingView;
import no.anisa.ui.paging.PageReplacementView;

public class MainLayout extends AppLayout implements RouterLayout {

    private boolean darkMode = false;

    public MainLayout() {
        DrawerToggle toggle = new DrawerToggle();

        H1 title = new H1("OS Scheduler Visualizer");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)").set("margin", "0");

        Button darkModeToggle = new Button(VaadinIcon.MOON.create());
        darkModeToggle.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        darkModeToggle.getElement().setAttribute("aria-label", "Toggle dark mode");
        darkModeToggle.addClickListener(event -> toggleDarkMode());

        addToNavbar(toggle, title, darkModeToggle);

        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("CPU Scheduling", CpuSchedulingView.class, VaadinIcon.CLOCK.create()));
        nav.addItem(new SideNavItem("Page Replacement", PageReplacementView.class, VaadinIcon.TABLE.create()));

        addToDrawer(nav);
    }

    private void toggleDarkMode() {
        darkMode = !darkMode;
        UI.getCurrent().getElement().getThemeList().set(Lumo.DARK, darkMode);
    }
}
