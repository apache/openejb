/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id$
 */

package org.openejb.alt.config.ejb11;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.Serializable;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

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
    private org.openejb.alt.config.ejb11.Session _session;

    /**
     * Field _entity
     */
    private org.openejb.alt.config.ejb11.Entity _entity;


      //----------------/
     //- Constructors -/
    //----------------/

    public EnterpriseBeansItem() {
        super();
    } //-- org.openejb.alt.config.ejb11.EnterpriseBeansItem()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'entity'.
     * 
     * @return the value of field 'entity'.
     */
    public org.openejb.alt.config.ejb11.Entity getEntity()
    {
        return this._entity;
    } //-- org.openejb.alt.config.ejb11.Entity getEntity() 

    /**
     * Returns the value of field 'session'.
     * 
     * @return the value of field 'session'.
     */
    public org.openejb.alt.config.ejb11.Session getSession()
    {
        return this._session;
    } //-- org.openejb.alt.config.ejb11.Session getSession() 

    /**
     * Sets the value of field 'entity'.
     * 
     * @param entity the value of field 'entity'.
     */
    public void setEntity(org.openejb.alt.config.ejb11.Entity entity)
    {
        this._entity = entity;
    } //-- void setEntity(org.openejb.alt.config.ejb11.Entity) 

    /**
     * Sets the value of field 'session'.
     * 
     * @param session the value of field 'session'.
     */
    public void setSession(org.openejb.alt.config.ejb11.Session session)
    {
        this._session = session;
    } //-- void setSession(org.openejb.alt.config.ejb11.Session) 

}
