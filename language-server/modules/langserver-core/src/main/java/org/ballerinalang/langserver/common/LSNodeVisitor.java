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

import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS.BLangLocalXMLNS;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS.BLangPackageXMLNS;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFunctionClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangGroupBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangHaving;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLimit;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderByVariable;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOutputRateLimit;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangPatternClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangPatternStreamingEdgeInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangPatternStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectExpression;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSetAssignment;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangStreamAction;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangTableQuery;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhere;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWindow;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWithinClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckPanickedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess.BLangStructFunctionVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangArrayAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangJSONAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangMapAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangStructFieldAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangTupleAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangXMLAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BFunctionPointerInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BLangActionInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsLikeExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownDocumentationLine;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownReturnParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression.BLangMatchExprPatternClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangJSONLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangMapLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangStreamLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangStructLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangFieldVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangFunctionVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangLocalVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangPackageVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangTypeLoad;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableQueryExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTrapExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTupleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitForAllExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerFlushExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerSyncSendExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttributeAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLSequenceLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAbort;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForever;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStreamingQueryStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;

/**
 * Common node visitor to override and remove assertion errors from BLangNodeVisitor methods.
 */
public class LSNodeVisitor extends BLangNodeVisitor {
    @Override
    public void visit(BLangPackage pkgNode) {
        // No implementation
    }

    @Override
    public void visit(BLangTestablePackage testablePkgNode) {
        // No implementation
    }

    @Override
    public void visit(BLangCompilationUnit compUnit) {
        // No implementation
    }

