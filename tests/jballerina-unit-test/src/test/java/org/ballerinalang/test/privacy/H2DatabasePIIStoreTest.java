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
package org.ballerinalang.test.privacy;

import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.test.utils.SQLDBUtils;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Test class for Privacy API.
 *
 * @since 0.982.1
 */
public class H2DatabasePIIStoreTest {

    private CompileResult result;
    private static final String DB_NAME = "TestDBH2";
    private static final String DB_DIRECTORY_H2 = "./target/H2PIIStore/";

    private static final String UUID_REGEX = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
    private static final String SAMPLE_PII_VALUE = "Personally Identifiable Information";

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/privacy/h2_pii_store.bal");
        SQLDBUtils.deleteFiles(new File(DB_DIRECTORY_H2), DB_NAME);
        SQLDBUtils.initH2Database(DB_DIRECTORY_H2, DB_NAME, "datafiles/privacy/PII_Store_Table_Create.sql");
    }

    @Test
    public void testPseudonymizeValidPii() {
        BValue[] args = { new BString(SAMPLE_PII_VALUE) };
        BValue[] returns = BRunUtil.invoke(result, "pseudonymizePii", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertTrue(((BString) returns[0]).value().matches(UUID_REGEX));
    }

    @Test
    public void testDepseudonymizeValidId() {
        BValue[] args = { new BString(SAMPLE_PII_VALUE) };
        BValue[] returns = BRunUtil.invoke(result, "pseudonymizePii", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertTrue(((BString) returns[0]).value().matches(UUID_REGEX));

        BValue[] depseudonymizeArgs = { returns[0] };
        BValue[] depseudonymizeReturns = BRunUtil.invoke(result, "depseudonymizePii", depseudonymizeArgs);
        Assert.assertEquals(depseudonymizeReturns.length, 1);
        Assert.assertTrue(depseudonymizeReturns[0] instanceof BString);
        Assert.assertTrue(((BString) depseudonymizeReturns[0]).value().equals(SAMPLE_PII_VALUE));
    }

    @Test
    public void testDepseudonymizeInvalidId() {
        String invalidId = "12345";
        BValue[] depseudonymizeArgs = { new BString(invalidId) };
        BValue[] depseudonymizeReturns = BRunUtil.invoke(result, "depseudonymizePii", depseudonymizeArgs);
        Assert.assertEquals(depseudonymizeReturns.length, 1);
        Assert.assertTrue(depseudonymizeReturns[0] instanceof BError);
        Assert.assertEquals(((BError) depseudonymizeReturns[0]).getReason(),
                "Identifier " + invalidId + " is not found in PII store");
    }

    @Test
    public void testDeleteValidId() {
        BValue[] args = { new BString(SAMPLE_PII_VALUE) };
        BValue[] returns = BRunUtil.invoke(result, "pseudonymizePii", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertTrue(((BString) returns[0]).value().matches(UUID_REGEX));

        BValue[] deleteReturns = BRunUtil.invoke(result, "deletePii", returns);
        Assert.assertEquals(deleteReturns.length, 1);
        Assert.assertNull(deleteReturns[0]);

        BValue[] depseudonymizeReturns = BRunUtil.invoke(result, "depseudonymizePii", returns);
        Assert.assertEquals(depseudonymizeReturns.length, 1);
        Assert.assertTrue(depseudonymizeReturns[0] instanceof BError);
        Assert.assertEquals(((BError) depseudonymizeReturns[0]).getReason(),
                "Identifier " + ((BString) returns[0]).value() + " is not found in PII store");
    }

    @Test
    public void testDeleteInvalidId() {
        String invalidId = "12345";
        BValue[] deleteArgs = { new BString(invalidId) };
        BValue[] deleteReturns = BRunUtil.invoke(result, "deletePii", deleteArgs);
        Assert.assertEquals(deleteReturns.length, 1);
        Assert.assertTrue(deleteReturns[0] instanceof BError);
        Assert.assertEquals(((BError) deleteReturns[0]).getReason(),
                "Identifier " + invalidId + " is not found in PII store");
    }

    @Test
    public void testPseudonymizeSamePiiTwice() {
        BValue[] args1 = { new BString(SAMPLE_PII_VALUE) };
        BValue[] returns1 = BRunUtil.invoke(result, "pseudonymizePii", args1);
        Assert.assertEquals(returns1.length, 1);
        Assert.assertTrue(returns1[0] instanceof BString);
        Assert.assertTrue(((BString) returns1[0]).value().matches(UUID_REGEX));

        BValue[] args2 = { new BString(SAMPLE_PII_VALUE) };
        BValue[] returns2 = BRunUtil.invoke(result, "pseudonymizePii", args2);
        Assert.assertEquals(returns2.length, 1);
        Assert.assertTrue(returns2[0] instanceof BString);
        Assert.assertTrue(((BString) returns2[0]).value().matches(UUID_REGEX));

        Assert.assertNotEquals(((BString) returns1[0]).value(), ((BString) returns2[0]).value());
    }

    @Test (expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*error: Table name is required.*")
    public void testEmptyTableName() {
        BRunUtil.invoke(result, "pseudonymizePiiWithEmptyTableName",
                new BValue[] { new BString(SAMPLE_PII_VALUE) });
    }

    @AfterClass(alwaysRun = true)
    public void stopStoreDB() {
        BRunUtil.invoke(result, "shutdown");
    }

    @AfterSuite
    public void cleanup() {
        SQLDBUtils.deleteDirectory(new File(DB_DIRECTORY_H2));
    }

}
