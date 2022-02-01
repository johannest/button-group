# ButtonGroup for V14

Very simple implementation of group of Buttons. The main use-case is for allowing setDisableOnClick for a group of Buttons. 

The implementation is based on `com.vaadin.flow.component.button.Button`'s `disableOnClick` implementation.

### Usage example

```
Button button1 = new Button("Button 1");
Button button2 = new Button("Button 2");
Button button3 = new Button("Button 3");
buttons = Arrays.asList(button1, button2, button3);

// add buttons to ButtonGroup before adding click listeners
ButtonGroup buttonGroup = new ButtonGroup(buttons);
// enable disableOnClick
buttonGroup.setDisableOnClick(true);

// add button specific click listeners
buttons.forEach(b -> b.addClickListener(e -> {
    try {Thread.sleep(5000L);} catch (InterruptedException ex) {}
    buttonGroup.setEnabled(true);
}));

// add button group to a layout
add(buttonGroup);
``` 

### Deployment

Starting the test/demo server:
```
mvn jetty:run
```

This deploys demo at http://localhost:8080
