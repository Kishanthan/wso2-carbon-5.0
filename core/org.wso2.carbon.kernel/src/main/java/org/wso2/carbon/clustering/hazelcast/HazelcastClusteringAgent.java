/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.clustering.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Member;
import org.wso2.carbon.base.CarbonBaseUtils;
import org.wso2.carbon.base.Parameter;
import org.wso2.carbon.base.ParameterHolder;
import org.wso2.carbon.clustering.api.ClusteringAgent;
import org.wso2.carbon.clustering.api.ClusteringCommand;
import org.wso2.carbon.clustering.api.ClusteringConstants;
import org.wso2.carbon.clustering.api.ClusteringException;
import org.wso2.carbon.clustering.api.ClusteringMessage;
//import org.wso2.carbon.clustering.apicontrol.ControlCommand;
import org.wso2.carbon.clustering.api.GroupManagementAgent;
//import org.apache.axis2.context.ConfigurationContext;
//import org.apache.axis2.description.Parameter;
//import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.clustering.hazelcast.aws.AWSBasedMembershipScheme;
import org.wso2.carbon.clustering.hazelcast.multicast.MulticastBasedMembershipScheme;
import org.wso2.carbon.clustering.hazelcast.util.MemberUtils;
import org.wso2.carbon.clustering.hazelcast.wka.WKABasedMembershipScheme;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * This is the main ClusteringAgent class which is based on Hazelcast
 */
@SuppressWarnings("unused")
public class HazelcastClusteringAgent implements ClusteringAgent {
    private static final Log log = LogFactory.getLog(HazelcastClusteringAgent.class);
    public static final String DEFAULT_SUB_DOMAIN = "__$default";

    private Config primaryHazelcastConfig;
    private HazelcastInstance primaryHazelcastInstance;

    private HazelcastMembershipScheme membershipScheme;
//    private ConfigurationContext configurationContext;
    private ITopic<ClusteringMessage> clusteringMessageTopic;

    /**
     * The mode in which this member operates such as "loadBalance" or "application"
     */
//    private OperationMode mode;

    /**
     * Static members
     */
    private List<org.wso2.carbon.clustering.api.Member> wkaMembers;

    /**
     * Map[key, value=Map[key, value]] = [domain, [subDomain, GroupManagementAgent]]
     */
    private final Map<String, Map<String, GroupManagementAgent>> groupManagementAgents =
            new HashMap<String, Map<String, GroupManagementAgent>>();
    private boolean clusterManagementMode;
    private String primaryDomain;

    private ParameterHolder paramHolder = new ParameterHolder();

    @Override
    public void addParameter(Parameter parameter) {
        paramHolder.addParameter(parameter);
    }

    @Override
    public Parameter getParameter(String paramName) {
        return paramHolder.getParameter(paramName);
    }

    public void init() throws ClusteringException {
        MemberUtils.init(paramHolder.getParameterMap());

        primaryHazelcastConfig = new Config();
        setHazelcastProperties();
//        new LoginModuleConfig().
//        primaryHazelcastConfig.getSecurityConfig().addMemberLoginModuleConfig(new UsernamePasswordCredentials());

        Parameter managementCenterURL = paramHolder.getParameter(HazelcastConstants.MGT_CENTER_URL);
        if (managementCenterURL != null) {
            primaryHazelcastConfig.getManagementCenterConfig().setEnabled(true).setUrl((String) managementCenterURL.getValue());
        }

        Parameter licenseKey = paramHolder.getParameter(HazelcastConstants.LICENSE_KEY);
        if (licenseKey != null) {
            primaryHazelcastConfig.setLicenseKey((String) licenseKey.getValue());
        }

        primaryDomain = getClusterDomain();
        primaryHazelcastConfig.setInstanceName(primaryDomain + ".instance");
        log.info("Cluster domain: " + primaryDomain);
        GroupConfig groupConfig = primaryHazelcastConfig.getGroupConfig();
        groupConfig.setName(primaryDomain);
        Parameter memberPassword = paramHolder.getParameter(HazelcastConstants.GROUP_PASSWORD);
        if (memberPassword != null) {
            groupConfig.setPassword((String) memberPassword.getValue());
        }

        NetworkConfig nwConfig = primaryHazelcastConfig.getNetworkConfig();
        Parameter localMemberHost = paramHolder.getParameter(HazelcastConstants.LOCAL_MEMBER_HOST);
        if (localMemberHost != null) {
            nwConfig.setPublicAddress(((String) localMemberHost.getValue()).trim());
        } else {
            try {
                String ipAddress = CarbonBaseUtils.getIpAddress();
                nwConfig.setPublicAddress(ipAddress);
            } catch (SocketException e) {
                log.error("Could not set local member host", e);
            }
        }
        Parameter localMemberPort = paramHolder.getParameter(HazelcastConstants.LOCAL_MEMBER_PORT);
        if (localMemberPort != null) {
            String port = ((String) localMemberPort.getValue()).trim();
            nwConfig.setPort(Integer.parseInt(port));  // localMemberPort
        }

        configureMembershipScheme(nwConfig);
        MapConfig mapConfig = new MapConfig("foo");
        mapConfig.setEvictionPolicy("LRU");
        primaryHazelcastConfig.addMapConfig(mapConfig);

        if (clusterManagementMode) {
            for (Map.Entry<String, Map<String, GroupManagementAgent>> entry : groupManagementAgents.entrySet()) {
                for (GroupManagementAgent agent : entry.getValue().values()) {
                    if (agent instanceof HazelcastGroupManagementAgent) {
                        ((HazelcastGroupManagementAgent) agent).init(primaryHazelcastConfig);
                    }
                }
            }
        }
        primaryHazelcastInstance = HazelcastInstanceManager.getInstance().init(primaryHazelcastConfig);

        membershipScheme.setPrimaryHazelcastInstance(primaryHazelcastInstance);

        clusteringMessageTopic = primaryHazelcastInstance.getTopic(HazelcastConstants.CLUSTERING_MESSAGE_TOPIC);
        clusteringMessageTopic.addMessageListener(new HazelcastClusterMessageListener());

        final Member localMember = primaryHazelcastInstance.getCluster().getLocalMember();
        membershipScheme.setLocalMember(localMember);
        org.wso2.carbon.clustering.api.Member carbonLocalMember =
                MemberUtils.getLocalMember(primaryDomain, nwConfig.getPublicAddress(), nwConfig.getPort());
        log.info("Local member: [" + localMember.getUuid() + "] - " + carbonLocalMember);
        MemberUtils.getMembersMap(primaryHazelcastInstance, primaryDomain).put(localMember.getUuid(),
                                                                               carbonLocalMember);
        membershipScheme.joinGroup();
        log.info("Cluster initialization completed");

    }

