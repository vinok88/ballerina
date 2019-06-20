/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.jvm.types;

import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;

import java.util.StringJoiner;

/**
 * {@code AttachedFunction} represents a attached function in Ballerina.
 *
 * @since 0.995.0
 */
public class AttachedFunction extends BFunctionType {

    public String funcName;
    public BFunctionType type;
    public int flags;
    public BObjectType parent;

    public AttachedFunction(String funcName, BObjectType parent, BFunctionType type, int flags) {
        this.funcName = funcName;
        this.type = type;
        this.parent = parent;
        this.flags = flags;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",", "function " + funcName + "(", ") returns (" + type.retType + ")");
        for (BType type : type.paramTypes) {
            sj.add(type.getName());
        }
        return sj.toString();
    }

    @Override
    public BType[] getParameterType() {
        return type.paramTypes;
    }

    @Override
    public String getName() {
        return this.funcName;
    }

    @Override
    public void addAnnotation(String key, MapValue annotation) {
        this.type.addAnnotation(key, annotation);
    }

    @Override
    public ArrayValue getAnnotation(String pkgPath, String name) {
        return this.type.getAnnotation(pkgPath, name);
    }

    @Override
    public String getAnnotationKey() {
        return parent.typeName + "." + funcName;
    }
}
