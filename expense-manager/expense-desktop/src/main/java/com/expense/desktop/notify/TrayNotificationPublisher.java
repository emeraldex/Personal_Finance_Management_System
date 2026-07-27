package com.expense.desktop.notify;

import com.expense.core.network.AppNotification;
import com.expense.core.network.NotificationPublisher;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.util.Optional;

/**
 * {@link NotificationPublisher} backed by the OS system tray (AWT). Balloons
 * appear even when the app window is minimised, which is the point of the
 * seam's out-of-app delivery. Honours the seam's fire-and-forget contract: any
 * AWT failure is swallowed so a lost balloon never disturbs business logic.
 */
public final class TrayNotificationPublisher implements NotificationPublisher {

    private final TrayIcon icon;

    private TrayNotificationPublisher(TrayIcon icon) {
        this.icon = icon;
    }

    /**
     * Adds a tray icon and returns a publisher over it, or empty where no tray
     * is available (headless JVM, many Linux desktops) so the caller can fall
     * back to in-window delivery.
     */
    public static Optional<TrayNotificationPublisher> create(String appName) {
        try {
            if (GraphicsEnvironment.isHeadless() || !SystemTray.isSupported()) {
                return Optional.empty();
            }
            TrayIcon icon = new TrayIcon(paintIcon(), appName);
            icon.setImageAutoSize(true);
            SystemTray.getSystemTray().add(icon);
            return Optional.of(new TrayNotificationPublisher(icon));
        } catch (AWTException | RuntimeException e) {
            return Optional.empty();
        }
    }

    @Override
    public void publish(AppNotification notification) {
        try {
            icon.displayMessage(notification.title(), notification.body(),
                    messageType(notification.severity()));
        } catch (RuntimeException ignored) {
            // Fire-and-forget by contract.
        }
    }

    /** Removes the tray icon; call once on application shutdown. */
    public void dispose() {
        try {
            SystemTray.getSystemTray().remove(icon);
        } catch (RuntimeException ignored) {
            // Nothing sensible to do while shutting down.
        }
    }

    private static TrayIcon.MessageType messageType(AppNotification.Severity severity) {
        return switch (severity) {
            case INFO -> TrayIcon.MessageType.INFO;
            case WARNING -> TrayIcon.MessageType.WARNING;
            case ALERT -> TrayIcon.MessageType.ERROR;
        };
    }

    private static BufferedImage paintIcon() {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0x21, 0x96, 0xF3));
        g.fillOval(1, 1, 14, 14);
        g.dispose();
        return image;
    }
}
