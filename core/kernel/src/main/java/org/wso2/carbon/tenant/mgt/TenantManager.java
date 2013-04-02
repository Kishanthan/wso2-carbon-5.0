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


public interface TenantManager {

    /**
     * Adds a tenant to the system
     * 
     * @param tenant The tenant to be added
     * @return The Id of the tenant
     * @throws TenantManagementException
     */
    int addTenant(Tenant tenant) throws TenantManagementException;

    /**
     * Updates a tenant in the system
     *
     * @param tenant The tenant to be updated
     * @throws TenantManagementException
     */
    void updateTenant(Tenant tenant) throws TenantManagementException;

    /**
     * Gets a Tenant object
     *
     * @param tenantId The tenant Id of the tenant
     * @return The tenant object
     * @throws TenantManagementException
     */
    Tenant getTenant(int tenantId) throws TenantManagementException;

    /**
     * Gets all tenants in the system.
     *
     * @return An array of all tenants
     *
     * @throws TenantManagementException
     */
    Tenant[] getAllTenants() throws TenantManagementException;

    /**
     * Gets  tenants in the system which matches the given domain String(which can be used for partial searches).
     *
     * @return An array of tenants which matches the domain
     *
     * @throws TenantManagementException
     */
    Tenant[] getAllTenantsForTenantDomainStr(String domain) throws TenantManagementException;

    /**
     * Retrieves the domain given a tenant Id
     *
     * @param tenantId The Id of the tenant
     * @return
     * @throws TenantManagementException
     */
    String getDomain(int tenantId) throws TenantManagementException;

    /**
     * Retrieves the tenant Id given the domain
     *
     * @param domain The domain of the tenant
     * @return
     * @throws TenantManagementException
     */
    int getTenantId(String domain) throws TenantManagementException;

    /**
     * Activates a tenant
     *
     * @param tenantId The Id of the tenant
     * @throws TenantManagementException
     */
    void activateTenant(int tenantId) throws TenantManagementException;

    /**
     * De-activates a tenant
     *
     * @param tenantId The Id of the tenant
     * @throws TenantManagementException
     */
    void deactivateTenant(int tenantId) throws TenantManagementException;

    /**
     * Checks whether a tenant is active
     *
     * @param tenantId The Id of the tenant
     * @return
     * @throws TenantManagementException
     */
    boolean isTenantActive(int tenantId) throws TenantManagementException;

    /**
     * Deletes a tenant from the system
     *
     * @param tenantId
     * @throws TenantManagementException
     */
    void deleteTenant(int tenantId) throws TenantManagementException;

    /**
     * Checks whether the super tenant.
     *
     * @return
     * @throws TenantManagementException
     */
    String getSuperTenantDomain() throws TenantManagementException;

    /**
     * Get all tenant domains of user if the cross domain membership is available
     *
     * @param username
     * @return
     * @throws TenantManagementException
     */
    String[] getAllTenantDomainStrOfUser(String username) throws TenantManagementException;
}
