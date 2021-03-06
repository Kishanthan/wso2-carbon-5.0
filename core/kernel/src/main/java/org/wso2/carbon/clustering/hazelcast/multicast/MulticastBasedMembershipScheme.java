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
package org.wso2.carbon.clustering.hazelcast.multicast;

import com.hazelcast.config.MulticastConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import org.wso2.carbon.base.Parameter;
import org.wso2.carbon.clustering.api.ClusteringException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.clustering.hazelcast.HazelcastMembershipScheme;
import org.wso2.carbon.clustering.hazelcast.util.MemberUtils;

import java.util.Map;

/**
 * Multicast based membership scheme based on Hazelcast
 */
public class MulticastBasedMembershipScheme implements HazelcastMembershipScheme {
    private static final Log log = LogFactory.getLog(MulticastBasedMembershipScheme.class);
    private final Map<String, Parameter> parameters;
    private String primaryDomain;
    private MulticastConfig config;
    private HazelcastInstance primaryHazelcastInstance;

    public MulticastBasedMembershipScheme(Map<String, Parameter> parameters,
                                          String primaryDomain,
                                          MulticastConfig config) {
        this.parameters = parameters;
        this.primaryDomain = primaryDomain;
        this.config = config;
    }

    @Override
    public void init() throws ClusteringException {
        config.setEnabled(true);
        configureMulticastParameters();
    }

    private void configureMulticastParameters() throws ClusteringException {
        Parameter mcastAddress = getParameter(MulticastConstants.MULTICAST_ADDRESS);
        if (mcastAddress != null) {
            config.setMulticastGroup((String) mcastAddress.getValue());
        }
        Parameter mcastPort = getParameter(MulticastConstants.MULTICAST_PORT);
        if (mcastPort != null) {
            config.setMulticastPort(Integer.parseInt(((String) (mcastPort.getValue())).trim()));
        }
        Parameter mcastTimeout = getParameter(MulticastConstants.MULTICAST_TIMEOUT);
        if (mcastTimeout != null) {
            config.setMulticastTimeoutSeconds(Integer.parseInt(((String) (mcastTimeout.getValue())).trim()));
        }
        Parameter mcastTTL = getParameter(MulticastConstants.MULTICAST_TTL);
        if (mcastTTL != null) {
            config.setMulticastTimeToLive(Integer.parseInt(((String) (mcastTTL.getValue())).trim()));
        }
    }

    public Parameter getParameter(String name) {
        return parameters.get(name);
    }

    @Override
    public void joinGroup() throws ClusteringException {
        primaryHazelcastInstance.getCluster().addMembershipListener(new MulticastMembershipListener());
    }

    @Override
    public void setPrimaryHazelcastInstance(HazelcastInstance primaryHazelcastInstance) {
        this.primaryHazelcastInstance = primaryHazelcastInstance;
    }

    @Override
    public void setLocalMember(Member localMember) {
        // Nothing to do
    }

    private class MulticastMembershipListener implements MembershipListener {
        private Map<String, org.wso2.carbon.clustering.api.Member> members;

        public MulticastMembershipListener() {
            members = MemberUtils.getMembersMap(primaryHazelcastInstance, primaryDomain);
        }

        @Override
        public void memberAdded(MembershipEvent membershipEvent) {
            Member member = membershipEvent.getMember();
            log.info("Member joined [" + member.getUuid() + "]: " + member.getInetSocketAddress().toString());
        }

        @Override
        public void memberRemoved(MembershipEvent membershipEvent) {
            Member member = membershipEvent.getMember();
            log.info("Member left [" + member.getUuid() + "]: " + member.getInetSocketAddress().toString());
            members.remove(member.getUuid());
        }
    }
}
