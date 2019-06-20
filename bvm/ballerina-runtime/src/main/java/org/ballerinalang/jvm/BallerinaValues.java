/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.ballerinalang.jvm;

import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BObjectType;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.ValueCreator;

import java.util.Map;

/**
 * The factory utility class that creates runtime values from given package and type names.
 *
 * @since 0.995.0
 */
public class BallerinaValues {

    /**
     * Method that creates a runtime record value using the given package name and record type name.
     *
     * @param pkgName the name of the package that the record type resides.
     * @param recordTypeName name of the record type.
     * @return value of the record.
     */
    public static MapValue<String, Object> createRecordValue(String pkgName, String recordTypeName) {
        ValueCreator valueCreator = ValueCreator.getValueCreator(pkgName);
        return valueCreator.createRecordValue(recordTypeName);
    }

    /**
     * Method that creates a runtime object value using the given package name and object type name.
     *
     * @param pkgName the name of the package that the record type resides.
     * @param objectTypeName name of the object type.
     * @param fieldValues values to be used for fields when creating the object value instance.
     * @return value of the object.
     */
    public static ObjectValue createObjectValue(String pkgName, String objectTypeName, Object... fieldValues) {
        ValueCreator valueCreator = ValueCreator.getValueCreator(pkgName);
        ObjectValue objectValue = valueCreator.createObjectValue(objectTypeName);
        int valCount = 0;
        BObjectType objectType = objectValue.getType();
        for (BField field : objectType.getFields().values()) {
            Object value;
            if (fieldValues.length >= valCount + 1) {
                value = fieldValues[valCount];
            } else {
                BType fieldType = field.getFieldType();
                if (fieldType.getTag() == TypeTags.OBJECT_TYPE_TAG) {
                    // This is a hack to avoid self references. This should be fixed properly.
                    if (objectTypeName.equals(fieldType.getName())) {
                        continue;
                    }
                    value = createObjectValue(fieldType.getPackage().toString(), fieldType.getName());
                } else if (fieldType.getTag() == TypeTags.RECORD_TYPE_TAG) {
                    value = createRecordValue(fieldType.getPackage().toString(), fieldType.getName());
                } else {
                    value = fieldType.getEmptyValue();
                }
            }
            objectValue.set(field.name, value);
            valCount++;
        }
        return objectValue;
    }

    /**
     * Method to populate a runtime record value with given field values.
     *
     * @param record which needs to get populated
     * @param values field values of the record.
     * @return value of the record.
     */
    public static MapValue<String, Object> createRecord(MapValue<String, Object> record, Object... values) {
        BRecordType recordType = (BRecordType) record.getType();
        MapValue<String, Object> mapValue = new MapValueImpl<>(recordType);
        int i = 0;
        for (Map.Entry<String, BField> fieldEntry : recordType.getFields().entrySet()) {
            mapValue.put(fieldEntry.getKey(), values[i++]);
        }
        return mapValue;
    }
}
