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

import java.util.Properties;
import java.io.*;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.ConfigUtils;

/**
 * Class ServiceProvider.
 *
 * @version $Revision$ $Date$
 */
public class ServiceProvider implements java.io.Serializable {


    //--------------------------/
    //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _id
     */
    private java.lang.String _id;

    /**
     * Field _providerType
     */
    private java.lang.String _providerType;

    /**
     * Field _displayName
     */
    private java.lang.String _displayName;

    /**
     * Field _description
     */
    private java.lang.String _description;

    /**
     * Field _className
     */
    private java.lang.String _className;

    /**
     * internal content storage
     */
    private java.lang.String _content = "";

    /**
     * Field _propertiesFile
     */
    private org.apache.openejb.config.sys.PropertiesFile _propertiesFile;

    /**
     * Field _lookup
     */
    private org.apache.openejb.config.sys.Lookup _lookup;


    //----------------/
    //- Constructors -/
    //----------------/

    public ServiceProvider() {
        super();
        setContent("");
    } //-- org.apache.openejb.config.sys.ServiceProvider()


    //-----------/
    //- Methods -/
    //-----------/

    public Properties getProperties() throws OpenEJBException {
        Properties props = new Properties();
        try {
            /*
             * 1. Load properties from the properties file referenced
             *    by the service provider
             */
            if (getPropertiesFile() != null) {
                props = loadProperties(this.getPropertiesFile().getFile());
            }
            /*
             * 2. Load properties from the content in the this provider
             *    element of the service-jar.xml
             */
            if (this.getContent() != null) {
                StringBufferInputStream in = new StringBufferInputStream(this.getContent());
                props = loadProperties(in, props);
            }
        } catch (OpenEJBException ex) {
            ConfigUtils.handleException("conf.0013",
                    this.getId(),
                    null,
                    ex.getLocalizedMessage());
        }
        return props;
    }

    private static Properties loadProperties(String pFile) throws OpenEJBException {
        return loadProperties(pFile, new Properties());
    }

    private static Properties loadProperties(String propertiesFile, Properties defaults)
            throws OpenEJBException {
        try {
            File pfile = new File(propertiesFile);
            InputStream in = new FileInputStream(pfile);
            return loadProperties(in, defaults);
        } catch (FileNotFoundException ex) {
            ConfigUtils.handleException("conf.0006", propertiesFile, ex.getLocalizedMessage());
        } catch (IOException ex) {
            ConfigUtils.handleException("conf.0007", propertiesFile, ex.getLocalizedMessage());
        } catch (SecurityException ex) {
            ConfigUtils.handleException("conf.0005", propertiesFile, ex.getLocalizedMessage());
        }
        return defaults;
    }

    private static Properties loadProperties(InputStream in, Properties defaults)
            throws OpenEJBException {
        try {
            /*
            This may not work as expected.  The desired effect is that
            the load method will read in the properties and overwrite
            the values of any properties that may have previously been
            defined.
            */
            defaults.load(in);
        } catch (IOException ex) {
            ConfigUtils.handleException("conf.0012", ex.getLocalizedMessage());
        }
        return defaults;
    }

    /**
     * Returns the value of field 'className'.
     *
     * @return the value of field 'className'.
     */
    public java.lang.String getClassName() {
        return this._className;
    } //-- java.lang.String getClassName() 

    /**
     * Returns the value of field 'content'. The field 'content'
     * has the following description: internal content storage
     *
     * @return the value of field 'content'.
     */
    public java.lang.String getContent() {
        return this._content;
    } //-- java.lang.String getContent() 

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
     * Returns the value of field 'id'.
     *
     * @return the value of field 'id'.
     */
    public java.lang.String getId() {
        return this._id;
    } //-- java.lang.String getId() 

    /**
     * Returns the value of field 'lookup'.
     *
     * @return the value of field 'lookup'.
     */
    public org.apache.openejb.config.sys.Lookup getLookup() {
        return this._lookup;
    } //-- org.apache.openejb.config.sys.Lookup getLookup()

    /**
     * Returns the value of field 'propertiesFile'.
     *
     * @return the value of field 'propertiesFile'.
     */
    public org.apache.openejb.config.sys.PropertiesFile getPropertiesFile() {
        return this._propertiesFile;
    } //-- org.apache.openejb.config.sys.PropertiesFile getPropertiesFile()

    /**
     * Returns the value of field 'providerType'.
     *
     * @return the value of field 'providerType'.
     */
    public java.lang.String getProviderType() {
        return this._providerType;
    } //-- java.lang.String getProviderType() 

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
     * Sets the value of field 'className'.
     *
     * @param className the value of field 'className'.
     */
    public void setClassName(java.lang.String className) {
        this._className = className;
    } //-- void setClassName(java.lang.String) 

    /**
     * Sets the value of field 'content'. The field 'content' has
     * the following description: internal content storage
     *
     * @param content the value of field 'content'.
     */
    public void setContent(java.lang.String content) {
        this._content = content;
    } //-- void setContent(java.lang.String) 

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
     * Sets the value of field 'id'.
     *
     * @param id the value of field 'id'.
     */
    public void setId(java.lang.String id) {
        this._id = id;
    } //-- void setId(java.lang.String) 

    /**
     * Sets the value of field 'lookup'.
     *
     * @param lookup the value of field 'lookup'.
     */
    public void setLookup(org.apache.openejb.config.sys.Lookup lookup) {
        this._lookup = lookup;
    } //-- void setLookup(org.apache.openejb.config.sys.Lookup)

    /**
     * Sets the value of field 'propertiesFile'.
     *
     * @param propertiesFile the value of field 'propertiesFile'.
     */
    public void setPropertiesFile(org.apache.openejb.config.sys.PropertiesFile propertiesFile) {
        this._propertiesFile = propertiesFile;
    } //-- void setPropertiesFile(org.apache.openejb.config.sys.PropertiesFile)

    /**
     * Sets the value of field 'providerType'.
     *
     * @param providerType the value of field 'providerType'.
     */
    public void setProviderType(java.lang.String providerType) {
        this._providerType = providerType;
    } //-- void setProviderType(java.lang.String) 

    /**
     * Method unmarshal
     *
     * @param reader
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.apache.openejb.config.sys.ServiceProvider) Unmarshaller.unmarshal(org.apache.openejb.config.sys.ServiceProvider.class, reader);
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
