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

import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.List;

import org.openejb.util.FastThreadLocal;

/**
 * This class represents all artifacts of the IntraVM in a stream.
 * <p>
 * Classes create this object in the writeReplace method.
 * <p>
 * When the object is serialized, the writeReplace method is invoked
 * and this artifact is written to the stream instead.  The original
 * object instance is placed in a HashMap and not serialized.
 * <p>
 * During deserialization, it is this object that is deserialized.
 * This class implements the readResolve method of the serialization API.
 * In the readResolve method, the original object instance is retrieved
 * from the HashMap and returned instead.
 * <p>
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
 */
public class IntraVmArtifact implements Externalizable {

    /**
     * A handle created using information about the object
     * instance for which this IntraVMArtifact was created.
     */
    private int instanceHandle;
    
    /**
     * Holds a list of threads.  Each thread gets a HashMap to store 
     * instances artifacts of the intra-vm.  The instances are not serialized,
     * instead, a key for the object is serialized to the stream.
     * 
     * At deserialization, the key is used to get the original object
     * instance from the List
     */
    private static FastThreadLocal thread = new FastThreadLocal();
    
    /**
     * Error detailing that the List for this Thread can not be found.
     */
    private static final String NO_MAP_ERROR = "There is no HashMap stored in the thread.  This object may have been moved outside the Virtual Machine.";
    
    /**
     * Error detailing that the object instance can not be found in the thread's List.
     */
    private static final String NO_ARTIFACT_ERROR = "The artifact this object represents could not be found.";

    /**
     * Used to creat an IntraVmArtifact object that can represent
     * the true intra-vm artifact in a stream.
     * 
     * @param obj    The object instance this class should represent in the stream.
     */
    public IntraVmArtifact(Object obj) {
        // the prev implementation used a hash map and removed the handle in the readResolve method
        // which would prevent marshaling of objects with the same hashcode in one request.
        List list = (List)thread.get();
        if (list == null) {
            list = new ArrayList();
            thread.set(list);
        }
        instanceHandle = list.size();
        list.add(obj);
            }

    /**
     * This class is Externalizable and this public, no-arg, constructor is required.
     * 
     * This constructor should only be used by the deserializing stream.
     */
    public IntraVmArtifact() {
    }

    /**
     * Writes the instanceHandle to the stream.
     * 
     * @param out
     * @exception IOException
     */
    public void writeExternal(ObjectOutput out) throws IOException{
        out.write(instanceHandle);                                            
    }

    /**
     * Reads the instanceHandle from the stream
     * 
     * @param in
     * @exception IOException
     */
    public void readExternal(ObjectInput in) throws IOException{
        instanceHandle = in.read();
    }

    /**
     * During deserialization, it is this object that is deserialized.
     * This class implements the readResolve method of the serialization API.
     * In the readResolve method, the original object instance is retrieved
     * from the List and returned instead.
     * 
     * @return 
     * @exception ObjectStreamException
     */
    private Object readResolve() throws ObjectStreamException{
        List list = (List) thread.get();
        if (list == null) throw new InvalidObjectException(NO_MAP_ERROR);
        Object artifact = list.get(instanceHandle);
        if (artifact == null) throw new InvalidObjectException(NO_ARTIFACT_ERROR+instanceHandle);
        if(list.size()==instanceHandle+1) {
            list.clear();
        }
        return artifact;
    }

}
