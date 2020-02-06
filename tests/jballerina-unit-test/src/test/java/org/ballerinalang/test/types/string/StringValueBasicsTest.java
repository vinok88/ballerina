/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.types.string;

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BRunUtil.IS_STRING_VALUE_PROP;

/**
 * Test StringValue impl of ballerina string.
 */
public class StringValueBasicsTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        System.setProperty(IS_STRING_VALUE_PROP, "true");
        result = BCompileUtil.compile("test-src/types/string/string-value-test.bal");
    }

    @Test
    public void testConcatBMPStrings() {
        BValue[] returns = BRunUtil.invoke(result, "concatBMP");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "red apple");
    }

    @Test
    public void testNonBMPStringLength() {
        BValue[] returns = BRunUtil.invoke(result, "nonBMPLength");
        Assert.assertEquals(returns[0].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);
    }

    @Test
    public void testRecordStringValuePut() {
        BValue[] returns = BRunUtil.invoke(result, "recordStringValuePut");
        //TODO assert return value has BString
    }

    @Test
    public void testError() {
        BValue[] returns = BRunUtil.invoke(result, "testError");
        Assert.assertEquals(returns[0].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);
    }

    @Test
    public void testArrayStore() {
        BValue[] returns = BRunUtil.invoke(result, "testArrayStore");
        Assert.assertEquals(returns[0].getClass(), BValueArray.class);
        Assert.assertEquals(((BValueArray) returns[0]).getStringArray()[0], "h😀llo");
    }

    @Test
    public void testObjects() {
        BValue[] returns = BRunUtil.invoke(result, "testObjects");
        Assert.assertTrue(returns[0].stringValue().contains("h😀llo"));
    }

    @AfterClass
    public void down() {
        System.clearProperty(IS_STRING_VALUE_PROP);
    }

}
