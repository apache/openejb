/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.9.3.9+</a>, using an
 * XML Schema.
 * $Id$
 */

package org.openejb.alt.config.ejb11;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * 
 * @version $Revision$ $Date$
**/
public class Query implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.lang.String _description;

    private QueryMethod _queryMethod;

    private java.lang.String _objectQl;


      //----------------/
     //- Constructors -/
    //----------------/

    public Query() {
        super();
    } //-- org.openejb.alt.config.ejb11.Query()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'description'.
     * @return the value of field 'description'.
    **/
    public java.lang.String getDescription()
    {
        return this._description;
    } //-- java.lang.String getDescription() 

    /**
     * Returns the value of field 'objectQl'.
     * @return the value of field 'objectQl'.
    **/
    public java.lang.String getObjectQl()
    {
        return this._objectQl;
    } //-- java.lang.String getObjectQl() 

    /**
     * Returns the value of field 'queryMethod'.
     * @return the value of field 'queryMethod'.
    **/
    public QueryMethod getQueryMethod()
    {
        return this._queryMethod;
    } //-- QueryMethod getQueryMethod() 

    /**
    **/
    public boolean isValid()
    {
        try {
            validate();
        }
        catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    } //-- boolean isValid() 

    /**
     * 
     * @param out
    **/
    public void marshal(java.io.Writer out)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, out);
    } //-- void marshal(java.io.Writer) 

    /**
     * 
     * @param handler
    **/
    public void marshal(org.xml.sax.DocumentHandler handler)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, handler);
    } //-- void marshal(org.xml.sax.DocumentHandler) 

    /**
     * Sets the value of field 'description'.
     * @param description the value of field 'description'.
    **/
    public void setDescription(java.lang.String description)
    {
        this._description = description;
    } //-- void setDescription(java.lang.String) 

    /**
     * Sets the value of field 'objectQl'.
     * @param objectQl the value of field 'objectQl'.
    **/
    public void setObjectQl(java.lang.String objectQl)
    {
        this._objectQl = objectQl;
    } //-- void setObjectQl(java.lang.String) 

    /**
     * Sets the value of field 'queryMethod'.
     * @param queryMethod the value of field 'queryMethod'.
    **/
    public void setQueryMethod(QueryMethod queryMethod)
    {
        this._queryMethod = queryMethod;
    } //-- void setQueryMethod(QueryMethod) 

    /**
     * 
     * @param reader
    **/
    public static org.openejb.alt.config.ejb11.Query unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.openejb.alt.config.ejb11.Query) Unmarshaller.unmarshal(org.openejb.alt.config.ejb11.Query.class, reader);
    } //-- org.openejb.alt.config.ejb11.Query unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
