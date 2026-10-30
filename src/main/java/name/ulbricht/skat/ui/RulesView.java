package name.ulbricht.skat.ui;

import java.util.EnumMap;
import java.util.Map;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * The rules window: the chapters as big buttons on the left, the chosen chapter on the right.
 * "Warum?" opens it at the chapter that fits the situation.
 */
final class RulesView {

    private static final double WIDTH = 900;
    private static final double TEXT_WIDTH = 590;

    private final Texts texts;
    private final Map<RuleTopic, Button> buttons = new EnumMap<>(RuleTopic.class);
    private final Label heading = new Label();
    private final Label text = new Label();
    private final VBox panel;

    RulesView(Texts texts, Node closeButton) {
        this.texts = texts;
        VBox chapters = new VBox(6);
        for (RuleTopic topic : RuleTopic.values()) {
            Button button = new Button(texts.get("rules.title." + topic.name()));
            button.getStyleClass().add("rules-chapter");
            button.setMaxWidth(Double.MAX_VALUE);
            button.setOnAction(event -> show(topic));
            buttons.put(topic, button);
            chapters.getChildren().add(button);
        }
        heading.getStyleClass().add("panel-heading");
        text.getStyleClass().add("rules-text");
        text.setWrapText(true);
        text.setPrefWidth(TEXT_WIDTH);
        text.setMinHeight(Region.USE_PREF_SIZE);
        VBox page = new VBox(10, heading, text);
        HBox.setHgrow(page, Priority.ALWAYS);
        HBox body = new HBox(22, chapters, page);

        Label title = new Label(texts.get("rules.title"));
        title.getStyleClass().add("panel-title");
        panel = new VBox(14, title, body, closeButton);
        panel.getStyleClass().add("panel");
        panel.setAlignment(Pos.TOP_LEFT);
        panel.setMaxWidth(WIDTH);
        panel.setMaxHeight(Region.USE_PREF_SIZE);
    }

    /** Returns the window showing {@code topic}. */
    Node node(RuleTopic topic) {
        show(topic);
        return panel;
    }

    private void show(RuleTopic topic) {
        heading.setText(texts.get("rules.title." + topic.name()));
        text.setText(texts.get("rules.text." + topic.name()));
        for (Map.Entry<RuleTopic, Button> entry : buttons.entrySet()) {
            entry.getValue().getStyleClass().remove("on");
            if (entry.getKey() == topic) {
                entry.getValue().getStyleClass().add("on");
            }
        }
    }
}
