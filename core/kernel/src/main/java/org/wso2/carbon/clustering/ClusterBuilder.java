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
package org.wso2.carbon.clustering;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.base.Parameter;
import org.wso2.carbon.clustering.api.ClusteringAgent;
import org.wso2.carbon.clustering.api.ClusteringConstants;
import org.wso2.carbon.clustering.api.ClusteringException;
import org.wso2.carbon.clustering.api.GroupManagementAgent;
import org.wso2.carbon.clustering.api.Member;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: class description
 */
public class ClusterBuilder {
    private static final Log log = LogFactory.getLog(ClusterBuilder.class);

    private String clusterConfigFile;

    public ClusterBuilder(String clusterConfigFile) {
        this.clusterConfigFile = clusterConfigFile;
    }

    public void build() {
        try {
            log.info("Initializing cluster...");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(clusterConfigFile);

            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            Element clusterElement = doc.getDocumentElement();
            clusterElement.normalize();
            System.out.println(clusterElement);

            if (isEnabled(clusterElement)) {
                String clazz = clusterElement.getAttribute("class");
                Class exampleClass = Class.forName(clazz);
                ClusteringAgent clusteringAgent = (ClusteringAgent) exampleClass.newInstance();


//                clusteringAgent.setConfigurationContext(configCtx);

                //loading the parameters.
                processParameters(clusterElement, clusteringAgent);

                // loading the application domains
                loadGroupManagement(clusteringAgent, clusterElement);

                // loading the members
                loadWellKnownMembers(clusteringAgent, clusterElement);

//                axisConfig.setClusteringAgent(clusteringAgent);

                clusteringAgent.init();
            }
        } catch (Exception e) {
            String msg = "Cannot initialize cluster";
            log.error(msg, e);
        }
    }

    private void processParameters(Element clusterElement, ClusteringAgent clusteringAgent) {
        NodeList parameterList = clusterElement.getElementsByTagName("parameter");
        int length = parameterList.getLength();
        for (int i = 0; i < length; i++) {
            Element paramEle = (Element) parameterList.item(i);
            Parameter parameter = new Parameter(paramEle.getAttribute("name"),
                                                paramEle.getTextContent());
            parameter.setParameterElement(paramEle);
            clusteringAgent.addParameter(parameter);
        }
    }

    private boolean isEnabled(Element element) {
        boolean enabled = true;
        String enableAttr = element.getAttribute("enable");
        if (enableAttr != null) {
            enabled = Boolean.parseBoolean(enableAttr.trim());
        }
        return enabled;
    }

    private void loadGroupManagement(ClusteringAgent clusteringAgent,
                                     Element clusterElement) throws ClusteringException {
        Element lbEle = (Element) clusterElement.getElementsByTagName("groupManagement").item(0);
        if (lbEle != null) {
            if (isEnabled(lbEle)) {
                log.info("Running in group management mode");
            } else {
                log.info("Running in application mode");
                return;
            }

            NodeList applicationDomainList = lbEle.getElementsByTagName("applicationDomain");
            int length = applicationDomainList.getLength();
            for (int i = 0; i < length; i++) {
                Element element = (Element) applicationDomainList.item(i);
                String domainName = element.getAttribute("domain");
                if (domainName != null) {
                    domainName = domainName.trim();
                }
                String name = element.getAttribute("name");
                if (name != null) {
                    domainName = name.trim();
                }
                String subDomainName = element.getAttribute("subDomain");
                if (subDomainName != null) {
                    subDomainName = subDomainName.trim();
                }
                String handlerClass = element.getAttribute("agent").trim();
                String descAttrib = element.getAttribute("description");
                String description = "Description not found";
                if (descAttrib != null) {
                    description = descAttrib.trim();
                }
                GroupManagementAgent groupManagementAgent;
                try {
                    groupManagementAgent = (GroupManagementAgent) Class.forName(handlerClass).newInstance();
                    groupManagementAgent.setDescription(description);
                    groupManagementAgent.setDomain(domainName);
                    groupManagementAgent.setSubDomain(subDomainName);
                } catch (Exception e) {
                    String msg = "Could not instantiate GroupManagementAgent " + handlerClass +
                                 " for domain " + domainName;
                    log.error(msg, e);
                    throw new ClusteringException(msg, e);
                }
                clusteringAgent.addGroupManagementAgent(groupManagementAgent, domainName);
            }
        }
    }

    private void loadWellKnownMembers(ClusteringAgent clusteringAgent, Element clusterElement) {
        clusteringAgent.setMembers(new ArrayList<Member>());
        Parameter membershipSchemeParam = clusteringAgent.getParameter("membershipScheme");
        if (membershipSchemeParam != null) {
            String membershipScheme = ((String) membershipSchemeParam.getValue()).trim();
            if (membershipScheme.equals(ClusteringConstants.MembershipScheme.WKA_BASED)) {
                List<Member> members = new ArrayList<Member>();
                Element membersEle =
                        (Element) clusterElement.getElementsByTagName("members").item(0);
                if (membersEle != null) {
                    NodeList memberList = membersEle.getElementsByTagName("member");
                    int length = memberList.getLength();
                    for (int i = 0; i < length; i++) {
                        Element memberEle = (Element) memberList.item(i);
                        String hostName =
                                memberEle.getElementsByTagName("hostName").item(0).getTextContent().trim();
                        String port =
                                memberEle.getElementsByTagName("port").item(0).getTextContent().trim();
                        members.add(new Member(replaceVariables(hostName),
                                               Integer.parseInt(replaceVariables(port))));
                    }
                }
                clusteringAgent.setMembers(members);
            }
        }
    }

    private String replaceVariables(String text) {
        int indexOfStartingChars;
        int indexOfClosingBrace;

        // The following condition deals with properties.
        // Properties are specified as ${system.property},
        // and are assumed to be System properties
        if ((indexOfStartingChars = text.indexOf("${")) != -1 &&
            (indexOfClosingBrace = text.indexOf("}")) != -1) { // Is a property used?
            String var = text.substring(indexOfStartingChars + 2,
                                        indexOfClosingBrace);

            String propValue = System.getProperty(var);
            if (propValue == null) {
                propValue = System.getenv(var);
            }
            if (propValue != null) {
                text = text.substring(0, indexOfStartingChars) + propValue +
                       text.substring(indexOfClosingBrace + 1);
            }
        }
        return text;
    }
}
