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
package org.wso2.carbon.clustering.api;

import java.util.List;

/**
 * This is the interface through which the load balancing event are notified.
 * This will only be used when this member is running in loadBalance mode. In order to do this,
 * in the axis2.xml file, set the value of the "mode" parameter to "loadBalance" and then provide
 * the class that implements this interface using the "loadBalanceEventHandler" entry.
 */
public interface GroupManagementAgent {

    /**
     * Set the cluster domain
     *
     * @param domain the cluster domain
     */
    void setDomain(String domain);

    /**
     * Set the cluster sub-domain
     *
     * @param subDomain the cluster sub-domain
     */
    void setSubDomain(String subDomain);

    /**
     * Set the  description about this group management agent
     *
     * @param description The description
     */
    void setDescription(String description);

    /**
     * Get the  description about this group management agent
     *
     * @return The description
     */
    String getDescription();

    /**
     * An application member joined the application group
     *
     * @param member Represents the member who joined
     */
    void applicationMemberAdded(Member member);

    /**
     * An application member left the application group
     *
     * @param member Represents the member who left
     */
    void applicationMemberRemoved(Member member);

    /**
     * Get the list of current members
     *
     * @return List of current members
     */
    List<Member> getMembers();


    /**
     * Send a GroupManagementCommand to the group
     *
     * @param command The command
     * @throws ClusteringException If an error occurs while sending the command
     */
    void send(GroupManagementCommand command) throws ClusteringException;


    void setGroupMgtPort(int groupMgtPort);
}
