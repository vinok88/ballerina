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
package org.ballerinalang.jvm.types;

import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;

/**
 * {@code AnnotatableType} represents a type description which contains annotations.
 *
 * @since 0.995.0
 */
public abstract class AnnotatableType extends BType {

    protected MapValue<String, ArrayValue> annotations = new MapValueImpl<>();

    AnnotatableType(String typeName, BPackage pkg, Class<?> valueClass) {
        super(typeName, pkg, valueClass);
    }

    public abstract String getAnnotationKey();

    public void addAnnotation(String key, MapValue annotation) {
        ArrayValue arrayValue = this.annotations.computeIfAbsent(key,
                k -> new ArrayValue(new BArrayType(BTypes.typeAnydata)));
        arrayValue.append(annotation);
    }

    public ArrayValue getAnnotation(String pkgPath, String name) {
        String key = pkgPath + ":" + name;
        return this.annotations.get(key);
    }
}
