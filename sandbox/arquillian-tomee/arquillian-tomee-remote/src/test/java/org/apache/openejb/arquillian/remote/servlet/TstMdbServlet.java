package org.apache.openejb.arquillian.remote.servlet;

import org.apache.openejb.arquillian.remote.ejb.TstMdb;

import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author rmannibucau
 */
public class TstMdbServlet extends HttpServlet {
    @Resource private ConnectionFactory connectionFactory;
    @Resource(name = "QUEUE") private Queue queue;

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Connection connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(queue);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            producer.send(session.createTextMessage("a servlet sent a message to a MDB"));

            producer.close();
            session.close();
            connection.close();

            TstMdb.Latch.LATCH.await(30, TimeUnit.SECONDS);
        } catch (Exception ex) {
            resp.getOutputStream().print("no message: " + ex.getMessage());
            return;
        }
        if (TstMdb.MessageKeeper.MESSAGES.size() > 0) {
            resp.getOutputStream().print("last message = " + TstMdb.MessageKeeper.MESSAGES.getLast());
        } else {
            resp.getOutputStream().print("no message");
        }
    }
}
