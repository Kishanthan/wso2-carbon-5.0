/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.base;

import java.io.File;
import java.lang.management.ManagementPermission;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * Generic Base Utility methods
 */
public class CarbonBaseUtils {

    /**
     * Remove default constructor and make it not available to initialize.
     */

    private CarbonBaseUtils() {
        throw new AssertionError("Instantiating utility class...");

    }

    /**
     * Method to test whether a given user has permission to execute the given
     * method.
     */
    public static void checkSecurity() {
        SecurityManager secMan = System.getSecurityManager();
        if (secMan != null) {
            secMan.checkPermission(new ManagementPermission("control"));
        }
    }

    /**
     * Method to test whether a given user has permission to execute the given
     * method.
     *
     * @param allowedClasses
     *            the classes that are allowed to make calls irrespective of the
     *            user having desired permissions.
     */
    public static void checkSecurity(List<String> allowedClasses) {
        SecurityManager secMan = System.getSecurityManager();
        if (secMan != null) {
            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            String callingClass = trace[3].getClassName();
            if (!allowedClasses.contains(callingClass)) {
                secMan.checkPermission(new ManagementPermission("control"));
            }
        }
    }

    /**
     * Method to test whether a given user has permission to execute the given
     * method.
     *
     * @param allowedMethods
     *            the methods that are allowed to make calls irrespective of the
     *            user having desired permissions.
     */
    public static void checkSecurity(Map<String, String> allowedMethods) {
        SecurityManager secMan = System.getSecurityManager();
        if (secMan != null) {
            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            for (int i = 3; i < trace.length; i++) {
                String callingClass = trace[i].getClassName();
                String methodName = trace[i].getMethodName();
                if (allowedMethods.keySet().contains(callingClass)
                        && allowedMethods.get(callingClass).equals(methodName)) {
                    return;
                }
            }
            secMan.checkPermission(new ManagementPermission("control"));
        }
    }

    public static String getServerXml() {
        String carbonXML = System
                .getProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH);
        /*
           * if user set the system property telling where is the configuration
           * directory
           */
        if (carbonXML == null) {
            return getCarbonConfigDirPath() + File.separator + "carbon.xml";
        }
        return carbonXML + File.separator + "carbon.xml";
    }

    public static String getCarbonConfigDirPath() {
        String carbonConfigDirPath = System
                .getProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH);
        if (carbonConfigDirPath == null) {
            carbonConfigDirPath = System
                    .getenv(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH_ENV);
            if (carbonConfigDirPath == null) {
                return getCarbonHome() + File.separator + "repository"
                        + File.separator + "conf";
            }
        }
        return carbonConfigDirPath;
    }

    public static String getCarbonHome() {
        String carbonHome = System.getProperty(CarbonBaseConstants.CARBON_HOME);
        if (carbonHome == null) {
            carbonHome = System.getenv(CarbonBaseConstants.CARBON_HOME_ENV);
            System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        }
        return carbonHome;
    }

    public static String getCarbonRepositoryLocation() {
        String carbonRepoLocation = System.
                getProperty(CarbonBaseConstants.CARBON_REPOSITORY_LOCATION);

        if (carbonRepoLocation == null) {
            carbonRepoLocation = getCarbonHome() + File.separator + "repository" +
                                 File.separator + "deployment" + File.separator + "server";
            System.setProperty(CarbonBaseConstants.CARBON_REPOSITORY_LOCATION, carbonRepoLocation);
        }

        return carbonRepoLocation;
    }

    /**
     * Returns the ip address to be used for the replyto epr
     * CAUTION:
     * This will go through all the available network interfaces and will try to return an ip address.
     * First this will try to get the first IP which is not loopback address (127.0.0.1). If none is found
     * then this will return this will return 127.0.0.1.
     * This will <b>not<b> consider IPv6 addresses.
     * <p/>
     * TODO:
     * - Improve this logic to genaralize it a bit more
     * - Obtain the ip to be used here from the Call API
     *
     * @return Returns String.
     * @throws java.net.SocketException
     */
    public static String getIpAddress() throws SocketException {
        Enumeration e = NetworkInterface.getNetworkInterfaces();
        String address = "127.0.0.1";

        while (e.hasMoreElements()) {
            NetworkInterface netface = (NetworkInterface) e.nextElement();
            Enumeration addresses = netface.getInetAddresses();

            while (addresses.hasMoreElements()) {
                InetAddress ip = (InetAddress) addresses.nextElement();
                if (!ip.isLoopbackAddress() && isIP(ip.getHostAddress())) {
                    return ip.getHostAddress();
                }
            }
        }

        return address;
    }

    private static boolean isIP(String hostAddress) {
        return hostAddress.split("[.]").length == 4;
    }
}
