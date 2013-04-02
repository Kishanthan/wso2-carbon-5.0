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

package org.wso2.carbon.tenant.mgt;


import org.wso2.carbon.user.api.UserStoreException;

import java.util.Map;

public interface TenantManagementService {

    /**
     * @param tenantManager
     * @throws org.wso2.carbon.user.api.UserStoreException
     *
     */
    void setTenantManager(TenantManager tenantManager) throws UserStoreException;

    /**
     * Get tenant manager
     *
     * @return TenantManager
     */
    TenantManager getTenantManager();

    /**
     * Gets the properties of the Tenant.
     *
     * @param tenant
     * @return
     * @throws UserStoreException
     */
    Map<String, String> getProperties(Tenant tenant) throws UserStoreException;

    /**
     * Get tenant mgt configuration read from tenant-mgt.xml
     */
    TenantManagementConfiguration getTenantMgtConfiguration();
}
