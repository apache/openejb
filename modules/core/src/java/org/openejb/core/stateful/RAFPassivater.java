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


package org.openejb.core.stateful;


import java.io.RandomAccessFile;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.openejb.spi.Serializer;


// optimization: replace HashTable with HashMap (vc no debug hashmap)
public class RAFPassivater implements PassivationStrategy{

    int fileID = 0;
    Hashtable masterTable = new Hashtable();

    static class Pointer {
        int fileid;
        long filepointer;
        int bytesize;
        public Pointer(int file, long pointer, int bytecount){
            fileid = file;
            filepointer = pointer;
            bytesize = bytecount;
        }
    }

    public void init(Properties props) throws org.openejb.SystemException {}


    public synchronized void passivate(Hashtable stateTable)
    throws org.openejb.SystemException{
        try{
            fileID++;

            RandomAccessFile ras = new RandomAccessFile(System.getProperty("java.io.tmpdir", File.separator + "tmp") + File.separator + "passivation"+fileID+".ser","rw");
            Enumeration enumeration = stateTable.keys();
            Pointer lastPointer = null;
            while(enumeration.hasMoreElements()){
                Object id = enumeration.nextElement();
                Object obj = stateTable.get(id);
                byte [] bytes = Serializer.serialize(obj);
                long filepointer = ras.getFilePointer();

                if( lastPointer == null ) lastPointer = new Pointer(fileID, filepointer, (int)(filepointer));
                else lastPointer = new Pointer(fileID, filepointer, (int)(filepointer-lastPointer.filepointer));

                masterTable.put(id,lastPointer);
                ras.write(bytes);
            }
            ras.close( );
        }catch(Exception e){
            throw new org.openejb.SystemException(e);
        }
    }

    public synchronized Object activate(Object primaryKey)
    throws org.openejb.SystemException{

        Pointer pointer = (Pointer)masterTable.get(primaryKey);
        if(pointer == null)
            return null;

        try{
            RandomAccessFile ras = new RandomAccessFile(System.getProperty("java.io.tmpdir", File.separator + "tmp") + File.separator + "passivation"+pointer.fileid+".ser","r");
            byte [] bytes = new byte[(int)pointer.bytesize];
            ras.seek(pointer.filepointer);
            ras.readFully(bytes);
            ras.close();
            return Serializer.deserialize(bytes);
        }catch(Exception e){
            throw new org.openejb.SystemException(e);
        }

    }

}