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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;
import java.util.Date;

/**
 * @version $Rev$ $Date$
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Repository {

    public Repository() {
    }

    // 4f527aa7ac8ab3
    private String profileId;

    // org.apache.openejb
    private String profileName;

    // repository
    private String profileType;

    // orgapacheopenejb-072
    private String repositoryId;

    // org.apache.openejb-072 (u:dblevins, a:76.175.195.209)
    private String repositoryName;

    // open
    private String type;

    // release
    private String policy;

    // dblevins
    private String userId;

    // Apache-Maven/3.0.3 (Java 1.6.0_26; Mac OS X 10.7.2)
    private String userAgent;

    // 76.175.195.209
    private String ipAddress;

    // https://repository.apache.org/content/repositories/orgapacheopenejb-072
    private URI repositoryURI;

    // Sun Jan 15 23:22:43 UTC 2012
    @XmlJavaTypeAdapter(DateAdapter.class)
    private Date createdDate;

    // n/a
    @XmlJavaTypeAdapter(DateAdapter.class)
    private Date closedDate;

    // maven2
    private String provider;

    // releases
    private String releaseRepositoryId;

    // Releases
    private String releaseRepositoryName;


    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileType() {
        return profileType;
    }

    public void setProfileType(String profileType) {
        this.profileType = profileType;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public URI getRepositoryURI() {
        return repositoryURI;
    }

    public void setRepositoryURI(URI repositoryURI) {
        this.repositoryURI = repositoryURI;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(Date closedDate) {
        this.closedDate = closedDate;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getReleaseRepositoryId() {
        return releaseRepositoryId;
    }

    public void setReleaseRepositoryId(String releaseRepositoryId) {
        this.releaseRepositoryId = releaseRepositoryId;
    }

    public String getReleaseRepositoryName() {
        return releaseRepositoryName;
    }

    public void setReleaseRepositoryName(String releaseRepositoryName) {
        this.releaseRepositoryName = releaseRepositoryName;
    }

    @Override
    public String toString() {
        return "Repository{" +
                "repositoryId='" + repositoryId + '\'' +
                ", type='" + type + '\'' +
                ", createdDate=" + createdDate +
                '}';
    }
}
