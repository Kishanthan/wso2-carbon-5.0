/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.securevault.internal;

import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.securevault.CarbonSecureVaultService;
import org.wso2.carbon.api.SecureVaultService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @scr.component name="secure.vault.component" immediate="true"
 */

public class SecureValultComponent {

    private CarbonSecureVaultService carbonSecureVaultService = null;
    private static final Log log = LogFactory.getLog(SecureValultComponent.class);

    protected void activate(ComponentContext ctxt) {

        log.info("Starting the Secure Vault service");
        this.carbonSecureVaultService = new CarbonSecureVaultService();
        ctxt.getBundleContext().registerService(SecureVaultService.class.getName(), carbonSecureVaultService, null);

    }

    protected void deactivate(ComponentContext ctxt) {
        this.carbonSecureVaultService.shutDown();
    }

}
