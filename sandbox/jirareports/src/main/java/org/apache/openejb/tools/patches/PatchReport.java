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
package org.apache.openejb.tools.patches;

import org.codehaus.swizzle.jira.MapObjectList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @version $Rev$ $Date$
 */
public class PatchReport {

    public static void main(String[] args) throws Throwable {
        System.setProperty("template", "patches.vm");
        org.codehaus.swizzle.jirareport.Main.main(args);
    }

    public List getList() {
        return new MapObjectList();
    }

    public Map<String, Log> patches(String svnurl) throws Exception {
        //svn log --xml "-rHEAD:{2010-01-01}"

        final String[] args = {"svn", "log", "--xml", "-rHEAD:{2010-06-01}", svnurl};
        final Process process = Runtime.getRuntime().exec(args);
        Pipe.out(process);
        Pipe.err(process);

        final JAXBContext context = JAXBContext.newInstance(Log.class);
        final Unmarshaller unmarshaller = context.createUnmarshaller();

        final Log log = (Log) unmarshaller.unmarshal(process.getInputStream());

        Map<String, Log> patches = new HashMap<String, Log>();

        for (Entry entry : log.getEntries()) {
            final String message = entry.getMessage().toLowerCase();

            final boolean matches = message.contains("patch") || message.contains("submitted") || message.contains("fix from ");
            if (!matches) continue;

            Log authorLog = patches.get(entry.getAuthor());
            if (authorLog == null) {
                authorLog = new Log();
                patches.put(entry.getAuthor(), authorLog);
            }

            authorLog.getEntries().add(entry);
        }

        return patches;
    }

    public Date ago(int i) {
        return new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(i));
    }
}
