/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.AttachPoint;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.ballerinalang.model.tree.types.BuiltInReferenceTypeNode;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BErrorTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BServiceSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable.BLangRecordVariableKeyValue;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAccessExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef.BLangRecordVarRefKeyValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTupleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch.BLangMatchStaticBindingPatternClause;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch.BLangMatchStructuredBindingPatternClause;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.AttachPoints;
import org.wso2.ballerinalang.util.Flags;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import static org.ballerinalang.model.tree.NodeKind.LITERAL;
import static org.ballerinalang.model.tree.NodeKind.NUMERIC_LITERAL;
import static org.ballerinalang.model.tree.NodeKind.RECORD_LITERAL_EXPR;

/**
 * @since 0.94
 */
public class SemanticAnalyzer extends BLangNodeVisitor {

    private static final CompilerContext.Key<SemanticAnalyzer> SYMBOL_ANALYZER_KEY =
            new CompilerContext.Key<>();
    private static final String ANONYMOUS_RECORD_NAME = "anonymous-record";
    private static final String NULL_LITERAL = "null";
    private static final String LEFT_BRACE = "{";
    private static final String RIGHT_BRACE = "}";
    private static final String SPACE = " ";
    public static final String COLON = ":";

    private SymbolTable symTable;
    private SymbolEnter symbolEnter;
    private Names names;
    private SymbolResolver symResolver;
    private TypeChecker typeChecker;
    private Types types;
    private StreamsQuerySemanticAnalyzer streamsQuerySemanticAnalyzer;
    private BLangDiagnosticLog dlog;
    private TypeNarrower typeNarrower;
    private ConstantAnalyzer constantAnalyzer;
    private ConstantValueResolver constantValueResolver;

    private SymbolEnv env;
    private BType expType;
    private DiagnosticCode diagCode;
    private BType resType;

    // Stack holding the fall-back environments. fall-back env is the env to go back
    // after visiting the current env.
    private Stack<SymbolEnv> prevEnvs = new Stack<>();

    public static SemanticAnalyzer getInstance(CompilerContext context) {
        SemanticAnalyzer semAnalyzer = context.get(SYMBOL_ANALYZER_KEY);
        if (semAnalyzer == null) {
            semAnalyzer = new SemanticAnalyzer(context);
        }

        return semAnalyzer;
    }

    public SemanticAnalyzer(CompilerContext context) {
        context.put(SYMBOL_ANALYZER_KEY, this);

        this.symTable = SymbolTable.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
        this.names = Names.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.typeChecker = TypeChecker.getInstance(context);
        this.types = Types.getInstance(context);
        this.streamsQuerySemanticAnalyzer = StreamsQuerySemanticAnalyzer.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.typeNarrower = TypeNarrower.getInstance(context);
        this.constantAnalyzer = ConstantAnalyzer.getInstance(context);
        this.constantValueResolver = ConstantValueResolver.getInstance(context);
    }

    public BLangPackage analyze(BLangPackage pkgNode) {
        pkgNode.accept(this);
        return pkgNode;
    }


    // Visitor methods

    public void visit(BLangPackage pkgNode) {
        if (pkgNode.completedPhases.contains(CompilerPhase.TYPE_CHECK)) {
            return;
        }
        SymbolEnv pkgEnv = this.symTable.pkgEnvMap.get(pkgNode.symbol);

        // Visit constants first.
        pkgNode.topLevelNodes.stream().filter(pkgLevelNode -> pkgLevelNode.getKind() == NodeKind.CONSTANT)
                .forEach(constant -> analyzeDef((BLangNode) constant, pkgEnv));
        this.constantValueResolver.resolve(pkgNode.constants);

        pkgNode.topLevelNodes.stream().filter(pkgLevelNode -> pkgLevelNode.getKind() != NodeKind.CONSTANT)
                .filter(pkgLevelNode -> !(pkgLevelNode.getKind() == NodeKind.FUNCTION
                        && ((BLangFunction) pkgLevelNode).flagSet.contains(Flag.LAMBDA)))
                .forEach(topLevelNode -> analyzeDef((BLangNode) topLevelNode, pkgEnv));

        while (pkgNode.lambdaFunctions.peek() != null) {
            BLangLambdaFunction lambdaFunction = pkgNode.lambdaFunctions.poll();
            BLangFunction function = lambdaFunction.function;
            lambdaFunction.type = function.symbol.type;
            analyzeDef(lambdaFunction.function, lambdaFunction.cachedEnv);
        }

        pkgNode.getTestablePkgs().forEach(testablePackage -> visit((BLangPackage) testablePackage));
        pkgNode.completedPhases.add(CompilerPhase.TYPE_CHECK);
    }

