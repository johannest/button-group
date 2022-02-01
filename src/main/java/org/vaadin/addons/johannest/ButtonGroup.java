package org.vaadin.addons.johannest;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.internal.nodefeature.ElementAttributeMap;
import com.vaadin.flow.internal.nodefeature.NodeFeature;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Simple Button group component for allowing setDisableOnClick for multiple button.
 * The setDisableOnClick implementation is mimicked from {@link Button}.
 */
public class ButtonGroup extends HorizontalLayout {

    private List<Button> buttons;
    private boolean disableOnClick;

    /**
     * Constructs a ButtonGroup for a list of Buttons.
     *
     * @param buttons buttons to be grouped together.
     */
    public ButtonGroup(List<Button> buttons) {
        this.buttons = buttons;
        buttons.forEach(button -> button.addClickListener(e -> {
            if (disableOnClick) {
                doDisableOnClick();
            }
        }));
        add(buttons.toArray(new Button[0]));
    }

    /**
     * Set the button group such that it is disabled on click.
     * <p>
     * Enabling the button group needs to happen from the server.
     *
     * @param disableOnClick true to disable all buttons immediately when one is clicked.
     */
    public void setDisableOnClick(boolean disableOnClick) {
        this.disableOnClick = disableOnClick;
        if (disableOnClick) {
            buttons.forEach(button -> button.getElement().setAttribute("disableOnClickGroup", "true"));
        } else {
            buttons.forEach(button -> button.getElement().removeAttribute("disableOnClickGroup"));
        }
    }

    /**
     * Enable/disable all buttons of the group.
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        buttons.forEach(button -> button.setEnabled(true));
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        initDisableOnClick();
    }

    private void initDisableOnClick() {
        // creates a js function which disables buttons
        StringBuilder sb = new StringBuilder("var disableGroupEvent = function () {");
        for (int i = 1; i <= buttons.size(); i++) {
            sb.append("if($").append(i).append(".getAttribute('disableOnClickGroup')){ $").append(i).append(".setAttribute('disabled', 'true');};");
        }
        // mark the group disabled as well
        sb.append("$0.setAttribute('disabled', 'true');");
        sb.append("};");
        // creates a js snippet which calls disableGroupEvent function when a button is clicked
        for (int i = 1; i <= buttons.size(); i++) {
            sb.append("$").append(i).append(".addEventListener('click', disableGroupEvent);");
        }
        this.getElement().executeJs(sb.toString(), getElementArray(this, buttons));
    }

    // required to make it possible to enable buttons again from server-side
    private void doDisableOnClick() {
        if (disableOnClick && buttons != null) {
            // synchronize server-side state
            ElementAttributeMap elementAttributeMap = this.getElement().getNode().getFeature(ElementAttributeMap.class);
            elementAttributeMap.set("disabled", "true");
            Map<NodeFeature, Serializable> changes = this.getElement().getNode().getChangeTracker(elementAttributeMap, () -> null);
            buttons.forEach(button -> button.getElement().getNode().getFeature(ElementAttributeMap.class).set("disabled", "true"));

            if (changes != null) {
                changes.remove("disabled");
                this.setEnabled(false);
                buttons.forEach(button -> button.setEnabled(false));

                // set buttons enabled again (on the client-side) when requested
                this.getUI().ifPresent((ui) -> {
                    ui.beforeClientResponse(this, (executionContext) -> {
                        if (this.isEnabled()) {
                            StringBuilder sb = new StringBuilder("$0.disabled = false;");
                            for (int i = 1; i <= buttons.size(); i++) {
                                sb.append("$").append(i).append(".disabled = false;");
                            }
                            executionContext.getUI().getPage().executeJs(sb.toString(), getElementArray(this, buttons));
                        }
                    });
                });
            }
        }
    }

    private Serializable[] getElementArray(Component firstComponent, List<Button> otherComponents) {
        Serializable[] toArray = new Serializable[otherComponents.size() + 1];
        toArray[0] = firstComponent.getElement();
        for (int i = 0; i < otherComponents.size(); i++) {
            toArray[i + 1] = otherComponents.get(i).getElement();
        }
        return toArray;
    }
}
