/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.config.sys;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class Lookup.
 *
 * @version $Revision$ $Date$
 */
public class Lookup implements java.io.Serializable {


    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _jndiName
     */
    private java.lang.String _jndiName;

    /**
     * Field _jndiProviderId
     */
    private java.lang.String _jndiProviderId;


    //----------------/
    //- Constructors -/
    //----------------/

    public Lookup() {
        super();
    } //-- org.apache.openejb.config.sys.Lookup()


    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'jndiName'.
     *
     * @return the value of field 'jndiName'.
     */
    public java.lang.String getJndiName() {
        return this._jndiName;
    } //-- java.lang.String getJndiName() 

    /**
     * Returns the value of field 'jndiProviderId'.
     *
     * @return the value of field 'jndiProviderId'.
     */
    public java.lang.String getJndiProviderId() {
        return this._jndiProviderId;
    } //-- java.lang.String getJndiProviderId() 

    /**
     * Method isValid
     */
    public boolean isValid() {
        try {
            validate();
        } catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    } //-- boolean isValid() 

    /**
     * Method marshal
     *
     * @param out
     */
    public void marshal(java.io.Writer out)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {

        Marshaller.marshal(this, out);
    } //-- void marshal(java.io.Writer) 

    /**
     * Method marshal
     *
     * @param handler
     */
    public void marshal(org.xml.sax.ContentHandler handler)
            throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {

        Marshaller.marshal(this, handler);
    } //-- void marshal(org.xml.sax.ContentHandler) 

    /**
     * Sets the value of field 'jndiName'.
     *
     * @param jndiName the value of field 'jndiName'.
     */
    public void setJndiName(java.lang.String jndiName) {
        this._jndiName = jndiName;
    } //-- void setJndiName(java.lang.String) 

    /**
     * Sets the value of field 'jndiProviderId'.
     *
     * @param jndiProviderId the value of field 'jndiProviderId'.
     */
    public void setJndiProviderId(java.lang.String jndiProviderId) {
        this._jndiProviderId = jndiProviderId;
    } //-- void setJndiProviderId(java.lang.String) 

    /**
     * Method unmarshal
     *
     * @param reader
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.apache.openejb.config.sys.Lookup) Unmarshaller.unmarshal(org.apache.openejb.config.sys.Lookup.class, reader);
    } //-- java.lang.Object unmarshal(java.io.Reader) 

    /**
     * Method validate
     */
    public void validate()
            throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
