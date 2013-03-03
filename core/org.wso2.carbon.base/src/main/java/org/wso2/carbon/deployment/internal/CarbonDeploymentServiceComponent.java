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

    public void activate(ComponentContext componentContext) {
        ServerConfiguration carbonServerConfiguration = ServerConfiguration.getInstance();
        initCarbonDeploymentEngine(carbonServerConfiguration);
    }

    public void deactivate(ComponentContext componentContext) {

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
