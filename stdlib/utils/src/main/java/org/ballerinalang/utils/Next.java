/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/
package org.ballerinalang.utils;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BFiniteType;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.TypeFlags;
import org.ballerinalang.jvm.values.DecimalValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.RefValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

/**
 * Get the next value of an iterator.
 *
 * @since 0.995.0
 */
@BallerinaFunction(orgName = "ballerina", 
                   packageName = "utils", 
                   functionName = "next",
                   args = { @Argument(name = "value", type = TypeKind.ANY) }, 
                   returnType = { @ReturnType(type = TypeKind.ANY) })
public class Next {

    private static final String KEY = "value";

    public static Object next(Strand strand, Object iterator) {
        if (!(iterator instanceof Iterator)) {
            // shouldn't reach here
            throw new IllegalStateException("Iterator expected");
        }

        Iterator<?> itr = (Iterator<?>) iterator;
        if (!Optional.of(itr).get().hasNext()) {
            // If we don't have a next value, that means we have reached the end of the iterable list. So
            // we set null to the corresponding registry location.
            return null;
        }

        // Check whether we have a next value.
        // Get the next value.
        Object value = Optional.of(itr).get().next();
        // We create a new map and add the value to the map with the key `value`. Then we set this
        // map to the corresponding registry location.
        BMapType mapType = new BMapType(getType(value));
        MapValueImpl<String, Object> newMap = new MapValueImpl<>(mapType);
        newMap.put(KEY, value);
        return newMap;
    }

    private static org.ballerinalang.jvm.types.BType getType(Object value) {
        if (value == null) {
            return BTypes.typeNull;
        } else if (value instanceof Number || value instanceof String || value instanceof Boolean ||
                value instanceof DecimalValue) {
            return getFiniteType(value);
        } else {
            return ((RefValue) value).getType();
        }
    }

    private static org.ballerinalang.jvm.types.BType getFiniteType(Object value) {
        Set<Object> valueSpace = new HashSet<>();
        valueSpace.add(value);
        int masks = value == null ? TypeFlags.NILABLE : 0;
        return new BFiniteType(value.toString(), valueSpace,
                TypeFlags.addToMask(masks, TypeFlags.ANYDATA, TypeFlags.PURETYPE));
    }
}
