package org.apache.openejb.maven.plugin.spi;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.archive.Archive;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ExtendedAnnotationFinder extends org.apache.xbean.finder.AnnotationFinder {
    private static Field annotatedField;

    static { // low cost way to get info
        try {
            annotatedField = ExtendedAnnotationFinder.class.getSuperclass().getDeclaredField("annotated");
            annotatedField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            // shouldn't occur
        }
    }

    public ExtendedAnnotationFinder(final Archive archive) {
        super(archive);
    }

    public Map<String, List<AnnotationFinder.Info>> getAnnotated() {
        try {
            return (Map<String, List<AnnotationFinder.Info>>) annotatedField.get(this);
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
