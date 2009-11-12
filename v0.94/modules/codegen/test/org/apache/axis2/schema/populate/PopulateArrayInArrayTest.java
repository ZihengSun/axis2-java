package org.apache.axis2.schema.populate;

import junit.framework.TestCase;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
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

public class PopulateArrayInArrayTest extends TestCase{
    private String xmlString = "<myobject>" +
            "<soapStructures>" +
            "<varFloat>3.3</varFloat>" +
            "<varInt>5</varInt>" +
            "<varString>Hello11</varString>" +
            "<varString>Hello11</varString>" +
            "<varString>Hello12</varString>" +
            "<varString>Hello13</varString>" +
            "</soapStructures>" +
            "<soapStructures>" +
            "<varFloat>3.31</varFloat>" +
            "<varInt>51</varInt>" +
            "<varString>Hello21</varString>" +
            "<varString>Hello21</varString>" +
            "<varString>Hello22</varString>" +
            "<varString>Hello23</varString>" +
            "</soapStructures>" +
            "</myobject>";

    public void testPopulate() throws Exception{

            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(xmlString.getBytes()));
            Class clazz = Class.forName("org.soapinterop.xsd.Myobject");
            Method parseMethod = clazz.getMethod("parse",new Class[]{XMLStreamReader.class});
            Object obj = parseMethod.invoke(null,new Object[]{reader});


            Object myObject = null ;
            BeanInfo beanInfo =  Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (int i = 0; i < propertyDescriptors.length; i++) {
                PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
                if ("myobject".equals(propertyDescriptor.getDisplayName())){
                    Method readMethod = propertyDescriptor.getReadMethod();
                    myObject = readMethod.invoke(obj,new Object[]{});
                    break;
                }


            }

            assertNotNull(myObject);
    }
}