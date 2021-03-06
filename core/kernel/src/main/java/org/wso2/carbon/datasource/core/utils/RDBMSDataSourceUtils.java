/**
 *  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.datasource.core.utils;

import org.apache.commons.io.IOUtils;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.datasource.api.DataSourceException;
import org.wso2.carbon.datasource.core.rdbms.RDBMSConfiguration;
import org.wso2.carbon.datasource.core.rdbms.RDBMSDataSourceConstants;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Utility class for RDBMS data sources.
 */
public class RDBMSDataSourceUtils {

    public static void assignBeanProps(Object obj, Map<String, Object> props)
            throws DataSourceException {
        Method method;
        for (Entry<String, Object> prop : props.entrySet()) {
            method = getSetterMethod(obj, getSetterMethodNameFromPropName(prop.getKey()));
            if (method == null) {
                throw new DataSourceException("Setter method for property '" + prop.getKey()
                        + "' cannot be found");
            }
            try {
                method.invoke(obj, convertStringToGivenType(prop.getValue(),
                        method.getParameterTypes()[0]));
            } catch (Exception e) {
                throw new DataSourceException("Cannot invoke setter for property '" +
                        prop.getKey() + "'", e);
            }
        }
    }

    public static String replaceSystemVariablesInXml(String xmlConfiguration) throws Exception {
        InputStream in = replaceSystemVariablesInXml(new ByteArrayInputStream(xmlConfiguration.getBytes()));
        try {
            xmlConfiguration = IOUtils.toString(in);
        } catch (IOException e) {
            throw new Exception("Error in converting InputStream to String");
        }
        return xmlConfiguration;
    }

