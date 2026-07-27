package com.expense.desktop.notify;

import com.expense.core.network.AppNotification;
import com.expense.core.network.NotificationPublisher;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Objects;

/**
 * In-window fallback {@link NotificationPublisher} for desktops without a
 * system tray: shows a small auto-hiding toast in the bottom-right corner of
 * the main window. Safe to call from any thread; rendering hops to the FX
 * application thread.
 */
public final class ToastNotificationPublisher implements NotificationPublisher {

    private static final Duration SHOW_FOR = Duration.seconds(6);

    private final Stage owner;

    public ToastNotificationPublisher(Stage owner) {
        this.owner = Objects.requireNonNull(owner, "owner");
    }

    @Override
    public void publish(AppNotification notification) {
        Platform.runLater(() -> show(notification));
    }

    private void show(AppNotification n) {
        if (!owner.isShowing()) {
            return;
        }
        Label title = new Label(n.title());
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        Label body = new Label(n.body());
        body.setStyle("-fx-text-fill: white;");
        body.setWrapText(true);
        body.setMaxWidth(320);
        VBox box = new VBox(4, title, body);
        box.setPadding(new Insets(10, 14, 10, 14));
        box.setStyle("-fx-background-color: " + backgroundFor(n.severity())
                + "; -fx-background-radius: 8;");

        Popup popup = new Popup();
        popup.getContent().add(box);
        popup.show(owner);
        // Sizes are only known after show(); anchor to the bottom-right corner.
        popup.setX(owner.getX() + owner.getWidth() - box.getWidth() - 24);
        popup.setY(owner.getY() + owner.getHeight() - box.getHeight() - 24);

        PauseTransition hide = new PauseTransition(SHOW_FOR);
        hide.setOnFinished(e -> popup.hide());
        hide.play();
    }

    private static String backgroundFor(AppNotification.Severity severity) {
        return switch (severity) {
            case INFO -> "#455A64";
            case WARNING -> "#B26A00";
            case ALERT -> "#C62828";
        };
    }
}
