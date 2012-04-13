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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.tools.release.cmd;

import org.apache.openejb.tools.release.Command;
import org.apache.openejb.tools.release.Commit;
import org.apache.openejb.tools.release.Release;
import org.apache.openejb.tools.release.util.Exec;
import org.apache.openejb.tools.release.util.Join;
import org.apache.openejb.tools.release.util.ObjectList;
import org.apache.openejb.tools.release.util.Options;
import org.codehaus.swizzle.jira.Issue;
import org.codehaus.swizzle.jira.IssueType;
import org.codehaus.swizzle.jira.Jira;
import org.codehaus.swizzle.jira.MapObject;
import org.codehaus.swizzle.jira.Version;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @version $Rev$ $Date$
 */
@Command
public class CommitsPerDay {

    public static void main(String... args) throws Exception {

        final String tag = "http://svn.apache.org/repos/asf/openejb/";

        final String start = "2007-01-01";

        final InputStream in = Exec.read("svn", "log", "--xml", "-rHEAD:{" + start + "}", tag);

        final JAXBContext context = JAXBContext.newInstance(Commit.Log.class);
        final Unmarshaller unmarshaller = context.createUnmarshaller();

        final Commit.Log log = (Commit.Log) unmarshaller.unmarshal(in);

        ObjectList<Commit> commits = log.getCommits();
        commits = commits.ascending("revision");

        final Date end = new Date();

        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(start);

        final ListIterator<Commit> iterator = commits.listIterator();

        while (lesser(date, end)) {

            int c = 0;

            Date next = increment(date);

            while (iterator.hasNext()) {
                final Commit commit = iterator.next();
                if (lesser(commit.getDate(), next)) {
                    c++;
                } else {
                    iterator.previous();
                    break;
                }
            }

            System.out.print(c + ", ");

            date = next;
        }

    }

    private static boolean lesser(Date a, Date b) {
        return a.compareTo(b) < 0;
    }

    private static Date increment(Date previous) {
        final long l = TimeUnit.DAYS.toMillis(30);
        final long time = previous.getTime() + l;
        return new Date(time);
    }
}