    @Override
    public void visit(BLangImportPackage importPkgNode) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        // No implementation
    }

    @Override
    public void visit(BLangFunction funcNode) {
        // No implementation
    }

    @Override
    public void visit(BLangService serviceNode) {
        // No implementation
    }

    @Override
    public void visit(BLangResource resourceNode) {
        // No implementation
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        // No implementation
    }

    @Override
    public void visit(BLangConstant constant) {
        // No implementation
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        // No implementation
    }

    @Override
    public void visit(BLangWorker workerNode) {
        // No implementation
    }

    @Override
    public void visit(BLangEndpoint endpointNode) {
        // No implementation
    }

    @Override
    public void visit(BLangIdentifier identifierNode) {
        // No implementation
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {
        // No implementation
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        // No implementation
    }

    // Statements
    @Override
    public void visit(BLangBlockStmt blockNode) {
        // No implementation
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        // No implementation
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        // No implementation
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignNode) {
        // No implementation
    }

    @Override
    public void visit(BLangAbort abortNode) {
        // No implementation
    }

    @Override
    public void visit(BLangRetry retryNode) {
        // No implementation
    }

    @Override
    public void visit(BLangContinue continueNode) {
        // No implementation
    }

    @Override
    public void visit(BLangBreak breakNode) {
        // No implementation
    }

    @Override
    public void visit(BLangReturn returnNode) {
        // No implementation
    }

    @Override
    public void visit(BLangThrow throwNode) {
        // No implementation
    }

    @Override
    public void visit(BLangPanic panicNode) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        // No implementation
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        // No implementation
    }

    @Override
    public void visit(BLangIf ifNode) {
        // No implementation
    }

    @Override
    public void visit(BLangMatch matchNode) {
        // No implementation
    }

    @Override
    public void visit(BLangMatch.BLangMatchTypedBindingPatternClause patternClauseNode) {
        // No implementation
    }

    @Override
    public void visit(BLangForeach foreach) {
        // No implementation
    }

    @Override
    public void visit(BLangWhile whileNode) {
        // No implementation
    }

    @Override
    public void visit(BLangLock lockNode) {
        // No implementation
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        // No implementation
    }

    @Override
    public void visit(BLangTryCatchFinally tryNode) {
        // No implementation
    }

    @Override
    public void visit(BLangTupleDestructure stmt) {
        // No implementation
    }

    @Override
    public void visit(BLangRecordDestructure stmt) {
        // No implementation
    }

    @Override
    public void visit(BLangErrorDestructure stmt) {
        // No implementation
    }

    @Override
    public void visit(BLangCatch catchNode) {
        // No implementation
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        // No implementation
    }

    @Override
    public void visit(BLangOrderBy orderBy) {
        // No implementation
    }

    @Override
    public void visit(BLangOrderByVariable orderByVariable) {
        // No implementation
    }

    @Override
    public void visit(BLangLimit limit) {
        // No implementation
    }

    @Override
    public void visit(BLangGroupBy groupBy) {
        // No implementation
    }

    @Override
    public void visit(BLangHaving having) {
        // No implementation
    }

    @Override
    public void visit(BLangSelectExpression selectExpression) {
        // No implementation
    }

    @Override
    public void visit(BLangSelectClause selectClause) {
        // No implementation
    }

    @Override
    public void visit(BLangWhere whereClause) {
        // No implementation
    }

    @Override
    public void visit(BLangStreamingInput streamingInput) {
        // No implementation
    }

    @Override
    public void visit(BLangJoinStreamingInput joinStreamingInput) {
        // No implementation
    }

    @Override
    public void visit(BLangTableQuery tableQuery) {
        // No implementation
    }

    @Override
    public void visit(BLangStreamAction streamAction) {
        // No implementation
    }

    @Override
    public void visit(BLangFunctionClause functionClause) {
        // No implementation
    }

    @Override
    public void visit(BLangSetAssignment setAssignmentClause) {
        // No implementation
    }

    @Override
    public void visit(BLangPatternStreamingEdgeInput patternStreamingEdgeInput) {
        // No implementation
    }

    @Override
    public void visit(BLangWindow windowClause) {
        // No implementation
    }

    @Override
    public void visit(BLangPatternStreamingInput patternStreamingInput) {
        // No implementation
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        // No implementation
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        // No implementation
    }

    @Override
    public void visit(BLangForever foreverStatement) {
        // No implementation
    }


    // Expressions

    @Override
    public void visit(BLangLiteral literalExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangTableLiteral tableLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructorExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangTupleVarRef varRefExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangRecordVarRef varRefExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangErrorVarRef varRefExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangTypeInit connectorInitExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangActionInvocation actionInvocationExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangInvocation.BLangBuiltInMethodInvocation builtInMethodInvocation) {
        // No implementation
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangWaitExpr awaitExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangTrapExpr trapExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangTypedescExpr accessExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {
        // No implementation
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        // No implementation
    }

    @Override
    public void visit(BLangArrowFunction bLangArrowFunction) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangIntRangeExpression intRangeExpression) {
        // No implementation
    }

    @Override
    public void visit(BLangTableQueryExpression tableQueryExpression) {
        // No implementation
    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {
        // No implementation
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
        // No implementation
    }

    @Override
    public void visit(BLangStreamingQueryStatement streamingQueryStatement) {
        // No implementation
    }

    @Override
    public void visit(BLangWithinClause withinClause) {
        // No implementation
    }

    @Override
    public void visit(BLangOutputRateLimit outputRateLimit) {
        // No implementation
    }

    @Override
    public void visit(BLangPatternClause patternClause) {
        // No implementation
    }

    @Override
    public void visit(BLangIsAssignableExpr assignableExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangMatchExpression bLangMatchExpression) {
        // No implementation
    }

    @Override
    public void visit(BLangMatchExprPatternClause bLangMatchExprPatternClause) {
        // No implementation
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangErrorConstructorExpr errorConstructorExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangIsLikeExpr typeTestExpr) {
        // No implementation
    }

    // Type nodes

    @Override
    public void visit(BLangValueType valueType) {
        // No implementation
    }

    @Override
    public void visit(BLangArrayType arrayType) {
        // No implementation
    }

    @Override
    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
        // No implementation
    }

    @Override
    public void visit(BLangConstrainedType constrainedType) {
        // No implementation
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
        // No implementation
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode) {
        // No implementation
    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode) {
        // No implementation
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        // No implementation
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        // No implementation
    }

    @Override
    public void visit(BLangFiniteTypeNode finiteTypeNode) {
        // No implementation
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {
        // No implementation
    }

    @Override
    public void visit(BLangErrorType errorType) {
        // No implementation
    }


    // expressions that will used only from the Desugar phase

    @Override
    public void visit(BLangLocalVarRef localVarRef) {
        // No implementation
    }

    @Override
    public void visit(BLangFieldVarRef fieldVarRef) {
        // No implementation
    }

    @Override
    public void visit(BLangPackageVarRef packageVarRef) {
        // No implementation
    }

    @Override
    public void visit(BLangFunctionVarRef functionVarRef) {
        // No implementation
    }

    @Override
    public void visit(BLangTypeLoad typeLoad) {
        // No implementation
    }

    @Override
    public void visit(BLangStructFieldAccessExpr fieldAccessExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangStructFunctionVarRef functionVarRef) {
        // No implementation
    }

    @Override
    public void visit(BLangMapAccessExpr mapKeyAccessExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangArrayAccessExpr arrayIndexAccessExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangTupleAccessExpr arrayIndexAccessExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLAccessExpr xmlIndexAccessExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangJSONLiteral jsonLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangMapLiteral mapLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangStructLiteral structLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangStreamLiteral streamLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangRecordLiteral.BLangChannelLiteral channelLiteral) {
        // No implementation
    }

    @Override
    public void visit(BFunctionPointerInvocation bFunctionPointerInvocation) {
        // No implementation
    }

    @Override
    public void visit(BLangInvocation.BLangAttachedFunctionInvocation iExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangJSONArrayLiteral jsonArrayLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangJSONAccessExpr jsonAccessExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangLocalXMLNS xmlnsNode) {
        // No implementation
    }

    @Override
    public void visit(BLangPackageXMLNS xmlnsNode) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLSequenceLiteral bLangXMLSequenceLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangStatementExpression bLangStatementExpression) {
        // No implementation
    }

    @Override
    public void visit(BLangMarkdownDocumentationLine bLangMarkdownDocumentationLine) {
        // No implementation
    }

    @Override
    public void visit(BLangMarkdownParameterDocumentation bLangDocumentationParameter) {
        // No implementation
    }

    @Override
    public void visit(BLangMarkdownReturnParameterDocumentation bLangMarkdownReturnParameterDocumentation) {
        // No implementation
    }

    @Override
    public void visit(BLangMarkdownDocumentation bLangMarkdownDocumentation) {
        // No implementation
    }

    @Override
    public void visit(BLangTupleVariable bLangTupleVariable) {
        // No implementation
    }

    @Override
    public void visit(BLangTupleVariableDef bLangTupleVariableDef) {
        // No implementation
    }

    @Override
    public void visit(BLangRecordVariable bLangRecordVariable) {
        // No implementation
    }

    @Override
    public void visit(BLangRecordVariableDef bLangRecordVariableDef) {
        // No implementation
    }

    @Override
    public void visit(BLangErrorVariable bLangErrorVariable) {
        // No implementation
    }

    @Override
    public void visit(BLangErrorVariableDef bLangErrorVariableDef) {
        // No implementation
    }

    @Override
    public void visit(BLangMatch.BLangMatchStaticBindingPatternClause bLangMatchStmtStaticBindingPatternClause) {
        // No implementation
    }

    @Override
    public void visit(
            BLangMatch.BLangMatchStructuredBindingPatternClause bLangMatchStmtStructuredBindingPatternClause) {
        // No implementation
    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangWaitForAllExpr waitForAllExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitLiteral waitLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangCheckPanickedExpr checkPanickedExpr) {
        // no implementation
    }

    @Override
    public void visit(BLangNumericLiteral literalExpr) {
        // no implementation
    }

    @Override
    public void visit(BLangGroupExpr groupExpr) {
        // no implementation
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangTupleLiteral tupleLiteral) {
        // no implementation
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangArrayLiteral arrayLiteral) {
        // no implementation
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangConstRef constRef) {
        // no implementation
    }
}
