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

package org.apache.openejb.assembler;

import java.util.Properties;

import org.apache.openejb.OpenEJBException;

/**
 * Factory for creating an instance of the OpenEjbConfiguration
 * 
 * <p>
 * The OpenEjbConfiguration object structure provides the information about the
 * configuration of OpenEJB and the container system and is used by the
 * org.apache.openejb.assembler.Assembler to build a running unstance of OpenEJB.</p>
 * 
 * <p>
 * The default OpenEjbConfigurationFactory is DomOpenEjbConfigurationFactory, which
 * creates an OpenEjbConfiguration object based on XML config files located on the
 * local system.</p>
 * 
 * <p>
 * Other OpenEjbConfigurationFactory implementations can be created that might populate
 * this object using a different approach.  Other usefull implementations might be:<br>
 * <UL>
 * <LI>Populating the OpenEjbConfiguration from values in a RDBMS.
 * <LI>Populating the OpenEjbConfiguration from values in a Properties file.
 * <LI>Retrieving the OpenEjbConfiguration from a ODBMS.
 * <LI>Creating the OpenEjbConfiguration using a JavaBeans enabled editing tool or wizard.
 * </UL>
 * 
 * <p>
 * If you are interested in creating alternate an OpenEjbConfigurationFactory to do
 * any of the above techniques or a new approach, email the
 * <a href="mailto:openejb-dev@exolab.org">OpenEJB Developer list</a> with a description
 * of the new OpenEjbConfigurationFactory implementation.
 * </p>
 * 
 * @see org.apache.openejb.spi.Assembler
 * @see org.apache.openejb.assembler.Assembler
 * @see org.apache.openejb.assembler.OpenEjbConfiguration
 * @see org.apache.openejb.assembler.OpenEjbConfigurationFactory
 * @see org.apache.openejb.alt.xmlconf.DomOpenEjbConfigurationFactory
 */
public interface OpenEjbConfigurationFactory {
    

    public void init(Properties props)throws OpenEJBException;
    
    public OpenEjbConfiguration getOpenEjbConfiguration()throws OpenEJBException;

    /*
     * Not used yet.
     * Will be used in the future to give the ability to load and reload container systems
     * after OpenEJB has been started.
    public ContainerSystemInfo getContainerSystemInformation()throws OpenEJBException;

    */

}
