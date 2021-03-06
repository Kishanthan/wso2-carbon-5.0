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
package org.wso2.carbon.user.core.authorization;

public class DBConstants {

    public static final String GET_EXISTING_USER_PERMISSIONS = "SELECT UM_USER_NAME, UM_RESOURCE_ID, UM_IS_ALLOWED, UM_ACTION FROM " +
                "UM_PERMISSION, UM_USER_PERMISSION WHERE UM_USER_PERMISSION.UM_PERMISSION_ID=UM_PERMISSION.UM_ID AND " +
                "UM_PERMISSION.UM_TENANT_ID=? AND UM_USER_PERMISSION.UM_TENANT_ID=?";
    public static final String GET_EXISTING_ROLE_PERMISSIONS = "SELECT UM_ROLE_NAME, UM_RESOURCE_ID, UM_IS_ALLOWED, UM_ACTION FROM " +
                "UM_PERMISSION, UM_ROLE_PERMISSION WHERE UM_ROLE_PERMISSION.UM_PERMISSION_ID=UM_PERMISSION.UM_ID AND " +
                "UM_PERMISSION.UM_TENANT_ID=? AND UM_ROLE_PERMISSION.UM_TENANT_ID=?";
        
    public static final String GET_PERMISSION_ID_SQL = "SELECT UM_ID FROM UM_PERMISSION WHERE " +
                "UM_RESOURCE_ID=? AND UM_ACTION=? AND UM_TENANT_ID=?";
    public static final String ADD_PERMISSION_SQL = "INSERT INTO UM_PERMISSION (UM_RESOURCE_ID, UM_ACTION, " +
                "UM_TENANT_ID) VALUES (?, ?, ?)";
    public static final String ADD_ROLE_PERMISSION_SQL = "INSERT INTO UM_ROLE_PERMISSION (UM_PERMISSION_ID," +
    		" UM_ROLE_NAME, UM_IS_ALLOWED, UM_TENANT_ID) VALUES (?, ?, ?, ?)";
    public static final String ADD_USER_PERMISSION_SQL = "INSERT INTO UM_USER_PERMISSION (UM_PERMISSION_ID," +
    		" UM_USER_NAME, UM_IS_ALLOWED, UM_TENANT_ID) VALUES (?, ?, ?, ?)";
   
    // when deleting permissions
    public static final String ON_DELETE_PERMISSION_UM_ROLE_PERMISSIONS_SQL = "DELETE FROM UM_ROLE_PERMISSION WHERE " +
                "UM_PERMISSION_ID IN (SELECT UM_ID FROM UM_PERMISSION WHERE UM_RESOURCE_ID=?) AND UM_TENANT_ID=?";
    public static final String ON_DELETE_PERMISSION_UM_USER_PERMISSIONS_SQL = "DELETE FROM UM_USER_PERMISSION WHERE " +
                "UM_PERMISSION_ID IN (SELECT UM_ID FROM UM_PERMISSION WHERE UM_RESOURCE_ID=?) AND UM_TENANT_ID=?";
    public static final String DELETE_PERMISSION_SQL = "DELETE FROM UM_PERMISSION WHERE " +
                "UM_RESOURCE_ID = ? AND UM_TENANT_ID=?";
    public static final String DELETE_ROLE_PERMISSION_SQL = "DELETE FROM UM_ROLE_PERMISSION WHERE UM_ROLE_NAME=? " +
                "AND UM_PERMISSION_ID = (SELECT UM_ID FROM UM_PERMISSION WHERE UM_RESOURCE_ID = ? AND " +
                "UM_ACTION = ? AND UM_TENANT_ID=?) AND UM_TENANT_ID=?";
    public static final String DELETE_USER_PERMISSION_SQL = "DELETE FROM UM_USER_PERMISSION WHERE UM_USER_NAME=? " +
                "AND UM_PERMISSION_ID = (SELECT UM_ID FROM UM_PERMISSION WHERE UM_RESOURCE_ID = ? AND " +
                "UM_ACTION = ? AND UM_TENANT_ID=?) AND UM_TENANT_ID=?";
    public static final String ON_DELETE_ROLE_DELETE_PERMISSION_SQL = "DELETE FROM UM_ROLE_PERMISSION WHERE UM_ROLE_NAME=? AND UM_TENANT_ID=?";
    public static final String ON_DELETE_USER_DELETE_PERMISSION_SQL = "DELETE FROM UM_USER_PERMISSION WHERE UM_USER_NAME=? AND UM_TENANT_ID=?";
    
    public static final String DELETE_ROLE_PERMISSIONS_BASED_ON_ACTION ="DELETE FROM UM_ROLE_PERMISSION WHERE UM_ROLE_NAME=? " +
                "AND UM_PERMISSION_ID IN (SELECT UM_ID FROM UM_PERMISSION WHERE UM_ACTION = ? AND UM_TENANT_ID=?) AND UM_TENANT_ID=?";

    public static final String UPDATE_UM_ROLE_NAME_PERMISSION_SQL = "UPDATE UM_ROLE_PERMISSION set UM_ROLE_NAME=? WHERE UM_ROLE_NAME=? AND UM_TENANT_ID=?";
    
}
