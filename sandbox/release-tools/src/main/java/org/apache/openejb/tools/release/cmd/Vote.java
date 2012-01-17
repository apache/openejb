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
import org.apache.openejb.tools.release.Release;
import org.apache.openejb.tools.release.util.Exec;
import org.apache.rat.tentacles.Templates;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;

/**
 * @version $Rev$ $Date$
 */
@Command(dependsOn = {Legal.class, Binaries.class})
public class Vote {

    public static void main(String[] args) throws IOException {

        final Templates.Builder template = Templates.template("vote.vm");

        for (Field field : Release.class.getFields()) {
            try {
                template.add(field.getName(), field.get(null));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        final String email = template.apply();

        System.out.println(email);

        final OutputStream out = Exec.open("ssh", "people.apache.org", "/usr/sbin/sendmail -it");
        out.write(email.getBytes());
        out.flush();
        out.close();
    }
}
