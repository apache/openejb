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
package org.apache.openejb.tools.twitter;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class JsonResponseParser {

    private static Logger logger = Logger.getLogger(JsonResponseParser.class);

    public static String getResponseBody(HttpResponse response) {
        ResponseHandler<String> responseHander = new BasicResponseHandler();
        String responseBody = null;
        try {
            responseBody = (String) responseHander.handleResponse(response);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.debug("Response Body Data:" + responseBody);

        return responseBody;
    }

    @SuppressWarnings("rawtypes")
    public static List<Map> getListFromJson(Reader jsonDataReader) {
        ObjectMapper mapper = new ObjectMapper();
        List<Map> result = null;
        try {
            result = mapper.readValue(jsonDataReader, new TypeReference<ArrayList<Map>>() {
            });
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.debug("Json to List of key value pairs:" + result);
        return result;
    }


    @SuppressWarnings("rawtypes")
    public static Map getMapFromJson(Reader jsonDataReader) {
        Map result = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            result = mapper.readValue(jsonDataReader, new TypeReference<Map>() {
            });
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;

    }

}
