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

package org.apache.tomee.webapp.command.impl;

import org.apache.tomee.webapp.command.Command;
import org.apache.tomee.webapp.command.CommandSession;
import org.apache.tomee.webapp.helper.rest.Application;
import org.apache.tomee.webapp.helper.rest.Services;
import org.apache.tomee.webapp.helper.rest.WebServiceHelperImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetWebServices implements Command {

    private Map<String, Object> getServicesMap(final Services services) {
        final Map<String, Object> result = new HashMap<String, Object>();
        final List<Application> applications = services.getApplications();
        for (Application application : applications) {
            result.put("name", application.getName());
            result.put("services", application.getServices());

        }
        return result;
    }

    @Override
    public Object execute(final CommandSession session, final Map<String, Object> params) throws Exception {
        // Is this user authenticated?
        session.assertAuthenticated();

        final Map<String, Object> json = new HashMap<String, Object>();
        json.put("rest", getServicesMap(WebServiceHelperImpl.restWebServices()));
        json.put("soap", getServicesMap(WebServiceHelperImpl.soapWebServices()));

        return json;
    }
}
