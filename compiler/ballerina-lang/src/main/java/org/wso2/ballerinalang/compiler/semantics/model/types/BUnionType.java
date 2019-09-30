/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.semantics.model.types;

import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.types.UnionType;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeDescriptor;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@code UnionType} represents a union type in Ballerina.
 *
 * @since 0.966.0
 */
public class BUnionType extends BType implements UnionType {
    private boolean nullable;

    private LinkedHashSet<BType> memberTypes;
    private Optional<Boolean> isAnyData = Optional.empty();
    private Optional<Boolean> isPureType = Optional.empty();

    private BUnionType(BTypeSymbol tsymbol, LinkedHashSet<BType> memberTypes, boolean nullable) {
        super(TypeTags.UNION, tsymbol);
        this.memberTypes = memberTypes;
        this.nullable = nullable;
    }

    @Override
    public Set<BType> getMemberTypes() {
        return Collections.unmodifiableSet(this.memberTypes);
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.UNION;
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(getKind().typeName());
        this.memberTypes.stream()
                .filter(memberType -> memberType.tag != TypeTags.NIL)
                .forEach(memberType -> joiner.add(memberType.toString()));
        String typeStr = this.memberTypes.stream().filter(memberType -> memberType.tag != TypeTags.NIL).count() > 1
                ? "(" + joiner.toString() + ")" : joiner.toString();
        boolean hasNilType = this.memberTypes.stream().anyMatch(type -> type.tag == TypeTags.NIL);
        return (nullable && hasNilType) ? (typeStr + Names.QUESTION_MARK.value) : typeStr;
    }

    @Override
    public String getDesc() {
        StringBuilder sig = new StringBuilder(TypeDescriptor.SIG_UNION + memberTypes.size() + ";");
        memberTypes.forEach(memberType -> sig.append(memberType.getDesc()));
        return sig.toString();
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    /**
     * Creates a union type using the types specified in the `types` set. The created union will not have union types in
     * its member types set. If the set contains the nil type, calling isNullable() will return true.
     *
     * @param tsymbol Type symbol for the union.
     * @param types   The types to be used to define the union.
     * @return The created union type.
     */
    public static BUnionType create(BTypeSymbol tsymbol, LinkedHashSet<BType> types) {
        LinkedHashSet<BType> memberTypes = toFlatTypeSet(types);
        boolean hasNilableType = memberTypes.stream().anyMatch(t -> t.isNullable() && t.tag != TypeTags.NIL);
        if (hasNilableType) {
            memberTypes = memberTypes.stream().filter(t -> t.tag != TypeTags.NIL)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        return new BUnionType(tsymbol, memberTypes, memberTypes.stream().anyMatch(BType::isNullable));
    }

    /**
     * Creates a union type using the provided types. If the set contains the nil type, calling isNullable() will return
     * true.
     *
     * @param tsymbol Type symbol for the union.
     * @param types   The types to be used to define the union.
     * @return The created union type.
     */
    public static BUnionType create(BTypeSymbol tsymbol, BType... types) {
        LinkedHashSet<BType> memberTypes = Arrays.stream(types).collect(Collectors.toCollection(LinkedHashSet::new));
        return create(tsymbol, memberTypes);
    }

    /**
     * Adds the specified type as a member of the union. If the specified type is also a union, all the member types of
     * it are added to the union. If the newly added type is nil or is a nil-able type, the union type will also be a
     * nil-able type.
     *
     * @param type Type to be added to the union.
     */
    public void add(BType type) {
        if (type.tag == TypeTags.UNION) {
            assert type instanceof BUnionType;
            this.memberTypes.addAll(toFlatTypeSet(((BUnionType) type).memberTypes));
        } else {
            this.memberTypes.add(type);
        }

        this.nullable = this.nullable || type.isNullable();
    }

    /**
     * Adds all the types in the specified set as members of the union.
     *
     * @param types Types to be added to the union.
     */
    public void addAll(LinkedHashSet<BType> types) {
        types.forEach(this::add);
    }

    public void remove(BType type) {
        if (type.tag == TypeTags.UNION) {
            assert type instanceof BUnionType;
            this.memberTypes.removeAll(((BUnionType) type).getMemberTypes());
        } else {
            this.memberTypes.remove(type);
        }

        if (type.isNullable()) {
            this.nullable = false;
        }
    }

    /**
     * Returns an iterator to iterate over the member types of the union.
     *
     * @return  An iterator over the member set.
     */
    public Iterator<BType> iterator() {
        return this.memberTypes.iterator();
    }

    @Override
    public boolean isAnydata() {
        if (this.isAnyData.isPresent()) {
            return this.isAnyData.get();
        }

        for (BType memberType : this.memberTypes) {
            if (!memberType.isAnydata()) {
                this.isAnyData = Optional.of(false);
                return false;
            }
        }

        this.isAnyData = Optional.of(true);
        return true;
    }

    @Override
    public boolean isPureType() {
        if (this.isPureType.isPresent()) {
            return this.isPureType.get();
        }

        for (BType memberType : this.memberTypes) {
            if (!memberType.isPureType()) {
                this.isPureType = Optional.of(false);
                return false;
            }
        }

        this.isPureType = Optional.of(true);
        return true;
    }

    private static LinkedHashSet<BType> toFlatTypeSet(LinkedHashSet<BType> types) {
        return types.stream()
                .flatMap(type -> type.tag == TypeTags.UNION ?
                        ((BUnionType) type).memberTypes.stream() : Stream.of(type))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
