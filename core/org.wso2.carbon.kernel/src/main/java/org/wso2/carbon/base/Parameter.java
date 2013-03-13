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
package org.wso2.carbon.base;

import org.w3c.dom.Element;

/**
 * TODO: class description
 */
public class Parameter {

    private String name;
    private Object value;
    private Element parameterElement;

    public Parameter(String name) {
        this.name = name;
    }

    public Parameter(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public Parameter(String name, Element parameterElement) {
        this(name, (Object) parameterElement);
        this.parameterElement = parameterElement;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Element getParameterElement() {
        return parameterElement;
    }

    public void setParameterElement(Element parameterElement) {
        this.parameterElement = parameterElement;
    }
}
