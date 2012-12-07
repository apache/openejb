/*
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
package org.apache.openejb;

/**
 * @version $Rev$ $Date$
 */
public enum BeanType {
    STATEFUL, STATELESS, SINGLETON, BMP_ENTITY, CMP_ENTITY, MESSAGE_DRIVEN, MANAGED;

    public boolean isEntity(){
        return this == CMP_ENTITY || this == BMP_ENTITY;
    }

    public boolean isSession(){
        return this == STATEFUL || this == STATELESS || this == SINGLETON || this == MANAGED;
    }

    public boolean isCdiCompatible(){
        return isSession() || isMessageDriven();
    }

    public boolean isMessageDriven() {
        return this == MESSAGE_DRIVEN;
    }
}
