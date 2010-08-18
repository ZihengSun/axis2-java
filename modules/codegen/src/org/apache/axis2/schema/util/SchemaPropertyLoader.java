package org.apache.axis2.schema.util;

import org.apache.axis2.schema.SchemaCompiler;
import org.apache.axis2.schema.SchemaConstants;
import org.apache.axis2.schema.typemap.TypeMap;
import org.apache.axis2.schema.writer.BeanWriter;

import java.util.Properties;
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

/**
 * Loads the properties  for the schema compiler.
 */
public class SchemaPropertyLoader {
    private static String beanTemplate = null;
    private static BeanWriter beanWriterInstance = null;
    private static TypeMap typeMapperInstance = null;
    private static Properties propertyMap;

    static {
        try {
            //load the properties
            Properties props = new Properties();
            props.load(SchemaCompiler.class.getResourceAsStream(SchemaConstants.SchemaPropertyNames.SCHEMA_COMPILER_PROPERTIES));

            String beanWriterClassName = props.getProperty(SchemaConstants.SchemaPropertyNames.BEAN_WRITER_KEY);
            if (beanWriterClassName != null) {
                beanWriterInstance = (BeanWriter) Class.forName(beanWriterClassName).newInstance();
            }

            String typeMapperClassName = props.getProperty(SchemaConstants.SchemaPropertyNames.BEAN_WRITER_TYPEMAP_KEY);
            if (typeMapperClassName != null) {
                typeMapperInstance = (TypeMap) Class.forName(typeMapperClassName).newInstance();
            }

            beanTemplate = props.getProperty(SchemaConstants.SchemaPropertyNames.BEAN_WRITER_TEMPLATE_KEY);

            //set the props as the property map
            propertyMap = props;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    /**
     * Exposes the whole property set
     * @return Returns Properties.
     */
    public static Properties getPropertyMap() {
        return propertyMap;
    }

    public static String getBeanTemplate() {
        return beanTemplate;
    }

    public static BeanWriter getBeanWriterInstance() {
        return beanWriterInstance;
    }

    public static TypeMap getTypeMapperInstance() {
        return typeMapperInstance;
    }
}