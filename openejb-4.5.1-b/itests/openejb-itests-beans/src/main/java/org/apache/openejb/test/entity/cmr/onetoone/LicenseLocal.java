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
package org.apache.openejb.test.entity.cmr.onetoone;

import javax.ejb.EJBLocalObject;

/**
 *
 * @version $Revision$ $Date$
 */
public interface LicenseLocal extends EJBLocalObject {

    // CMP
    public Integer getId();
    public void setId(Integer id);

    public String getNumber();
    public void setNumber(String number);

    public Integer getPoints();
    public void setPoints(Integer points);

    public String getNotes();
    public void setNotes(String notes);

    // CMR
    public PersonLocal getPerson();
    public void setPerson(PersonLocal person);
    
}
