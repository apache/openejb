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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.superbiz.customid;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

@Stateless
public class TestBean implements TestLocal {

    @PersistenceContext
    private EntityManager em;

    @Resource
    private DataSource dataSource;

    public void sql(String sql) throws Exception {
        Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
    }

    public void tx1() {
        Invoice invoice = new Invoice(1, "Red", 1.30);
        LineItem item = new LineItem("1", 10);

        item.setInvoice(invoice);
        invoice.getLineItems().add(item);
        em.persist(invoice);
    }

    public void tx2() {
        Invoice invoice = em.find(Invoice.class, new InvoiceKey(1, "Red"));
        List<LineItem> list = invoice.getLineItems();
        System.out.println(list.size());
    }
}
