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
package org.apache.openejb.tools.release;

import org.apache.log4j.Logger;

/**
* @version $Rev$ $Date$
*/
class Log4jLog implements Options.Log {
    private final Logger log;

    public Log4jLog(Logger log) {
        this.log = log;
    }

    @Override
    public boolean isDebugEnabled() {
        return isInfoEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    @Override
    public boolean isWarningEnabled() {
        return true;
    }

    @Override
    public void warning(String message, Throwable t) {
        log.warn(message, t);
    }

    @Override
    public void warning(String message) {
        log.warn(message);
    }

    @Override
    public void debug(String message, Throwable t) {
        info(message, t);
    }

    @Override
    public void debug(String message) {
        info(message);
    }

    @Override
    public void info(String message, Throwable t) {
        log.info(message, t);
    }

    @Override
    public void info(String message) {
        log.info(message);
    }
}
