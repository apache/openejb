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

package org.apache.openejb.config.ejb11;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class EjbJar.
 *
 * @version $Revision$ $Date$
 */
public class EjbJar implements java.io.Serializable {


    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _id
     */
    private java.lang.String _id;

    /**
     * Field _description
     */
    private java.lang.String _description;

    /**
     * Field _displayName
     */
    private java.lang.String _displayName;

    /**
     * Field _smallIcon
     */
    private java.lang.String _smallIcon;

    /**
     * Field _largeIcon
     */
    private java.lang.String _largeIcon;

    /**
     * Field _enterpriseBeans
     */
    private org.apache.openejb.config.ejb11.EnterpriseBeans _enterpriseBeans;

    /**
     * Field _assemblyDescriptor
     */
    private org.apache.openejb.config.ejb11.AssemblyDescriptor _assemblyDescriptor;

    /**
     * Field _ejbClientJar
     */
    private java.lang.String _ejbClientJar;


    //----------------/
    //- Constructors -/
    //----------------/

    public EjbJar() {
        super();
    } //-- org.apache.openejb.config.ejb11.EjbJar()


    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'assemblyDescriptor'.
     *
     * @return the value of field 'assemblyDescriptor'.
     */
    public org.apache.openejb.config.ejb11.AssemblyDescriptor getAssemblyDescriptor() {
        return this._assemblyDescriptor;
    } //-- org.apache.openejb.config.ejb11.AssemblyDescriptor getAssemblyDescriptor()

    /**
     * Returns the value of field 'description'.
     *
     * @return the value of field 'description'.
     */
    public java.lang.String getDescription() {
        return this._description;
    } //-- java.lang.String getDescription() 

    /**
     * Returns the value of field 'displayName'.
     *
     * @return the value of field 'displayName'.
     */
    public java.lang.String getDisplayName() {
        return this._displayName;
    } //-- java.lang.String getDisplayName() 

    /**
     * Returns the value of field 'ejbClientJar'.
     *
     * @return the value of field 'ejbClientJar'.
     */
    public java.lang.String getEjbClientJar() {
        return this._ejbClientJar;
    } //-- java.lang.String getEjbClientJar() 

    /**
     * Returns the value of field 'enterpriseBeans'.
     *
     * @return the value of field 'enterpriseBeans'.
     */
    public org.apache.openejb.config.ejb11.EnterpriseBeans getEnterpriseBeans() {
        return this._enterpriseBeans;
    } //-- org.apache.openejb.config.ejb11.EnterpriseBeans getEnterpriseBeans()

    /**
     * Returns the value of field 'id'.
     *
     * @return the value of field 'id'.
     */
    public java.lang.String getId() {
        return this._id;
    } //-- java.lang.String getId() 

    /**
     * Returns the value of field 'largeIcon'.
     *
     * @return the value of field 'largeIcon'.
     */
    public java.lang.String getLargeIcon() {
        return this._largeIcon;
    } //-- java.lang.String getLargeIcon() 

    /**
     * Returns the value of field 'smallIcon'.
     *
     * @return the value of field 'smallIcon'.
     */
    public java.lang.String getSmallIcon() {
        return this._smallIcon;
    } //-- java.lang.String getSmallIcon() 

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
     * Sets the value of field 'assemblyDescriptor'.
     *
     * @param assemblyDescriptor the value of field
     *                           'assemblyDescriptor'.
     */
    public void setAssemblyDescriptor(org.apache.openejb.config.ejb11.AssemblyDescriptor assemblyDescriptor) {
        this._assemblyDescriptor = assemblyDescriptor;
    } //-- void setAssemblyDescriptor(org.apache.openejb.config.ejb11.AssemblyDescriptor)

    /**
     * Sets the value of field 'description'.
     *
     * @param description the value of field 'description'.
     */
    public void setDescription(java.lang.String description) {
        this._description = description;
    } //-- void setDescription(java.lang.String) 

    /**
     * Sets the value of field 'displayName'.
     *
     * @param displayName the value of field 'displayName'.
     */
    public void setDisplayName(java.lang.String displayName) {
        this._displayName = displayName;
    } //-- void setDisplayName(java.lang.String) 

    /**
     * Sets the value of field 'ejbClientJar'.
     *
     * @param ejbClientJar the value of field 'ejbClientJar'.
     */
    public void setEjbClientJar(java.lang.String ejbClientJar) {
        this._ejbClientJar = ejbClientJar;
    } //-- void setEjbClientJar(java.lang.String) 

    /**
     * Sets the value of field 'enterpriseBeans'.
     *
     * @param enterpriseBeans the value of field 'enterpriseBeans'.
     */
    public void setEnterpriseBeans(org.apache.openejb.config.ejb11.EnterpriseBeans enterpriseBeans) {
        this._enterpriseBeans = enterpriseBeans;
    } //-- void setEnterpriseBeans(org.apache.openejb.config.ejb11.EnterpriseBeans)

    /**
     * Sets the value of field 'id'.
     *
     * @param id the value of field 'id'.
     */
    public void setId(java.lang.String id) {
        this._id = id;
    } //-- void setId(java.lang.String) 

    /**
     * Sets the value of field 'largeIcon'.
     *
     * @param largeIcon the value of field 'largeIcon'.
     */
    public void setLargeIcon(java.lang.String largeIcon) {
        this._largeIcon = largeIcon;
    } //-- void setLargeIcon(java.lang.String) 

    /**
     * Sets the value of field 'smallIcon'.
     *
     * @param smallIcon the value of field 'smallIcon'.
     */
    public void setSmallIcon(java.lang.String smallIcon) {
        this._smallIcon = smallIcon;
    } //-- void setSmallIcon(java.lang.String) 

    /**
     * Method unmarshal
     *
     * @param reader
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.apache.openejb.config.ejb11.EjbJar) Unmarshaller.unmarshal(org.apache.openejb.config.ejb11.EjbJar.class, reader);
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
