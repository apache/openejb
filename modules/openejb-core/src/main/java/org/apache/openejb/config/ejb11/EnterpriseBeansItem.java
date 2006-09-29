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
    private org.apache.openejb.config.ejb11.Session _session;

    /**
     * Field _entity
     */
    private org.apache.openejb.config.ejb11.Entity _entity;


    //----------------/
    //- Constructors -/
    //----------------/

    public EnterpriseBeansItem() {
        super();
    } //-- org.apache.openejb.config.ejb11.EnterpriseBeansItem()


    //-----------/
    //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'entity'.
     *
     * @return the value of field 'entity'.
     */
    public org.apache.openejb.config.ejb11.Entity getEntity() {
        return this._entity;
    } //-- org.apache.openejb.config.ejb11.Entity getEntity()

    /**
     * Returns the value of field 'session'.
     *
     * @return the value of field 'session'.
     */
    public org.apache.openejb.config.ejb11.Session getSession() {
        return this._session;
    } //-- org.apache.openejb.config.ejb11.Session getSession()

    /**
     * Sets the value of field 'entity'.
     *
     * @param entity the value of field 'entity'.
     */
    public void setEntity(org.apache.openejb.config.ejb11.Entity entity) {
        this._entity = entity;
    } //-- void setEntity(org.apache.openejb.config.ejb11.Entity)

    /**
     * Sets the value of field 'session'.
     *
     * @param session the value of field 'session'.
     */
    public void setSession(org.apache.openejb.config.ejb11.Session session) {
        this._session = session;
    } //-- void setSession(org.apache.openejb.config.ejb11.Session)

}
