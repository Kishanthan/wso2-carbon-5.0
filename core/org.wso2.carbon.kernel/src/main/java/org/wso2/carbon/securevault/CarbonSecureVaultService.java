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

package org.wso2.carbon.securevault;

import org.wso2.carbon.api.SecureVaultService;
import org.wso2.carbon.exception.CarbonException;
import org.wso2.securevault.SecretResolverFactory;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecurityConstants;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAttribute;

import javax.xml.namespace.QName;

public class CarbonSecureVaultService implements SecureVaultService {

    private SecretResolver secretResolver = null;

    public CarbonSecureVaultService() {
        this.secretResolver = SecretResolverFactory.create((OMElement) null, false);
        SecretManagerInitializer secretManagerInitializer = new SecretManagerInitializer();
        this.secretResolver.init(secretManagerInitializer.getSecuretCallBackHandler());
    }

    public String resolveSecret(String alias) throws CarbonException {
        return this.secretResolver.resolve(alias);
    }

    public String resolveSecret(OMElement omElement) throws CarbonException {
        OMAttribute omAttribute =
                omElement.getAttribute(new QName(SecurityConstants.SECURE_VAULT_NS,
                                                     SecurityConstants.SECURE_VAULT_ALIAS));
        return this.secretResolver.resolve(omAttribute.getAttributeValue());  
    }

    public boolean isTokenProtected(OMElement omElement) throws CarbonException {

        OMAttribute omAttribute =
                omElement.getAttribute(new QName(SecurityConstants.SECURE_VAULT_NS,
                                                     SecurityConstants.SECURE_VAULT_ALIAS));
        
        return (omAttribute != null);
    }

    public void shutDown() {
        if (this.secretResolver != null) {
            this.secretResolver.shutDown();
        }
    }
}