    public void visit(BLangXMLNS xmlnsNode) {
        xmlnsNode.type = symTable.stringType;

        // Namespace node already having the symbol means we are inside an init-function,
        // and the symbol has already been declared by the original statement.
        if (xmlnsNode.symbol == null) {
            symbolEnter.defineNode(xmlnsNode, env);
        }

        typeChecker.checkExpr(xmlnsNode.namespaceURI, env, symTable.stringType);
    }

    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        analyzeNode(xmlnsStmtNode.xmlnsDecl, env);
    }

    public void visit(BLangFunction funcNode) {
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, env);
        //set function param flag to final
        funcNode.symbol.params.forEach(param -> param.flags |= Flags.FUNCTION_FINAL);

        if (!funcNode.flagSet.contains(Flag.WORKER)) {
            // annotation validation for workers is done for the invocation.
            funcNode.annAttachments.forEach(annotationAttachment -> {
                if (Symbols.isFlagOn(funcNode.symbol.flags, Flags.RESOURCE)) {
                    annotationAttachment.attachPoints.add(AttachPoint.Point.RESOURCE);
                } else if (funcNode.attachedFunction) {
                    annotationAttachment.attachPoints.add(AttachPoint.Point.OBJECT_METHOD);
                }
                annotationAttachment.attachPoints.add(AttachPoint.Point.FUNCTION);
                this.analyzeDef(annotationAttachment, funcEnv);
            });
            validateAnnotationAttachmentCount(funcNode.annAttachments);
        }

        if (funcNode.returnTypeNode != null) {
            funcNode.returnTypeAnnAttachments.forEach(annotationAttachment -> {
                annotationAttachment.attachPoints.add(AttachPoint.Point.RETURN);
                this.analyzeDef(annotationAttachment, funcEnv);
            });
            validateAnnotationAttachmentCount(funcNode.returnTypeAnnAttachments);
        }

        if (Symbols.isNative(funcNode.symbol)) {
            funcNode.externalAnnAttachments.forEach(annotationAttachment -> {
                annotationAttachment.attachPoints.add(AttachPoint.Point.EXTERNAL);
                this.analyzeDef(annotationAttachment, funcEnv);
            });
            validateAnnotationAttachmentCount(funcNode.externalAnnAttachments);
        }

        for (BLangSimpleVariable param : funcNode.requiredParams) {
            symbolEnter.defineExistingVarSymbolInEnv(param.symbol, funcNode.clonedEnv);
            this.analyzeDef(param, funcNode.clonedEnv);
        }
        if (funcNode.restParam != null) {
            symbolEnter.defineExistingVarSymbolInEnv(funcNode.restParam.symbol, funcNode.clonedEnv);
            this.analyzeDef(funcNode.restParam, funcNode.clonedEnv);
        }

        validateObjectAttachedFunction(funcNode);

        // Check for native functions
        if (Symbols.isNative(funcNode.symbol) || funcNode.interfaceFunction) {
            if (funcNode.body != null) {
                dlog.error(funcNode.pos, DiagnosticCode.EXTERN_FUNCTION_CANNOT_HAVE_BODY, funcNode.name);
            }
            return;
        }

        if (funcNode.body != null) {
            analyzeStmt(funcNode.body, funcEnv);
        }

        if (funcNode.anonForkName != null) {
            funcNode.symbol.enclForkName = funcNode.anonForkName;
        }

        this.processWorkers(funcNode, funcEnv);
    }

    private void processWorkers(BLangInvokableNode invNode, SymbolEnv invEnv) {
        if (invNode.workers.size() > 0) {
            invEnv.scope.entries.putAll(invNode.body.scope.entries);
            invNode.workers.forEach(e -> this.symbolEnter.defineNode(e, invEnv));
            invNode.workers.forEach(e -> analyzeNode(e, invEnv));
        }
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        if (typeDefinition.typeNode.getKind() == NodeKind.OBJECT_TYPE
                || typeDefinition.typeNode.getKind() == NodeKind.RECORD_TYPE
                || typeDefinition.typeNode.getKind() == NodeKind.ERROR_TYPE
                || typeDefinition.typeNode.getKind() == NodeKind.FINITE_TYPE_NODE) {
            analyzeDef(typeDefinition.typeNode, env);
        }

        typeDefinition.annAttachments.forEach(annotationAttachment -> {
            if (typeDefinition.typeNode.getKind() == NodeKind.OBJECT_TYPE) {
                annotationAttachment.attachPoints.add(AttachPoint.Point.OBJECT);
            }
            annotationAttachment.attachPoints.add(AttachPoint.Point.TYPE);

            annotationAttachment.accept(this);
        });
        validateAnnotationAttachmentCount(typeDefinition.annAttachments);
    }

    public void visit(BLangTypeConversionExpr conversionExpr) {
        conversionExpr.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachPoints.add(AttachPoint.Point.TYPE);
            if (conversionExpr.typeNode.getKind() == NodeKind.OBJECT_TYPE) {
                annotationAttachment.attachPoints.add(AttachPoint.Point.OBJECT);
            }

            annotationAttachment.accept(this);
        });
        validateAnnotationAttachmentCount(conversionExpr.annAttachments);
    }

    @Override
    public void visit(BLangFiniteTypeNode finiteTypeNode) {
        finiteTypeNode.valueSpace.forEach(val -> {
            if (val.type.tag == TypeTags.NIL && NULL_LITERAL.equals(((BLangLiteral) val).originalValue)) {
                dlog.error(val.pos, DiagnosticCode.INVALID_USE_OF_NULL_LITERAL);
            }
        });
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        SymbolEnv objectEnv = SymbolEnv.createTypeEnv(objectTypeNode, objectTypeNode.symbol.scope, env);

        boolean isAbstract = objectTypeNode.flagSet.contains(Flag.ABSTRACT);
        objectTypeNode.fields.forEach(field -> {
            analyzeDef(field, objectEnv);
            if (isAbstract) {
                if (field.flagSet.contains(Flag.PRIVATE)) {
                    this.dlog.error(field.pos, DiagnosticCode.PRIVATE_FIELD_ABSTRACT_OBJECT, field.symbol.name);
                }

                if (field.expr != null) {
                    this.dlog.error(field.expr.pos, DiagnosticCode.FIELD_WITH_DEFAULT_VALUE_ABSTRACT_OBJECT);
                }
            }
        });

        // Visit functions as they are not in the same scope/env as the object fields
        objectTypeNode.functions.forEach(func -> {
            analyzeDef(func, env);
            if (isAbstract && func.flagSet.contains(Flag.PRIVATE)) {
                this.dlog.error(func.pos, DiagnosticCode.PRIVATE_FUNC_ABSTRACT_OBJECT, func.name,
                        objectTypeNode.symbol.name);
            }
            if (isAbstract && func.flagSet.contains(Flag.NATIVE)) {
                this.dlog.error(func.pos, DiagnosticCode.EXTERN_FUNC_ABSTRACT_OBJECT, func.name,
                        objectTypeNode.symbol.name);
            }
            if (func.flagSet.contains(Flag.RESOURCE) && func.flagSet.contains(Flag.NATIVE)) {
                this.dlog.error(func.pos, DiagnosticCode.RESOURCE_FUNCTION_CANNOT_BE_EXTERN, func.name);
            }
        });

        // Validate the referenced functions that don't have implementations within the function.
        ((BObjectTypeSymbol) objectTypeNode.symbol).referencedFunctions
                .forEach(func -> validateReferencedFunction(objectTypeNode.pos, func, env));

        if (objectTypeNode.initFunction == null) {
            return;
        }

        if (objectTypeNode.initFunction.flagSet.contains(Flag.PRIVATE)) {
            this.dlog.error(objectTypeNode.initFunction.pos, DiagnosticCode.PRIVATE_OBJECT_CONSTRUCTOR,
                    objectTypeNode.symbol.name);
            return;
        }

        if (objectTypeNode.flagSet.contains(Flag.ABSTRACT)) {
            this.dlog.error(objectTypeNode.initFunction.pos, DiagnosticCode.ABSTRACT_OBJECT_CONSTRUCTOR,
                    objectTypeNode.symbol.name);
            return;
        }

        if (objectTypeNode.initFunction.flagSet.contains(Flag.NATIVE)) {
            this.dlog.error(objectTypeNode.initFunction.pos, DiagnosticCode.OBJECT_INIT_FUNCTION_CANNOT_BE_EXTERN,
                            objectTypeNode.symbol.name);
            return;
        }

        analyzeDef(objectTypeNode.initFunction, env);
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        SymbolEnv recordEnv = SymbolEnv.createTypeEnv(recordTypeNode, recordTypeNode.symbol.scope, env);
        recordTypeNode.fields.forEach(field -> analyzeDef(field, recordEnv));
        analyzeDef(recordTypeNode.initFunction, recordEnv);
        validateDefaultable(recordTypeNode);
    }

    @Override
    public void visit(BLangErrorType errorType) {
        BType reasonType = getReasonType(errorType);

        if (!types.isAssignable(reasonType, symTable.stringType)) {
            dlog.error(errorType.reasonType.pos, DiagnosticCode.INVALID_ERROR_REASON_TYPE, reasonType);
        } else if (errorType.reasonType != null) {
            validateModuleQualifiedReasons(errorType.reasonType.pos, reasonType);
        }

        if (errorType.detailType == null) {
            return;
        }

        BType detailType = errorType.detailType.type;
        if (!types.isValidErrorDetailType(detailType)) {
            dlog.error(errorType.detailType.pos, DiagnosticCode.INVALID_ERROR_DETAIL_TYPE, detailType,
                    symTable.detailType);
        }
    }

    private BType getReasonType(BLangErrorType errorType) {
        // Reason type not specified take default reason type.
        if (errorType.reasonType == null) {
            return symTable.stringType;
        }
        return errorType.reasonType.type;
    }

    private void validateModuleQualifiedReasons(DiagnosticPos pos, BType reasonType) {
        switch (reasonType.tag) {
            case TypeTags.STRING:
                return;
            case TypeTags.FINITE:
                BFiniteType finiteType = (BFiniteType) reasonType;
                for (BLangExpression expr : finiteType.valueSpace) {
                    validateModuleQualifiedReason(pos, (String) ((BLangLiteral) expr).value);
                }
                return;
            case TypeTags.UNION:
                ((BUnionType) reasonType).getMemberTypes().forEach(type -> validateModuleQualifiedReasons(pos, type));
        }
    }

    private void validateModuleQualifiedReason(DiagnosticPos pos, String reason) {
        if (!reason.startsWith(LEFT_BRACE)) {
            return;
        }

        PackageID currentPackageId = env.enclPkg.packageID;
        if (currentPackageId.isUnnamed || reason.contains(SPACE) ||
                !reason.startsWith(LEFT_BRACE.concat(currentPackageId.toString().split(COLON)[0])
                                           .concat(RIGHT_BRACE))) {
            dlog.warning(pos, DiagnosticCode.NON_MODULE_QUALIFIED_ERROR_REASON, reason);
        }
    }

    public void visit(BLangAnnotation annotationNode) {
        annotationNode.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachPoints.add(AttachPoint.Point.ANNOTATION);
            annotationAttachment.accept(this);
        });
        validateAnnotationAttachmentCount(annotationNode.annAttachments);
    }

    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        BSymbol symbol = this.symResolver.resolveAnnotation(annAttachmentNode.pos, env,
                names.fromString(annAttachmentNode.pkgAlias.getValue()),
                names.fromString(annAttachmentNode.getAnnotationName().getValue()));
        if (symbol == this.symTable.notFoundSymbol) {
            this.dlog.error(annAttachmentNode.pos, DiagnosticCode.UNDEFINED_ANNOTATION,
                    annAttachmentNode.getAnnotationName().getValue());
            return;
        }
        // Validate Attachment Point against the Annotation Definition.
        BAnnotationSymbol annotationSymbol = (BAnnotationSymbol) symbol;
        annAttachmentNode.annotationSymbol = annotationSymbol;
        if (annotationSymbol.maskedPoints > 0 &&
                !Symbols.isAttachPointPresent(annotationSymbol.maskedPoints,
                                              AttachPoints.asMask(annAttachmentNode.attachPoints))) {
            String msg = annAttachmentNode.attachPoints.stream()
                    .map(point -> point.name().toLowerCase())
                    .collect(Collectors.joining(", "));
            this.dlog.error(annAttachmentNode.pos, DiagnosticCode.ANNOTATION_NOT_ALLOWED, annotationSymbol, msg);
        }
        // Validate Annotation Attachment expression against Annotation Definition type.
        validateAnnotationAttachmentExpr(annAttachmentNode, annotationSymbol);
    }

    public void visit(BLangSimpleVariable varNode) {

        if (varNode.isDeclaredWithVar) {
            validateWorkerAnnAttachments(varNode.expr);
            handleDeclaredWithVar(varNode);
            transferForkFlag(varNode);
            return;
        }

        int ownerSymTag = env.scope.owner.tag;
        if ((ownerSymTag & SymTag.INVOKABLE) == SymTag.INVOKABLE) {
            // This is a variable declared in a function, an action or a resource
            // If the variable is parameter then the variable symbol is already defined
            if (varNode.symbol == null) {
                symbolEnter.defineNode(varNode, env);
                varNode.annAttachments.forEach(annotationAttachment -> {
                    annotationAttachment.attachPoints.add(AttachPoint.Point.VAR);
                    annotationAttachment.accept(this);
                });
            } else {
                varNode.annAttachments.forEach(annotationAttachment -> {
                    annotationAttachment.attachPoints.add(AttachPoint.Point.PARAMETER);
                    annotationAttachment.accept(this);
                });
            }
        } else {
            varNode.annAttachments.forEach(annotationAttachment -> {
                if (Symbols.isFlagOn(varNode.symbol.flags, Flags.LISTENER)) {
                    annotationAttachment.attachPoints.add(AttachPoint.Point.LISTENER);
                } else if (Symbols.isFlagOn(varNode.symbol.flags, Flags.SERVICE)) {
                    annotationAttachment.attachPoints.add(AttachPoint.Point.SERVICE);
                } else {
                    annotationAttachment.attachPoints.add(AttachPoint.Point.VAR);
                }
                annotationAttachment.accept(this);
            });
        }
        validateAnnotationAttachmentCount(varNode.annAttachments);

        validateWorkerAnnAttachments(varNode.expr);

        if (isIgnoredOrEmpty(varNode)) {
            // Fake symbol to prevent runtime failures down the line.
            varNode.symbol = new BVarSymbol(0, Names.IGNORE, env.enclPkg.packageID, symTable.anyType, env.scope.owner);
        }

        BType lhsType = varNode.symbol.type;
        varNode.type = lhsType;

        // Analyze the init expression
        BLangExpression rhsExpr = varNode.expr;
        if (rhsExpr == null) {
            if (lhsType.tag == TypeTags.ARRAY && typeChecker.isArrayOpenSealedType((BArrayType) lhsType)) {
                dlog.error(varNode.pos, DiagnosticCode.SEALED_ARRAY_TYPE_NOT_INITIALIZED);
            }
            return;
        }

        // Here we create a new symbol environment to catch self references by keep the current
        // variable symbol in the symbol environment
        // e.g. int a = x + a;
        SymbolEnv varInitEnv = SymbolEnv.createVarInitEnv(varNode, env, varNode.symbol);

        typeChecker.checkExpr(rhsExpr, varInitEnv, lhsType);
        if (Symbols.isFlagOn(varNode.symbol.flags, Flags.LISTENER) &&
                !types.checkListenerCompatibility(varNode.symbol.type)) {
            dlog.error(varNode.pos, DiagnosticCode.INVALID_LISTENER_VARIABLE, varNode.name);
        }

        transferForkFlag(varNode);
    }

    private void transferForkFlag(BLangSimpleVariable varNode) {
        // Transfer FORK flag to workers future value.
        if (varNode.expr != null && varNode.expr.getKind() == NodeKind.INVOCATION
                && varNode.flagSet.contains(Flag.WORKER)) {

            BLangInvocation expr = (BLangInvocation) varNode.expr;
            if (expr.name.value.startsWith("0") && (expr.symbol.flags & Flags.FORKED) == Flags.FORKED) {
                varNode.symbol.flags |= Flags.FORKED;
            }
        }
    }

    /**
     * Validate annotation attachment of the `start` action or workers.
     *
     * @param expr expression to be validated.
     */
    private void validateWorkerAnnAttachments(BLangExpression expr) {
        if (expr != null && expr.getKind() == NodeKind.INVOCATION && ((BLangInvocation) expr).async) {
            ((BLangInvocation) expr).annAttachments.forEach(annotationAttachment -> {
                annotationAttachment.attachPoints.add(AttachPoint.Point.WORKER);
                annotationAttachment.accept(this);
            });
            validateAnnotationAttachmentCount(((BLangInvocation) expr).annAttachments);
        }
    }

    public void visit(BLangRecordVariable varNode) {

        if (varNode.isDeclaredWithVar) {
            handleDeclaredWithVar(varNode);
            return;
        }

        if (varNode.type == null) {
            varNode.type = symResolver.resolveTypeNode(varNode.typeNode, env);
        }

        if (!validateRecordVariable(varNode)) {
            varNode.type = symTable.semanticError;
            return;
        }

        symbolEnter.defineNode(varNode, env);

        if (varNode.expr == null) {
            // we have no rhs to do type checking
            return;
        }

        typeChecker.checkExpr(varNode.expr, env, varNode.type);

    }

    public void visit(BLangTupleVariable varNode) {

        if (varNode.isDeclaredWithVar) {
            expType = resolveTupleType(varNode);
            handleDeclaredWithVar(varNode);
            return;
        }

        if (varNode.type == null) {
            varNode.type = symResolver.resolveTypeNode(varNode.typeNode, env);
        }

        if (!(checkTypeAndVarCountConsistency(varNode))) {
            varNode.type = symTable.semanticError;
            return;
        }

        symbolEnter.defineNode(varNode, env);

        if (varNode.expr == null) {
            // we have no rhs to do type checking
            return;
        }

        typeChecker.checkExpr(varNode.expr, env, varNode.type);
    }

    private BType resolveTupleType(BLangTupleVariable varNode) {
        List<BType> memberTypes = new ArrayList<>(varNode.memberVariables.size());
        for (BLangVariable memberVariable : varNode.memberVariables) {
            if (memberVariable.getKind() == NodeKind.TUPLE_VARIABLE) {
                memberTypes.add(resolveTupleType((BLangTupleVariable) memberVariable));
            } else {
                memberTypes.add(symTable.noType);
            }
        }
        return new BTupleType(memberTypes);
    }

    public void visit(BLangErrorVariable varNode) {
        // Error variable declarations (destructuring etc.)
        if (varNode.isDeclaredWithVar) {
            handleDeclaredWithVar(varNode);
            return;
        }

        if (varNode.type == null) {
            varNode.type = symResolver.resolveTypeNode(varNode.typeNode, env);
        }

        // match err1 { error(reason,....) => ... }
        // reason must be a const of subtype of string.
        // then we match the error with this specific reason.
        if (!varNode.reasonVarPrefixAvailable && varNode.type == null) {
            BErrorType errorType = new BErrorType(varNode.type.tsymbol, null, null);

            if (varNode.type.tag == TypeTags.UNION) {
                Set<BType> members = types.expandAndGetMemberTypesRecursive(varNode.type);
                List<BErrorType> errorMembers = members.stream()
                        .filter(m -> m.tag == TypeTags.ERROR)
                        .map(m -> (BErrorType) m)
                        .collect(Collectors.toList());

                if (errorMembers.isEmpty()) {
                    dlog.error(varNode.pos, DiagnosticCode.INVALID_ERROR_MATCH_PATTERN);
                    return;
                } else if (errorMembers.size() == 1) {
                    errorType.detailType = errorMembers.get(0).detailType;
                    errorType.reasonType = errorMembers.get(0).reasonType;
                } else {
                    errorType.detailType = symTable.detailType;
                    errorType.reasonType = symTable.stringType;
                }
                varNode.type = errorType;
            } else if (varNode.type.tag == TypeTags.ERROR) {
                errorType.detailType = ((BErrorType) varNode.type).detailType;
            }

            // Set error reason type.
            // For var error binding pattern, set reason type to string.
            // For error match pattern with const reason, set the reason type to provided const type.
            if (varNode.reasonMatchConst != null) {
                BTypeSymbol reasonConstTypeSymbol = new BTypeSymbol(SymTag.FINITE_TYPE,
                        Flags.PUBLIC, names.fromString(""), this.env.enclPkg.packageID, null, this.env.scope.owner);
                varNode.reasonMatchConst.type = symTable.stringType;
                typeChecker.checkExpr(varNode.reasonMatchConst, env);

                LinkedHashSet<BLangExpression> members = new LinkedHashSet<>();
                members.add(varNode.reasonMatchConst);
                errorType.reasonType = new BFiniteType(reasonConstTypeSymbol, members);
            } else {
                errorType.reasonType = symTable.stringType;
            }
        }
        if (!validateErrorVariable(varNode)) {
            varNode.type = symTable.semanticError;
            return;
        }
        symbolEnter.defineNode(varNode, env);
        if (varNode.expr == null) {
            // We have no rhs to do type checking.
            return;
        }
        typeChecker.checkExpr(varNode.expr, env, varNode.type);

    }

    private void handleDeclaredWithVar(BLangVariable variable) {
        BLangExpression varRefExpr = variable.expr;
        BType rhsType = typeChecker.checkExpr(varRefExpr, this.env, expType);

        switch (variable.getKind()) {
            case VARIABLE:
                if (!validateVariableDefinition(varRefExpr)) {
                    rhsType = symTable.semanticError;
                }

                BLangSimpleVariable simpleVariable = (BLangSimpleVariable) variable;

                Name varName = names.fromIdNode(simpleVariable.name);
                if (varName == Names.IGNORE) {
                    dlog.error(simpleVariable.pos, DiagnosticCode.NO_NEW_VARIABLES_VAR_ASSIGNMENT);
                    return;
                }

                simpleVariable.type = rhsType;

                int ownerSymTag = env.scope.owner.tag;
                if ((ownerSymTag & SymTag.INVOKABLE) == SymTag.INVOKABLE) {
                    // This is a variable declared in a function, an action or a resource
                    // If the variable is parameter then the variable symbol is already defined
                    if (simpleVariable.symbol == null) {
                        symbolEnter.defineNode(simpleVariable, env);
                    }
                }

                // Set the type to the symbol. If the variable is a global variable, a symbol is already created in the
                // symbol enter. If the variable is a local variable, the symbol will be created above.
                simpleVariable.symbol.type = rhsType;
                break;
            case TUPLE_VARIABLE:
                if (variable.isDeclaredWithVar && variable.expr.getKind() == NodeKind.LIST_CONSTRUCTOR_EXPR) {
                    dlog.error(varRefExpr.pos, DiagnosticCode.INVALID_LITERAL_FOR_TYPE, "tuple binding pattern");
                    variable.type = symTable.semanticError;
                    return;
                }
                if (TypeTags.TUPLE != rhsType.tag) {
                    dlog.error(varRefExpr.pos, DiagnosticCode.INVALID_TYPE_DEFINITION_FOR_TUPLE_VAR, rhsType);
                    variable.type = symTable.semanticError;
                    return;
                }

                BLangTupleVariable tupleVariable = (BLangTupleVariable) variable;
                tupleVariable.type = rhsType;

                if (!(checkTypeAndVarCountConsistency(tupleVariable))) {
                    tupleVariable.type = symTable.semanticError;
                    return;
                }

                symbolEnter.defineNode(tupleVariable, env);

                break;
            case RECORD_VARIABLE:
                if (TypeTags.RECORD != rhsType.tag && TypeTags.MAP != rhsType.tag && TypeTags.JSON != rhsType.tag) {
                    dlog.error(varRefExpr.pos, DiagnosticCode.INVALID_TYPE_DEFINITION_FOR_RECORD_VAR, rhsType);
                    variable.type = symTable.semanticError;
                }

                BLangRecordVariable recordVariable = (BLangRecordVariable) variable;
                recordVariable.type = rhsType;

                if (!validateRecordVariable(recordVariable)) {
                    recordVariable.type = symTable.semanticError;
                }
                break;
            case ERROR_VARIABLE:
                if (TypeTags.ERROR != rhsType.tag) {
                    dlog.error(variable.expr.pos, DiagnosticCode.INVALID_TYPE_DEFINITION_FOR_ERROR_VAR, rhsType);
                    variable.type = symTable.semanticError;
                    return;
                }
                BLangErrorVariable errorVariable = (BLangErrorVariable) variable;
                errorVariable.type = rhsType;
                if (!validateErrorVariable(errorVariable)) {
                    errorVariable.type = symTable.semanticError;
                    return;
                }
                symbolEnter.defineNode(errorVariable, env);
                break;
        }
    }

    private void handleDeclaredVarInForeach(BLangVariable variable, BType rhsType, SymbolEnv blockEnv) {
        switch (variable.getKind()) {
            case VARIABLE:
                BLangSimpleVariable simpleVariable = (BLangSimpleVariable) variable;
                Name varName = names.fromIdNode(simpleVariable.name);
                if (varName == Names.IGNORE) {
                    dlog.error(simpleVariable.pos, DiagnosticCode.UNDERSCORE_NOT_ALLOWED);
                    return;
                }

                simpleVariable.type = rhsType;

                int ownerSymTag = blockEnv.scope.owner.tag;
                if ((ownerSymTag & SymTag.INVOKABLE) == SymTag.INVOKABLE) {
                    // This is a variable declared in a function, an action or a resource
                    // If the variable is parameter then the variable symbol is already defined
                    if (simpleVariable.symbol == null) {
                        symbolEnter.defineNode(simpleVariable, blockEnv);
                    }
                }
                recursivelySetFinalFlag(simpleVariable);
                break;
            case TUPLE_VARIABLE:
                BLangTupleVariable tupleVariable = (BLangTupleVariable) variable;
                if (TypeTags.TUPLE != rhsType.tag && TypeTags.UNION != rhsType.tag) {
                    dlog.error(variable.pos, DiagnosticCode.INVALID_TYPE_DEFINITION_FOR_TUPLE_VAR, rhsType);
                    recursivelyDefineVariables(tupleVariable, blockEnv);
                    return;
                }

                tupleVariable.type = rhsType;

                if (rhsType.tag == TypeTags.TUPLE && !(checkTypeAndVarCountConsistency(tupleVariable,
                        (BTupleType) tupleVariable.type, blockEnv))) {
                    return;
                }

                if (rhsType.tag == TypeTags.UNION && !(checkTypeAndVarCountConsistency(tupleVariable, null,
                        blockEnv))) {
                    return;
                }

                symbolEnter.defineNode(tupleVariable, blockEnv);
                recursivelySetFinalFlag(tupleVariable);
                break;
            case RECORD_VARIABLE:
                BLangRecordVariable recordVariable = (BLangRecordVariable) variable;
                recordVariable.type = rhsType;
                validateRecordVariable(recordVariable, blockEnv);
                recursivelySetFinalFlag(recordVariable);
                break;
            case ERROR_VARIABLE:
                BLangErrorVariable errorVariable = (BLangErrorVariable) variable;
                if (TypeTags.ERROR != rhsType.tag) {
                    dlog.error(variable.pos, DiagnosticCode.INVALID_TYPE_DEFINITION_FOR_ERROR_VAR, rhsType);
                    recursivelyDefineVariables(errorVariable, blockEnv);
                    return;
                }
                errorVariable.type = rhsType;
                validateErrorVariable(errorVariable);
                recursivelySetFinalFlag(errorVariable);
                break;
        }
    }

    private void recursivelyDefineVariables(BLangVariable variable, SymbolEnv blockEnv) {
        switch (variable.getKind()) {
            case VARIABLE:
                Name name = names.fromIdNode(((BLangSimpleVariable) variable).name);
                if (name == Names.IGNORE) {
                    return;
                }
                variable.type = symTable.semanticError;
                symbolEnter.defineVarSymbol(variable.pos, variable.flagSet, variable.type, name, blockEnv);
                break;
            case TUPLE_VARIABLE:
                ((BLangTupleVariable) variable).memberVariables.forEach(memberVariable ->
                        recursivelyDefineVariables(memberVariable, blockEnv));
                break;
            case RECORD_VARIABLE:
                ((BLangRecordVariable) variable).variableList.forEach(value ->
                        recursivelyDefineVariables(value.valueBindingPattern, blockEnv));
                break;
        }
    }

    private void recursivelySetFinalFlag(BLangVariable variable) {
        if (variable == null) {
            return;
        }

        switch (variable.getKind()) {
            case VARIABLE:
                if (variable.symbol == null) {
                    return;
                }
                variable.symbol.flags |= Flags.FINAL;
                break;
            case TUPLE_VARIABLE:
                BLangTupleVariable tupleVariable = (BLangTupleVariable) variable;
                tupleVariable.memberVariables.forEach(this::recursivelySetFinalFlag);
                recursivelySetFinalFlag(tupleVariable.restVariable);
                break;
            case RECORD_VARIABLE:
                BLangRecordVariable recordVariable = (BLangRecordVariable) variable;
                recordVariable.variableList.forEach(value -> recursivelySetFinalFlag(value.valueBindingPattern));
                recursivelySetFinalFlag((BLangVariable) recordVariable.restParam);
                break;
            case ERROR_VARIABLE:
                BLangErrorVariable errorVariable = (BLangErrorVariable) variable;
                recursivelySetFinalFlag(errorVariable.reason);
                recursivelySetFinalFlag(errorVariable.restDetail);
                errorVariable.detail.forEach(bLangErrorDetailEntry ->
                        recursivelySetFinalFlag(bLangErrorDetailEntry.valueBindingPattern));
                break;
        }
    }

    private boolean checkTypeAndVarCountConsistency(BLangTupleVariable varNode) {
        return checkTypeAndVarCountConsistency(varNode, null, env);
    }

    private boolean checkTypeAndVarCountConsistency(BLangTupleVariable varNode, BTupleType tupleTypeNode,
                                                    SymbolEnv env) {

        if (tupleTypeNode == null) {
        /*
          This switch block will resolve the tuple type of the tuple variable.
          For example consider the following - [int, string]|[boolean, float] [a, b] = foo();
          Since the varNode type is a union, the types of 'a' and 'b' will be resolved as follows:
          Type of 'a' will be (int | boolean) while the type of 'b' will be (string | float).
          Consider anydata (a, b) = foo();
          Here, the type of 'a'and type of 'b' will be both anydata.
         */
            switch (varNode.type.tag) {
                case TypeTags.UNION:
                    Set<BType> unionType = types.expandAndGetMemberTypesRecursive(varNode.type);
                    List<BType> possibleTypes = unionType.stream()
                            .filter(type -> {
                                if (TypeTags.TUPLE == type.tag &&
                                        (varNode.memberVariables.size() == ((BTupleType) type).tupleTypes.size())) {
                                    return true;
                                }
                                return TypeTags.ANY == type.tag || TypeTags.ANYDATA == type.tag;
                            })
                            .collect(Collectors.toList());

                    if (possibleTypes.isEmpty()) {
                        dlog.error(varNode.pos, DiagnosticCode.INVALID_TYPE_DEFINITION_FOR_TUPLE_VAR, varNode.type);
                        return false;
                    }

                    if (possibleTypes.size() > 1) {
                        List<BType> memberTupleTypes = new ArrayList<>();
                        for (int i = 0; i < varNode.memberVariables.size(); i++) {
                            LinkedHashSet<BType> memberTypes = new LinkedHashSet<>();
                            for (BType possibleType : possibleTypes) {
                                if (possibleType.tag == TypeTags.TUPLE) {
                                    memberTypes.add(((BTupleType) possibleType).tupleTypes.get(i));
                                } else {
                                    memberTupleTypes.add(varNode.type);
                                }
                            }

                            if (memberTypes.size() > 1) {
                                memberTupleTypes.add(BUnionType.create(null, memberTypes));
                            } else {
                                memberTupleTypes.addAll(memberTypes);
                            }
                        }
                        tupleTypeNode = new BTupleType(memberTupleTypes);
                        break;
                    }

                    if (possibleTypes.get(0).tag == TypeTags.TUPLE) {
                        tupleTypeNode = (BTupleType) possibleTypes.get(0);
                        break;
                    }

                    List<BType> memberTypes = new ArrayList<>();
                    for (int i = 0; i < varNode.memberVariables.size(); i++) {
                        memberTypes.add(possibleTypes.get(0));
                    }
                    tupleTypeNode = new BTupleType(memberTypes);
                    break;
                case TypeTags.ANY:
                case TypeTags.ANYDATA:
                    List<BType> memberTupleTypes = new ArrayList<>();
                    for (int i = 0; i < varNode.memberVariables.size(); i++) {
                        memberTupleTypes.add(varNode.type);
                    }
                    tupleTypeNode = new BTupleType(memberTupleTypes);
                    if (varNode.restVariable != null) {
                        tupleTypeNode.restType = varNode.type;
                    }
                    break;
                case TypeTags.TUPLE:
                    tupleTypeNode = (BTupleType) varNode.type;
                    break;
                default:
                    dlog.error(varNode.pos, DiagnosticCode.INVALID_TYPE_DEFINITION_FOR_TUPLE_VAR, varNode.type);
                    return false;
            }
        }

        if (tupleTypeNode.tupleTypes.size() != varNode.memberVariables.size()
                || (tupleTypeNode.restType == null && varNode.restVariable != null)
                ||  (tupleTypeNode.restType != null && varNode.restVariable == null)) {
            dlog.error(varNode.pos, DiagnosticCode.INVALID_TUPLE_BINDING_PATTERN);
            return false;
        }

        int ignoredCount = 0;
        List<BLangVariable> memberVariables = new ArrayList<>(varNode.memberVariables);
        if (varNode.restVariable != null) {
            memberVariables.add(varNode.restVariable);
        }
        for (int i = 0; i < memberVariables.size(); i++) {
            BLangVariable var = memberVariables.get(i);
            BType type = (i <= tupleTypeNode.tupleTypes.size() - 1) ? tupleTypeNode.tupleTypes.get(i) :
                    new BArrayType(tupleTypeNode.restType);
            if (var.getKind() == NodeKind.VARIABLE) {
                // '_' is allowed in tuple variables. Not allowed if all variables are named as '_'
                BLangSimpleVariable simpleVar = (BLangSimpleVariable) var;
                Name varName = names.fromIdNode(simpleVar.name);
                if (varName == Names.IGNORE) {
                    ignoredCount++;
                    simpleVar.type = symTable.anyType;
                    types.checkType(varNode.pos, type, simpleVar.type,
                            DiagnosticCode.INCOMPATIBLE_TYPES);
                    continue;
                }
            }
            var.type = type;
            analyzeNode(var, env);
        }

        if (!varNode.memberVariables.isEmpty() && ignoredCount == varNode.memberVariables.size()
                && varNode.restVariable == null) {
            dlog.error(varNode.pos, DiagnosticCode.NO_NEW_VARIABLES_VAR_ASSIGNMENT);
            return false;
        }
        return true;
    }

    private boolean validateRecordVariable(BLangRecordVariable recordVar) {
        return validateRecordVariable(recordVar, env);
    }

    private boolean validateRecordVariable(BLangRecordVariable recordVar, SymbolEnv env) {
        BRecordType recordVarType;
        /*
          This switch block will resolve the record type of the record variable.
          For example consider the following -
          type Foo record {int a, boolean b};
          type Bar record {string a, float b};
          Foo|Bar {a, b} = foo();
          Since the varNode type is a union, the types of 'a' and 'b' will be resolved as follows:
          Type of 'a' will be a union of the types of field 'a' in both Foo and Bar.
          i.e. type of 'a' is (int | string) and type of 'b' is (boolean | float).
          Consider anydata {a, b} = foo();
          Here, the type of 'a'and type of 'b' will be both anydata.
         */
        switch (recordVar.type.tag) {
            case TypeTags.UNION:
                BUnionType unionType = (BUnionType) recordVar.type;
                Set<BType> bTypes = types.expandAndGetMemberTypesRecursive(unionType);
                List<BType> possibleTypes = bTypes.stream()
                        .filter(rec -> doesRecordContainKeys(rec, recordVar.variableList, recordVar.restParam != null))
                        .collect(Collectors.toList());

                if (possibleTypes.isEmpty()) {
                    dlog.error(recordVar.pos, DiagnosticCode.INVALID_RECORD_BINDING_PATTERN, recordVar.type);
                    return false;
                }

                if (possibleTypes.size() > 1) {
                    BRecordTypeSymbol recordSymbol = Symbols.createRecordSymbol(0,
                            names.fromString(ANONYMOUS_RECORD_NAME), env.enclPkg.symbol.pkgID, null, env.scope.owner);
                    recordVarType = (BRecordType) symTable.recordType;

                    List<BField> fields = populateAndGetPossibleFieldsForRecVar(recordVar, possibleTypes, recordSymbol);

                    if (recordVar.restParam != null) {
                        LinkedHashSet<BType> memberTypes = possibleTypes.stream()
                                .map(possibleType -> {
                                    if (possibleType.tag == TypeTags.RECORD) {
                                        return ((BRecordType) possibleType).restFieldType;
                                    } else if (possibleType.tag == TypeTags.MAP) {
                                        return ((BMapType) possibleType).constraint;
                                    } else {
                                        return possibleType;
                                    }
                                })
                                .collect(Collectors.toCollection(LinkedHashSet::new));
                        recordVarType.restFieldType = memberTypes.size() > 1 ?
                                BUnionType.create(null, memberTypes) :
                                memberTypes.iterator().next();
                    }
                    recordVarType.tsymbol = recordSymbol;
                    recordVarType.fields = fields;
                    recordSymbol.type = recordVarType;
                    break;
                }

                if (possibleTypes.get(0).tag == TypeTags.RECORD) {
                    recordVarType = (BRecordType) possibleTypes.get(0);
                    break;
                }

                if (possibleTypes.get(0).tag == TypeTags.MAP) {
                    recordVarType = createSameTypedFieldsRecordType(recordVar,
                            ((BMapType) possibleTypes.get(0)).constraint);
                    break;
                }

                recordVarType = createSameTypedFieldsRecordType(recordVar, possibleTypes.get(0));
                break;
            case TypeTags.RECORD:
                recordVarType = (BRecordType) recordVar.type;
                break;
            case TypeTags.MAP:
                recordVarType = createSameTypedFieldsRecordType(recordVar, ((BMapType) recordVar.type).constraint);
                break;
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
                recordVarType = createSameTypedFieldsRecordType(recordVar, recordVar.type);
                break;
            default:
                dlog.error(recordVar.pos, DiagnosticCode.INVALID_RECORD_BINDING_PATTERN, recordVar.type);
                return false;
        }

        Map<String, BField> recordVarTypeFields = recordVarType.fields.stream()
                .collect(Collectors.toMap(field -> field.getName().getValue(), field -> field));

        boolean validRecord = true;
        int ignoredCount = 0;
        for (BLangRecordVariableKeyValue variable : recordVar.variableList) {
            // Infer the type of each variable in recordVariable from the given record type
            // so that symbol enter is done recursively
            if (names.fromIdNode(variable.getKey()) == Names.IGNORE) {
                dlog.error(recordVar.pos, DiagnosticCode.UNDERSCORE_NOT_ALLOWED);
                continue;
            }

            BLangVariable value = variable.getValue();
            if (value.getKind() == NodeKind.VARIABLE) {
                // '_' is allowed in record variables. Not allowed if all variables are named as '_'
                BLangSimpleVariable simpleVar = (BLangSimpleVariable) value;
                Name varName = names.fromIdNode(simpleVar.name);
                if (varName == Names.IGNORE) {
                    ignoredCount++;
                    simpleVar.type = symTable.anyType;
                    if (!recordVarTypeFields.containsKey(variable.getKey().getValue())) {
                        continue;
                    }
                    types.checkType(variable.valueBindingPattern.pos,
                            recordVarTypeFields.get((variable.getKey().getValue())).type, simpleVar.type,
                            DiagnosticCode.INCOMPATIBLE_TYPES);
                    continue;
                }
            }
            if (!recordVarTypeFields.containsKey(variable.getKey().getValue())) {
                if (recordVarType.sealed) {
                    validRecord = false;
                    dlog.error(recordVar.pos, DiagnosticCode.INVALID_FIELD_IN_RECORD_BINDING_PATTERN,
                            variable.getKey().getValue(), recordVar.type);
                } else {
                    BType restType;
                    if (recordVarType.restFieldType.tag == TypeTags.ANYDATA ||
                            recordVarType.restFieldType.tag == TypeTags.ANY) {
                        restType = recordVarType.restFieldType;
                    } else {
                        restType = BUnionType.create(null, recordVarType.restFieldType, symTable.nilType);
                    }
                    value.type = restType;
                    value.accept(this);
                }
                continue;
            }

            value.type = recordVarTypeFields.get((variable.getKey().getValue())).type;
            value.accept(this);
        }

        if (!recordVar.variableList.isEmpty() && ignoredCount == recordVar.variableList.size()
                && recordVar.restParam == null) {
            dlog.error(recordVar.pos, DiagnosticCode.NO_NEW_VARIABLES_VAR_ASSIGNMENT);
            return false;
        }

        if (recordVar.restParam != null) {
            ((BLangVariable) recordVar.restParam).type = getRestParamType(recordVarType);
            symbolEnter.defineNode((BLangNode) recordVar.restParam, env);
        }

        return validRecord;
    }

    private boolean validateErrorVariable(BLangErrorVariable errorVariable) {
        BErrorType errorType;
        switch (errorVariable.type.tag) {
            case TypeTags.UNION:
                BUnionType unionType = ((BUnionType) errorVariable.type);
                List<BErrorType> possibleTypes = unionType.getMemberTypes().stream()
                        .filter(type -> TypeTags.ERROR == type.tag)
                        .map(BErrorType.class::cast)
                        .collect(Collectors.toList());
                if (possibleTypes.isEmpty()) {
                    dlog.error(errorVariable.pos, DiagnosticCode.INVALID_ERROR_BINDING_PATTERN, errorVariable.type);
                    return false;
                }
                if (possibleTypes.size() > 1) {
                    LinkedHashSet<BType> detailType = new LinkedHashSet<>();
                    for (BErrorType possibleErrType : possibleTypes) {
                        detailType.add(possibleErrType.detailType);
                    }
                    BType errorDetailType = detailType.size() > 1
                                    ? BUnionType.create(null, detailType)
                                    : detailType.iterator().next();
                    errorType = new BErrorType(null, symTable.stringType,
                            errorDetailType);
                } else {
                    errorType = possibleTypes.get(0);
                }
                break;
            case TypeTags.ERROR:
                errorType = (BErrorType) errorVariable.type;
                break;
            default:
                dlog.error(errorVariable.pos, DiagnosticCode.INVALID_ERROR_BINDING_PATTERN, errorVariable.type);
                return false;
        }
        errorVariable.type = errorType;
        boolean isReasonIgnored = false;
        BLangSimpleVariable reasonVariable = errorVariable.reason;
        if (Names.IGNORE == names.fromIdNode(reasonVariable.name)) {
            reasonVariable.type = symTable.noType;
            isReasonIgnored = true;
        } else {
            errorVariable.reason.type = errorType.reasonType;
            errorVariable.reason.accept(this);
        }

        if (errorVariable.detail == null || (errorVariable.detail.isEmpty()
                && !isRestDetailBindingAvailable(errorVariable))) {
            if (isReasonIgnored) {
                dlog.error(errorVariable.pos, DiagnosticCode.NO_NEW_VARIABLES_VAR_ASSIGNMENT);
                return false;
            }
            return validateErrorReasonMatchPatternSyntax(errorVariable);
        }

        if (errorType.detailType.getKind() == TypeKind.RECORD) {
            return validateErrorVariable(errorVariable, errorType);
        } else if (errorType.detailType.getKind() == TypeKind.UNION) {
            BErrorTypeSymbol errorTypeSymbol = new BErrorTypeSymbol(SymTag.ERROR, Flags.PUBLIC, Names.ERROR,
                    env.enclPkg.packageID, symTable.errorType, env.scope.owner);
            // todo: need to support string subtypes as reason type.
            errorVariable.type = new BErrorType(errorTypeSymbol, symTable.stringType, symTable.detailType);
            return validateErrorVariable(errorVariable);
        }

        if (isRestDetailBindingAvailable(errorVariable)) {
            // TODO : Fix me.
            errorVariable.restDetail.type = symTable.detailType;
            errorVariable.restDetail.accept(this);
        }
        return true;
    }

    private boolean validateErrorVariable(BLangErrorVariable errorVariable, BErrorType errorType) {
        if (!validateErrorReasonMatchPatternSyntax(errorVariable)) {
            return false;
        }

        BRecordType recordType = (BRecordType) errorType.detailType;
        Map<String, BField> fieldMap = recordType.fields.stream()
                .collect(Collectors.toMap(f -> f.name.value, f -> f));
        for (BLangErrorVariable.BLangErrorDetailEntry errorDetailEntry : errorVariable.detail) {
            String entryName = errorDetailEntry.key.getValue();
            BField entryField = fieldMap.get(entryName);

            BLangVariable boundVar = errorDetailEntry.valueBindingPattern;
            if (entryField != null) {
                if ((entryField.symbol.flags & Flags.OPTIONAL) == Flags.OPTIONAL) {
                    boundVar.type = BUnionType.create(null, entryField.type, symTable.nilType);
                } else {
                    boundVar.type = entryField.type;
                }
            } else {
                if (recordType.sealed) {
                    dlog.error(errorVariable.pos, DiagnosticCode.INVALID_ERROR_BINDING_PATTERN, errorVariable.type);
                    boundVar.type = symTable.semanticError;
                    return false;
                } else {
                    boundVar.type = BUnionType.create(null, recordType.restFieldType, symTable.nilType);
                }
            }

            boolean isIgnoredVar = boundVar.getKind() == NodeKind.VARIABLE
                    && ((BLangSimpleVariable) boundVar).name.value.equals(Names.IGNORE.value);
            if (!isIgnoredVar) {
                boundVar.accept(this);
            }
        }

        if (isRestDetailBindingAvailable(errorVariable)) {
            BTypeSymbol typeSymbol = createTypeSymbol(SymTag.TYPE);
            BMapType restType = new BMapType(TypeTags.MAP, recordType.restFieldType, typeSymbol);
            typeSymbol.type = restType;
            errorVariable.restDetail.type = restType;
            errorVariable.restDetail.accept(this);
        }
        return true;
    }

    private boolean validateErrorReasonMatchPatternSyntax(BLangErrorVariable errorVariable) {
        if (errorVariable.isInMatchStmt
                && !errorVariable.reasonVarPrefixAvailable
                && errorVariable.reasonMatchConst == null
                && isReasonSpecified(errorVariable)) {

            BSymbol reasonConst = symResolver.lookupSymbol(
                    this.env.enclEnv, names.fromString(errorVariable.reason.name.value), SymTag.CONSTANT);
            if (reasonConst == symTable.notFoundSymbol) {
                dlog.error(errorVariable.reason.pos, DiagnosticCode.INVALID_ERROR_REASON_BINDING_PATTERN,
                        errorVariable.reason.name);
            } else {
                dlog.error(errorVariable.reason.pos, DiagnosticCode.UNSUPPORTED_ERROR_REASON_CONST_MATCH);
            }
            return false;
        }
        return true;
    }

    private boolean isReasonSpecified(BLangErrorVariable errorVariable) {
        return !isIgnoredOrEmpty(errorVariable.reason);
    }

    private boolean isIgnoredOrEmpty(BLangSimpleVariable varNode) {
        return varNode.name.value.equals(Names.IGNORE.value) || varNode.name.value.equals("");
    }

    private boolean isRestDetailBindingAvailable(BLangErrorVariable errorVariable) {
        return errorVariable.restDetail != null &&
                !errorVariable.restDetail.name.value.equals(Names.IGNORE.value);
    }

    private BTypeSymbol createTypeSymbol(int type) {
        return new BTypeSymbol(type, Flags.PUBLIC, Names.EMPTY, env.enclPkg.packageID,
                            null, env.scope.owner);
    }

    /**
     * This method will resolve field types based on a list of possible types.
     * When a record variable has multiple possible assignable types, each field will be a union of the relevant
     * possible types field type.
     *
     * @param recordVar record variable whose fields types are to be resolved
     * @param possibleTypes list of possible types
     * @param recordSymbol symbol of the record type to be used in creating fields
     * @return the list of fields
     */
    private List<BField> populateAndGetPossibleFieldsForRecVar(BLangRecordVariable recordVar, List<BType> possibleTypes,
                                                               BRecordTypeSymbol recordSymbol) {
        List<BField> fields = new ArrayList<>();
        for (BLangRecordVariableKeyValue bLangRecordVariableKeyValue : recordVar.variableList) {
            String fieldName = bLangRecordVariableKeyValue.key.value;
            LinkedHashSet<BType> memberTypes = new LinkedHashSet<>();
            for (BType possibleType : possibleTypes) {
                if (possibleType.tag == TypeTags.RECORD) {
                    BRecordType possibleRecordType = (BRecordType) possibleType;
                    Optional<BField> optionalField = possibleRecordType.fields.stream()
                            .filter(field -> field.getName().getValue().equals(fieldName))
                            .findFirst();
                    if (optionalField.isPresent()) {
                        BField bField = optionalField.get();
                        if (Symbols.isOptional(bField.symbol)) {
                            memberTypes.add(symTable.nilType);
                        }
                        memberTypes.add(bField.type);
                    } else {
                        memberTypes.add(possibleRecordType.restFieldType);
                        memberTypes.add(symTable.nilType);
                    }
                    continue;
                }
                if (possibleType.tag == TypeTags.MAP) {
                    BMapType possibleMapType = (BMapType) possibleType;
                    memberTypes.add(possibleMapType.constraint);
                    continue;
                }
                memberTypes.add(possibleType); // possible type is any or anydata}
            }

            BType fieldType = memberTypes.size() > 1 ?
                    BUnionType.create(null, memberTypes) : memberTypes.iterator().next();
            fields.add(new BField(names.fromString(fieldName), recordVar.pos,
                    new BVarSymbol(0, names.fromString(fieldName), env.enclPkg.symbol.pkgID,
                            fieldType, recordSymbol)));
        }
        return fields;
    }

    private BRecordType createSameTypedFieldsRecordType(BLangRecordVariable recordVar, BType fieldTypes) {
        BType fieldType;
        if (fieldTypes.isNullable()) {
            fieldType = fieldTypes;
        } else {
            fieldType = BUnionType.create(null, fieldTypes, symTable.nilType);
        }

        BRecordTypeSymbol recordSymbol = Symbols.createRecordSymbol(0, names.fromString(ANONYMOUS_RECORD_NAME),
                env.enclPkg.symbol.pkgID, null, env.scope.owner);
        //TODO check below field position
        List<BField> fields = recordVar.variableList.stream()
                .map(bLangRecordVariableKeyValue -> bLangRecordVariableKeyValue.key.value)
                .map(fieldName -> new BField(names.fromString(fieldName), recordVar.pos, new BVarSymbol(0,
                        names.fromString(fieldName), env.enclPkg.symbol.pkgID, fieldType, recordSymbol)))
                .collect(Collectors.toList());

        BRecordType recordVarType = (BRecordType) symTable.recordType;
        recordVarType.fields = fields;
        recordSymbol.type = recordVarType;
        recordVarType.tsymbol = recordSymbol;

        // Since this is for record variables, we consider its record type as an open record type.
        recordVarType.sealed = false;
        recordVarType.restFieldType = fieldTypes; // TODO: 7/26/19 Check if this should be `fieldType`

        return recordVarType;
    }

    private boolean doesRecordContainKeys(BType varType, List<BLangRecordVariableKeyValue> variableList,
                                          boolean hasRestParam) {
        if (varType.tag == TypeTags.MAP || varType.tag == TypeTags.ANY || varType.tag == TypeTags.ANYDATA) {
            return true;
        }
        if (varType.tag != TypeTags.RECORD) {
            return false;
        }
        BRecordType recordVarType = (BRecordType) varType;
        Map<String, BField> recordVarTypeFields = recordVarType.fields
                .stream()
                .collect(Collectors.toMap(
                        field -> field.getName().getValue(),
                        field -> field
                ));

        for (BLangRecordVariableKeyValue var : variableList) {
            if (!recordVarTypeFields.containsKey(var.key.value) && recordVarType.sealed) {
                return false;
            }
        }

        if (!hasRestParam) {
            return true;
        }

        return !recordVarType.sealed;
    }

    // Statements

    public void visit(BLangBlockStmt blockNode) {
        env = SymbolEnv.createBlockEnv(blockNode, env);
        blockNode.stmts.forEach(stmt -> analyzeStmt(stmt, env));
    }

    public void visit(BLangSimpleVariableDef varDefNode) {
        // This will prevent cases Eg:- int _ = 100;
        // We have prevented '_' from registering variable symbol at SymbolEnter, Hence this validation added.
        Name varName = names.fromIdNode(varDefNode.var.name);
        if (varName == Names.IGNORE) {
            dlog.error(varDefNode.var.pos, DiagnosticCode.NO_NEW_VARIABLES_VAR_ASSIGNMENT);
            return;
        }

        analyzeDef(varDefNode.var, env);
    }

    public void visit(BLangRecordVariableDef varDefNode) {
        // TODO: 10/18/18 Need to support record literals as well
        if (varDefNode.var.expr.getKind() == RECORD_LITERAL_EXPR) {
            dlog.error(varDefNode.pos, DiagnosticCode.INVALID_LITERAL_FOR_TYPE, "record binding pattern");
            return;
        }
        analyzeDef(varDefNode.var, env);
    }

    public void visit(BLangErrorVariableDef varDefNode) {
        analyzeDef(varDefNode.errorVariable, env);
    }

    @Override
    public void visit(BLangTupleVariableDef tupleVariableDef) {
        analyzeDef(tupleVariableDef.var, env);
    }

    private Boolean validateLhsVar(BLangExpression vRef) {
        if (vRef.getKind() == NodeKind.INVOCATION) {
            dlog.error(((BLangInvocation) vRef).pos, DiagnosticCode.INVALID_INVOCATION_LVALUE_ASSIGNMENT, vRef);
            return false;
        }
        if (vRef.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR
                || vRef.getKind() == NodeKind.INDEX_BASED_ACCESS_EXPR) {
            validateLhsVar(((BLangAccessExpression) vRef).expr);
        }
        return true;
    }

    public void visit(BLangCompoundAssignment compoundAssignment) {
        List<BType> expTypes = new ArrayList<>();
        BLangExpression varRef = compoundAssignment.varRef;
        // Check whether the variable reference is an function invocation or not.
        boolean isValidVarRef = validateLhsVar(varRef);
        if (isValidVarRef) {
            compoundAssignment.varRef.compoundAssignmentLhsVar = true;
            this.typeChecker.checkExpr(varRef, env);
            expTypes.add(varRef.type);
        } else {
            expTypes.add(symTable.semanticError);
        }
        this.typeChecker.checkExpr(compoundAssignment.expr, env);

        checkConstantAssignment(varRef);

        if (expTypes.get(0) != symTable.semanticError && compoundAssignment.expr.type != symTable.semanticError) {
            BSymbol opSymbol = this.symResolver.resolveBinaryOperator(compoundAssignment.opKind, expTypes.get(0),
                    compoundAssignment.expr.type);
            if (opSymbol == symTable.notFoundSymbol) {
                dlog.error(compoundAssignment.pos, DiagnosticCode.BINARY_OP_INCOMPATIBLE_TYPES,
                        compoundAssignment.opKind, expTypes.get(0), compoundAssignment.expr.type);
            } else {
                compoundAssignment.modifiedExpr = getBinaryExpr(varRef,
                        compoundAssignment.expr,
                        compoundAssignment.opKind,
                        opSymbol);
                compoundAssignment.modifiedExpr.parent = compoundAssignment;
                this.types.checkTypes(compoundAssignment.modifiedExpr,
                        Lists.of(compoundAssignment.modifiedExpr.type), expTypes);
            }
        }
    }

    public void visit(BLangAssignment assignNode) {
        BLangExpression varRef = assignNode.varRef;
        if (varRef.getKind() == NodeKind.INDEX_BASED_ACCESS_EXPR ||
                varRef.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR) {
            ((BLangAccessExpression) varRef).leafNode = true;
        }

        // Check each LHS expression.
        setTypeOfVarRefInAssignment(varRef);
        expType = varRef.type;

        typeChecker.checkExpr(assignNode.expr, this.env, expType);

        validateWorkerAnnAttachments(assignNode.expr);

        resetTypeNarrowing(varRef, assignNode.expr);
    }

    @Override
    public void visit(BLangTupleDestructure tupleDeStmt) {
        setTypeOfVarRefInBindingAssignment(tupleDeStmt.varRef);
        BType type = typeChecker.checkExpr(tupleDeStmt.expr, this.env, tupleDeStmt.varRef.type);

        if (tupleDeStmt.expr.type.tag == TypeTags.ARRAY) {
            // TODO: https://github.com/ballerina-platform/ballerina-lang/issues/17927
            dlog.error(tupleDeStmt.expr.pos, DiagnosticCode.BINDING_PATTERN_NOT_YET_SUPPORTED, tupleDeStmt.expr.type);
            return;
        }

        if (type.tag != TypeTags.SEMANTIC_ERROR) {
            checkTupleVarRefEquivalency(tupleDeStmt.pos, tupleDeStmt.varRef,
                    tupleDeStmt.expr.type, tupleDeStmt.expr.pos);
        }
    }

    @Override
    public void visit(BLangRecordDestructure recordDeStmt) {
        // recursively visit the var refs and create the record type
        setTypeOfVarRefInBindingAssignment(recordDeStmt.varRef);
        typeChecker.checkExpr(recordDeStmt.varRef, env);

        if (recordDeStmt.expr.getKind() == RECORD_LITERAL_EXPR) {
            // TODO: 10/18/18 Need to support record literals as well
            dlog.error(recordDeStmt.expr.pos, DiagnosticCode.INVALID_RECORD_LITERAL_BINDING_PATTERN);
            return;
        }
        typeChecker.checkExpr(recordDeStmt.expr, this.env);
        checkRecordVarRefEquivalency(recordDeStmt.pos, recordDeStmt.varRef, recordDeStmt.expr.type,
                recordDeStmt.expr.pos);
    }

    @Override
    public void visit(BLangErrorDestructure errorDeStmt) {
        if (errorDeStmt.varRef.reason.getKind() != NodeKind.SIMPLE_VARIABLE_REF ||
                names.fromIdNode(((BLangSimpleVarRef) errorDeStmt.varRef.reason).variableName) != Names.IGNORE) {
            setTypeOfVarRefInBindingAssignment(errorDeStmt.varRef.reason);
        } else {
            // set reason var refs type to no type if the variable name is '_'
            errorDeStmt.varRef.reason.type = symTable.noType;
        }
        if (errorDeStmt.expr.getKind() == NodeKind.INVOCATION
                && ((BLangInvocation) errorDeStmt.expr).name.value.equals(Names.ERROR.value)) {
            dlog.error(errorDeStmt.expr.pos, DiagnosticCode.INVALID_ERROR_LITERAL_BINDING_PATTERN);
            return;
        }
        typeChecker.checkExpr(errorDeStmt.expr, this.env);
        checkErrorVarRefEquivalency(errorDeStmt.pos, errorDeStmt.varRef, errorDeStmt.expr.type, errorDeStmt.expr.pos);
    }

    /**
     * When rhs is an expression of type record, this method will check the type of each field in the
     * record type against the record var ref fields.
     *
     * @param pos       diagnostic pos
     * @param lhsVarRef type of the record var ref
     * @param rhsType   the type on the rhs
     * @param rhsPos    position of the rhs expression
     */
    private void checkRecordVarRefEquivalency(DiagnosticPos pos, BLangRecordVarRef lhsVarRef, BType rhsType,
                                              DiagnosticPos rhsPos) {
        if (rhsType.tag == TypeTags.MAP) {
            BMapType rhsMapType = (BMapType) rhsType;
            BType expectedType;
            switch (rhsMapType.constraint.tag) {
                case TypeTags.ANY:
                case TypeTags.ANYDATA:
                case TypeTags.JSON:
                    expectedType = rhsMapType.constraint;
                    break;
                case TypeTags.UNION:
                    BUnionType unionType = (BUnionType) rhsMapType.constraint;
                    LinkedHashSet<BType> unionMemberTypes = new LinkedHashSet<BType>() {{
                        addAll(unionType.getMemberTypes());
                        add(symTable.nilType);
                    }};
                    expectedType = BUnionType.create(null, unionMemberTypes);
                    break;
                default:
                    expectedType = BUnionType.create(null, new LinkedHashSet<BType>() {{
                        add(rhsMapType.constraint);
                        add(symTable.nilType);
                    }});
                    break;
            }
            lhsVarRef.recordRefFields.forEach(field -> types.checkType(field.variableReference.pos,
                    expectedType, field.variableReference.type, DiagnosticCode.INCOMPATIBLE_TYPES));

            if (lhsVarRef.restParam != null) {
                types.checkType(((BLangSimpleVarRef) lhsVarRef.restParam).pos, rhsMapType,
                        ((BLangSimpleVarRef) lhsVarRef.restParam).type, DiagnosticCode.INCOMPATIBLE_TYPES);
            }

            return;
        }

        if (rhsType.tag != TypeTags.RECORD) {
            dlog.error(rhsPos, DiagnosticCode.INCOMPATIBLE_TYPES, "record type", rhsType);
            return;
        }

        BRecordType rhsRecordType = (BRecordType) rhsType;

        // check if all fields in record var ref are found in rhs record type
        lhsVarRef.recordRefFields.stream()
                .filter(lhsField -> rhsRecordType.fields.stream()
                        .noneMatch(rhsField -> lhsField.variableName.value.equals(rhsField.name.toString())))
                .forEach(lhsField -> dlog.error(pos, DiagnosticCode.INVALID_FIELD_IN_RECORD_BINDING_PATTERN,
                        lhsField.variableName.value, rhsType));

        for (BField rhsField : rhsRecordType.fields) {
            List<BLangRecordVarRefKeyValue> expField = lhsVarRef.recordRefFields.stream()
                    .filter(field -> field.variableName.value.equals(rhsField.name.toString()))
                    .collect(Collectors.toList());

            if (expField.isEmpty()) {
                continue;
            }

            if (expField.size() > 1) {
                dlog.error(pos, DiagnosticCode.MULTIPLE_RECORD_REF_PATTERN_FOUND, rhsField.name);
                return;
            }
            BLangExpression variableReference = expField.get(0).variableReference;
            if (variableReference.getKind() == NodeKind.RECORD_VARIABLE_REF) {
                checkRecordVarRefEquivalency(variableReference.pos,
                        (BLangRecordVarRef) variableReference, rhsField.type, rhsPos);
            } else if (variableReference.getKind() == NodeKind.TUPLE_VARIABLE_REF) {
                checkTupleVarRefEquivalency(pos, (BLangTupleVarRef) variableReference, rhsField.type, rhsPos);
            } else if (variableReference.getKind() == NodeKind.ERROR_VARIABLE_REF) {
                checkErrorVarRefEquivalency(pos, (BLangErrorVarRef) variableReference, rhsField.type, rhsPos);
            } else if (variableReference.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                Name varName = names.fromIdNode(((BLangSimpleVarRef) variableReference).variableName);
                if (varName == Names.IGNORE) {
                    continue;
                }
                types.checkType(variableReference.pos, rhsField.type,
                        variableReference.type, DiagnosticCode.INCOMPATIBLE_TYPES);
            } else {
                dlog.error(variableReference.pos, DiagnosticCode.INVALID_VARIABLE_REFERENCE_IN_BINDING_PATTERN,
                        variableReference);
            }
        }

        if (lhsVarRef.restParam != null) {
            types.checkType(((BLangSimpleVarRef) lhsVarRef.restParam).pos, getRestParamType(rhsRecordType),
                            ((BLangSimpleVarRef) lhsVarRef.restParam).type, DiagnosticCode.INCOMPATIBLE_TYPES);
        }

        //Check whether this is an readonly field.
        checkReadonlyAssignment(lhsVarRef);

        checkConstantAssignment(lhsVarRef);
    }

    private BMapType getRestParamType(BRecordType recordType)  {
        BType memberType;

        if (hasErrorTypedField(recordType)) {
            memberType = hasOnlyPureTypedFields(recordType) ? symTable.pureType :
                    BUnionType.create(null, symTable.anyType, symTable.errorType);
        } else {
            memberType = hasOnlyAnydataTypedFields(recordType) ? symTable.anydataType : symTable.anyType;
        }

        return new BMapType(TypeTags.MAP, memberType, null);
    }

    private boolean hasOnlyAnydataTypedFields(BRecordType recordType) {
        boolean allAnydataFields = recordType.fields.stream()
                .map(field -> field.type)
                .allMatch(fieldType -> fieldType.isAnydata());
        return allAnydataFields && (recordType.sealed || recordType.restFieldType.isAnydata());
    }

    private boolean hasOnlyPureTypedFields(BRecordType recordType) {
        boolean allPureFields = recordType.fields.stream()
                .map(field -> field.type)
                .allMatch(fieldType -> fieldType.isPureType());
        return allPureFields && (recordType.sealed || recordType.restFieldType.isPureType());
    }

    private boolean hasErrorTypedField(BRecordType recordType) {
        return hasErrorType(recordType.restFieldType) ||
                recordType.fields.stream().map(field -> field.type).anyMatch(this::hasErrorType);
    }

    private boolean hasErrorType(BType type) {
        if (type.tag != TypeTags.UNION) {
            return type.tag == TypeTags.ERROR;
        }

        return ((BUnionType) type).getMemberTypes().stream().anyMatch(this::hasErrorType);
    }

    private void checkTupleVarRefEquivalency(DiagnosticPos pos, BLangTupleVarRef target, BType source,
                                             DiagnosticPos rhsPos) {
        if (source.tag != TypeTags.TUPLE) {
            dlog.error(rhsPos, DiagnosticCode.INCOMPATIBLE_TYPES, target.type, source);
            return;
        }

        if (target.restParam == null) {
            if (((BTupleType) source).restType != null) {
                dlog.error(rhsPos, DiagnosticCode.INCOMPATIBLE_TYPES, target.type, source);
                return;
            } else if (((BTupleType) source).tupleTypes.size() != target.expressions.size()) {
                dlog.error(rhsPos, DiagnosticCode.INCOMPATIBLE_TYPES, target.type, source);
                return;
            }
        }

        List<BType> sourceTypes = new ArrayList<>(((BTupleType) source).tupleTypes);
        if (((BTupleType) source).restType != null) {
            sourceTypes.add(((BTupleType) source).restType);
        }

        for (int i = 0; i < sourceTypes.size(); i++) {
            BLangExpression varRefExpr;
            BType targetType;
            if ((target.expressions.size() > i)) {
                varRefExpr = target.expressions.get(i);
                targetType = varRefExpr.type;
            } else {
                varRefExpr = (BLangExpression) target.restParam;
                targetType = ((BArrayType) varRefExpr.type).eType;
            }

            if (NodeKind.RECORD_VARIABLE_REF == varRefExpr.getKind()) {
                BLangRecordVarRef recordVarRef = (BLangRecordVarRef) varRefExpr;
                checkRecordVarRefEquivalency(pos, recordVarRef, sourceTypes.get(i), rhsPos);
            } else if (NodeKind.TUPLE_VARIABLE_REF == varRefExpr.getKind()) {
                BLangTupleVarRef tupleVarRef = (BLangTupleVarRef) varRefExpr;
                checkTupleVarRefEquivalency(pos, tupleVarRef, sourceTypes.get(i), rhsPos);
            } else if (NodeKind.ERROR_VARIABLE_REF == varRefExpr.getKind()) {
                BLangErrorVarRef errorVarRef = (BLangErrorVarRef) varRefExpr;
                checkErrorVarRefEquivalency(pos, errorVarRef, sourceTypes.get(i), rhsPos);
            } else if (varRefExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) varRefExpr;
                Name varName = names.fromIdNode(simpleVarRef.variableName);
                if (varName == Names.IGNORE) {
                    continue;
                }
                if (!types.isAssignable(sourceTypes.get(i), targetType)) {
                    dlog.error(rhsPos, DiagnosticCode.INCOMPATIBLE_TYPES, target.type, source);
                    break;
                }
            } else {
                dlog.error(varRefExpr.pos, DiagnosticCode.INVALID_VARIABLE_REFERENCE_IN_BINDING_PATTERN, varRefExpr);
            }
        }
    }

    private void checkErrorVarRefEquivalency(DiagnosticPos pos, BLangErrorVarRef lhsRef, BType rhsType,
                                             DiagnosticPos rhsPos) {
        if (rhsType.tag != TypeTags.ERROR) {
            dlog.error(rhsPos, DiagnosticCode.INCOMPATIBLE_TYPES, symTable.errorType, rhsType);
            return;
        }
        typeChecker.checkExpr(lhsRef, env);
        if (lhsRef.type == symTable.semanticError) {
            return;
        }

        BErrorType expErrorType = (BErrorType) lhsRef.type;

        BErrorType rhsErrorType = (BErrorType) rhsType;
        if (lhsRef.reason.type.tag != TypeTags.NONE) {
            if (!types.isAssignable(rhsErrorType.reasonType, expErrorType.reasonType)) {
                dlog.error(lhsRef.reason.pos, DiagnosticCode.INCOMPATIBLE_TYPES, expErrorType.reasonType,
                        rhsErrorType.reasonType);
            }
        }

        // Wrong error detail type in error type def, error already emitted  to dlog.
        if (rhsErrorType.detailType.tag != TypeTags.RECORD) {
            return;
        }
        BRecordType rhsDetailType = (BRecordType) rhsErrorType.detailType;
        Map<String, BField> fields = rhsDetailType.fields.stream()
                .collect(Collectors.toMap(field -> field.name.value, field -> field));

        BType wideType = interpolateWideType(rhsDetailType, lhsRef.detail);
        for (BLangNamedArgsExpression detailItem : lhsRef.detail) {
            BField matchedDetailItem = fields.get(detailItem.name.value);
            BType matchedType;
            if (matchedDetailItem == null) {
                if (rhsDetailType.sealed) {
                    dlog.error(detailItem.pos, DiagnosticCode.INVALID_FIELD_IN_RECORD_BINDING_PATTERN, detailItem.name);
                    return;
                } else {
                    matchedType = BUnionType.create(null, symTable.nilType, rhsDetailType.restFieldType);
                }
            } else if (Symbols.isOptional(matchedDetailItem.symbol)) {
                matchedType = BUnionType.create(null, symTable.nilType, matchedDetailItem.type);
            } else {
                matchedType = matchedDetailItem.type;
            }

            if (!types.isAssignable(matchedType, detailItem.expr.type)) {
                dlog.error(detailItem.pos, DiagnosticCode.INCOMPATIBLE_TYPES,
                        detailItem.expr.type, matchedType);
            }
            checkErrorDetailRefItem(detailItem.pos, rhsPos, detailItem, matchedType);
        }
        if (lhsRef.restVar != null && !isIgnoreVar(lhsRef)) {
            BMapType expRestType = new BMapType(TypeTags.MAP, wideType, null);
            if (lhsRef.restVar.type.tag != TypeTags.MAP
                    || !types.isAssignable(wideType, ((BMapType) lhsRef.restVar.type).constraint)) {
                dlog.error(lhsRef.restVar.pos, DiagnosticCode.INCOMPATIBLE_TYPES, lhsRef.restVar.type, expRestType);
                return;
            }
            typeChecker.checkExpr(lhsRef.restVar, env);
        }
    }

    private BType interpolateWideType(BRecordType rhsDetailType, List<BLangNamedArgsExpression> detailType) {
        Set<String> extractedKeys = detailType.stream().map(detail -> detail.name.value).collect(Collectors.toSet());

        BUnionType wideType = BUnionType.create(null);
        for (BField field : rhsDetailType.fields) {
            // avoid fields extracted from binding pattern
            if (!extractedKeys.contains(field.name.value)) {
                wideType.add(field.type);
            }
        }
        if (!rhsDetailType.sealed) {
            wideType.add(rhsDetailType.restFieldType);
        }
        return wideType;
    }

    private boolean isIgnoreVar(BLangErrorVarRef lhsRef) {
        if (lhsRef.restVar != null && lhsRef.restVar.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            return ((BLangSimpleVarRef) lhsRef.restVar).variableName.value.equals(Names.IGNORE.value);
        }
        return false;
    }

    private void checkErrorDetailRefItem(DiagnosticPos pos, DiagnosticPos rhsPos, BLangNamedArgsExpression detailItem,
                                         BType expectedType) {
        if (detailItem.expr.getKind() == NodeKind.RECORD_VARIABLE_REF) {
            typeChecker.checkExpr(detailItem.expr, env);
            checkRecordVarRefEquivalency(pos, (BLangRecordVarRef) detailItem.expr, expectedType,
                    rhsPos);
            return;
        }

        if (detailItem.getKind() == NodeKind.SIMPLE_VARIABLE_REF && detailItem.name.value.equals(Names.IGNORE.value)) {
            return;
        }

        setTypeOfVarRefInBindingAssignment(detailItem.expr);
    }

    private void checkConstantAssignment(BLangExpression varRef) {
        if (varRef.type == symTable.semanticError) {
            return;
        }

        if (varRef.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
            return;
        }

        BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) varRef;
        if (simpleVarRef.pkgSymbol != null && simpleVarRef.pkgSymbol.tag == SymTag.XMLNS) {
            dlog.error(varRef.pos, DiagnosticCode.XML_QNAME_UPDATE_NOT_ALLOWED);
            return;
        }

        Name varName = names.fromIdNode(simpleVarRef.variableName);
        if (!Names.IGNORE.equals(varName) && env.enclInvokable != env.enclPkg.initFunction) {
            if ((simpleVarRef.symbol.flags & Flags.FINAL) == Flags.FINAL) {
                dlog.error(varRef.pos, DiagnosticCode.CANNOT_ASSIGN_VALUE_FINAL, varRef);
            } else if ((simpleVarRef.symbol.flags & Flags.CONSTANT) == Flags.CONSTANT) {
                dlog.error(varRef.pos, DiagnosticCode.CANNOT_ASSIGN_VALUE_TO_CONSTANT);
            } else if ((simpleVarRef.symbol.flags & Flags.FUNCTION_FINAL) == Flags.FUNCTION_FINAL) {
                dlog.error(varRef.pos, DiagnosticCode.CANNOT_ASSIGN_VALUE_FUNCTION_ARGUMENT, varRef);
            }
        }
    }

    private void checkReadonlyAssignment(BLangExpression varRef) {
        if (varRef.type == symTable.semanticError) {
            return;
        }

        BLangVariableReference varRefExpr = (BLangVariableReference) varRef;
        if (varRefExpr.symbol != null) {
            if (env.enclPkg.symbol.pkgID != varRefExpr.symbol.pkgID && varRefExpr.lhsVar
                    && (varRefExpr.symbol.flags & Flags.READONLY) == Flags.READONLY) {
                dlog.error(varRefExpr.pos, DiagnosticCode.CANNOT_ASSIGN_VALUE_READONLY, varRefExpr);
            }
        }
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        // Creates a new environment here.
        SymbolEnv stmtEnv = new SymbolEnv(exprStmtNode, this.env.scope);
        this.env.copyTo(stmtEnv);
        BType bType = typeChecker.checkExpr(exprStmtNode.expr, stmtEnv, symTable.noType);
        if (bType != symTable.nilType && bType != symTable.semanticError) {
            dlog.error(exprStmtNode.pos, DiagnosticCode.ASSIGNMENT_REQUIRED);
        }
        validateWorkerAnnAttachments(exprStmtNode.expr);
    }

    @Override
    public void visit(BLangIf ifNode) {
        typeChecker.checkExpr(ifNode.expr, env, symTable.booleanType);
        BType actualType = ifNode.expr.type;
        if (TypeTags.TUPLE == actualType.tag) {
            dlog.error(ifNode.expr.pos, DiagnosticCode.INCOMPATIBLE_TYPES, symTable.booleanType, actualType);
        }

        SymbolEnv ifEnv = typeNarrower.evaluateTruth(ifNode.expr, ifNode.body, env);
        analyzeStmt(ifNode.body, ifEnv);

        if (ifNode.elseStmt != null) {
            SymbolEnv elseEnv = typeNarrower.evaluateFalsity(ifNode.expr, ifNode.elseStmt, env);
            analyzeStmt(ifNode.elseStmt, elseEnv);
        }
    }

    @Override
    public void visit(BLangMatch matchNode) {
        List<BType> exprTypes;
        BType exprType = typeChecker.checkExpr(matchNode.expr, env, symTable.noType);
        if (exprType.tag == TypeTags.UNION) {
            BUnionType unionType = (BUnionType) exprType;
            exprTypes = new ArrayList<>(unionType.getMemberTypes());
        } else {
            exprTypes = Lists.of(exprType);
        }

        matchNode.patternClauses.forEach(patternClause -> {
            patternClause.matchExpr = matchNode.expr;
            patternClause.accept(this);
        });
        matchNode.exprTypes = exprTypes;
    }

    @Override
    public void visit(BLangMatchStaticBindingPatternClause patternClause) {
        checkStaticMatchPatternLiteralType(patternClause.literal);
        analyzeStmt(patternClause.body, this.env);
    }

    private BType checkStaticMatchPatternLiteralType(BLangExpression expression) {

        switch (expression.getKind()) {
            case LITERAL:
            case NUMERIC_LITERAL:
                return typeChecker.checkExpr(expression, this.env);
            case BINARY_EXPR:
                BLangBinaryExpr binaryExpr = (BLangBinaryExpr) expression;

                BType lhsType = checkStaticMatchPatternLiteralType(binaryExpr.lhsExpr);
                BType rhsType = checkStaticMatchPatternLiteralType(binaryExpr.rhsExpr);
                if (lhsType.tag == TypeTags.NONE || rhsType.tag == TypeTags.NONE) {
                    dlog.error(binaryExpr.pos, DiagnosticCode.INVALID_LITERAL_FOR_MATCH_PATTERN);
                    expression.type = symTable.errorType;
                    return expression.type;
                }

                expression.type = symTable.anyType;
                return expression.type;
            case RECORD_LITERAL_EXPR:
                BLangRecordLiteral recordLiteral = (BLangRecordLiteral) expression;
                recordLiteral.type = new BMapType(TypeTags.MAP, symTable.anydataType, null);
                for (BLangRecordLiteral.BLangRecordKeyValue recLiteralKeyValue : recordLiteral.keyValuePairs) {
                    if (isValidRecordLiteralKey(recLiteralKeyValue)) {
                        BType fieldType = checkStaticMatchPatternLiteralType(recLiteralKeyValue.valueExpr);
                        if (fieldType.tag == TypeTags.NONE) {
                            dlog.error(recLiteralKeyValue.valueExpr.pos,
                                    DiagnosticCode.INVALID_LITERAL_FOR_MATCH_PATTERN);
                            expression.type = symTable.errorType;
                            return expression.type;
                        }
                        types.setImplicitCastExpr(recLiteralKeyValue.valueExpr, fieldType, symTable.anyType);
                    } else {
                        recLiteralKeyValue.key.expr.type = symTable.errorType;
                        dlog.error(recLiteralKeyValue.key.expr.pos, DiagnosticCode.INVALID_RECORD_LITERAL_KEY);
                    }
                }
                return recordLiteral.type;
            case LIST_CONSTRUCTOR_EXPR:
                BLangListConstructorExpr listConstructor = (BLangListConstructorExpr) expression;
                List<BType> results = new ArrayList<>();
                for (int i = 0; i < listConstructor.exprs.size(); i++) {
                    BType literalType = checkStaticMatchPatternLiteralType(listConstructor.exprs.get(i));
                    if (literalType.tag == TypeTags.NONE) { // not supporting '_' for now
                        dlog.error(listConstructor.exprs.get(i).pos, DiagnosticCode.INVALID_LITERAL_FOR_MATCH_PATTERN);
                        expression.type = symTable.errorType;
                        return expression.type;
                    }
                    results.add(literalType);
                }
                // since match patterns do not support arrays, this will be treated as an tuple.
                listConstructor.type = new BTupleType(results);
                return listConstructor.type;
            case GROUP_EXPR:
                BLangGroupExpr groupExpr = (BLangGroupExpr) expression;
                BType literalType = checkStaticMatchPatternLiteralType(groupExpr.expression);
                if (literalType.tag == TypeTags.NONE) { // not supporting '_' for now
                    dlog.error(groupExpr.expression.pos, DiagnosticCode.INVALID_LITERAL_FOR_MATCH_PATTERN);
                    expression.type = symTable.errorType;
                    return expression.type;
                }
                groupExpr.type = literalType;
                return groupExpr.type;
            case SIMPLE_VARIABLE_REF:
                // only support "_" in static match
                Name varName = names.fromIdNode(((BLangSimpleVarRef) expression).variableName);
                if (varName == Names.IGNORE) {
                    expression.type = symTable.anyType;
                    return expression.type;
                }
                BType exprType = typeChecker.checkExpr(expression, env);
                if (exprType.tag == TypeTags.SEMANTIC_ERROR ||
                        ((BLangSimpleVarRef) expression).symbol.getKind() != SymbolKind.CONSTANT) {
                    dlog.error(expression.pos, DiagnosticCode.INVALID_LITERAL_FOR_MATCH_PATTERN);
                    expression.type = symTable.noType;
                    return expression.type;
                }
                return exprType;
            default:
                dlog.error(expression.pos, DiagnosticCode.INVALID_LITERAL_FOR_MATCH_PATTERN);
                expression.type = symTable.errorType;
                return expression.type;
        }
    }

    private boolean isValidRecordLiteralKey(BLangRecordLiteral.BLangRecordKeyValue recLiteralKeyValue) {
        NodeKind kind = recLiteralKeyValue.key.expr.getKind();
        return kind == NodeKind.SIMPLE_VARIABLE_REF ||
                ((kind == NodeKind.LITERAL || kind == NodeKind.NUMERIC_LITERAL) &&
                        typeChecker.checkExpr(recLiteralKeyValue.key.expr, this.env).tag == TypeTags.STRING);
    }

    @Override
    public void visit(BLangMatchStructuredBindingPatternClause patternClause) {
        patternClause.bindingPatternVariable.type = patternClause.matchExpr.type;
        patternClause.bindingPatternVariable.expr = patternClause.matchExpr;
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(patternClause.body, env);

        if (patternClause.typeGuardExpr != null) {
            analyzeDef(patternClause.bindingPatternVariable, blockEnv);
            typeChecker.checkExpr(patternClause.typeGuardExpr, blockEnv);
            blockEnv = typeNarrower.evaluateTruth(patternClause.typeGuardExpr, patternClause.body, blockEnv);
        } else {
            analyzeDef(patternClause.bindingPatternVariable, blockEnv);
        }

        analyzeStmt(patternClause.body, blockEnv);
    }

    @Override
    public void visit(BLangForeach foreach) {
        // Check the collection's type.
        typeChecker.checkExpr(foreach.collection, env);
        // Set the type of the foreach node's type node.
        types.setForeachTypedBindingPatternType(foreach);
        // Create a new block environment for the foreach node's body.
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(foreach.body, env);
        // Check foreach node's variables and set types.
        handleForeachVariables(foreach, blockEnv);
        // Analyze foreach node's statements.
        analyzeStmt(foreach.body, blockEnv);
    }

    @Override
    public void visit(BLangWhile whileNode) {
        typeChecker.checkExpr(whileNode.expr, env, symTable.booleanType);

        BType actualType = whileNode.expr.type;
        if (TypeTags.TUPLE == actualType.tag) {
            dlog.error(whileNode.expr.pos, DiagnosticCode.INCOMPATIBLE_TYPES, symTable.booleanType, actualType);
        }

        SymbolEnv whileEnv = typeNarrower.evaluateTruth(whileNode.expr, whileNode.body, env);
        analyzeStmt(whileNode.body, whileEnv);
    }

    @Override
    public void visit(BLangLock lockNode) {
        analyzeStmt(lockNode.body, env);
    }

    @Override
    public void visit(BLangService serviceNode) {
        BServiceSymbol serviceSymbol = (BServiceSymbol) serviceNode.symbol;
        SymbolEnv serviceEnv = SymbolEnv.createServiceEnv(serviceNode, serviceSymbol.scope, env);
        serviceNode.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachPoints.add(AttachPoint.Point.SERVICE);
            this.analyzeDef(annotationAttachment, serviceEnv);
        });
        validateAnnotationAttachmentCount(serviceNode.annAttachments);

        if (serviceNode.isAnonymousServiceValue) {
            return;
        }

        for (BLangExpression attachExpr : serviceNode.attachedExprs) {
            final BType exprType = typeChecker.checkExpr(attachExpr, env);
            if (exprType != symTable.semanticError && !types.checkListenerCompatibility(exprType)) {
                dlog.error(attachExpr.pos, DiagnosticCode.INCOMPATIBLE_TYPES, Names.LISTENER, exprType);
            } else if (exprType != symTable.semanticError && serviceNode.listenerType == null) {
                serviceNode.listenerType = exprType;
            } else if (exprType != symTable.semanticError) {
                this.types.isSameType(exprType, serviceNode.listenerType);
            }

            if (attachExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                final BLangSimpleVarRef attachVarRef = (BLangSimpleVarRef) attachExpr;
                if (attachVarRef.symbol != null && !Symbols.isFlagOn(attachVarRef.symbol.flags, Flags.LISTENER)) {
                    dlog.error(attachVarRef.pos, DiagnosticCode.INVALID_LISTENER_ATTACHMENT);
                }
            } else if (attachExpr.getKind() != NodeKind.TYPE_INIT_EXPR) {
                dlog.error(attachExpr.pos, DiagnosticCode.INVALID_LISTENER_ATTACHMENT);
            }
        }
    }

    private void validateDefaultable(BLangRecordTypeNode recordTypeNode) {
        for (BLangSimpleVariable field : recordTypeNode.fields) {
            if (field.flagSet.contains(Flag.OPTIONAL) && field.expr != null) {
                dlog.error(field.pos, DiagnosticCode.DEFAULT_VALUES_NOT_ALLOWED_FOR_OPTIONAL_FIELDS, field.name.value);
            }
        }
    }

    @Override
    public void visit(BLangResource resourceNode) {
    }

    @Override
    public void visit(BLangTryCatchFinally tryCatchFinally) {
        dlog.error(tryCatchFinally.pos, DiagnosticCode.TRY_STMT_NOT_SUPPORTED);
    }

    @Override
    public void visit(BLangCatch bLangCatch) {
        SymbolEnv catchBlockEnv = SymbolEnv.createBlockEnv(bLangCatch.body, env);
        analyzeNode(bLangCatch.param, catchBlockEnv);
        if (bLangCatch.param.type.tag != TypeTags.ERROR) {
            dlog.error(bLangCatch.param.pos, DiagnosticCode.INCOMPATIBLE_TYPES, symTable.errorType,
                    bLangCatch.param.type);
        }
        analyzeStmt(bLangCatch.body, catchBlockEnv);
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        analyzeStmt(transactionNode.transactionBody, env);
        if (transactionNode.onRetryBody != null) {
            analyzeStmt(transactionNode.onRetryBody, env);
        }

        if (transactionNode.committedBody != null) {
            analyzeStmt(transactionNode.committedBody, env);
        }

        if (transactionNode.abortedBody != null) {
            analyzeStmt(transactionNode.abortedBody, env);
        }

        if (transactionNode.retryCount != null) {
            typeChecker.checkExpr(transactionNode.retryCount, env, symTable.intType);
            checkRetryStmtValidity(transactionNode.retryCount);
        }
        
        // Transaction node will be desugar to lambda function, hence transaction environment scope variables needs to
        // be added as closure variables.
        env.scope.entries
                .values().stream().map(scopeEntry -> scopeEntry.symbol)
                .filter(bSymbol -> bSymbol instanceof BVarSymbol)
                .forEach(bSymbol -> bSymbol.closure = true);
        env.scope.owner.scope.entries
                .values().stream().map(scopeEntry -> scopeEntry.symbol)
                .filter(bSymbol -> bSymbol instanceof BVarSymbol)
                .forEach(bSymbol -> bSymbol.closure = true);
    }

    @Override
    public void visit(BLangAbort abortNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangRetry retryNode) {
        /* ignore */
    }

    private boolean isJoinResultType(BLangSimpleVariable var) {
        BLangType type = var.typeNode;
        if (type instanceof BuiltInReferenceTypeNode) {
            return ((BuiltInReferenceTypeNode) type).getTypeKind() == TypeKind.MAP;
        }
        return false;
    }

    private BLangSimpleVariableDef createVarDef(BLangSimpleVariable var) {
        BLangSimpleVariableDef varDefNode = new BLangSimpleVariableDef();
        varDefNode.var = var;
        varDefNode.pos = var.pos;
        return varDefNode;
    }

    private BLangBlockStmt generateCodeBlock(StatementNode... statements) {
        BLangBlockStmt block = new BLangBlockStmt();
        for (StatementNode stmt : statements) {
            block.addStatement(stmt);
        }
        return block;
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        for (BLangSimpleVariableDef worker : forkJoin.workers) {
            BLangFunction function = ((BLangLambdaFunction) worker.var.expr).function;
            function.symbol.enclForkName = function.anonForkName;
            ((BInvokableSymbol) worker.var.symbol).enclForkName = function.anonForkName;
        }
    }

    @Override
    public void visit(BLangWorker workerNode) {
        SymbolEnv workerEnv = SymbolEnv.createWorkerEnv(workerNode, this.env);
        this.analyzeNode(workerNode.body, workerEnv);
    }

    @Override
    public void visit(BLangEndpoint endpointNode) {
    }


    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        // TODO Need to remove this cached env
        workerSendNode.env = this.env;
        this.typeChecker.checkExpr(workerSendNode.expr, this.env);

        BSymbol symbol = symResolver.lookupSymbol(env, names.fromIdNode(workerSendNode.workerIdentifier), SymTag
                .VARIABLE);

        if (symTable.notFoundSymbol.equals(symbol)) {
            workerSendNode.type = symTable.semanticError;
        } else {
            workerSendNode.type = symbol.type;
        }

        if (workerSendNode.isChannel) {
            dlog.error(workerSendNode.pos, DiagnosticCode.UNDEFINED_ACTION);
        }
    }

    @Override
    public void visit(BLangReturn returnNode) {
        this.typeChecker.checkExpr(returnNode.expr, this.env, this.env.enclInvokable.returnTypeNode.type);
        validateWorkerAnnAttachments(returnNode.expr);
    }

    BType analyzeDef(BLangNode node, SymbolEnv env) {
        return analyzeNode(node, env);
    }

    BType analyzeStmt(BLangStatement stmtNode, SymbolEnv env) {
        return analyzeNode(stmtNode, env);
    }

    BType analyzeNode(BLangNode node, SymbolEnv env) {
        return analyzeNode(node, env, symTable.noType, null);
    }

    @Override
    public void visit(BLangContinue continueNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangBreak breakNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangThrow throwNode) {
        dlog.error(throwNode.pos, DiagnosticCode.THROW_STMT_NOT_SUPPORTED);
    }

    @Override
    public void visit(BLangPanic panicNode) {
        this.typeChecker.checkExpr(panicNode.expr, env, symTable.errorType);
    }

    BType analyzeNode(BLangNode node, SymbolEnv env, BType expType, DiagnosticCode diagCode) {
        this.prevEnvs.push(this.env);
        BType preExpType = this.expType;
        DiagnosticCode preDiagCode = this.diagCode;

        // TODO Check the possibility of using a try/finally here
        this.env = env;
        this.expType = expType;
        this.diagCode = diagCode;
        node.accept(this);
        this.env = this.prevEnvs.pop();
        this.expType = preExpType;
        this.diagCode = preDiagCode;

        return resType;
    }

    @Override
    public void visit(BLangForever foreverStatement) {
        streamsQuerySemanticAnalyzer.analyze(foreverStatement, env);
    }

    @Override
    public void visit(BLangConstant constant) {
        if (names.fromIdNode(constant.name) == Names.IGNORE) {
            dlog.error(constant.name.pos, DiagnosticCode.UNDERSCORE_NOT_ALLOWED);
        }
        if (constant.typeNode != null && !types.isAllowedConstantType(constant.typeNode.type)) {
            dlog.error(constant.typeNode.pos, DiagnosticCode.CANNOT_DEFINE_CONSTANT_WITH_TYPE, constant.typeNode);
        }


        constant.annAttachments.forEach(annotationAttachment -> {
            annotationAttachment.attachPoints.add(AttachPoint.Point.CONST);
            annotationAttachment.accept(this);
        });

        BLangExpression expression = constant.expr;
        if (!(expression.getKind() == LITERAL || expression.getKind() == NUMERIC_LITERAL)
                && constant.typeNode == null) {
            constant.type = symTable.semanticError;
            dlog.error(expression.pos, DiagnosticCode.TYPE_REQUIRED_FOR_CONST_WITH_EXPRESSIONS);
            return; // This has to return, because constant.symbol.type is required for further validations.
        }

        typeChecker.checkExpr(expression, env, constant.symbol.type);

        // Check nested expressions.
        constantAnalyzer.visit(constant);
    }

    // TODO: 7/10/19 Remove this once const support is added for lists. A separate method is introduced temporarily
    //  since we allow array/tuple literals with cont exprs for annotations
    private void checkAnnotConstantExpression(BLangExpression expression) {
        // Recursively check whether all the nested expressions in the provided expression are constants or can be
        // evaluated to constants.
        switch (expression.getKind()) {
            case LITERAL:
            case NUMERIC_LITERAL:
                break;
            case SIMPLE_VARIABLE_REF:
                BSymbol symbol = ((BLangSimpleVarRef) expression).symbol;
                // Symbol can be null in some invalid scenarios. Eg - const string m = { name: "Ballerina" };
                if (symbol != null && (symbol.tag & SymTag.CONSTANT) != SymTag.CONSTANT) {
                    dlog.error(expression.pos, DiagnosticCode.EXPRESSION_IS_NOT_A_CONSTANT_EXPRESSION);
                }
                break;
            case RECORD_LITERAL_EXPR:
                ((BLangRecordLiteral) expression).keyValuePairs.forEach(pair -> {
                    checkAnnotConstantExpression(pair.key.expr);
                    checkAnnotConstantExpression(pair.valueExpr);
                });
                break;
            case LIST_CONSTRUCTOR_EXPR:
                ((BLangListConstructorExpr) expression).exprs.forEach(this::checkAnnotConstantExpression);
                break;
            case FIELD_BASED_ACCESS_EXPR:
                checkAnnotConstantExpression(((BLangFieldBasedAccess) expression).expr);
                break;
            default:
                dlog.error(expression.pos, DiagnosticCode.EXPRESSION_IS_NOT_A_CONSTANT_EXPRESSION);
                break;
        }
    }

    private void handleForeachVariables(BLangForeach foreachStmt, SymbolEnv blockEnv) {
        BLangVariable variableNode = (BLangVariable) foreachStmt.variableDefinitionNode.getVariable();
        // Check whether the foreach node's variables are declared with var.
        if (foreachStmt.isDeclaredWithVar) {
            // If the foreach node's variables are declared with var, type is `varType`.
            handleDeclaredVarInForeach(variableNode, foreachStmt.varType, blockEnv);
            return;
        }
        // If the type node is available, we get the type from it.
        BType typeNodeType = symResolver.resolveTypeNode(variableNode.typeNode, blockEnv);
        // Then we need to check whether the RHS type is assignable to LHS type.
        if (types.isAssignable(foreachStmt.varType, typeNodeType)) {
            // If assignable, we set types to the variables.
            handleDeclaredVarInForeach(variableNode, foreachStmt.varType, blockEnv);
            return;
        }
        // Log an error and define a symbol with the node's type to avoid undeclared symbol errors.
        dlog.error(variableNode.typeNode.pos, DiagnosticCode.INCOMPATIBLE_TYPES, foreachStmt.varType, typeNodeType);
        handleDeclaredVarInForeach(variableNode, typeNodeType, blockEnv);
    }

    private void checkRetryStmtValidity(BLangExpression retryCountExpr) {
        boolean error = true;
        NodeKind retryKind = retryCountExpr.getKind();
        if (retryKind == LITERAL || retryKind == NUMERIC_LITERAL) {
            if (retryCountExpr.type.tag == TypeTags.INT) {
                int retryCount = Integer.parseInt(((BLangLiteral) retryCountExpr).getValue().toString());
                if (retryCount >= 0) {
                    error = false;
                }
            }
        } else if (retryKind == NodeKind.SIMPLE_VARIABLE_REF) {
            if (((BLangSimpleVarRef) retryCountExpr).symbol.flags == Flags.FINAL) {
                if (((BLangSimpleVarRef) retryCountExpr).symbol.type.tag == TypeTags.INT) {
                    error = false;
                }
            }
        }
        if (error) {
            this.dlog.error(retryCountExpr.pos, DiagnosticCode.INVALID_RETRY_COUNT);
        }
    }

    private void checkTransactionHandlerValidity(BLangExpression transactionHanlder) {
        if (transactionHanlder != null) {
            BSymbol handlerSymbol = ((BLangSimpleVarRef) transactionHanlder).symbol;
            if (handlerSymbol != null && handlerSymbol.kind != SymbolKind.FUNCTION) {
                dlog.error(transactionHanlder.pos, DiagnosticCode.INVALID_FUNCTION_POINTER_ASSIGNMENT_FOR_HANDLER);
            }
            if (transactionHanlder.type.tag == TypeTags.INVOKABLE) {
                BInvokableType handlerType = (BInvokableType) transactionHanlder.type;
                int parameterCount = handlerType.paramTypes.size();
                if (parameterCount != 1) {
                    dlog.error(transactionHanlder.pos, DiagnosticCode.INVALID_TRANSACTION_HANDLER_ARGS);
                }
                if (handlerType.paramTypes.get(0).tag != TypeTags.STRING) {
                    dlog.error(transactionHanlder.pos, DiagnosticCode.INVALID_TRANSACTION_HANDLER_ARGS);
                }
                if (handlerType.retType.tag != TypeTags.NIL) {
                    dlog.error(transactionHanlder.pos, DiagnosticCode.INVALID_TRANSACTION_HANDLER_SIGNATURE);
                }
            } else {
                dlog.error(transactionHanlder.pos, DiagnosticCode.LAMBDA_REQUIRED_FOR_TRANSACTION_HANDLER);
            }
        }
    }

    private BLangExpression getBinaryExpr(BLangExpression lExpr,
                                          BLangExpression rExpr,
                                          OperatorKind opKind,
                                          BSymbol opSymbol) {
        BLangBinaryExpr binaryExpressionNode = (BLangBinaryExpr) TreeBuilder.createBinaryExpressionNode();
        binaryExpressionNode.lhsExpr = lExpr;
        binaryExpressionNode.rhsExpr = rExpr;
        binaryExpressionNode.pos = rExpr.pos;
        binaryExpressionNode.opKind = opKind;
        if (opSymbol != symTable.notFoundSymbol) {
            binaryExpressionNode.type = opSymbol.type.getReturnType();
            binaryExpressionNode.opSymbol = (BOperatorSymbol) opSymbol;
        } else {
            binaryExpressionNode.type = symTable.semanticError;
        }
        return binaryExpressionNode;
    }

    private boolean validateVariableDefinition(BLangExpression expr) {
        // following cases are invalid.
        // var a = { x : y };
        // var a = new ;
        final NodeKind kind = expr.getKind();
        if (kind == RECORD_LITERAL_EXPR || (kind == NodeKind.TYPE_INIT_EXPR
                && ((BLangTypeInit) expr).userDefinedType == null)) {
            dlog.error(expr.pos, DiagnosticCode.INVALID_ANY_VAR_DEF);
            return false;
        }
        return true;
    }

    private void setTypeOfVarRefInBindingAssignment(BLangExpression expr) {
        // In binding assignments, lhs supports only simple, record, error, tuple varRefs.
        if (expr.getKind() != NodeKind.SIMPLE_VARIABLE_REF
                && expr.getKind() != NodeKind.RECORD_VARIABLE_REF
                && expr.getKind() != NodeKind.ERROR_VARIABLE_REF
                && expr.getKind() != NodeKind.TUPLE_VARIABLE_REF) {
            dlog.error(expr.pos, DiagnosticCode.INVALID_VARIABLE_REFERENCE_IN_BINDING_PATTERN, expr);
            expr.type = symTable.semanticError;
        }
        setTypeOfVarRef(expr);
    }

    private void setTypeOfVarRefInAssignment(BLangExpression expr) {
        // In assignments, lhs supports only simple, record, error, tuple
        // varRefs and field, xml and index based access expressions.
        if (expr.getKind() != NodeKind.SIMPLE_VARIABLE_REF
                && expr.getKind() != NodeKind.INDEX_BASED_ACCESS_EXPR
                && expr.getKind() != NodeKind.FIELD_BASED_ACCESS_EXPR
                && expr.getKind() != NodeKind.XML_ATTRIBUTE_ACCESS_EXPR
                && expr.getKind() != NodeKind.RECORD_VARIABLE_REF
                && expr.getKind() != NodeKind.ERROR_VARIABLE_REF
                && expr.getKind() != NodeKind.TUPLE_VARIABLE_REF) {
            dlog.error(expr.pos, DiagnosticCode.INVALID_VARIABLE_ASSIGNMENT, expr);
            expr.type = symTable.semanticError;
        }
        setTypeOfVarRef(expr);
    }

    private void setTypeOfVarRef(BLangExpression expr) {
        BLangVariableReference varRefExpr = (BLangVariableReference) expr;
        varRefExpr.lhsVar = true;
        typeChecker.checkExpr(varRefExpr, env);

        // Check whether this is an readonly field.
        checkReadonlyAssignment(varRefExpr);
        checkConstantAssignment(varRefExpr);

        // If this is an update of a type narrowed variable, the assignment should allow assigning
        // values of its original type. Therefore treat all lhs simpleVarRefs in their original type.
        if (isSimpleVarRef(expr)) {
            BVarSymbol originSymbol = ((BVarSymbol) ((BLangSimpleVarRef) expr).symbol).originalSymbol;
            if (originSymbol != null) {
                varRefExpr.type = originSymbol.type;
            }
        }
    }

    private void validateAnnotationAttachmentExpr(BLangAnnotationAttachment annAttachmentNode,
                                                  BAnnotationSymbol annotationSymbol) {
        if (annotationSymbol.attachedType == null ||
                types.isAssignable(annotationSymbol.attachedType.type, symTable.trueType)) {
            if (annAttachmentNode.expr != null) {
                this.dlog.error(annAttachmentNode.pos, DiagnosticCode.ANNOTATION_ATTACHMENT_CANNOT_HAVE_A_VALUE,
                                annotationSymbol.name);
            }
            return;
        }


        // At this point the type is a subtype of  map<anydata>|record{ anydata...; } or
        // map<anydata>[]|record{ anydata...; }[], thus an expression is required.
        if (annAttachmentNode.expr == null) {
            this.dlog.error(annAttachmentNode.pos, DiagnosticCode.ANNOTATION_ATTACHMENT_REQUIRES_A_VALUE,
                            annotationSymbol.name);
            return;
        }

        BType annotType = annotationSymbol.attachedType.type;
        this.typeChecker.checkExpr(annAttachmentNode.expr, env,
                                   annotType.tag == TypeTags.ARRAY ? ((BArrayType) annotType).eType : annotType);

        if (Symbols.isFlagOn(annotationSymbol.flags, Flags.CONSTANT)) {
            if (annotationSymbol.points.stream().anyMatch(attachPoint -> !attachPoint.source)) {
                constantAnalyzer.analyzeExpr(annAttachmentNode.expr);
                return;
            }
            checkAnnotConstantExpression(annAttachmentNode.expr);
        }
    }

    private void validateAnnotationAttachmentCount(List<BLangAnnotationAttachment> attachments) {
        Map<BAnnotationSymbol, Integer> attachmentCounts = new HashMap<>();
        for (BLangAnnotationAttachment attachment : attachments) {
            if (attachment.annotationSymbol == null) {
                continue;
            }

            attachmentCounts.merge(attachment.annotationSymbol, 1, Integer::sum);
        }

        attachmentCounts.forEach((symbol, count) -> {
            if ((symbol.attachedType == null || symbol.attachedType.type.tag != TypeTags.ARRAY) && count > 1) {
                this.dlog.error(attachments.stream()
                                        .filter(attachment -> attachment.annotationSymbol.equals(symbol))
                                        .findFirst()
                                        .get().pos,
                                DiagnosticCode.ANNOTATION_ATTACHMENT_CANNOT_SPECIFY_MULTIPLE_VALUES,
                                symbol.name);
            }
        });
    }

    /**
     * Validate functions attached to objects.
     *
     * @param funcNode Function node
     */
    private void validateObjectAttachedFunction(BLangFunction funcNode) {
        if (!funcNode.attachedFunction) {
            return;
        }

        // If the function is attached to an abstract object, it don't need to have an implementation.
        if (Symbols.isFlagOn(funcNode.receiver.type.tsymbol.flags, Flags.ABSTRACT)) {
            if (funcNode.body != null) {
                dlog.error(funcNode.pos, DiagnosticCode.ABSTRACT_OBJECT_FUNCTION_CANNOT_HAVE_BODY, funcNode.name,
                        funcNode.receiver.type);
            }
            return;
        }

        // There must be an implementation at the outer level, if the function is an interface.
        if (funcNode.interfaceFunction && !env.enclPkg.objAttachedFunctions.contains(funcNode.symbol)) {
            dlog.error(funcNode.pos, DiagnosticCode.INVALID_INTERFACE_ON_NON_ABSTRACT_OBJECT, funcNode.name,
                    funcNode.receiver.type);
        }
    }

    private void validateReferencedFunction(DiagnosticPos pos, BAttachedFunction func, SymbolEnv env) {
        if (Symbols.isFlagOn(func.symbol.receiverSymbol.type.tsymbol.flags, Flags.ABSTRACT)) {
            return;
        }

        if (!Symbols.isFunctionDeclaration(func.symbol)) {
            return;
        }

        // There must be an implementation at the outer level, if the function is an interface.
        if (!env.enclPkg.objAttachedFunctions.contains(func.symbol)) {
            dlog.error(pos, DiagnosticCode.INVALID_INTERFACE_ON_NON_ABSTRACT_OBJECT, func.funcName,
                    func.symbol.receiverSymbol.type);
        }
    }

    private boolean isSimpleVarRef(BLangExpression expr) {
        if (expr.type.tag == TypeTags.SEMANTIC_ERROR ||
                expr.type.tag == TypeTags.NONE ||
                expr.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
            return false;
        }

        if (((BLangSimpleVarRef) expr).symbol == null) {
            return false;
        }

        return (((BLangSimpleVarRef) expr).symbol.tag & SymTag.VARIABLE) == SymTag.VARIABLE;
    }

    private void resetTypeNarrowing(BLangExpression lhsExpr, BLangExpression rhsExpr) {
        if (!isSimpleVarRef(lhsExpr)) {
            return;
        }

        BVarSymbol varSymbol = (BVarSymbol) ((BLangSimpleVarRef) lhsExpr).symbol;
        if (varSymbol.originalSymbol == null) {
            return;
        }

        // If the rhs's type is not assignable to the variable's narrowed type,
        // then the type narrowing will no longer hold. Thus define the original
        // symbol in all the scopes that are affected by this assignment.
        if (!types.isAssignable(rhsExpr.type, varSymbol.type)) {
            defineOriginalSymbol(lhsExpr, varSymbol.originalSymbol, env);
            env = prevEnvs.peek();
        }
    }

    private void defineOriginalSymbol(BLangExpression lhsExpr, BVarSymbol varSymbol, SymbolEnv env) {
        BSymbol foundSym = symResolver.lookupSymbol(env, varSymbol.name, varSymbol.tag);

        // Terminate if we reach the env where the original symbol available
        if (foundSym == varSymbol) {
            return;
        }

        // Traverse back to all the fall-back-environments, and update the env with the new symbol.
        // Here the existing fall-back env will be replaced by a new env.
        // i.e: [new fall-back env] = [snapshot of old fall-back env] + [new symbol]
        env = SymbolEnv.createTypeNarrowedEnv(lhsExpr, env);
        symbolEnter.defineTypeNarrowedSymbol(lhsExpr.pos, env, varSymbol, varSymbol.type);
        SymbolEnv prevEnv = prevEnvs.pop();
        defineOriginalSymbol(lhsExpr, varSymbol, prevEnv);
        prevEnvs.push(env);
    }
}
