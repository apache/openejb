package transactiontests;

import javax.ejb.*;

public class CMPEntityBean extends TransactionsImpl implements EntityBean {

    //
    // Container-Managed fields
    // must be public and follow rules for Java Object Serialization
    //

    // Example:
    // public String name;

    //
    // Creation methods
    //

    protected boolean verbose=true;
    public String id;
    public String value;
    public Object[] wasteSomeSpaceToTriggerPassivation = new Object[3000];
    protected Object resourceAllocatedInActivate;
    protected Object valueComputedDuringLoad;
    protected boolean setEntityContextCalled;
    protected boolean unsetEntityContextCalled;
    protected boolean createCalled;
    protected boolean passivateCalled;
    protected boolean activateCalled;
    
    public CMPEntityBean() {
    }

    public String ejbCreate() throws CreateException {
        if(verbose) {
            System.out.println("ejbCreate() on "+this);
        }

        /* 9.4.2 The Container must ensure that the values of the container-managed fields are set to the Java language
        defaults (e.g. 0 for integer, null for pointers) prior to invoking an ejbCreate(...) method on an
        instance. */
        
        if(id!=null || value !=null) {
            System.err.println("Container violates 9.4.2. CMP fields should have been null. id="+id+" value="+value);
        }
            
        if(setEntityContextCalled==false) {
            System.err.println("Container violates section 9.1.4: it didn't call setEntityContext.");
        }
    // We create the PK here
        StringBuffer bf = new StringBuffer(30);
        bf.append("ux");
        bf.append(System.currentTimeMillis());
        // Thank you, Oracle!
        for(int i=bf.length(); i<30; ++i) {
            bf.append(' ');
        }
        id=bf.toString();
        value="novalue";
        createCalled=true;
        return null;
    }

    public void ejbPostCreate()  {
        if(verbose) {
            System.out.println("ejbPostCreate() on "+this);
        }
    }

    //
    // EnterpriseBean interface implementation
    //

    public void setEntityContext(EntityContext ctx) {
        if(verbose) {
            System.out.println("setEntityContext() on "+this);
        }
        setEntityContextCalled=true;
        _ctx = ctx;
    }

    public void unsetEntityContext() {
        if(verbose) {
            System.out.println("unsetEntityContext() on "+this);
        }
        _ctx = null;
        unsetEntityContextCalled=true;
    }

    public void ejbRemove() throws javax.ejb.RemoveException {
        if(verbose) {
            System.out.println("ejbRemove() on "+this);
        }
        checkActivation();
    }

    public void ejbActivate() {
        if(verbose) {
            System.out.println("ejbActivate() on "+this);
        }
        activateCalled=true;
        passivateCalled=false;
        resourceAllocatedInActivate=new Object();
    }

    public void ejbPassivate() {
        if(verbose) {
            System.out.println("ejbPassivate() on "+this);
        }
        activateCalled=false;
        passivateCalled=true;
        createCalled=false;
        resourceAllocatedInActivate=null;
    }

    public void ejbLoad() {
        if(verbose) {
            System.out.println("ejbLoad() on "+this);
        }
        valueComputedDuringLoad=id;
    }

    public void ejbStore() {
        if(verbose) {
            System.out.println("ejbStore() on "+this);
        }
        valueComputedDuringLoad=null;
    }

    public javax.transaction.UserTransaction getUserTransaction() {
        return _ctx.getUserTransaction();
    }

    private void checkActivation() {
        if(!activateCalled && ! createCalled) {
            System.err.println("Container didn't call ejbActivate on this instance");
        }
        if(valueComputedDuringLoad==null) {
            System.err.println("Container didn't call ejbLoad on this instance");
        }
        if(!id.equals(((EntityContext)_ctx).getPrimaryKey())) {
            System.err.println("Container didn't set pk field correctly on ejbRemove. Have="+id+" should have:" + ((EntityContext)_ctx).getPrimaryKey());
        }
        if(value==null) {
            System.err.println("Container didn't set value correctly");
        }
    }
        
    public void setValue(String d) {
        checkActivation();
        value=d;
    }

    public String getValue() {
        checkActivation();
        return value;
    }

}
