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

package org.wso2.carbon.api;

import org.wso2.carbon.exception.CarbonException;
import org.apache.axiom.om.OMElement;

/**
 *  Secure valut sevice to handle securing the passwords. Other components can use this interface to resolve the
 *  correct non seured value for the secured keys
 */
public interface SecureVaultService {

    /**
     *
     * @param alias  - key which represents an secured password
     * @return   - plain text value for the alias
     * @throws CarbonException
     */
    public String resolveSecret(String alias) throws CarbonException;

    /**
     *
     * @param omElement  - OMElement corresponding to the pass word element. It is assume that the key alias is there as element
     * attribute
     * @return    - plain text value for the for the alias
     * @throws CarbonException
     */
    public String resolveSecret(OMElement omElement) throws CarbonException;

    /**
     *
     * @param omElement
     * @return    - whether this is a secured element or not
     * @throws CarbonException
     */
    public boolean isTokenProtected(OMElement omElement) throws CarbonException;

}
