// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
import ballerina/io;

function testArrayWithErrors1() returns string {
    error<string, map<string|boolean>> err1 = error("Error One", { message: "msgOne", fatal: true });
    error<string, map<string|boolean>> err2 = error("Error Two", { message: "msgTwo", fatal: false });
    error<string, map<string|boolean>> err3 = error("Error Three", { message: "msgThree", fatal: true });
    error<string, map<string|boolean>>?[3] errorArray = [err1, err2, err3];

    string result1 = "";
    foreach var error(reason, detail) in errorArray { // invalid error variable; expecting an error type but found 'error?' in type definition

    }
    return result1;
}

function testArrayWithErrors2() returns string {
    error<string, map<string|boolean>> err1 = error("Error One", { message: "msgOne", fatal: true });
    error<string, map<string|boolean>> err2 = error("Error Two", { message: "msgTwo", fatal: false });
    error<string, map<string|boolean>> err3 = error("Error Three", { message: "msgThree", fatal: true });
    error<string, map<string|boolean>>[3] errorArray = [err1, err2, err3];

    string result1 = "";
    foreach error<string, map<string>> error(reason, detail) in errorArray { // incompatible types: expected 'error', found 'error'

    }
    return result1;
}

function testArrayWithErrors3() returns string {
    error<string, map<string|boolean>> err1 = error("Error One", { message: "msgOne", fatal: true });
    error<string, map<string|boolean>> err2 = error("Error Two", { message: "msgTwo", fatal: false });
    error<string, map<string|boolean>> err3 = error("Error Three", { message: "msgThree", fatal: true });
    error<string, map<string|boolean>>[3] errorArray = [err1, err2, err3];

    string result1 = "";
    foreach var error(reason, detail) in errorArray {
        map<string> x = detail; // incompatible types: expected 'map<string>', found 'map<string|boolean>'
    }

    foreach var error(reason1, { message }) in errorArray {
        string|boolean x = message; // incompatible types: expected 'string|boolean', found 'string|boolean?'
    }
    return result1;
}

function testMapWithErrors1() returns string {
    error<string, map<string|boolean>> err1 = error("Error One", { message: "msgOne", fatal: true });
    error<string, map<string|boolean>> err2 = error("Error Two", { message: "msgTwo", fatal: false });
    error<string, map<string|boolean>> err3 = error("Error Three", { message: "msgThree", fatal: true });
    map<error<string, map<string|boolean>>?> errMap = { a: err1, b: err2, c: err3 };

    string result1 = "";
    foreach var error(reason, detail) in errMap { // invalid error variable; expecting an error type but found '(string,error?)' in type definition

    }

    return result1;
}

function testMapWithErrors2() returns string {
    error<string, map<string|boolean>> err1 = error("Error One", { message: "msgOne", fatal: true });
    error<string, map<string|boolean>> err2 = error("Error Two", { message: "msgTwo", fatal: false });
    error<string, map<string|boolean>> err3 = error("Error Three", { message: "msgThree", fatal: true });
    map<error<string, map<string|boolean>>> errMap = { a: err1, b: err2, c: err3 };


    string result1 = "";
    foreach [string, error<string, map<string>>] [key, error(reason, detail)] in errMap { // incompatible types: expected '(string,error)', found '(string,error)'

    }
    return result1;
}

function testMapWithErrors3() returns string {
    error<string, map<string|boolean>> err1 = error("Error One", { message: "msgOne", fatal: true });
    error<string, map<string|boolean>> err2 = error("Error Two", { message: "msgTwo", fatal: false });
    error<string, map<string|boolean>> err3 = error("Error Three", { message: "msgThree", fatal: true });
    map<error<string, map<string|boolean>>> errMap = { a: err1, b: err2, c: err3 };

    string result1 = "";
    foreach var [key, error(reason, detail)] in errMap {
        map<string> x = detail; // incompatible types: expected 'map<string>', found 'map<string|boolean>'
    }

    foreach var [key, error(reason1, { message })] in errMap {
        string|boolean x = message; // incompatible types: expected 'string|boolean', found 'string|boolean?'
    }
    return result1;
}
