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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * {@code BUnionType} represents a union type in Ballerina.
 *
 * @since 0.995.0
 */
public class BUnionType extends BType {

    public static final char PIPE = '|';
    private List<BType> memberTypes;
    private Boolean nullable;
    private String cachedToString;
    private int typeFlags;

    /**
     * Create a {@code BUnionType} which represents the union type.
     */
    public BUnionType() {
        super(null, null, Object.class);
    }

    /**
     * Create a {@code BUnionType} which represents the union type.
     *
     * @param memberTypes of the union type
     * @param typeFlags flags associated with the type
     */
    public BUnionType(List<BType> memberTypes, int typeFlags) {
        super(null, null, Object.class);
        this.memberTypes = memberTypes;
        this.typeFlags = typeFlags;
    }

    public BUnionType(List<BType> memberTypes) {
        this(memberTypes, 0);
        boolean nilable = false, isAnydata = true, isPureType = true;
        for (BType memberType : memberTypes) {
            nilable |= memberType.isNilable();
            isAnydata &= memberType.isAnydata();
            isPureType &= memberType.isPureType();
        }

        if (nilable) {
            this.typeFlags = TypeFlags.addToMask(this.typeFlags, TypeFlags.NILABLE);
        }
        if (isAnydata) {
            this.typeFlags = TypeFlags.addToMask(this.typeFlags, TypeFlags.ANYDATA);
        }
        if (isPureType) {
            this.typeFlags = TypeFlags.addToMask(this.typeFlags, TypeFlags.PURETYPE);
        }
    }

    public BUnionType(BType[] memberTypes, int typeFlags) {
        this(Arrays.asList(memberTypes), typeFlags);
    }

    public List<BType> getMemberTypes() {
        return memberTypes;
    }

    public boolean isNullable() {
        return isNilable();
    }

    @Override
    public <V extends Object> V getZeroValue() {
        if (isNilable() || memberTypes.stream().anyMatch(BType::isNilable)) {
            return null;
        }

        return memberTypes.get(0).getZeroValue();
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        if (isNilable() || memberTypes.stream().anyMatch(BType::isNilable)) {
            return null;
        }

        return memberTypes.get(0).getEmptyValue();
    }

    @Override
    public int getTag() {
        return TypeTags.UNION_TAG;
    }

    @Override
    public String toString() {
        if (cachedToString == null) {
            StringBuilder sb = new StringBuilder();
            int size = memberTypes.size();
            int i = 0;
            while (i < size) {
                sb.append(memberTypes.get(i).toString());
                if (++i < size) {
                    sb.append(PIPE);
                }
            }
            cachedToString = sb.toString();

        }
        return cachedToString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof BUnionType)) {
            return false;
        }

        BUnionType that = (BUnionType) o;
        if (this.memberTypes.size() != that.memberTypes.size()) {
            return false;
        }

        for (int i = 0; i < memberTypes.size(); i++) {
            if (!this.memberTypes.get(i).equals(that.memberTypes.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return toString();
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), memberTypes);
    }

    public boolean isNilable() {
        // TODO: use the flag
        if (nullable == null) {
            nullable = checkNillable(memberTypes);
        }
        return nullable;
    }

    private boolean checkNillable(List<BType> memberTypes) {
        for (BType t : memberTypes) {
            if (t.isNilable()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isAnydata() {
        return TypeFlags.isFlagOn(this.typeFlags, TypeFlags.ANYDATA);
    }

    @Override
    public boolean isPureType() {
        return TypeFlags.isFlagOn(this.typeFlags, TypeFlags.PURETYPE);
    }

    public int getTypeFlags() {
        return this.typeFlags;
    }
}
