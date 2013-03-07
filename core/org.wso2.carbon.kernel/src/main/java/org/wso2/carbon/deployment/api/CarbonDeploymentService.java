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

package org.wso2.carbon.deployment.api;

import org.wso2.carbon.deployment.CarbonDeploymentException;
import org.wso2.carbon.deployment.Deployer;

/**
 * Interface that exposes {@link org.wso2.carbon.deployment.CarbonDeploymentEngine} functionality
 *
 */

public interface CarbonDeploymentService {

    /**
     * Configure and prepare the repository associated with this deployment engine.
     *
     * @param repositoryDirectory repository directory
     * @throws CarbonDeploymentException on error
     */
    public void init(String repositoryDirectory) throws CarbonDeploymentException;

    /**
     * Add and initialize a new Deployer.
     *
     * @param deployer  the deployer
     * @param directory the directory which will be scanned for deployable artifacts
     * @param extension the extension of the deployable artifacts for this Deployer
     */
    public void addDeployer(Deployer deployer, String directory, String extension);


    /**
     * Retrieve the deployer from the current deployers set, by giving the associated
     * extension and direction
     *
     * @param directory associated directory of the deployer
     * @param extension associated extension of the deployer
     * @return Deployer instance
     */
    public Deployer getDeployer(String directory, String extension);

}
