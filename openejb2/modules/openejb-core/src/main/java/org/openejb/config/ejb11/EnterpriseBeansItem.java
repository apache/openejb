/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id$
 */

package org.openejb.config.ejb11;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/


/**
 * Class EnterpriseBeansItem.
 *
 * @version $Revision$ $Date$
 */
public class EnterpriseBeansItem implements java.io.Serializable {


    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _session
     */
    private org.openejb.config.ejb11.Session _session;

    /**
     * Field _entity
     */
    private org.openejb.config.ejb11.Entity _entity;


    //----------------/
    //- Constructors -/
    //----------------/

    public EnterpriseBeansItem() {
        super();
    } //-- org.openejb.config.ejb11.EnterpriseBeansItem()


    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'entity'.
     *
     * @return the value of field 'entity'.
     */
    public org.openejb.config.ejb11.Entity getEntity() {
        return this._entity;
    } //-- org.openejb.config.ejb11.Entity getEntity() 

    /**
     * Returns the value of field 'session'.
     *
     * @return the value of field 'session'.
     */
    public org.openejb.config.ejb11.Session getSession() {
        return this._session;
    } //-- org.openejb.config.ejb11.Session getSession() 

    /**
     * Sets the value of field 'entity'.
     *
     * @param entity the value of field 'entity'.
     */
    public void setEntity(org.openejb.config.ejb11.Entity entity) {
        this._entity = entity;
    } //-- void setEntity(org.openejb.config.ejb11.Entity) 

    /**
     * Sets the value of field 'session'.
     *
     * @param session the value of field 'session'.
     */
    public void setSession(org.openejb.config.ejb11.Session session) {
        this._session = session;
    } //-- void setSession(org.openejb.config.ejb11.Session) 

}
