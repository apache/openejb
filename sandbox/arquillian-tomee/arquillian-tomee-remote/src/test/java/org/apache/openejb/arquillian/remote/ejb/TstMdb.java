package org.apache.openejb.arquillian.remote.ejb;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.TextMessage;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

/**
 * @author rmannibucau
 */
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "QUEUE")})
public class TstMdb implements MessageListener {
    @Override public void onMessage(Message message) {
        if (message instanceof TextMessage) {
            String text = "";
            try {
                text = ((TextMessage) message).getText();
            } catch (JMSException jmsEx) {
                // no-op
            }
            MessageKeeper.MESSAGES.add(text);
            Latch.LATCH.countDown();
        }
    }

    public static class Latch {
        public static final CountDownLatch LATCH = new CountDownLatch(1);
    }

    public static class MessageKeeper {
        public static final LinkedList<String> MESSAGES = new LinkedList<String>();
    }
}
