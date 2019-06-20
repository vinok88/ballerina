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
package org.ballerinalang.test.object;

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Test cases for user defined object types in ballerina.
 */
public class ObjectTest {

    @Test(description = "Test Basic object as struct")
    public void testBasicStructAsObject() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-simple-struct.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleObjectAsStruct");

        Assert.assertEquals(returns.length, 4);

        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BInteger.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
        Assert.assertEquals(returns[1].stringValue(), "sample name");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 50);
        Assert.assertEquals(returns[3].stringValue(), "february");
    }

    @Test(description = "Test Object field defaultable")
    public void testObjectFieldDefaultable() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-field-defaultable.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectFieldDefaultable");

        Assert.assertEquals(returns.length, 4);

        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BInteger.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
        Assert.assertEquals(returns[1].stringValue(), "sample name");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 50);
        Assert.assertEquals(returns[3].stringValue(), "february");
    }

    @Test(description = "Test Basic object as struct with just new")
    public void testBasicStructAsObjectWithJustNew() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-simple-struct.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleObjectAsStructWithNew");

        Assert.assertEquals(returns.length, 4);

        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BInteger.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
        Assert.assertEquals(returns[1].stringValue(), "sample name");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 50);
        Assert.assertEquals(returns[3].stringValue(), "february");
    }

    @Test(description = "Test object with init function")
    public void testObjectWithSimpleInit() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-with-init-func.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectWithSimpleInit");

        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BInteger.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 17);
        Assert.assertEquals(returns[1].stringValue(), "sample value1");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 99);
        Assert.assertEquals(returns[3].stringValue(), "default value");
    }

    @Test(description = "Test object with defaultable field in init function")
    public void testObjectWithDefaultableField() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-with-defaultable-field.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectWithSimpleInit");

        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BInteger.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 109);
        Assert.assertEquals(returns[1].stringValue(), "sample value1");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 50);
        Assert.assertEquals(returns[3].stringValue(), "default value");
    }

    @Test(description = "Test object with init with different values")
    public void testObjectWithSimpleInitWithDiffValues() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-with-init-func.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectWithSimpleInitWithDiffValues");

        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BInteger.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 37);
        Assert.assertEquals(returns[1].stringValue(), "sample value1");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 675);
        Assert.assertEquals(returns[3].stringValue(), "adding value in invocation");
    }

    @Test(description = "Test object without RHS type")
    public void testObjectWithoutRHSType() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-with-init-func.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectWithoutRHSType");

        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BInteger.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 37);
        Assert.assertEquals(returns[1].stringValue(), "sample value1");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 675);
        Assert.assertEquals(returns[3].stringValue(), "adding value in invocation");
    }

    @Test(description = "Test object with init attached function")
    public void testObjectWithAttachedFunction() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-with-attach-funcs.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectWithAttachedFunc1");

        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BInteger.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 361);
        Assert.assertEquals(returns[1].stringValue(), "added values february");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 99);
        Assert.assertEquals(returns[3].stringValue(), "february");
    }

    @Test(description = "Test object with self keyword")
    public void testObjectWithSelfKeyword() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-self-keyword.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectWithSelfKeyword");

        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BString.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(returns[0].stringValue(), "sample name");
        Assert.assertEquals(returns[1].stringValue(), "sample name");
        Assert.assertEquals(returns[2].stringValue(), "sample name");
        Assert.assertEquals(returns[3].stringValue(), "sample name");
    }

    @Test(description = "Test object with calling attached functions")
    public void testObjectCallAttachedFunctions() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-call-attached-functions.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectCallAttachedFunctions");

        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BString.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(returns[0].stringValue(), "sample name");
        Assert.assertEquals(returns[1].stringValue(), "sample name");
        Assert.assertEquals(returns[2].stringValue(), "sample name");
        Assert.assertEquals(returns[3].stringValue(), "sample name");
    }

    @Test(description = "Test object inside object with different values")
    public void testObjectInsideObject() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-self-keyword-pass-values.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectInsideObject");

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BString.class);

        Assert.assertEquals(returns[0].stringValue(), "sample name");
        Assert.assertEquals(returns[1].stringValue(), "changed value");
    }

    @Test(description = "Test object self as a value")
    public void testObjectPassSelfAsValue() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-self-keyword-pass-values.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetValueFromPassedSelf");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        Assert.assertEquals(returns[0].stringValue(), "sample name");
    }

    @Test(description = "Test object with init attached function")
    public void testObjectWithAttachedFunction1() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-with-interface.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectWithInterface");

        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BInteger.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 80);
        Assert.assertEquals(returns[1].stringValue(), "sample value1");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 100);
        Assert.assertEquals(returns[3].stringValue(), "adding value in invocation uuuu");
    }

    @Test(description = "Test object with attached function implementation")
    public void testObjectWithAttachedFunctionImpl() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-with-interface-and-impl.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectWithInterface");

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 17);
        Assert.assertEquals(returns[1].stringValue(), "february");
    }

    @Test(description = "Test object with default initializer")
    public void testObjectWithWithDefaultInitialize() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_declaration_test.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetDefaultValuesInObject");

        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BInteger.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
        Assert.assertEquals(returns[1].stringValue(), "sample value");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 0);
        Assert.assertEquals(returns[3].stringValue(), "");
    }

    @Test(description = "Test passing value to a defaultable object field")
    public void testPassingValueForDefaultableObjectField() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_values_for_defaultable_field.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "passValueForDefaultableObjectField");

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 50);
        Assert.assertEquals(returns[1].stringValue(), "passed in name value");
    }

    @Test(description = "Test shadowing object field")
    public void testShadowingObjectField() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_shadow_field.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testShadowingObjectField");

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 50);
        Assert.assertEquals(returns[1].stringValue(), "passed in name value");
    }

    @Test(description = "Test initializing object in return statement with same type")
    public void testNewAsReturnWithSameType() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_new_in_return.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testCreateObjectInReturnSameType");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);
    }

    @Test(description = "Test initializing object in return statement with different type")
    public void testNewAsReturnWithDifferentType() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_new_in_return.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testCreateObjectInReturnDifferentType");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 12);
    }

