/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.securevault;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.securevault.SecurityConstants;
import org.wso2.securevault.secret.SecretCallbackHandler;
import org.wso2.securevault.secret.SecretCallbackHandlerFactory;
import org.wso2.securevault.secret.SecretManager;
import org.wso2.securevault.secret.handler.SecretManagerSecretCallbackHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 */
public class SecretManagerInitializer {

    private SecretManager secretManager = null;
    private Properties properties = null;
    private static final Log log = LogFactory.getLog(SecretManagerInitializer.class);


    public SecretManagerInitializer() {

        this.secretManager = SecretManager.getInstance();

        this.properties = new Properties();

        if (this.secretManager.isInitialized()) {
            if (log.isDebugEnabled()) {
                log.debug("SecretManager already has been initialized.");
            }
        } else {
            this.properties = loadProperties();
            this.secretManager.init(this.properties);
        }


    }

    public SecretCallbackHandler getSecuretCallBackHandler() {

        SecretCallbackHandler passwordProvider = null;

        if (!this.secretManager.isInitialized()) {

            passwordProvider =
                    SecretCallbackHandlerFactory.createSecretCallbackHandler(this.properties,
                            Constants.GLOBAL_PREFIX + SecurityConstants.PASSWORD_PROVIDER_SIMPLE);
        }

        if (passwordProvider == null) {
            passwordProvider = new SecretManagerSecretCallbackHandler();
        }

        return passwordProvider;
    }


    private Properties loadProperties() {
        Properties properties = new Properties();
        String carbonHome = System.getProperty(Constants.CARBON_HOME);
        String filePath = carbonHome + File.separator + Constants.REPOSITORY_DIR + File.separator +
                Constants.CONF_DIR + File.separator + Constants.SECURITY_DIR + File.separator + Constants.SECRET_CONF;

        File dataSourceFile = new File(filePath);
        if (!dataSourceFile.exists()) {
            return properties;
        }

        InputStream in = null;
        try {
            in = new FileInputStream(dataSourceFile);
            properties.load(in);
        } catch (IOException e) {
            String msg = "Error loading properties from a file at :" + filePath;
            log.warn(msg, e);
            return properties;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {

                }
            }
        }
        return properties;
    }
}
