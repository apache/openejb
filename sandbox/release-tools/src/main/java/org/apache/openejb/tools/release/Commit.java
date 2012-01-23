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

import org.apache.openejb.tools.release.util.ObjectList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import java.util.Date;

/**
 * @version $Rev$ $Date$
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Commit {

    private Date date;

    @XmlAttribute
    private long revision;

    @XmlElement(name = "msg")
    private String message;

    private String author;

    @XmlElementWrapper(name = "paths")
    @XmlElement(name = "path")
    private ObjectList<Path> paths = new ObjectList<Path>();

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getRevision() {
        return revision;
    }

    public void setRevision(long revision) {
        this.revision = revision;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public ObjectList<Path> getPaths() {
        return paths;
    }

    @Override
    public String toString() {
        return "Commit{" +
                "revision=" + revision +
                ", date=" + date +
                ", author='" + author + '\'' +
                ", paths=" + paths.size() +
                ", message='" + message + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Commit commit = (Commit) o;

        if (revision != commit.revision) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (revision ^ (revision >>> 32));
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Path {

        @XmlAttribute
        private String kind;

        @XmlAttribute
        private String action;

        @XmlValue
        private String path;

        public String getKind() {
            return kind;
        }

        public void setKind(String kind) {
            this.kind = kind;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "log")
    public static class Log {

        @XmlElement(name = "logentry")
        private ObjectList<Commit> commits = new ObjectList<Commit>();

        public Log() {
        }

        public ObjectList<Commit> getCommits() {
            return commits;
        }
    }

}