//
//    @Test(description = "Test object with default initializer with default values") //TODO fix
//    public void testObjectWithWithDefaultInitializeWithDefaultValues() {
//        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_with_default_test.bal");
//        BValue[] returns = BRunUtil.invoke(compileResult, "testGetDefaultValuesInObjectFields");
//
//        Assert.assertEquals(returns.length, 2);
//        Assert.assertSame(returns[0].getClass(), BInteger.class);
//        Assert.assertSame(returns[1].getClass(), BString.class);
//
//        Assert.assertEquals(((BInteger) returns[0]).intValue(), 15);
//        Assert.assertEquals(returns[1].stringValue(), "hello world");
//    }

    @Test(description = "Test object with default initialize global variable")
    public void testObjectWithDefaultInitializeGlobalVar() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_declaration_test.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetDefaultValuesInObjectGlobalVar");

        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BInteger.class);
        Assert.assertSame(returns[3].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
        Assert.assertEquals(returns[1].stringValue(), "sample value");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 0);
        Assert.assertEquals(returns[3].stringValue(), "");
    }

    @Test(description = "Test object self reference with defaultable")
    public void testObjectSelfreferenceWithDefaultable() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_cyclic_" +
                                                                   "self_reference_with_default.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testCyclicReferenceWithDefaultable");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 89);
    }

    @Test(description = "Test function references from an object")
    public void testFunctionReferencesFromObjects() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_function_pointer.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectFunctionPointer");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 18);
    }

    @Test(description = "Test object any type field as a constructor parameter")
    public void testObjectAnyTypeFieldAsConstructorParam() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_field_any_type.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectWithAnyTypeField");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        Assert.assertEquals(returns[0].stringValue(), "grainier");
    }

    @Test(description = "Test object recursive reference with nillable")
    public void testRecursiveObjectRefWithNillable() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_recurs_with_nill.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testRecursiveObjectWithNill");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 90);
    }

    @Test(description = "Test object field with expr as defaultable")
    public void testFieldWithExpr() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_field_with_expr.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testFieldWithExpr");

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 88);
        Assert.assertEquals(returns[1].stringValue(), "sanjiva");
    }

    @Test(description = "Negative test to test multiple attach functions for same function interface and " +
            "attached function without function interface")
    public void testObjectNegativeTestForAttachFunctions() {
        CompileResult result = BCompileUtil.compile("test-src/object/object-with-interface-and-impl-negative.bal");
        Assert.assertEquals(result.getErrorCount(), 4);

        // test accessing object fields without "self" keyword in attached functions.
        BAssertUtil.validateError(result, 0, "undefined symbol 'age'", 14, 17);
        // test duplicate matching attach function implementations
        BAssertUtil.validateError(result, 1, "implementation already exist for the given " + "function " +
                "'attachInterface' in same module", 18, 1);

        // test object without matching function signature within the object
        BAssertUtil.validateError(result, 2, "cannot find function signature for" + " function 'attachInterfaceFunc' " +
                "in object 'Employee'", 27, 1);

        // test accessing object fields without "self" keyword in attached functions.
        BAssertUtil.validateError(result, 3, "undefined symbol 'age'", 28, 17);
    }

    @Test(description = "Negative test to test uninitialized object variables")
    public void testObjectNegativeTestForNonInitializable() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_with_non_defaultable_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 3);
        BAssertUtil.validateError(result, 0, "undefined function 'attachInterface' in object 'Person'", 5, 13);
        BAssertUtil.validateError(result, 1, "variable 'p' is not initialized", 5, 13);
        BAssertUtil.validateError(result, 2, "variable 'p' is not initialized", 5, 35);
    }

    @Test(description = "Negative test to test returning different type without type name")
    public void testObjectNegativeTestForReturnDifferentType() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_new_in_return_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 3);
        BAssertUtil.validateError(result, 0, "too many arguments in call to 'new()'", 18, 12);
        BAssertUtil.validateError(result, 1, "cannot infer type of the object from 'other'", 27, 19);
        BAssertUtil.validateError(result, 2, "invalid variable definition; can not infer the assignment type.", 27, 19);
    }

    @Test(description = "Negative test to test returning different type without type name")
    public void testUnInitializableObjFieldAsParam() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_un_initializable_field.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "uninitialized field 'foo'", 18, 5);
    }

    @Test(description = "Negative test to test self reference types", enabled = false)
    public void testSelfReferenceType() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_cyclic_self_reference.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "cyclic type reference in " + "'[Person, Employee, Foo, Bar]'", 32, 5);
    }

    @Test(description = "Negative test to test self reference types")
    public void testNonMatchingAttachedFunction() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_invalid_attached_func_def.bal");
        int index = 0;
        Assert.assertEquals(result.getErrorCount(), 14);
        BAssertUtil.validateError(result, index++, "no implementation found for the function 'test6' of non-abstract "
                + "object 'Person'", 23, 5);
        BAssertUtil.validateError(result, index++, "no implementation found for the function 'test7' of non-abstract "
                + "object 'Person'", 25, 5);
        BAssertUtil.validateError(result, index++, "cannot find matching interface " + "function 'test0' in the " +
                "object 'Person'", 41, 1);
        BAssertUtil.validateError(result, index++, "cannot find matching interface " + "function 'test1' in the " +
                "object 'Person'", 45, 1);
        BAssertUtil.validateError(result, index++, "cannot find matching interface " + "function 'test2' in the " +
                "object 'Person'", 49, 1);
        BAssertUtil.validateError(result, index++, "cannot find matching interface " + "function 'test3' in the " +
                "object 'Person'", 53, 1);
        BAssertUtil.validateError(result, index++, "incompatible types: expected " + "'string', found 'int'", 53, 44);
        BAssertUtil.validateError(result, index++, "cannot find matching interface " + "function 'test5' in the " +
                "object 'Person'", 61, 1);
        BAssertUtil.validateError(result, index++, "interface and implementation of function 'test6' of non-abstract "
                + "object 'Person' should have same visibility", 65, 1);
        BAssertUtil.validateError(result, index++, "interface and implementation of function 'test7' of non-abstract "
                + "object 'Person' should have same visibility", 69, 1);
        BAssertUtil.validateError(result, index++, "cannot find matching interface " + "function 'test9' in the " +
                "object 'Person'", 77, 1);
        BAssertUtil.validateError(result, index++, "this function must return a result", 77, 1);
        BAssertUtil.validateError(result, index++, "cannot find matching interface " + "function 'test12' in the " +
                "object 'Person'", 89, 1);
        BAssertUtil.validateError(result, index, "cannot find matching interface " + "function 'test13' in the object" +
                " 'Person'", 93, 1);
    }

    @Test(description = "Negative test to test initializing objects with only interface functions")
    public void testInitializingInterfaceObject() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_initialize_interface_object.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "cannot initialize abstract object 'Person'", 3, 16);
    }

    @Test(description = "Negative test to test initializing object with struct literal")
    public void testInitializingObjectWithStructLiteral() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_init_with_struct_literal.bal");
        Assert.assertEquals(result.getErrorCount(), 2);
        BAssertUtil.validateError(result, 0, "invalid usage of record literal with type 'Person'", 1, 13);
        BAssertUtil.validateError(result, 1, "invalid usage of record literal with type 'Person'", 4, 16);
    }

    @Test(description = "Negative test to test referring undefined field in constructor")
    public void testReferUndefinedFieldBal() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_access_undefined_field.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "undefined symbol 'abc'", 6, 9);
    }

    @Test(description = "Negative test to test invalid object init functions")
    public void testObjectInitFunctionNegative() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_init_function_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 2);
        BAssertUtil.validateError(result, 0, "object '__init()' function cannot be an 'external' function", 19, 5);
        BAssertUtil.validateError(result, 1, "object '__init()' function cannot be an 'external' function", 23, 5);
    }

    @Test(description = "Test nillable initialization")
    @SuppressWarnings("unchecked")
    public void testNillableInitialization() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_nillable_init.bal");
        BValue[] personInstances = BRunUtil.invoke(result, "getPersonInstances");
        Assert.assertEquals(getAgeField((BMap<String, BValue>) personInstances[0]), 0);
        Assert.assertEquals(getAgeField((BMap<String, BValue>) personInstances[1]), 0);
        Assert.assertEquals(getAgeField((BMap<String, BValue>) personInstances[2]), 0);
        Assert.assertEquals(getAgeField((BMap<String, BValue>) personInstances[3]), 0);
        Assert.assertEquals(getAgeField((BMap<String, BValue>) personInstances[4]), 0);
        Assert.assertEquals(getAgeField((BMap<String, BValue>) personInstances[5]), 0);

        BValue[] results = BRunUtil.invoke(result, "getEmployeeInstance");
        BMap<String, BValue> employee = (BMap<String, BValue>) results[0];
        Assert.assertEquals(getAgeField((BMap<String, BValue>) employee.get("p3")), 0);
        Assert.assertEquals(getAgeField((BMap<String, BValue>) employee.get("p4")), 0);
        Assert.assertEquals(getAgeField((BMap<String, BValue>) employee.get("p5")), 0);
        Assert.assertEquals(getAgeField((BMap<String, BValue>) employee.get("p6")), 0);
    }

    private long getAgeField(BMap<String, BValue> person) {
        return ((BInteger) person.get("age")).intValue();
    }

    @Test(description = "Negative test to test object visibility modifiers")
    public void testObjectVisibilityModifiers() {
        CompileResult result = BCompileUtil.compile(this, "test-src/object", "mod");
        Assert.assertEquals(result.getErrorCount(), 19);
        int index = 0;
        BAssertUtil.validateError(result, index++, "external function 'func2' cannot have a body", 14
                , 1);
        BAssertUtil.validateError(result, index++, "attempt to refer to non-accessible symbol 'name'", 46, 17);
        BAssertUtil.validateError(result, index++, "undefined field 'name' in object 'testorg/mod:0.0.0:Employee'",
                                  46, 17);
        BAssertUtil.validateError(result, index++, "attempt to refer to non-accessible symbol 'Employee.getAge'",
                                  50, 14);
        BAssertUtil.validateError(result, index++, "undefined function 'getAge' in object 'testorg/mod:0.0.0:Employee'",
                                  50, 14);
        BAssertUtil.validateError(result, index++, "attempt to refer to non-accessible symbol 'name'", 57, 17);
        BAssertUtil.validateError(result, index++, "undefined field 'name' in object 'testorg/pkg1:Employee'", 57, 17);
        BAssertUtil.validateError(result, index++, "attempt to refer to non-accessible symbol 'email'", 58, 17);
        BAssertUtil.validateError(result, index++, "undefined field 'email' in object 'testorg/pkg1:Employee'", 58, 17);
        BAssertUtil.validateError(result, index++, "attempt to refer to non-accessible symbol 'Employee.getAge'",
                                  61, 14);
        BAssertUtil.validateError(result, index++, "undefined function 'getAge' in object 'testorg/pkg1:Employee'",
                                  61, 14);
        BAssertUtil.validateError(result, index++, "attempt to refer to non-accessible symbol " + "'Employee" +
                ".getEmail'", 62, 17);
        BAssertUtil.validateError(result, index++, "undefined function 'getEmail' in object 'testorg/pkg1:Employee'",
                                  62, 17);
        BAssertUtil.validateError(result, index++, "no implementation found for the function 'getName' of non" +
                "-abstract object 'testorg/mod:0.0.0:Employee2'", 70, 5);
        BAssertUtil.validateError(result, index++, "no implementation found for the function 'getAge' of non-abstract" +
                " object 'testorg/mod:0.0.0:Employee2'", 72, 5);
        BAssertUtil.validateError(result, index++, "no implementation found for the function 'getEmail' of " +
                "non-abstract object 'testorg/mod:0.0.0:Employee2'", 74, 5);
        BAssertUtil.validateError(result, index++, "interface and implementation of function 'getName' of " +
                "non-abstract object 'testorg/mod:0.0.0:Employee2' should have same visibility", 77, 1);
        BAssertUtil.validateError(result, index++, "interface and implementation of function 'getAge' of non-abstract" +
                " object 'testorg/mod:0.0.0:Employee2' should have same visibility", 81, 1);
        BAssertUtil.validateError(result, index++, "interface and implementation of function 'getEmail' of " +
                "non-abstract object 'testorg/mod:0.0.0:Employee2' should have same visibility", 85, 1);
    }

    @Test(description = "Negative test to test unknown object field type")
    public void testUnknownObjectFieldType() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_undefined_field_type_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "unknown type 'Employee'", 3, 13);
    }

    @Test
    public void testAttachFunctionsWithIdenticalRestParams() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/attach_func_with_identical_rest_params" +
                                                                   ".bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testAttachFunctionsWithIdenticalRestParams");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "hello foo");
    }

    @Test
    public void testStructPrint() {
        PrintStream prevOut = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object-print.bal");
        System.setOut(new PrintStream(out));
        BValue[] returns = BRunUtil.invoke(compileResult, "testPrintingObject");
        System.setOut(prevOut);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BMap.class);
        Assert.assertEquals(returns[0].stringValue(), "{age:20, name:\"John\"}");
        Assert.assertEquals(out.toString().trim(), "{age:20, name:\"John\"}");
    }

    @Test
    public void testObjectInit() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_constructor.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testObjectInit");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test
    public void testObjectPrivateMethods() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_private_method.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testPrivateMethodAccess");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 15000);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 12500);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 11500);
    }

    @Test(description = "Negative test to test object private methods")
    public void testObjectPrivateMethodsNegative() {
        CompileResult result = BCompileUtil.compile("test-src/object/object_private_method_negative.bal");
        Assert.assertEquals(result.getErrorCount(), 4);
        int i = 0;
        BAssertUtil.validateError(result, i++,
                                  "attempt to refer to non-accessible symbol 'Person.incrementSalary'", 53, 5);
        BAssertUtil.validateError(result, i++,
                                  "undefined function 'incrementSalary' in object 'Person'", 53, 5);
        BAssertUtil.validateError(result, i++,
                                  "attempt to refer to non-accessible symbol 'Person.decrementAndUpdateSalary'", 54,
                                  13);
        BAssertUtil.validateError(result, i,
                                  "undefined function 'decrementAndUpdateSalary' in object 'Person'", 54, 13);
    }

    @Test
    public void testAbstractClientObject() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/abstract_client_object_method.bal");
        BValue[] returns = BRunUtil.invoke(compileResult, "testAbstractClientObject");
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 15000);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 12500);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 15000);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 12500);
    }

    @Test(description = "Test initialization of object union")
    @SuppressWarnings("unchecked")
    public void testUnionTypeObjectInit() {
        CompileResult objectTypeUnion = BCompileUtil.compile("test-src/object/object_type_union.bal");

        BValue[] a = BRunUtil.invoke(objectTypeUnion, "getObj4");
        BMap<String, BValue> aMap = (BMap<String, BValue>) a[0];
        Assert.assertEquals(((BInteger) aMap.get("val")).intValue(), 4);

        BValue[] ab = BRunUtil.invoke(objectTypeUnion, "getObj0");
        BMap<String, BValue> bMap = (BMap<String, BValue>) ab[0];
        Assert.assertEquals(((BInteger) bMap.get("val")).intValue(), 0);

        BValue[] tupple = BRunUtil.invoke(objectTypeUnion, "getLocals");
        BMap<String, BValue> localObj4 = (BMap<String, BValue>) tupple[0];
        Assert.assertEquals(((BInteger) localObj4.get("val")).intValue(), 4);

        BMap<String, BValue> localObj0 = (BMap<String, BValue>) tupple[1];
        Assert.assertEquals(((BInteger) localObj0.get("val")).intValue(), 0);

        BMap<String, BValue> localObj3 = (BMap<String, BValue>) tupple[2];
        Assert.assertEquals(((BInteger) localObj3.get("val")).intValue(), 3);
    }

    @Test(description = "Test initialization of object union with union members of mixed types")
    @SuppressWarnings("unchecked")
    public void testUnionTypeObjectInitMixedMember() {
        CompileResult objectTypeUnion = BCompileUtil.compile("test-src/object/object_type_union.bal");

        BValue[] a = BRunUtil.invoke(objectTypeUnion, "getMixedUnionMembers");
        BMap<String, BValue> aMap = (BMap<String, BValue>) a[0];
        Assert.assertEquals(((BInteger) aMap.get("val")).intValue(), 0);
    }

    @Test(description = "Test return new() when return type is a union")
    @SuppressWarnings("unchecked")
    public void testUnionTypeOReturnType() {
        CompileResult objectTypeUnion = BCompileUtil.compile("test-src/object/object_type_union.bal");

        BValue[] a = BRunUtil.invoke(objectTypeUnion, "returnDifferentObectInit1");
        BMap<String, BValue> aMap = (BMap<String, BValue>) a[0];
        Assert.assertEquals(((BInteger) aMap.get("age")).intValue(), 5);
    }

    @Test(description = "Test choosing union member differentiating on rest param type")
    @SuppressWarnings("unchecked")
    public void testUnionTypeSelectOnRestParamType() {
        CompileResult objectTypeUnion = BCompileUtil.compile("test-src/object/object_type_union.bal");

        BValue[] a = BRunUtil.invoke(objectTypeUnion, "selectOnRestParam");
        BMap<String, BValue> aMap = (BMap<String, BValue>) a[0];
        Assert.assertEquals(((BInteger) aMap.get("val")).intValue(), 5);

        BValue[] b = BRunUtil.invoke(objectTypeUnion, "selectOnRestParamInReturnType");
        BMap<String, BValue> retChoose = (BMap<String, BValue>) b[0];
        Assert.assertEquals(((BInteger) retChoose.get("val")).intValue(), 5);
    }

    @Test(dataProvider = "missingNativeImplFiles")
    public void testObjectWithMissingNativeImpl(String filePath) {
        try {
            BCompileUtil.compile(filePath);
        } catch (BLangRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("native function not available: Person.printName"));
            return;
        }
        Assert.fail("expected compilation to fail due to missing external implementation");
    }

    @Test(description = "Test invoking object inits with union params in another object's function")
    public void testUnionsAsAnInitParam() {
        CompileResult objectTypeUnion = BCompileUtil.compile("test-src/object/object_type_union.bal");
        BValue[] a = BRunUtil.invoke(objectTypeUnion, "testUnionsAsAnInitParam");
        BMap<String, BValue> foo = (BMap<String, BValue>) a[0];
        Assert.assertEquals(((BMap) foo.get("bar")).get("p").stringValue(), "{name:\"John Doe\"}");
    }

    @Test(description = "Negative test for object union type inference")
    public void testNegativeUnionTypeInit() {
        CompileResult resultNegative = BCompileUtil.compile("test-src/object/object_type_union_negative.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 4);
        BAssertUtil.validateError(resultNegative, 0, "ambiguous type 'Obj|Obj2|Obj3|Obj4'", 48, 25);
        BAssertUtil.validateError(resultNegative, 1, "ambiguous type 'Obj|Obj2|Obj3|Obj4'", 49, 25);
        BAssertUtil.validateError(resultNegative, 2, "cannot infer type of the object from 'Obj|Obj2|Obj3|Obj4'",
                                  50, 46);
        BAssertUtil.validateError(resultNegative, 3, "cannot infer type of the object from 'Bar?'", 71, 20);
    }

    @DataProvider
    public Object[][] missingNativeImplFiles() {
        return new Object[][]{
                {"test-src/object/object_with_missing_native_impl.bal"},
                {"test-src/object/object_with_missing_native_impl_2.bal"}
        };
    }
}
