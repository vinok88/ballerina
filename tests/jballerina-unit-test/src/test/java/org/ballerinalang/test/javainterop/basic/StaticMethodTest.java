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
package org.ballerinalang.test.javainterop.basic;

import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BHandleValue;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Date;

/**
 * Test cases for java interop static function invocations.
 *
 * @since 1.0.0
 */
public class StaticMethodTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/javainterop/basic/static_method_tests.bal");
    }

    @Test(description = "Test invoking a java static function that accepts and return nothing")
    public void testAcceptNothingAndReturnNothing() {
        BValue[] returns = BRunUtil.invoke(result, "testAcceptNothingAndReturnNothing");



        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0], null);
    }

    @Test(description = "Test invoking a java static function that accepts and return nothing")
    public void testInteropFunctionWithDifferentName() {
        BValue[] returns = BRunUtil.invoke(result, "testInteropFunctionWithDifferentName");



        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0], null);
    }

    @Test(description = "Test invoking a java static function that accepts nothing and returns a Date")
    public void testAcceptNothingButReturnDate() {
        BValue[] returns = BRunUtil.invoke(result, "testAcceptNothingButReturnDate");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(((BHandleValue) returns[0]).getValue() instanceof Date);
    }

    @Test(description = "Test invoking a java static function that accepts and returns a Date")
    public void testAcceptSomethingAndReturnSomething() {
        BValue[] args = new BValue[1];
        Date argValue = new Date();
        args[0] = new BHandleValue(argValue);
        BValue[] returns = BRunUtil.invoke(result, "testAcceptSomethingAndReturnSomething", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(((BHandleValue) returns[0]).getValue() instanceof Date);
        Assert.assertEquals(((BHandleValue) returns[0]).getValue(), argValue);
    }

    @Test(description = "Test static java method that accepts two parameters")
    public void testJavaInteropFunctionThatAcceptsTwoParameters() {
        BValue[] args = new BValue[2];
        args[0] = new BHandleValue("1");
        args[1] = new BHandleValue("2");
        BValue[] returns = BRunUtil.invoke(result, "testAcceptTwoParamsAndReturnSomething", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BHandleValue) returns[0]).getValue(), "12");
    }

    @Test(description = "Test static java method that accepts three parameters")
    public void testJavaInteropFunctionThatAcceptsThreeParameters() {
        BValue[] args = new BValue[3];
        args[0] = new BHandleValue(1);
        args[1] = new BHandleValue(2);
        args[2] = new BHandleValue(3);
        BValue[] returns = BRunUtil.invoke(result, "testAcceptThreeParamsAndReturnSomething", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BHandleValue) returns[0]).getValue(), 6);
    }

    @Test(description = "Test static java method that returns error value as objects")
    public void testReturnObjectValueOrError() {
        BValue[] returns = BRunUtil.invoke(result, "getObjectOrError");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BError) returns[0]).getReason(), "some reason");
    }

    public static Object returnObjectOrError() {
        return new ErrorValue("some reason", null);
    }
}
