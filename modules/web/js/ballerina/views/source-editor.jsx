/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import 'brace';
import 'brace/ext/language_tools';
import 'brace/ext/searchbox';
import React from 'react';
import PropTypes from 'prop-types';
import log from 'log';
import _ from 'lodash';
import commandManager from 'command';
import File from './../../workspace/file';
import SourceGenVisitor from './../visitors/source-gen/ballerina-ast-root-visitor';
import EnableDefaultWSVisitor from './../visitors/source-gen/enable-default-ws-visitor';
import SourceViewCompleterFactory from './../../ballerina/utils/source-view-completer-factory';
import { getLangServerClientInstance } from './../../langserver/lang-server-client-controller';
import { CHANGE_EVT_TYPES } from './constants';
import { CONTENT_MODIFIED } from './../../constants/events';
import { FORMAT } from './../../constants/commands';
import { parseFile } from './../../api-client/api-client';
import BallerinaASTDeserializer from './../ast/ballerina-ast-deserializer';
import debuggerHOC from '../../debugger/debugger-hoc';

const ace = global.ace;
const Range = ace.acequire('ace/range').Range;
const AceUndoManager = ace.acequire('ace/undomanager').UndoManager;

// require ballerina mode
const langTools = ace.acequire('ace/ext/language_tools');

const ballerinaMode = 'ace/mode/ballerina';
// load ballerina mode
ace.acequire(ballerinaMode);

// require possible themes
function requireAll(requireContext) {
    return requireContext.keys().map(requireContext);
}
requireAll(require.context('ace', false, /theme-/));

// ace look & feel configurations FIXME: Make this overridable from settings
const aceTheme = 'ace/theme/twilight';
const fontSize = '14px';
const scrollMargin = 20;

// override default undo manager of ace editor
class NotifyingUndoManager extends AceUndoManager {
    constructor(sourceView) {
        super();
        this.sourceView = sourceView;
    }
    execute(args) {
        super.execute(args);
        if (!this.sourceView.skipFileUpdate) {
            const changeEvent = {
                type: CHANGE_EVT_TYPES.SOURCE_MODIFIED,
                title: 'Modify source',
            };
            this.sourceView.props.file
                .setContent(this.sourceView.editor.session.getValue(), changeEvent);
        }
        this.sourceView.skipFileUpdate = false;
    }
}

class SourceView extends React.Component {

    constructor(props) {
        super(props);
        this.container = undefined;
        this.editor = undefined;
        this.inSilentMode = false;
        this.format = this.format.bind(this);
        this.sourceViewCompleterFactory = new SourceViewCompleterFactory();
    }

    /**
     * lifecycle hook for component did mount
     */
    componentDidMount() {
        if (!_.isNil(this.container)) {
            // initialize ace editor
            const editor = ace.edit(this.container);
            editor.getSession().setMode(ballerinaMode);
            editor.getSession().setUndoManager(new NotifyingUndoManager(this));
            editor.getSession().setValue(this.props.file.getContent());
            editor.setShowPrintMargin(false);
            // Avoiding ace warning
            editor.$blockScrolling = Infinity;
            editor.setTheme(aceTheme);
            editor.setFontSize(fontSize);
            editor.setOptions({
                enableBasicAutocompletion: true,
            });
            editor.setBehavioursEnabled(true);
            // bind auto complete to key press
            editor.commands.on('afterExec', (e) => {
                if (e.command.name === 'insertstring' && /^[\w.@:]$/.test(e.args)) {
                    setTimeout(() => {
                        try {
                            editor.execCommand('startAutocomplete');
                        } finally {
                            // nothing
                        }
                    }, 10);
                }
            });
            editor.renderer.setScrollMargin(scrollMargin, scrollMargin);
            this.editor = editor;
            // bind app keyboard shortcuts to ace editor
            this.props.commandManager.getCommands().forEach((command) => {
                this.bindCommand(command);
            });
            // register handler for source format command
            this.props.commandManager.registerHandler(FORMAT, this.format, this);
            // listen to changes done to file content
            // by other means (eg: design-view changes or redo/undo actions)
            // and update ace content accordingly
            this.props.file.on(CONTENT_MODIFIED, (evt) => {
                if (evt.originEvt.type !== CHANGE_EVT_TYPES.SOURCE_MODIFIED) {
                    // no need to update the file again, hence
                    // the second arg to skip update event
                    this.replaceContent(evt.newContent, true);
                }
            });

            editor.on('guttermousedown', (e) => {
                const target = e.domEvent.target;
                if (target.className.indexOf('ace_gutter-cell') === -1) {
                    return;
                }
                if (!editor.isFocused()) {
                    return;
                }

                const row = e.getDocumentPosition().row;
                const breakpoints = e.editor.session.getBreakpoints(row, 0);
                if (!breakpoints[row]) {
                    this.props.addBreakpoint(row + 1);
                    e.editor.session.setBreakpoint(row);
                } else {
                    this.props.removeBreakpoint(row + 1);
                    e.editor.session.clearBreakpoint(row);
                }
            });
        }
    }

