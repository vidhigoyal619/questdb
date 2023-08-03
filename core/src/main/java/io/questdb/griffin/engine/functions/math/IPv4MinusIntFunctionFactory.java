/*******************************************************************************
 *     ___                  _   ____  ____
 *    / _ \ _   _  ___  ___| |_|  _ \| __ )
 *   | | | | | | |/ _ \/ __| __| | | |  _ \
 *   | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *    \__\_\\__,_|\___||___/\__|____/|____/
 *
 *  Copyright (c) 2014-2019 Appsicle
 *  Copyright (c) 2019-2023 QuestDB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package io.questdb.griffin.engine.functions.math;

import io.questdb.cairo.CairoConfiguration;
import io.questdb.cairo.sql.Function;
import io.questdb.cairo.sql.Record;
import io.questdb.griffin.FunctionFactory;
import io.questdb.griffin.PlanSink;
import io.questdb.griffin.SqlExecutionContext;
import io.questdb.griffin.engine.functions.BinaryFunction;
import io.questdb.griffin.engine.functions.IPv4Function;
import io.questdb.std.IntList;
import io.questdb.std.Numbers;
import io.questdb.std.ObjList;

public class IPv4MinusIntFunctionFactory implements FunctionFactory {
    @Override
    public String getSignature() {
        return "-(XI)";
    }

    @Override
    public Function newInstance(
            int position,
            ObjList<Function> args,
            IntList argPositions,
            CairoConfiguration configuration,
            SqlExecutionContext sqlExecutionContext
    ) {
        return new IPv4MinusIntFunctionFactory.IPv4MinusIntFunction(args.getQuick(0), args.getQuick(1));
    }

    public static final class IPv4MinusIntFunction extends IPv4Function implements BinaryFunction {
        private final Function left;
        private final Function right;

        public IPv4MinusIntFunction(Function left, Function right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public int getIPv4(Record rec) {
            final long l = Numbers.ipv4ToLong(left.getIPv4(rec));
            final long r = Numbers.ipv4ToLong(right.getInt(rec));

            if (r >= l) {
                return Numbers.IPv4_NULL;
            }

            return (int) l != Numbers.IPv4_NULL && (int) r != Numbers.INT_NaN ? (int) (l - r) : Numbers.IPv4_NULL;
        }

        @Override
        public Function getLeft() {
            return left;
        }

        @Override
        public Function getRight() {
            return right;
        }

        @Override
        public void toPlan(PlanSink sink) {
            sink.val(left).val('+').val(right);
        }
    }
}