    /**
     *
     * @param xmlConfiguration InputStream that carries xml configuration
     * @return returns a InputStream that has evaluated system variables in input
     */
    public static InputStream replaceSystemVariablesInXml(InputStream xmlConfiguration) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document doc;
        try {
            builder = factory.newDocumentBuilder();
            doc = builder.parse(xmlConfiguration);
        } catch (Exception e) {
            throw new Exception("Error in building Document", e);
        }
        NodeList nodeList = null;
        if (doc != null) {
            nodeList = doc.getElementsByTagName("*");
        }
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                resolveLeafNodeValue(nodeList.item(i));
            }
        }
        return toInputStream(doc);
    }

    public static InputStream toInputStream(Document doc) throws Exception {
        InputStream in;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Source xmlSource = new DOMSource(doc);
            Result result = new StreamResult(outputStream);
            TransformerFactory.newInstance().newTransformer().transform(xmlSource, result);
            in = new ByteArrayInputStream(outputStream.toByteArray());
        } catch (TransformerException e) {
            throw new Exception("Error in transforming DOM to InputStream", e);
        }
        return in;
    }

    public static void resolveLeafNodeValue(Node node) {
        if (node != null) {
            Element element = (Element) node;
            NodeList childNodeList = element.getChildNodes();
            for (int j = 0; j < childNodeList.getLength(); j++) {
                Node chileNode = childNodeList.item(j);
                if (!chileNode.hasChildNodes()) {
                    String nodeValue = resolveSystemProperty(chileNode.getTextContent());
                    childNodeList.item(j).setTextContent(nodeValue);
                } else {
                    resolveLeafNodeValue(chileNode);
                }
            }
        }
    }

    public static String resolveSystemProperty(String text) {
        int indexOfStartingChars = -1;
        int indexOfClosingBrace;

        // The following condition deals with properties.
        // Properties are specified as ${system.property},
        // and are assumed to be System properties
        while (indexOfStartingChars < text.indexOf("${")
                && (indexOfStartingChars = text.indexOf("${")) != -1
                && (indexOfClosingBrace = text.indexOf('}')) != -1) { // Is a
            // property
            // used?
            String sysProp = text.substring(indexOfStartingChars + 2,
                    indexOfClosingBrace);
            String propValue = System.getProperty(sysProp);
            if (propValue != null) {
                text = text.substring(0, indexOfStartingChars) + propValue
                        + text.substring(indexOfClosingBrace + 1);
            }
            if (sysProp.equals("carbon.home") && propValue != null
                    && propValue.equals(".")) {

                text = new File(".").getAbsolutePath() + File.separator + text;

            }
        }
        return text;
    }

    private static Object convertStringToGivenType(Object value, Class<?> type)
            throws DataSourceException {
        if (String.class.equals(type) || Properties.class.equals(type)) {
            return value;
        }
        if (boolean.class.equals(type) || Boolean.class.equals(type)) {
            return Boolean.parseBoolean(String.valueOf(value));
        }
        if (int.class.equals(type) || Integer.class.equals(type)) {
            return Integer.parseInt(String.valueOf(value));
        }
        if (short.class.equals(type) || Short.class.equals(type)) {
            return Short.parseShort(String.valueOf(value));
        }
        if (byte.class.equals(type) || Byte.class.equals(type)) {
            return Byte.parseByte(String.valueOf(value));
        }
        if (long.class.equals(type) || Long.class.equals(type)) {
            return Long.parseLong(String.valueOf(value));
        }
        if (float.class.equals(type) || Float.class.equals(type)) {
            return Float.parseFloat(String.valueOf(value));
        }
        if (double.class.equals(type) || Double.class.equals(type)) {
            return Double.parseDouble(String.valueOf(value));
        }
        throw new DataSourceException("Cannot convert value: '" +
                value + "' to type: '" + type.getName() + "'");
    }

    public static boolean isEmptyString(String text) {
        if (text != null && text.trim().length() > 0) {
            return false;
        } else {
            return true;
        }
    }

    private static String getSetterMethodNameFromPropName(String propName) throws RuntimeException {
        if (isEmptyString(propName)) {
            throw new RuntimeException("Invalid property name");
        }
        return "set" + propName.substring(0, 1).toUpperCase() + propName.substring(1);
    }

    private static Method getSetterMethod(Object obj, String name) {
        Method[] methods = obj.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals(name)
                    && method.getReturnType().equals(void.class)
                    && method.getParameterTypes().length == 1) {
                return method;
            }
        }
        return null;
    }

    public static Map<String, String> extractPrimitiveFieldNameValuePairs(Object object)
            throws DataSourceException {
        Map<String, String> nameValueMap = new HashMap<String, String>();
        Method methods[] = object.getClass().getMethods();
        for (Method method : methods) {
            if (isMethodMatched(method)) {
                String FieldName = getFieldNameFromMethodName(method.getName());
                String result = null;
                try {
                    if (method.invoke(object) != null) {
                        result = method.invoke(object).toString();
                        nameValueMap.put(FieldName, result);
                    }
                } catch (Exception e) {
                    throw new DataSourceException(
                            "Error in retrieving " + FieldName + " value from the object :" + object.getClass() + e.getMessage(), e);
                }
            }
        }
        return nameValueMap;
    }

    private static String getFieldNameFromMethodName(String name) throws DataSourceException {
        String prefixGet = "get";
        String prefixIs = "is";
        String firstLetter = null;

        if (name.startsWith(prefixGet)) {
            firstLetter = name.substring(3, 4);
            name = name.substring(4);
        } else if (name.startsWith(prefixIs)) {
            firstLetter = name.substring(2, 3);
            name = name.substring(3);
        } else {
            throw new DataSourceException("Error in retrieving attribute name from method : "
                    + name);
        }
        firstLetter = firstLetter.toLowerCase();
        return firstLetter.concat(name);
    }

    private static boolean isMethodMatched(Method method) {
        String returnType = method.getReturnType().getSimpleName();
        String methodName = method.getName();

        if (!Modifier.isPublic(method.getModifiers())) {
            return false;
        }
        if (returnType.equals("void")) {
            return false;
        }
        if (!(methodName.startsWith("get") ||
                (methodName.startsWith("is") && (returnType.equals("Boolean") || returnType.equals("boolean"))))) {
            return false;
        }
        if (!(method.getReturnType().isPrimitive() ||
                Arrays.asList(RDBMSDataSourceConstants.CLASS_RETURN_TYPES).contains(returnType))){
            return false;
        }
        return true;
    }

    public static PoolConfiguration createPoolConfiguration(RDBMSConfiguration config)
            throws DataSourceException {
        PoolProperties props = new PoolProperties();
        props.setUrl(config.getUrl());
        if (config.isDefaultAutoCommit() != null) {
            props.setDefaultAutoCommit(config.isDefaultAutoCommit());
        }
        if (config.isDefaultReadOnly() != null) {
            props.setDefaultReadOnly(config.isDefaultReadOnly());
        }
        String isolationLevelString = config.getDefaultTransactionIsolation();
        if (isolationLevelString != null) {
            if (RDBMSDataSourceConstants.TX_ISOLATION_LEVELS.NONE.equals(isolationLevelString)) {
                props.setDefaultTransactionIsolation(Connection.TRANSACTION_NONE);
            } else if (RDBMSDataSourceConstants.TX_ISOLATION_LEVELS.READ_UNCOMMITTED.equals(isolationLevelString)) {
                props.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            } else if (RDBMSDataSourceConstants.TX_ISOLATION_LEVELS.READ_COMMITTED.equals(isolationLevelString)) {
                props.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            } else if (RDBMSDataSourceConstants.TX_ISOLATION_LEVELS.REPEATABLE_READ.equals(isolationLevelString)) {
                props.setDefaultTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            } else if (RDBMSDataSourceConstants.TX_ISOLATION_LEVELS.SERIALIZABLE.equals(isolationLevelString)) {
                props.setDefaultTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            }
        }
        props.setDefaultCatalog(config.getDefaultCatalog());
        props.setDriverClassName(config.getDriverClassName());
        props.setUsername(config.getUsername());
        props.setPassword(config.getPassword());
        if (config.getMaxActive() != null) {
            props.setMaxActive(config.getMaxActive());
        }
        if (config.getMaxIdle() != null) {
            props.setMaxIdle(config.getMaxIdle());
        }
        if (config.getMinIdle() != null) {
            props.setMinIdle(config.getMinIdle());
        }
        if (config.getInitialSize() != null) {
            props.setInitialSize(config.getInitialSize());
        }
        if (config.getMaxWait() != null) {
            props.setMaxWait(config.getMaxWait());
        }
        if (config.isTestOnBorrow() != null) {
            props.setTestOnBorrow(config.isTestOnBorrow());
        }
        if (config.isTestOnReturn() != null) {
            props.setTestOnReturn(config.isTestOnReturn());
        }
        if (config.isTestWhileIdle() != null) {
            props.setTestWhileIdle(config.isTestWhileIdle());
        }
        props.setValidationQuery(config.getValidationQuery());
        props.setValidatorClassName(config.getValidatorClassName());
        if (config.getTimeBetweenEvictionRunsMillis() != null) {
            props.setTimeBetweenEvictionRunsMillis(config.getTimeBetweenEvictionRunsMillis());
        }
        if (config.getNumTestsPerEvictionRun() != null) {
            props.setNumTestsPerEvictionRun(config.getNumTestsPerEvictionRun());
        }
        if (config.getMinEvictableIdleTimeMillis() != null) {
            props.setMinEvictableIdleTimeMillis(config.getMinEvictableIdleTimeMillis());
        }
        if (config.isAccessToUnderlyingConnectionAllowed() != null) {
            props.setAccessToUnderlyingConnectionAllowed(
                    config.isAccessToUnderlyingConnectionAllowed());
        }
        if (config.isRemoveAbandoned() != null) {
            props.setRemoveAbandoned(config.isRemoveAbandoned());
        }
        if (config.getRemoveAbandonedTimeout() != null) {
            props.setRemoveAbandonedTimeout(config.getRemoveAbandonedTimeout());
        }
        if (config.isLogAbandoned() != null) {
            props.setLogAbandoned(config.isLogAbandoned());
        }
        props.setConnectionProperties(config.getConnectionProperties());
        props.setInitSQL(config.getInitSQL());
        props.setJdbcInterceptors(config.getJdbcInterceptors());
        if (config.getValidationInterval() != null) {
            props.setValidationInterval(config.getValidationInterval());
        }
        if (config.isJmxEnabled() != null) {
            props.setJmxEnabled(config.isJmxEnabled());
        }
        if (config.isFairQueue() != null) {
            props.setFairQueue(config.isFairQueue());
        }
        if (config.getAbandonWhenPercentageFull() != null) {
            props.setAbandonWhenPercentageFull(config.getAbandonWhenPercentageFull());
        }
        if (config.getMaxAge() != null) {
            props.setMaxAge(config.getMaxAge());
        }
        if (config.isUseEquals() != null) {
            props.setUseEquals(config.isUseEquals());
        }
        if (config.getSuspectTimeout() != null) {
            props.setSuspectTimeout(config.getSuspectTimeout());
        }
        if (config.isAlternateUsernameAllowed() != null) {
            props.setAlternateUsernameAllowed(config.isAlternateUsernameAllowed());
        }
        if (config.getDataSourceClassName() != null) {
            handleExternalDataSource(props, config);
        }
        return props;
    }

    private static Map<String, Object> dataSourcePropsToMap(List<RDBMSConfiguration.DataSourceProperty> dsProps) {
        Map<String, Object> result = new HashMap<String, Object>();
        if (dsProps != null) {
            String[] prop;
            Map<String, Properties> tmpPropertiesObjects = new HashMap<String, Properties>();
            Properties tmpProp;
            for (RDBMSConfiguration.DataSourceProperty dsProp : dsProps) {
                prop = dsProp.getName().split("\\.");
                if (prop.length > 1) {
                    if (!tmpPropertiesObjects.containsKey(prop[0])) {
                        tmpProp = new Properties();
                        tmpPropertiesObjects.put(prop[0], tmpProp);
                    } else {
                        tmpProp = tmpPropertiesObjects.get(prop[0]);
                    }
                    tmpProp.setProperty(prop[1], dsProp.getValue());
                } else {
                    result.put(dsProp.getName(), dsProp.getValue());
                }
            }
            result.putAll(tmpPropertiesObjects);

        }
        return result;
    }

    private static void handleExternalDataSource(PoolProperties poolProps, RDBMSConfiguration config)
            throws DataSourceException {
        String dsClassName = config.getDataSourceClassName();
        try {
            Object extDataSource = Class.forName(dsClassName).newInstance();
            RDBMSDataSourceUtils.assignBeanProps(extDataSource,
                    dataSourcePropsToMap(config.getDataSourceProps()));
            poolProps.setDataSource(extDataSource);
        } catch (Exception e) {
            throw new DataSourceException("Error in creating external data source: " +
                    e.getMessage(), e);
        }
    }

}
