/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.paimon.file.predicate;

import org.apache.paimon.types.DataType;

import org.apache.paimon.format.FieldStats;

import java.util.List;
import java.util.Optional;

import static org.apache.paimon.file.predicate.CompareUtils.compareLiteral;

/** A {@link LeafFunction} to eval not in. */
public class NotIn extends LeafFunction {

    private static final long serialVersionUID = 1L;

    public static final NotIn INSTANCE = new NotIn();

    private NotIn() {}

    @Override
    public boolean test(DataType type, Object field, List<Object> literals) {
        if (field == null) {
            return false;
        }
        for (Object literal : literals) {
            if (literal == null || compareLiteral(type, literal, field) == 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean test(
            DataType type, long rowCount, FieldStats fieldStats, List<Object> literals) {
        Long nullCount = fieldStats.nullCount();
        if (nullCount != null && rowCount == nullCount) {
            return false;
        }
        for (Object literal : literals) {
            if (literal == null
                    || (compareLiteral(type, literal, fieldStats.minValue()) == 0
                            && compareLiteral(type, literal, fieldStats.maxValue()) == 0)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Optional<LeafFunction> negate() {
        return Optional.of(In.INSTANCE);
    }

    @Override
    public <T> T visit(FunctionVisitor<T> visitor, FieldRef fieldRef, List<Object> literals) {
        return visitor.visitNotIn(fieldRef, literals);
    }
}