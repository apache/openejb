/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.plugins.common;

import junit.framework.TestCase;
import org.jmock.Mockery;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.apache.openejb.config.AppModule;

public class EntityBeanPojoConverterTest extends TestCase {
    public void testShouldConvertEntityToPojo() throws Exception {
        Mockery context = new Mockery();
        final Sequence sequence = context.sequence("sequence");

        final IJDTFacade facade = context.mock(IJDTFacade.class);

        context.checking(new Expectations() {
            {
                one(facade).removeAbstractModifierFromClass("org.superbiz.ProductBean");
                inSequence(sequence);

                one(facade).removeInterface("org.superbiz.ProductBean", "javax.ejb.EntityBean");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.ProductBean", "getId", new String[0]);
                inSequence(sequence);
                will(returnValue("java.lang.Integer"));

                one(facade).addField("org.superbiz.ProductBean", "id", "java.lang.Integer");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "getId", new String[0], "return id;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "setId", new String[]{"java.lang.Integer"}, "this.id = ${0};");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.ProductBean", "getName", new String[0]);
                inSequence(sequence);
                will(returnValue("java.lang.String"));

                one(facade).addField("org.superbiz.ProductBean", "name", "java.lang.String");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "getName", new String[0], "return name;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "setName", new String[]{"java.lang.String"}, "this.name = ${0};");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.ProductBean", "getCode", new String[0]);
                inSequence(sequence);
                will(returnValue("java.lang.String"));

                one(facade).addField("org.superbiz.ProductBean", "code", "java.lang.String");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "getCode", new String[0], "return code;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "setCode", new String[]{"java.lang.String"}, "this.code = ${0};");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.ProductBean", "getDescription", new String[0]);
                inSequence(sequence);
                will(returnValue("java.lang.String"));

                one(facade).addField("org.superbiz.ProductBean", "description", "java.lang.String");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "getDescription", new String[0], "return description;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "setDescription", new String[]{"java.lang.String"}, "this.description = ${0};");
                inSequence(sequence);
            }
        });

        AppModule module = new TestFixture().getAppModule("basicentity-ejb-jar.xml", null);
        new EntityBeanPojoConverter(facade).convert(module);

        context.assertIsSatisfied();
    }

    public void testShouldConvertEntityWithOneToManyToPojo() throws Exception {
        Mockery context = new Mockery();
        final Sequence sequence = context.sequence("sequence");

        final IJDTFacade facade = context.mock(IJDTFacade.class);

        context.checking(new Expectations() {
            {
                one(facade).removeAbstractModifierFromClass("org.superbiz.OrderBean");
                inSequence(sequence);

                one(facade).removeInterface("org.superbiz.OrderBean", "javax.ejb.EntityBean");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.OrderBean", "getId", new String[0]);
                inSequence(sequence);
                will(returnValue("java.lang.Integer"));

                one(facade).addField("org.superbiz.OrderBean", "id", "java.lang.Integer");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderBean", "getId", new String[0], "return id;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderBean", "setId", new String[]{"java.lang.Integer"}, "this.id = ${0};");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromClass("org.superbiz.OrderLineBean");
                inSequence(sequence);

                one(facade).removeInterface("org.superbiz.OrderLineBean", "javax.ejb.EntityBean");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.OrderLineBean", "getId", new String[0]);
                inSequence(sequence);
                will(returnValue("java.lang.Integer"));

                one(facade).addField("org.superbiz.OrderLineBean", "id", "java.lang.Integer");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderLineBean", "getId", new String[0], "return id;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderLineBean", "setId", new String[]{"java.lang.Integer"}, "this.id = ${0};");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.OrderLineBean", "getQty", new String[0]);
                inSequence(sequence);
                will(returnValue("java.lang.Integer"));

                one(facade).addField("org.superbiz.OrderLineBean", "qty", "java.lang.Integer");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderLineBean", "getQty", new String[0], "return qty;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderLineBean", "setQty", new String[]{"java.lang.Integer"}, "this.qty = ${0};");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.OrderBean", "getOrderLine", new String[0]);
                inSequence(sequence);
                will(returnValue("java.util.Collection"));

                one(facade).addField("org.superbiz.OrderBean", "orderLine", "java.util.Collection");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderBean", "getOrderLine", new String[0], "return orderLine;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderBean", "setOrderLine", new String[]{"java.util.Collection"}, "this.orderLine = ${0};");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.OrderLineBean", "getOrder", new String[0]);
                inSequence(sequence);
                will(returnValue("org.superbiz.Order"));

                one(facade).addField("org.superbiz.OrderLineBean", "order", "org.superbiz.Order");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderLineBean", "getOrder", new String[0], "return order;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderLineBean", "setOrder", new String[]{"org.superbiz.Order"}, "this.order = ${0};");
                inSequence(sequence);
            }
        });

        AppModule module = new TestFixture().getAppModule("onetomany-ejb-jar.xml", null);
        new EntityBeanPojoConverter(facade).convert(module);

        context.assertIsSatisfied();
    }

    public void testShouldConvertEntityWithOneToOneToPojo() throws Exception {
        Mockery context = new Mockery();
        final Sequence sequence = context.sequence("sequence");

        final IJDTFacade facade = context.mock(IJDTFacade.class);

        context.checking(new Expectations() {
            {
                one(facade).removeAbstractModifierFromClass("org.superbiz.ProductBean");
                inSequence(sequence);

                one(facade).removeInterface("org.superbiz.ProductBean", "javax.ejb.EntityBean");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.ProductBean", "getId", new String[0]);
                inSequence(sequence);
                will(returnValue("java.lang.Integer"));

                one(facade).addField("org.superbiz.ProductBean", "id", "java.lang.Integer");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "getId", new String[0], "return id;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "setId", new String[]{"java.lang.Integer"}, "this.id = ${0};");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.ProductBean", "getName", new String[0]);
                inSequence(sequence);
                will(returnValue("java.lang.String"));

                one(facade).addField("org.superbiz.ProductBean", "name", "java.lang.String");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "getName", new String[0], "return name;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "setName", new String[]{"java.lang.String"}, "this.name = ${0};");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.ProductBean", "getCode", new String[0]);
                inSequence(sequence);
                will(returnValue("java.lang.String"));

                one(facade).addField("org.superbiz.ProductBean", "code", "java.lang.String");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "getCode", new String[0], "return code;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "setCode", new String[]{"java.lang.String"}, "this.code = ${0};");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.ProductBean", "getDescription", new String[0]);
                inSequence(sequence);
                will(returnValue("java.lang.String"));

                one(facade).addField("org.superbiz.ProductBean", "description", "java.lang.String");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "getDescription", new String[0], "return description;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "setDescription", new String[]{"java.lang.String"}, "this.description = ${0};");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromClass("org.superbiz.OrderLineBean");
                inSequence(sequence);

                one(facade).removeInterface("org.superbiz.OrderLineBean", "javax.ejb.EntityBean");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.OrderLineBean", "getId", new String[0]);
                inSequence(sequence);
                will(returnValue("java.lang.Integer"));

                one(facade).addField("org.superbiz.OrderLineBean", "id", "java.lang.Integer");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderLineBean", "getId", new String[0], "return id;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderLineBean", "setId", new String[]{"java.lang.Integer"}, "this.id = ${0};");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.OrderLineBean", "getQty", new String[0]);
                inSequence(sequence);
                will(returnValue("java.lang.Integer"));

                one(facade).addField("org.superbiz.OrderLineBean", "qty", "java.lang.Integer");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderLineBean", "getQty", new String[0], "return qty;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderLineBean", "setQty", new String[]{"java.lang.Integer"}, "this.qty = ${0};");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.OrderLineBean", "getProduct", new String[0]);
                inSequence(sequence);
                will(returnValue("org.superbiz.Product"));

                one(facade).addField("org.superbiz.OrderLineBean", "product", "org.superbiz.Product");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderLineBean", "getProduct", new String[0], "return product;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderLineBean", "setProduct", new String[]{"org.superbiz.Product"}, "this.product = ${0};");
                inSequence(sequence);
            }
        });

        AppModule module = new TestFixture().getAppModule("onetoone-ejb-jar.xml", null);
        new EntityBeanPojoConverter(facade).convert(module);

        context.assertIsSatisfied();
    }

    public void testShouldConvertEntityWithManyToManyToPojo() throws Exception {
        Mockery context = new Mockery();
        final Sequence sequence = context.sequence("sequence");

        final IJDTFacade facade = context.mock(IJDTFacade.class);

        context.checking(new Expectations() {
            {
                one(facade).removeAbstractModifierFromClass("org.superbiz.ProductBean");
                inSequence(sequence);

                one(facade).removeInterface("org.superbiz.ProductBean", "javax.ejb.EntityBean");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.ProductBean", "getId", new String[0]);
                inSequence(sequence);
                will(returnValue("java.lang.Integer"));

                one(facade).addField("org.superbiz.ProductBean", "id", "java.lang.Integer");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "getId", new String[0], "return id;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "setId", new String[]{"java.lang.Integer"}, "this.id = ${0};");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.ProductBean", "getName", new String[0]);
                inSequence(sequence);
                will(returnValue("java.lang.String"));

                one(facade).addField("org.superbiz.ProductBean", "name", "java.lang.String");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "getName", new String[0], "return name;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "setName", new String[]{"java.lang.String"}, "this.name = ${0};");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.ProductBean", "getCode", new String[0]);
                inSequence(sequence);
                will(returnValue("java.lang.String"));

                one(facade).addField("org.superbiz.ProductBean", "code", "java.lang.String");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "getCode", new String[0], "return code;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "setCode", new String[]{"java.lang.String"}, "this.code = ${0};");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.ProductBean", "getDescription", new String[0]);
                inSequence(sequence);
                will(returnValue("java.lang.String"));

                one(facade).addField("org.superbiz.ProductBean", "description", "java.lang.String");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "getDescription", new String[0], "return description;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "setDescription", new String[]{"java.lang.String"}, "this.description = ${0};");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromClass("org.superbiz.OrderLineBean");
                inSequence(sequence);

                one(facade).removeInterface("org.superbiz.OrderLineBean", "javax.ejb.EntityBean");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.OrderLineBean", "getId", new String[0]);
                inSequence(sequence);
                will(returnValue("java.lang.Integer"));

                one(facade).addField("org.superbiz.OrderLineBean", "id", "java.lang.Integer");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderLineBean", "getId", new String[0], "return id;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderLineBean", "setId", new String[]{"java.lang.Integer"}, "this.id = ${0};");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.OrderLineBean", "getQty", new String[0]);
                inSequence(sequence);
                will(returnValue("java.lang.Integer"));

                one(facade).addField("org.superbiz.OrderLineBean", "qty", "java.lang.Integer");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderLineBean", "getQty", new String[0], "return qty;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderLineBean", "setQty", new String[]{"java.lang.Integer"}, "this.qty = ${0};");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.OrderLineBean", "getProduct", new String[0]);
                inSequence(sequence);
                will(returnValue("java.util.Collection"));

                one(facade).addField("org.superbiz.OrderLineBean", "product", "java.util.Collection");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderLineBean", "getProduct", new String[0], "return product;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderLineBean", "setProduct", new String[]{"java.util.Collection"}, "this.product = ${0};");
                inSequence(sequence);
            }
        });

        AppModule module = new TestFixture().getAppModule("manytomany-ejb-jar.xml", null);
        new EntityBeanPojoConverter(facade).convert(module);

        context.assertIsSatisfied();
    }

    public void testShouldConvertEntityWithManyToOneToPojo() throws Exception {
        Mockery context = new Mockery();
        final Sequence sequence = context.sequence("sequence");

        final IJDTFacade facade = context.mock(IJDTFacade.class);

        context.checking(new Expectations() {
            {
                one(facade).removeAbstractModifierFromClass("org.superbiz.ProductBean");
                inSequence(sequence);

                one(facade).removeInterface("org.superbiz.ProductBean", "javax.ejb.EntityBean");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.ProductBean", "getId", new String[0]);
                inSequence(sequence);
                will(returnValue("java.lang.Integer"));

                one(facade).addField("org.superbiz.ProductBean", "id", "java.lang.Integer");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "getId", new String[0], "return id;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "setId", new String[]{"java.lang.Integer"}, "this.id = ${0};");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.ProductBean", "getName", new String[0]);
                inSequence(sequence);
                will(returnValue("java.lang.String"));

                one(facade).addField("org.superbiz.ProductBean", "name", "java.lang.String");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "getName", new String[0], "return name;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "setName", new String[]{"java.lang.String"}, "this.name = ${0};");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.ProductBean", "getCode", new String[0]);
                inSequence(sequence);
                will(returnValue("java.lang.String"));

                one(facade).addField("org.superbiz.ProductBean", "code", "java.lang.String");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "getCode", new String[0], "return code;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "setCode", new String[]{"java.lang.String"}, "this.code = ${0};");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.ProductBean", "getDescription", new String[0]);
                inSequence(sequence);
                will(returnValue("java.lang.String"));

                one(facade).addField("org.superbiz.ProductBean", "description", "java.lang.String");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "getDescription", new String[0], "return description;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "setDescription", new String[]{"java.lang.String"}, "this.description = ${0};");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromClass("org.superbiz.OrderLineBean");
                inSequence(sequence);

                one(facade).removeInterface("org.superbiz.OrderLineBean", "javax.ejb.EntityBean");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.OrderLineBean", "getId", new String[0]);
                inSequence(sequence);
                will(returnValue("java.lang.Integer"));

                one(facade).addField("org.superbiz.OrderLineBean", "id", "java.lang.Integer");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderLineBean", "getId", new String[0], "return id;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderLineBean", "setId", new String[]{"java.lang.Integer"}, "this.id = ${0};");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.OrderLineBean", "getQty", new String[0]);
                inSequence(sequence);
                will(returnValue("java.lang.Integer"));

                one(facade).addField("org.superbiz.OrderLineBean", "qty", "java.lang.Integer");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderLineBean", "getQty", new String[0], "return qty;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderLineBean", "setQty", new String[]{"java.lang.Integer"}, "this.qty = ${0};");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.OrderLineBean", "getProduct", new String[0]);
                inSequence(sequence);
                will(returnValue("java.util.Collection"));

                one(facade).addField("org.superbiz.OrderLineBean", "product", "java.util.Collection");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderLineBean", "getProduct", new String[0], "return product;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderLineBean", "setProduct", new String[]{"java.util.Collection"}, "this.product = ${0};");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.ProductBean", "getOrderLine", new String[0]);
                inSequence(sequence);
                will(returnValue("org.superbiz.OrderLine"));

                one(facade).addField("org.superbiz.ProductBean", "orderLine", "org.superbiz.OrderLine");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "getOrderLine", new String[0], "return orderLine;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.ProductBean", "setOrderLine", new String[]{"org.superbiz.OrderLine"}, "this.orderLine = ${0};");
                inSequence(sequence);
            }
        });

        AppModule module = new TestFixture().getAppModule("manytoone-ejb-jar.xml", null);
        new EntityBeanPojoConverter(facade).convert(module);

        context.assertIsSatisfied();
    }

    public void testShouldConvertEntityWithBadRelationshipToPojo() throws Exception {
        Mockery context = new Mockery();
        final Sequence sequence = context.sequence("sequence");

        final IJDTFacade facade = context.mock(IJDTFacade.class);

        context.checking(new Expectations() {
            {
                one(facade).removeAbstractModifierFromClass("org.superbiz.OrderBean");
                inSequence(sequence);

                one(facade).removeInterface("org.superbiz.OrderBean", "javax.ejb.EntityBean");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.OrderBean", "getId", new String[0]);
                inSequence(sequence);
                will(returnValue("java.lang.Integer"));

                one(facade).addField("org.superbiz.OrderBean", "id", "java.lang.Integer");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderBean", "getId", new String[0], "return id;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderBean", "setId", new String[]{"java.lang.Integer"}, "this.id = ${0};");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromClass("org.superbiz.OrderLineBean");
                inSequence(sequence);

                one(facade).removeInterface("org.superbiz.OrderLineBean", "javax.ejb.EntityBean");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.OrderLineBean", "getId", new String[0]);
                inSequence(sequence);
                will(returnValue("java.lang.Integer"));

                one(facade).addField("org.superbiz.OrderLineBean", "id", "java.lang.Integer");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderLineBean", "getId", new String[0], "return id;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderLineBean", "setId", new String[]{"java.lang.Integer"}, "this.id = ${0};");
                inSequence(sequence);

                one(facade).getMethodReturnType("org.superbiz.OrderLineBean", "getQty", new String[0]);
                inSequence(sequence);
                will(returnValue("java.lang.Integer"));

                one(facade).addField("org.superbiz.OrderLineBean", "qty", "java.lang.Integer");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderLineBean", "getQty", new String[0], "return qty;");
                inSequence(sequence);

                one(facade).removeAbstractModifierFromMethod("org.superbiz.OrderLineBean", "setQty", new String[]{"java.lang.Integer"}, "this.qty = ${0};");
                inSequence(sequence);
            }
        });

        AppModule module = new TestFixture().getAppModule("emptyrelationship-ejb-jar.xml", null);
        new EntityBeanPojoConverter(facade).convert(module);

        context.assertIsSatisfied();

    }
}
