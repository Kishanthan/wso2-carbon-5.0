package org.wso2.carbon.context;/*
*  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.w3c.dom.Element;

import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.management.ManagementPermission;

public class ContextUtils {

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

    public static OMElement toOM(Element element) throws Exception {
        return toOM(element, true);
    }

    /**
     * Convert DOM Element into a fully built OMElement
     * @param element
     * @param buildAll if true, full OM tree is immediately built. if false, caller is responsible
     * for building the tree and closing the parser.
     * @return
     * @throws Exception
     */
    public static OMElement toOM(Element element, boolean buildAll) throws Exception {

        Source source = new DOMSource(element);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Result result = new StreamResult(baos);

        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.transform(source, result);

        ByteArrayInputStream is = new ByteArrayInputStream(baos.toByteArray());
        XMLStreamReader reader = StAXUtils
                .createXMLStreamReader(is);

        StAXOMBuilder builder = new StAXOMBuilder(reader);
        builder.setCache(true);
        builder.releaseParserOnClose(true);

        OMElement omElement = builder.getDocumentElement();
        if (buildAll) {
            omElement.build();
            builder.close();
        }
        return omElement;
    }


}
