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
package org.ballerinalang.langserver.common;

import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.SymbolInfo;

import java.util.List;

/**
 * Common Keys related to all the language server operation contexts.
 */
public class CommonKeys {
    private CommonKeys() {
    }

    // Start non letter symbol keys
    public static final String OPEN_BRACE_KEY = "{";

    public static final String CLOSE_BRACE_KEY = "}";

    public static final String OPEN_PARENTHESES_KEY = "(";

    public static final String CLOSE_PARENTHESES_KEY = ")";

    public static final String OPEN_BRACKET_KEY = "[";

    public static final String CLOSE_BRACKET_KEY = "]";

    public static final String PKG_DELIMITER_KEYWORD = ":";

    public static final String SEMI_COLON_SYMBOL_KEY = ";";

    public static final String GT_SYMBOL_KEY = ">";

    public static final String LT_SYMBOL_KEY = "<";

    public static final String DOLLAR_SYMBOL_KEY = "$";
    // End non letter symbol keys

    public static final String ITR_OP_LAMBDA_PARAM_REPLACE_TOKEN = "%params%";

    public static final String ITR_OP_LAMBDA_KEY_REPLACE_TOKEN = "%key%";

    public static final String ITR_OP_LAMBDA_VALUE_REPLACE_TOKEN = "%value%";

    public static final String FUNCTION_KEYWORD_KEY = "function";

    public static final String RESOURCE_KEYWORD_KEY = "resource";

    public static final String SERVICE_KEYWORD_KEY = "service";

    public static final String OBJECT_KEYWORD_KEY = "object";

    public static final String RECORD_KEYWORD_KEY = "record";

    public static final String SELF_KEYWORD_KEY = "self";

    public static final String NEW_KEYWORD_KEY = "new";
    
    public static final String SLASH_KEYWORD_KEY = "/";
    
    public static final String BALLERINA_KW = "ballerina";
    
    public static final String BUILTIN_KW = "builtin";

    public static final LSContext.Key<List<SymbolInfo>> VISIBLE_SYMBOLS_KEY = new LSContext.Key<>();
}
