/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.9.2</a>, using an
 * XML Schema.
 * $Id$
 */

package org.openejb.alt.config.ejb11;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/


/**
 * 
 * @version $Revision$ $Date$
**/
public class EnterpriseBeansItem implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.lang.String _id;

    private Session _session;

    private Entity _entity;


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
    **/
    public Entity getEntity()
    {
        return this._entity;
    } //-- Entity getEntity() 

    /**
    **/
    public java.lang.String getId()
    {
        return this._id;
    } //-- java.lang.String getId() 

    /**
    **/
    public Session getSession()
    {
        return this._session;
    } //-- Session getSession() 

    /**
     * 
     * @param _entity
    **/
    public void setEntity(Entity _entity)
    {
        this._entity = _entity;
    } //-- void setEntity(Entity) 

    /**
     * 
     * @param _id
    **/
    public void setId(java.lang.String _id)
    {
        this._id = _id;
    } //-- void setId(java.lang.String) 

    /**
     * 
     * @param _session
    **/
    public void setSession(Session _session)
    {
        this._session = _session;
    } //-- void setSession(Session) 

}
