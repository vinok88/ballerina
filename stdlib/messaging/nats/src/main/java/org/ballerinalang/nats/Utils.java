/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nats;

import io.nats.client.Message;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.JSONParser;
import org.ballerinalang.jvm.JSONUtils;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.DecimalValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;

import java.nio.charset.StandardCharsets;

/**
 * Utilities for producing and consuming via NATS sever.
 */
public class Utils {

    public static ErrorValue createNatsError(String nuid, String detailedErrorMessage) {
        MapValue<String, Object> errorDetailRecord = BallerinaValues
                .createRecordValue(Constants.NATS_PACKAGE_ID, Constants.NATS_ERROR_DETAIL_RECORD);
        MapValue<String, Object> populatedDetailRecord = BallerinaValues
                .createRecord(errorDetailRecord, nuid, detailedErrorMessage);
        return BallerinaErrors.createError(Constants.NATS_ERROR_CODE, populatedDetailRecord);
    }

    public static ErrorValue createNatsError(String detailedErrorMessage) {
        MapValue<String, Object> errorDetailRecord = BallerinaValues
                .createRecordValue(Constants.NATS_PACKAGE_ID, Constants.NATS_ERROR_DETAIL_RECORD);
        errorDetailRecord.put("message", detailedErrorMessage);
        return BallerinaErrors.createError(Constants.NATS_ERROR_CODE, errorDetailRecord);
    }

    public static Object bindDataToIntendedType(byte[] data, BType intendedType) {
        int dataParamTypeTag = intendedType.getTag();
        Object dispatchedData;
        switch (dataParamTypeTag) {
            case TypeTags.STRING_TAG:
                dispatchedData = new String(data, StandardCharsets.UTF_8);
                break;
            case TypeTags.JSON_TAG:
                try {
                    dispatchedData = JSONParser.parse(new String(data, StandardCharsets.UTF_8));
                } catch (BallerinaException e) {
                    throw createNatsError("Error occurred in converting message content to json: " +
                            e.getMessage());
                }
                break;
            case TypeTags.INT_TAG:
                dispatchedData = Integer.valueOf(new String(data, StandardCharsets.UTF_8));
                break;
            case TypeTags.BOOLEAN_TAG:
                dispatchedData = Boolean.valueOf(new String(data, StandardCharsets.UTF_8));
                break;
            case TypeTags.FLOAT_TAG:
                dispatchedData = Double.valueOf(new String(data, StandardCharsets.UTF_8));
                break;
            case TypeTags.DECIMAL_TAG:
                dispatchedData = new DecimalValue(new String(data, StandardCharsets.UTF_8));
                break;
            case TypeTags.ARRAY_TAG:
                dispatchedData = new ArrayValue(data);
                break;
            case TypeTags.XML_TAG:
                dispatchedData = XMLFactory.parse(new String(data, StandardCharsets.UTF_8));
                break;
            case TypeTags.RECORD_TYPE_TAG:
                dispatchedData = JSONUtils.convertJSONToRecord(JSONParser.parse(new String(data,
                                StandardCharsets.UTF_8)), (BRecordType) intendedType);
                break;
            default:
                throw Utils.createNatsError("Unable to find a supported data type to bind the message data");
        }
        return dispatchedData;
    }

    public static ObjectValue getMessageObject(Message message) {
        ObjectValue msgObj;
        if (message != null) {
            ArrayValue msgData = new ArrayValue(message.getData());
            msgObj = BallerinaValues.createObjectValue(Constants.NATS_PACKAGE_ID,
                    Constants.NATS_MESSAGE_OBJ_NAME, message.getSubject(), msgData, message.getReplyTo());
        } else {
            ArrayValue msgData = new ArrayValue(new byte[0]);
            msgObj = BallerinaValues.createObjectValue(Constants.NATS_PACKAGE_ID,
                    Constants.NATS_MESSAGE_OBJ_NAME, "", msgData, "");
        }
        return msgObj;
    }

    public static byte[] convertDataIntoByteArray(Object data) {
        BType dataType = TypeChecker.getType(data);
        int typeTag = dataType.getTag();
        if (typeTag == org.wso2.ballerinalang.compiler.util.TypeTags.STRING) {
            return ((String) data).getBytes(StandardCharsets.UTF_8);
        } else {
            return ((ArrayValue) data).getBytes();
        }
    }

    public static AttachedFunction getAttachedFunction(ObjectValue serviceObject, String functionName) {
        AttachedFunction function = null;
        AttachedFunction[] resourceFunctions = serviceObject.getType().getAttachedFunctions();
        for (AttachedFunction resourceFunction : resourceFunctions) {
            if (functionName.equals(resourceFunction.getName())) {
                function = resourceFunction;
                break;
            }
        }
        return function;
    }
}