    private void setHazelcastProperties() {
        String hazelcastPropsFileName =
                System.getProperty("carbon.home") + File.separator + "repository" +
                File.separator + "conf" + File.separator + "hazelcast.properties";
        Properties hazelcastProperties = new Properties();
        if (new File(hazelcastPropsFileName).exists()) {
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(hazelcastPropsFileName);
                hazelcastProperties.load(fileInputStream);
            } catch (IOException e) {
                log.error("Cannot load properties from file " + hazelcastPropsFileName, e);
            } finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        log.error("Cannot close file " + hazelcastPropsFileName, e);
                    }
                }
            }
        }
        primaryHazelcastConfig.setProperties(hazelcastProperties);
    }

    /**
     * Get the clustering domain to which this node belongs to
     *
     * @return The clustering domain to which this node belongs to
     */
    private String getClusterDomain() {
        Parameter domainParam = paramHolder.getParameter(ClusteringConstants.Parameters.DOMAIN);
        String domain;
        if (domainParam != null) {
            domain = ((String) domainParam.getValue());
        } else {
            domain = ClusteringConstants.DEFAULT_DOMAIN;
        }
        return domain;
    }

    private void configureMembershipScheme(NetworkConfig nwConfig) throws ClusteringException {
        String scheme = getMembershipScheme();
        log.info("Using " + scheme + " based membership management scheme");
        if (scheme.equals(ClusteringConstants.MembershipScheme.WKA_BASED)) {
            membershipScheme = new WKABasedMembershipScheme(paramHolder.getParameterMap(), primaryDomain, wkaMembers,
                                                            primaryHazelcastConfig);
            membershipScheme.init();

            // If well-known members are not connected, wait here
            WKABasedMembershipScheme wkaBasedMembershipScheme =
                    (WKABasedMembershipScheme) membershipScheme;
            long start = System.currentTimeMillis();
            while (!wkaBasedMembershipScheme.areWellKnownMembersAvailable()) {
                if (System.currentTimeMillis() - start > 60000) {
                    log.warn("Waiting for all well-known members to become available");
                    start = System.currentTimeMillis();
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                }
                membershipScheme.init();
            }
        } else if (scheme.equals(ClusteringConstants.MembershipScheme.MULTICAST_BASED)) {
            membershipScheme = new MulticastBasedMembershipScheme(paramHolder.getParameterMap(), primaryDomain,
                                                                  nwConfig.getJoin().getMulticastConfig());
            membershipScheme.init();
        } else if (scheme.equals(HazelcastConstants.AWS_MEMBERSHIP_SCHEME)) {
            membershipScheme = new AWSBasedMembershipScheme(paramHolder.getParameterMap(), primaryDomain,
                                                            nwConfig.getJoin().getAwsConfig());
            membershipScheme.init();
        } else {
            String msg = "Invalid membership scheme '" + scheme +
                         "'. Supported schemes are multicast & wka";
            log.error(msg);
            throw new ClusteringException(msg);
        } //TODO: AWS membership scheme support
    }

    /**
     * Get the membership scheme applicable to this cluster
     *
     * @return The membership scheme. Only "wka" & "multicast" are valid return values.
     * @throws org.wso2.carbon.clustering.api.ClusteringException If the membershipScheme specified in the axis2.xml file is invalid
     */
    private String getMembershipScheme() throws ClusteringException {
        Parameter membershipSchemeParam =
                paramHolder.getParameter(ClusteringConstants.Parameters.MEMBERSHIP_SCHEME);
        String mbrScheme = ClusteringConstants.MembershipScheme.MULTICAST_BASED;
        if (membershipSchemeParam != null) {
            mbrScheme = ((String) membershipSchemeParam.getValue()).trim();
        }
        if (!mbrScheme.equals(ClusteringConstants.MembershipScheme.MULTICAST_BASED) &&
            !mbrScheme.equals(ClusteringConstants.MembershipScheme.WKA_BASED) &&
            !mbrScheme.equals(HazelcastConstants.AWS_MEMBERSHIP_SCHEME)) {
            String msg = "Invalid membership scheme '" + mbrScheme + "'. Supported schemes are " +
                         ClusteringConstants.MembershipScheme.MULTICAST_BASED + ", " +
                         ClusteringConstants.MembershipScheme.WKA_BASED + " & " +
                         HazelcastConstants.AWS_MEMBERSHIP_SCHEME;
            log.error(msg);
            throw new ClusteringException(msg);
        }
        return mbrScheme;
    }

    public void stop() {
        Hazelcast.shutdownAll();
    }

    public void shutdown() throws ClusteringException {
        try {
            Hazelcast.shutdownAll();
        } catch (Exception ignored) {
        }
    }

    public void setMembers(List<org.wso2.carbon.clustering.api.Member> wkaMembers) {
        this.wkaMembers = wkaMembers;
    }

    public List<org.wso2.carbon.clustering.api.Member> getMembers() {
        return wkaMembers;
    }

    public int getAliveMemberCount() {
        return MemberUtils.getMembersMap(primaryHazelcastInstance, primaryDomain).size();
    }

    public void addGroupManagementAgent(GroupManagementAgent agent, String applicationDomain) {
        addGroupManagementAgent(agent, applicationDomain, null);
    }

    @Override
    public void addGroupManagementAgent(GroupManagementAgent groupManagementAgent,
                                        String applicationDomain,
                                        String applicationSubDomain,
                                        int groupMgtPort) {
        addGroupManagementAgent(groupManagementAgent, applicationDomain, applicationSubDomain);
        groupManagementAgent.setGroupMgtPort(groupMgtPort);
    }

    public void addGroupManagementAgent(GroupManagementAgent agent, String applicationDomain,
                                        String applicationSubDomain) {
        if (applicationSubDomain == null) {
            applicationSubDomain = DEFAULT_SUB_DOMAIN; // default sub-domain since a sub-domain is not specified
        }
        log.info("Managing group application domain:" + applicationDomain + ", sub-domain:" +
                 applicationSubDomain + " using agent " + agent.getClass());
        if (!groupManagementAgents.containsKey(applicationDomain)) {
            groupManagementAgents.put(applicationDomain, new HashMap<String, GroupManagementAgent>());
        }
        agent.setDomain(applicationDomain);
        agent.setSubDomain(applicationSubDomain);
        groupManagementAgents.get(applicationDomain).put(applicationSubDomain, agent);
        clusterManagementMode = true;
    }

    public GroupManagementAgent getGroupManagementAgent(String applicationDomain) {
        return getGroupManagementAgent(applicationDomain, null);
    }

    public GroupManagementAgent getGroupManagementAgent(String applicationDomain,
                                                        String applicationSubDomain) {
        if (applicationSubDomain == null) {
            applicationSubDomain = DEFAULT_SUB_DOMAIN; // default sub-domain since a sub-domain is not specified
        }
        Map<String, GroupManagementAgent> groupManagementAgentMap = groupManagementAgents.get(applicationDomain);
        if (groupManagementAgentMap != null) {
            return groupManagementAgentMap.get(applicationSubDomain);
        }
        return null;
    }

    public Set<String> getDomains() {
        return groupManagementAgents.keySet();
    }

    public boolean isCoordinator() {
        return false;  //todo
    }

    public List<ClusteringCommand> sendMessage(ClusteringMessage clusteringMessage,
                                               boolean isSync) throws ClusteringException {
        if (clusteringMessageTopic != null) {
            clusteringMessageTopic.publish(clusteringMessage);
        }
        return new ArrayList<ClusteringCommand>();  // TODO: How to get the response? Send to another topic, and use a correlation ID to correlate
    }
}
