/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.test.entity.cmr.cmrmapping;

import javax.ejb.FinderException;

public class ManyOwningSideBean_ManyOwningSideBean extends ManyOwningSideBean {
    public Integer id;
    private Integer field1;
    private OneInverseSideLocal oneInverseSide;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getField1() {
        return field1;
    }

    public void setField1(Integer field1) {
        this.field1 = field1;
    }

    public OneInverseSideLocal getOneInverseSide() {
        return oneInverseSide;
    }

    public void setOneInverseSide(OneInverseSideLocal oneInverseSide) {
        this.oneInverseSide = oneInverseSide;
    }

    public OneInverseSideLocal ejbSelectSomething(Integer id) throws FinderException {
        return null;
    }
}
