/*
 *  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.deployment;

import java.io.File;

/**
 * Artifact represents an artifact to deploy in Carbon.
 */

public class Artifact {

    private File file;
    private String type;
    private long lastModifiedTime;


    public Artifact(File file) {
        this.file = file;
    }

    public Artifact(File file, String type) {
        this.file = file;
        this.type = type;
    }

    public Artifact(File file, String type, long lastModifiedTime) {
        this.file = file;
        this.type = type;
        this.lastModifiedTime = lastModifiedTime;
    }

    /**
     * Set the type of the deployment file
     *
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the type of the deployment file
     *
     * @return type
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the deployment file
     *
     * @return file
     */
    public File getFile() {
        return file;
    }

    /**
     * Returns the deployment file name
     *
     * @return file name
     */
    public String getName() {
        return file.getName();
    }

    /**
     * Returns the absolute path for the deployment file
     *
     * @return absolute path
     */
    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }

    /**
     * @return
     */
    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    /**
     * @param lastModifiedTime
     */
    public void setLastModifiedTime(long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    /**
     *
     * @return
     */
    public String getFileExtension() {
        int index = getName().lastIndexOf('.');
        return getName().substring(index + 1);
    }

}
