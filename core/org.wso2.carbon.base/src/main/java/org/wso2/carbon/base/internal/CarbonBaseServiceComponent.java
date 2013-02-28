/*
 *  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.base.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.api.SecretCallbackHandlerService;
import org.wso2.carbon.base.CarbonBaseUtils;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.base.ServerConfigurationException;
import org.wso2.carbon.api.ServerConfigurationService;
import org.wso2.carbon.exception.CarbonException;

import java.io.*;

/**
 * @scr.component name="carbon.base.service.component" immediate="true"
 * @scr.reference name="secure.vault.service" interface="org.wso2.carbon.api.SecretCallbackHandlerService"
 * cardinality="1..1" policy="dynamic" bind="setSecretCallbackHandlerService"  unbind="unsetSecretCallbackHandlerService"
 */
public class CarbonBaseServiceComponent {
    private static Log log = LogFactory.getLog(CarbonBaseServiceComponent.class);
    private CarbonBaseDataHolder dataHolder = CarbonBaseDataHolder.getInstance();
    private ServiceRegistration registration;

    public void activate(ComponentContext componentContext) throws Exception {

        ServerConfiguration carbonServerConfiguration = ServerConfiguration.getInstance();
        initServerConfiguration(carbonServerConfiguration);
        String portOffset = System.getProperty("portOffset",
                carbonServerConfiguration.getFirstProperty("Ports.Offset"));
        //setting the the retrieved ports.offset value as a system propery, in case it was not defined.
        //NIO transport make use of this system property
        System.setProperty("portOffset", portOffset);
        //register carbon server confg as an OSGi service
        registration = componentContext.getBundleContext().registerService(ServerConfigurationService.class.getName(), carbonServerConfiguration, null);

    }

    protected void setSecretCallbackHandlerService(SecretCallbackHandlerService secretCallbackHandlerService) {
         dataHolder.setSecretCallbackHandlerService(secretCallbackHandlerService);
    }

    protected void unsetSecretCallbackHandlerService(SecretCallbackHandlerService secretCallbackHandlerService) {
         dataHolder.setSecretCallbackHandlerService(null);
    }

    public void deactivate(ComponentContext componentContext) throws Exception {
        registration.unregister();
    }

    private void initServerConfiguration(ServerConfiguration carbonServerConfiguration) throws CarbonException {
        File carbonXML = new File(CarbonBaseUtils.getServerXml());
        InputStream in = null;
        try {
            in = new FileInputStream(carbonXML);
            carbonServerConfiguration.forceInit(in);
        } catch (ServerConfigurationException e) {
            String msg = "Could not initialize server configuration";
            log.fatal(msg);
            throw new CarbonException(msg);
        } catch (FileNotFoundException e) {
            throw new CarbonException(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.warn("Cannot close FileInputStream of file " + carbonXML.getAbsolutePath());
                }
            }
        }
    }

}
