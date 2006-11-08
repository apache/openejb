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
package org.openejb.test.entity.cmp2.model;

import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;


/**
 * @version $Revision$ $Date$
 */
public abstract class LineItemBean implements EntityBean {

    // CMP
    public abstract Integer getId();
    public abstract void setId(Integer primaryKey);

    public abstract int getQuantity();
    public abstract void setQuantity(int quantity);

    // CMR
    public abstract OrderLocal getOrder();
    public abstract void setOrder(OrderLocal order);

    public abstract ProductLocal getProduct();
    public abstract void setProduct(ProductLocal product);
    
    public Integer ejbCreate(Integer id, int quantity) throws CreateException {
        setId(id);
        setQuantity(quantity);
        return null;
    }

    public void ejbPostCreate(Integer id, int quantity) {
    }
    
    public void ejbLoad() {
    }

    public void setEntityContext(EntityContext ctx) {
    }

    public void unsetEntityContext() {
    }

    public void ejbStore() {
    }

    public void ejbRemove() {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }
}
