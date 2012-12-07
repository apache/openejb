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
package org.apache.openejb.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LengthInputStream extends FilterInputStream {
    private long length;

    public LengthInputStream(InputStream in) throws IOException {
        super(in);
    }

    @Override
    public int read() throws IOException {
        final int i = super.read();
        if (i > 0) length++;
        return i;
    }

    @Override
    public int read(byte[] b) throws IOException {
        final int i = super.read(b);
        if (i > 0) length += i;
        return i;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        final int i = super.read(b, off, len);
        if (i > 0) length += i;
        return i;
    }

    public long getLength() {
        return length;
    }
}
