/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.openejb.mdb.mockra;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

/**
 * @version $Revision$ $Date$
 */
public class MockMessage implements Message {
    
    private static final String PROPERTY_ENCODING = "UTF-8";
    private long timestamp;
    private String messageID;
    private String correlationID;
    private Destination replyTo;
    private Destination destination;
    private int deliveryMode;
    private boolean redelivered;
    private String type;
    private long expiration;
    private int priority;
    private HashMap properties;
    private boolean acknowledged;
    
    static public String getAsString(byte[] data) throws JMSException {
        try {
            return new String(data, PROPERTY_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new JMSException("JVM not compatible: "+e);
        }
    }
    
    static public byte[] getAsBytes(String id) throws JMSException {
        try {
            return id.getBytes(PROPERTY_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new JMSException("JVM not compatible: "+e);
        }
    }

    /**
     * @see javax.jms.Message#getJMSMessageID()
     */
    public String getJMSMessageID() throws JMSException {
        return messageID;
    }

    /**
     * @see javax.jms.Message#setJMSMessageID(java.lang.String)
     */
    public void setJMSMessageID(String id) throws JMSException {
        messageID = id;
    }


    /**
     * @see javax.jms.Message#getJMSTimestamp()
     */
    public long getJMSTimestamp() throws JMSException {
        return timestamp;
    }

    /**
     * @see javax.jms.Message#setJMSTimestamp(long)
     */
    public void setJMSTimestamp(long timestamp) throws JMSException {
        this.timestamp = timestamp;        
    }

    /**
     * @see javax.jms.Message#getJMSCorrelationIDAsBytes()
     */
    public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
        return getAsBytes(correlationID);
    }

    /**
     * @see javax.jms.Message#setJMSCorrelationIDAsBytes(byte[])
     */
    public void setJMSCorrelationIDAsBytes(byte[] correlationID) throws JMSException {
        this.correlationID = getAsString(correlationID);        
    }

    /**
     * @see javax.jms.Message#setJMSCorrelationID(java.lang.String)
     */
    public void setJMSCorrelationID(String correlationID) throws JMSException {
        this.correlationID = correlationID;        
    }

    /**
     * @see javax.jms.Message#getJMSCorrelationID()
     */
    public String getJMSCorrelationID() throws JMSException {
        return this.correlationID;
    }

    /**
     * @see javax.jms.Message#getJMSReplyTo()
     */
    public Destination getJMSReplyTo() throws JMSException {
        return replyTo;
    }

    /**
     * @see javax.jms.Message#setJMSReplyTo(javax.jms.Destination)
     */
    public void setJMSReplyTo(Destination replyTo) throws JMSException {
        this.replyTo = replyTo;        
    }

    /**
     * @see javax.jms.Message#getJMSDestination()
     */
    public Destination getJMSDestination() throws JMSException {
        return destination;
    }

    /**
     * @see javax.jms.Message#setJMSDestination(javax.jms.Destination)
     */
    public void setJMSDestination(Destination destination) throws JMSException {
        this.destination = destination;
    }

    /**
     * @see javax.jms.Message#getJMSDeliveryMode()
     */
    public int getJMSDeliveryMode() throws JMSException {
        return deliveryMode;
    }

    /**
     * @see javax.jms.Message#setJMSDeliveryMode(int)
     */
    public void setJMSDeliveryMode(int deliveryMode) throws JMSException {
        this.deliveryMode = deliveryMode;
    }

    /**
     * @see javax.jms.Message#getJMSRedelivered()
     */
    public boolean getJMSRedelivered() throws JMSException {
        return redelivered;
    }

    /**
     * @see javax.jms.Message#setJMSRedelivered(boolean)
     */
    public void setJMSRedelivered(boolean redelivered) throws JMSException {
        this.redelivered = redelivered;
    }

    /**
     * @see javax.jms.Message#getJMSType()
     */
    public String getJMSType() throws JMSException {
        return type;
    }

    /**
     * @see javax.jms.Message#setJMSType(java.lang.String)
     */
    public void setJMSType(String type) throws JMSException {
        this.type = type;        
    }

    /**
     * @see javax.jms.Message#getJMSExpiration()
     */
    public long getJMSExpiration() throws JMSException {
        return expiration;
    }

    /**
     * @see javax.jms.Message#setJMSExpiration(long)
     */
    public void setJMSExpiration(long expiration) throws JMSException {
        this.expiration = expiration;        
    }

    /**
     * @see javax.jms.Message#getJMSPriority()
     */
    public int getJMSPriority() throws JMSException {
        return priority;
    }

    /**
     * @see javax.jms.Message#setJMSPriority(int)
     */
    public void setJMSPriority(int priority) throws JMSException {
        this.priority = priority;
    }

    /**
     * @see javax.jms.Message#clearProperties()
     */
    public void clearProperties() throws JMSException {
        properties = null;
    }

    /**
     * @see javax.jms.Message#propertyExists(java.lang.String)
     */
    public boolean propertyExists(String name) throws JMSException {
        if( properties == null )
            return false;
        return properties.get(name)!=null;
    }

    /**
     * @see javax.jms.Message#getBooleanProperty(java.lang.String)
     */
    public boolean getBooleanProperty(String name) throws JMSException {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @see javax.jms.Message#getByteProperty(java.lang.String)
     */
    public byte getByteProperty(String name) throws JMSException {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see javax.jms.Message#getShortProperty(java.lang.String)
     */
    public short getShortProperty(String name) throws JMSException {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see javax.jms.Message#getIntProperty(java.lang.String)
     */
    public int getIntProperty(String name) throws JMSException {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see javax.jms.Message#getLongProperty(java.lang.String)
     */
    public long getLongProperty(String name) throws JMSException {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see javax.jms.Message#getFloatProperty(java.lang.String)
     */
    public float getFloatProperty(String name) throws JMSException {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see javax.jms.Message#getDoubleProperty(java.lang.String)
     */
    public double getDoubleProperty(String name) throws JMSException {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @see javax.jms.Message#getStringProperty(java.lang.String)
     */
    public String getStringProperty(String name) throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see javax.jms.Message#getObjectProperty(java.lang.String)
     */
    public Object getObjectProperty(String name) throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see javax.jms.Message#getPropertyNames()
     */
    public Enumeration getPropertyNames() throws JMSException {
        return new Vector(properties.keySet()).elements();
    }

    /**
     * @see javax.jms.Message#setBooleanProperty(java.lang.String, boolean)
     */
    public void setBooleanProperty(String name, boolean value) throws JMSException {
        properties.put(name, new Boolean(value));
    }

    /**
     * @see javax.jms.Message#setByteProperty(java.lang.String, byte)
     */
    public void setByteProperty(String name, byte value) throws JMSException {
        properties.put(name, new Long(value));
    }

    /**
     * @see javax.jms.Message#setShortProperty(java.lang.String, short)
     */
    public void setShortProperty(String name, short value) throws JMSException {
        properties.put(name, new Long(value));
    }

    /**
     * @see javax.jms.Message#setIntProperty(java.lang.String, int)
     */
    public void setIntProperty(String name, int value) throws JMSException {
        properties.put(name, new Long(value));
    }

    /**
     * @see javax.jms.Message#setLongProperty(java.lang.String, long)
     */
    public void setLongProperty(String name, long value) throws JMSException {
        properties.put(name, new Long(value));
    }

    /**
     * @see javax.jms.Message#setFloatProperty(java.lang.String, float)
     */
    public void setFloatProperty(String name, float value) throws JMSException {
        properties.put(name, new Double(value));
    }

    /**
     * @see javax.jms.Message#setDoubleProperty(java.lang.String, double)
     */
    public void setDoubleProperty(String name, double value) throws JMSException {
        properties.put(name, new Double(value));
    }

    /**
     * @see javax.jms.Message#setStringProperty(java.lang.String, java.lang.String)
     */
    public void setStringProperty(String name, String value) throws JMSException {
        properties.put(name, value);
    }

    /**
     * @see javax.jms.Message#setObjectProperty(java.lang.String, java.lang.Object)
     */
    public void setObjectProperty(String name, Object value) throws JMSException {
        // TODO Auto-generated method stub
        
    }

    /**
     * @see javax.jms.Message#acknowledge()
     */
    public void acknowledge() throws JMSException {
        acknowledged = true;
    }

    /**
     * @see javax.jms.Message#clearBody()
     */
    public void clearBody() throws JMSException {
        // TODO Auto-generated method stub
        
    }

    /**
     * @return Returns the properties.
     */
    public Map getProperties() {
        return properties;
    }

}
