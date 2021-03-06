/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.co.yahoo.dataplatform.mds.spread.column.filter;

import org.testng.annotations.Test;

import jp.co.yahoo.dataplatform.schema.objects.DoubleObj;
import jp.co.yahoo.dataplatform.schema.objects.FloatObj;
import jp.co.yahoo.dataplatform.schema.objects.IntegerObj;
import jp.co.yahoo.dataplatform.schema.objects.LongObj;
import jp.co.yahoo.dataplatform.schema.objects.ShortObj;

import static org.testng.Assert.assertEquals;

import java.io.IOException;

public class TestNumberFilter {

    @Test
    public void T_constractNotException() {
        new NumberFilter(NumberFilterType.EQUAL,     new IntegerObj(123));
        new NumberFilter(NumberFilterType.EQUAL,     new DoubleObj(1.23));
        new NumberFilter(NumberFilterType.EQUAL,     new LongObj(123));
        new NumberFilter(NumberFilterType.EQUAL,     new FloatObj((float) 1.23));
        new NumberFilter(NumberFilterType.EQUAL,     new ShortObj((short) 123));
        new NumberFilter(NumberFilterType.NOT_EQUAL, new IntegerObj(123));
        new NumberFilter(NumberFilterType.NOT_EQUAL, new DoubleObj(1.23));
        new NumberFilter(NumberFilterType.NOT_EQUAL, new LongObj(123));
        new NumberFilter(NumberFilterType.NOT_EQUAL, new FloatObj((float) 1.23));
        new NumberFilter(NumberFilterType.NOT_EQUAL, new ShortObj((short) 123));
        new NumberFilter(NumberFilterType.LT,        new IntegerObj(123));
        new NumberFilter(NumberFilterType.LT,        new DoubleObj(1.23));
        new NumberFilter(NumberFilterType.LT,        new LongObj(123));
        new NumberFilter(NumberFilterType.LT,        new FloatObj((float) 1.23));
        new NumberFilter(NumberFilterType.LT,        new ShortObj((short) 123));
        new NumberFilter(NumberFilterType.LE,        new IntegerObj(123));
        new NumberFilter(NumberFilterType.LE,        new DoubleObj(1.23));
        new NumberFilter(NumberFilterType.LE,        new LongObj(123));
        new NumberFilter(NumberFilterType.LE,        new FloatObj((float) 1.23));
        new NumberFilter(NumberFilterType.LE,        new ShortObj((short) 123));
        new NumberFilter(NumberFilterType.GT,        new IntegerObj(123));
        new NumberFilter(NumberFilterType.GT,        new DoubleObj(1.23));
        new NumberFilter(NumberFilterType.GT,        new LongObj(123));
        new NumberFilter(NumberFilterType.GT,        new FloatObj((float) 1.23));
        new NumberFilter(NumberFilterType.GT,        new ShortObj((short) 123));
        new NumberFilter(NumberFilterType.GE,        new IntegerObj(123));
        new NumberFilter(NumberFilterType.GE,        new DoubleObj(1.23));
        new NumberFilter(NumberFilterType.GE,        new LongObj(123));
        new NumberFilter(NumberFilterType.GE,        new FloatObj((float) 1.23));
        new NumberFilter(NumberFilterType.GE,        new ShortObj((short) 123));
    }

    @Test
    public void T_getNumberObject() throws IOException {
        NumberFilter target = new NumberFilter(NumberFilterType.EQUAL, new IntegerObj(123));
        assertEquals(target.getNumberObject().getInt(), 123);
    }

    @Test
    public void T_getNumberFilterType() {
        NumberFilter target = new NumberFilter(NumberFilterType.EQUAL, new IntegerObj(123));
        assertEquals(target.getNumberFilterType(), NumberFilterType.EQUAL);
    }

    @Test
    public void T_getFilterType() {
        NumberFilter target = new NumberFilter(NumberFilterType.EQUAL, new IntegerObj(123));
        assertEquals(target.getFilterType(), FilterType.NUMBER);
    }

}
