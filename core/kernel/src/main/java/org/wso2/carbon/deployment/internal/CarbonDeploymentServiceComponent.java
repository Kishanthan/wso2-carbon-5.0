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

package org.wso2.carbon.deployment.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.CarbonBaseUtils;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.deployment.CarbonDeploymentEngine;
import org.wso2.carbon.deployment.CarbonDeploymentException;

/**
 * @scr.component name="carbon.deployment.service.component" immediate="true"
 */

public class CarbonDeploymentServiceComponent {
    private static Log log = LogFactory.getLog(CarbonDeploymentServiceComponent.class);

    protected void activate(ComponentContext componentContext) {
        ServerConfiguration carbonServerConfiguration = ServerConfiguration.getInstance();
        initCarbonDeploymentEngine(carbonServerConfiguration);
    }

    protected void deactivate(ComponentContext componentContext) {

    }

    private void initCarbonDeploymentEngine(ServerConfiguration carbonServerConfiguration) {
        try {
            CarbonDeploymentEngine carbonDeploymentEngine =
                    new CarbonDeploymentEngine(CarbonBaseUtils.getCarbonRepositoryLocation());
            carbonDeploymentEngine.loadArtifacts();
        } catch (CarbonDeploymentException e) {
            String msg = "Could not initialize carbon deployment engine";
            log.fatal(msg);
        }
    }
}
