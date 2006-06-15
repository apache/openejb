package org.openejb.timer;

import org.openejb.EJBInvocation;

/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public interface EJBTimeoutInvocationFactory {

    EJBInvocation getEJBTimeoutInvocation(Object id, TimerImpl timer);

}
