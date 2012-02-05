package org.apache.openejb.colossus.generator.cdi;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@Lock(LockType.READ)
@Singleton
public class EJBWithFoo {
    @Inject @Any
    private Instance<IFoo> foos;

    public int fooSize() {
        int i = 0;
        for (IFoo foo : foos) {
            i++;
        }
        return i;
    }
}
