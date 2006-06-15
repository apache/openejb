package org.openejb.util;
import java.util.HashMap;

/**
* This class allows thread-specific storage (TSS) of values by key, so that object are
* stored by thread identity as well as an application specific key.  This makes it possible 
* have context sensitive TSS.
* 
* @version $ $
* @see org.openejb.core.SharedLocalConnectionManager;
*/

/*
* This variation of ThreadLocal accomplishes thread-specific storage by thread as well
* as by object.  Values are associated with both an key and a thread, which allows 
* each value to stored specific to an object and thread. 
*
* @see org.openejb.resource.SharedLocalConnectionManager
* @version $ $
*/
public class HashThreadLocal {
    HashMap keyMap = new HashMap();
    public synchronized void put(Object key, Object value){
        FastThreadLocal threadLocal = (FastThreadLocal)keyMap.get(key);
        if(threadLocal==null){
            threadLocal = new FastThreadLocal();
            keyMap.put(key, threadLocal);
        }
        threadLocal.set(value);
    }
    public synchronized Object get(Object key){
        FastThreadLocal threadLocal = (FastThreadLocal)keyMap.get(key);
        if(threadLocal==null)return null;
        return threadLocal.get();
    }
}