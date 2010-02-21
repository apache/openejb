package org.superbiz.moviefun;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.naming.NamingException;
import javax.naming.InitialContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class NotificationMonitor {
    private static TrayIcon trayIcon;

    public static void main(String[] args) throws NamingException, InterruptedException, AWTException, MalformedURLException {
        addSystemTrayIcon();

        // Boot the embedded EJB Container 
        new InitialContext();

        System.out.println("Starting monitor...");
    }

    private static void addSystemTrayIcon() throws AWTException, MalformedURLException {
        SystemTray tray = SystemTray.getSystemTray();

        URL moviepng = NotificationMonitor.class.getClassLoader().getResource("movie.png");
        Image image = Toolkit.getDefaultToolkit().getImage(moviepng);

        ActionListener exitListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Exiting monitor...");
                System.exit(0);
            }
        };

        PopupMenu popup = new PopupMenu();
        MenuItem defaultItem = new MenuItem("Exit");
        defaultItem.addActionListener(exitListener);
        popup.add(defaultItem);

        trayIcon = new TrayIcon(image, "Notification Monitor", popup);
        trayIcon.setImageAutoSize(true);
        tray.add(trayIcon);


    }

    public static void showAlert(String message) {
        synchronized (trayIcon) {
            trayIcon.displayMessage("Alert received", message, TrayIcon.MessageType.WARNING);
        }
    }

    @MessageDriven(activationConfig = {
            @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
            @ActivationConfigProperty(propertyName = "destination", propertyValue = "notifications")})
    public class NotificationsBean implements MessageListener {

        public void onMessage(Message message) {
            try {
                TextMessage textMessage = (TextMessage) message;
                String text = textMessage.getText();

                System.out.println("");
                System.out.println("====================================");
                System.out.println("Notification received: " + text);
                System.out.println("====================================");
                System.out.println("");

                NotificationMonitor.showAlert(text);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}