    /**
     * format handler
     */
    format() {
        parseFile(this.props.file)
            .then((parserRes) => {
                const ast = BallerinaASTDeserializer.getASTModel(parserRes);
                const enableDefaultWSVisitor = new EnableDefaultWSVisitor();
                ast.accept(enableDefaultWSVisitor);
                const sourceGenVisitor = new SourceGenVisitor();
                ast.accept(sourceGenVisitor);
                const formattedContent = sourceGenVisitor.getGeneratedSource();
                // Note the second arg. We need to inform others about this change.
                // Eg: undo manager should track this and the file should be updated.
                this.replaceContent(formattedContent, false);
            })
            .catch(error => log.error(error));
    }

    /**
     * Replace content of the editor while maintaining history
     *
     * @param {*} newContent content to insert
     */
    replaceContent(newContent, skipFileUpdate) {
        if (skipFileUpdate) {
            this.skipFileUpdate = true;
        }
        const session = this.editor.getSession();
        const contentRange = new Range(0, 0, session.getLength(),
                        session.getRowLength(session.getLength()));
        session.replace(contentRange, newContent);
    }

    shouldComponentUpdate() {
        // update ace editor - https://github.com/ajaxorg/ace/issues/1245
        this.editor.resize(true);
        // keep this component unaffected from react re-render
        return false;
    }

    /**
     * Binds a shortcut to ace editor so that it will trigger the command on source view upon key press.
     * All the commands registered app's command manager will be bound to source view upon render.
     *
     * @param command {Object}
     * @param command.id {String} Id of the command to dispatch
     * @param command.shortcuts {Object}
     * @param command.shortcuts.mac {Object}
     * @param command.shortcuts.mac.key {String} key combination for mac platform eg. 'Command+N'
     * @param command.shortcuts.other {Object}
     * @param command.shortcuts.other.key {String} key combination for other platforms eg. 'Ctrl+N'
     */
    bindCommand(command) {
        const id = command.id;
        const hasShortcut = _.has(command, 'shortcuts');
        const self = this;
        if (hasShortcut) {
            const macShortcut = _.replace(command.shortcuts.mac.key, '+', '-');
            const winShortcut = _.replace(command.shortcuts.other.key, '+', '-');
            this.editor.commands.addCommand({
                name: id,
                bindKey: { win: winShortcut, mac: macShortcut },
                exec() {
                    self.props.commandManager.dispatch(id);
                },
            });
        }
    }

    render() {
        return (
            <div className='text-editor' ref={(ref) => { this.container = ref; }} />
        );
    }

    /**
     * lifecycle hook for component will receive props
     */
    componentWillReceiveProps(nextProps) {
        if (!nextProps.parseFailed) {
            getLangServerClientInstance()
                .then((langserverClient) => {
                    // Set source view completer
                    const sourceViewCompleterFactory = this.sourceViewCompleterFactory;
                    const fileData = { fileName: nextProps.file.getName(),
                        filePath: nextProps.file.getPath(),
                        packageName: nextProps.file.getPackageName() };
                    const completer = sourceViewCompleterFactory.getSourceViewCompleter(langserverClient, fileData);
                    langTools.setCompleters(completer);
                })
                .catch(error => log.error(error));
        }

        const { debugHit, sourceViewBreakpoints } = nextProps;
        if (debugHit > 0) {
            if (this.debugPointMarker) {
                this.editor.getSession().removeMarker(this.debugPointMarker);
            }
            this.debugPointMarker = this.editor.getSession().addMarker(
                new Range(debugHit, 0, debugHit, 2000), 'debug-point-hit', 'line', true);
        }
        if (!debugHit && this.debugPointMarker) {
            this.editor.getSession().removeMarker(this.debugPointMarker);
        }

        this.editor.getSession().setBreakpoints(sourceViewBreakpoints);
    }
}

SourceView.propTypes = {
    file: PropTypes.instanceOf(File).isRequired,
    commandManager: PropTypes.instanceOf(commandManager).isRequired,
    parseFailed: PropTypes.bool.isRequired,
    sourceViewBreakpoints: PropTypes.arrayOf(Number).isRequired,
    addBreakpoint: PropTypes.func.isRequired,
    removeBreakpoint: PropTypes.func.isRequired,
    debugHit: PropTypes.number,
};

SourceView.defaultProps = {
    debugHit: null,
};

SourceView.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
};

export default debuggerHOC(SourceView);
