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
package org.apache.openejb.server;

import org.apache.openejb.monitoring.Managed;
import org.apache.openejb.monitoring.Stats;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @version $Rev$ $Date$
 */
@Managed
public class ServiceStats extends ServerServiceFilter {

    @Managed
    private final Stats stats = new Stats();

    public ServiceStats(final ServerService service) {
        super(service);
    }

    @Override
    public void service(final InputStream in, final OutputStream out) throws ServiceException, IOException {
        final long start = System.nanoTime();
        try {
            super.service(in, out);
        } finally {
            stats.record(System.nanoTime() - start);
        }
    }

    @Override
    public void service(final Socket socket) throws ServiceException, IOException {
        final long start = System.nanoTime();
        try {
            super.service(socket);
        } finally {
            stats.record(System.nanoTime() - start);
        }
    }

}
