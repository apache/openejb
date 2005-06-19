/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.core.ivm;

import org.openejb.util.FastThreadLocal;

/**
 * This class is used to demarcate intra-VM copy operations so
 * that intra-VM artifacts such as IntraVmHandle, IntraVmMetaData,
 * and BaseEjbProxyHandlers (EjbHomeProxyHandler and EjbObjectProxyHandler)
 * can know when they should replace themselves during serialization
 * with an IntraVmArtifact or a application server specific artifact.
 * <P>
 * Basically, we mark all local serialization operations the same
 * way you would mark a transaction.
 * <p>
 * <h2><b>LOCAL to LOCAL SERIALIZATION</b></h2> <p>
 * 
 * <i>Definition:</i><p>
 *     This is a full serialization/deserialization takes place in
 *     the local vm inside the marked scope of the IntraVM server.
 * <p>
 * <i>Circumstances:</i><p>
 *     When an IntraVM implementation of a javax.ejb.* interface is
 *     serialized in the scope of a local IntraVM serialization.
 * <p>
 *     These serializations happen when objects are passed as
 *     parameters or return values from one client/ejb to another
 *     client/ejb running inside the same VM.
 * <p>
 * <i>Action:</i><p>
 *     Temporarily cache the instance in memory during
 *     serialization, retrieve again during deserialization.
 * <p>
 * <i>Example Scenario:</i><p>
 *     BEFORE SERIALIZATION<br>
 * <br>1.  Call IntraVmCopyMonitor.preCopyOperation().
 * <br>2.  Method parameters are sent to ObjectOutputStream.
 * 
 * <p>SERIALIZATION<br>
 * <br>3.  ObjectOutputStream encounters an IntraVmMetaData instance
 *         in the object graph and calls its writeReplace method.
 * <br>4.  The IntraVmMetaData instance determines it is being
 *         serialized in the scope of an IntraVM serialization by
 *         calling IntraVmCopyMonitor.isIntraVmCopyOperation().
 * <br>5.  The IntraVmMetaData instance creates an IntraVmArtifact
 *         that caches it in a static hashtable keyed on a
 *         combination of the thread id and instance hashCode.
 * <br>6.  The IntraVmMetaData instance returns the IntraVmArtifact
 *         instance from the writeReplace method.
 * <br>7.  The ObjectOutputStream serializes the IntraVmArtifact
 *         instance in place of the IntraVmMetaData instance.
 * <P> DESERIALIZATION<br>
 * <br>8.  ObjectInputStream encounters and deserializes an
 *         IntraVmArtifact instance and calls its readResolve method.
 * <br>9.  The IntraVmArtifact instance uses the key it created in
 *         step 5 to retrieve the IntraVmMetaData instance from the
 *         static hashtable.
 * <br>10. The IntraVmArtifact instance returns the IntraVmMetaData
 *         instance from the readResolve method.
 * <br>11. ObjectInputStream places the IntraVmMetaData instance in
 *         the object graph in place of the IntraVmArtifact
 *         instance.
 * <P>AFTER<br>
 * <br>12. Method parameters are now de-referenced as mandated by the
 *         spec and can be passed into the bean's method.
 * <br>13. IntraVmCopyMonitor.postCopyOperation() is called, ending
 *         the local IntraVm serialization scope.
 * <p>
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 * @version $Revision$ $Date$
 */
public class IntraVmCopyMonitor {
    
    private static FastThreadLocal threadStorage = new FastThreadLocal();
    
    /**
     * Set to true if the current operation is an
     * IntraVM copy.
     */
    boolean intraVmCopyOperation = false;
    
    /**
     * Set to true if the current operation is the
     * passivation of a stateful session bean.
     */
    boolean statefulPassivationOperation = false;
    
    /**
     */
    IntraVmCopyMonitor(){}
        
    public static boolean exists(){
        return (threadStorage.get()!=null);
    }

    /**
     * Not to be used lightly.  This class is more performant if
     * every thread has a ThreadContext set only use this method
     * if the thread is never going to re-access the container.
     */
    public static void release( ){
        threadStorage.set(null);
    }


    /**
     * Returns the IntraVmCopyMonitor singleton.
     * 
     * If the IntraVmCopyMonitor has not already been 
     * create, it is instantiated.
     * 
     * @return IntraVmCopyMonitor
     */
    static IntraVmCopyMonitor getMonitor( ){
        IntraVmCopyMonitor monitor = (IntraVmCopyMonitor)threadStorage.get();
        if(monitor==null){
            monitor = new IntraVmCopyMonitor();
            threadStorage.set(monitor);
        }
        return monitor;
    }

    /**
     * Notifies the monitor for this thread just before a
     * copy operation is to take place.
     * 
     * This happens when one bean access another bean and 
     * arguments or return values are copied.
     */
    public static void preCopyOperation(){
        IntraVmCopyMonitor monitor = getMonitor();
        monitor.intraVmCopyOperation = true;
    }
    /**
     * Notifies the monitor for this thread just after a
     * copy operation has taken place.
     * 
     * This happens when one bean access another bean and 
     * arguments or return values are copied.
     */
    public static void postCopyOperation(){
        IntraVmCopyMonitor monitor = getMonitor();
        monitor.intraVmCopyOperation = false;
    }
    /**
     * Notifies the monitor for this thread just before a
     * stateful session bean is passified.
     * 
     * This happens when a stateful session bean is
     * passified and all its member variables are serialized.
     */
    public static void prePassivationOperation(){
        IntraVmCopyMonitor monitor = getMonitor();
        monitor.statefulPassivationOperation = true;
    }
    /**
     * Notifies the monitor for this thread just after a
     * stateful session bean is passified.
     * 
     * This happens when a stateful session bean is
     * passified and all its member variables are serialized.
     */
    public static void postPassivationOperation(){
        IntraVmCopyMonitor monitor = getMonitor();
        monitor.statefulPassivationOperation = false;
    }
    /**
     * Returns true if the current operation is an
     * IntraVM copy.
     * 
     * @return boolean
     */
    public static boolean isIntraVmCopyOperation(){
        IntraVmCopyMonitor monitor = getMonitor();
        if(monitor.intraVmCopyOperation)
            return true;
        else
            return false;
    }
    /**
     * Returns true if the current operation is the
     * passivation of a stateful session bean.
     * 
     * @return boolean 
     */
    public static boolean isStatefulPassivationOperation(){
        IntraVmCopyMonitor monitor = getMonitor();
        if(monitor.statefulPassivationOperation)
            return true;
        else
            return false;
    }
}
