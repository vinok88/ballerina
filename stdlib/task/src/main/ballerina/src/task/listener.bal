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

import ballerina/lang.'object as lang;

# Represents a ballerina task listener, which can be used to schedule and execute tasks periodically.
public type Listener object {
    *lang:Listener;
    boolean started = false;

    private TimerConfiguration|AppointmentConfiguration listenerConfiguration;

    public function __init(TimerConfiguration|AppointmentConfiguration configs) {
        if (configs is TimerConfiguration) {
            if (configs["initialDelayInMillis"] == ()) {
                configs.initialDelayInMillis = configs.intervalInMillis;
            }
        }
        self.listenerConfiguration = configs;
        var initResult = self.init();
        if (initResult is error) {
            panic initResult;
        }
    }

    public function __attach(service s, string? name = ()) returns error? {
        // ignore param 'name'
        var result = self.register(s, {});
        if (result is error) {
            panic result;
        }
    }

    public function __detach(service s) returns error? {
        return self.detach(s);
    }

    public function __start() returns error? {
        var result = self.start();
        if (result is error) {
            panic result;
        }
        lock {
            self.started = true;
        }
    }

    public function __gracefulStop() returns error? {
        return ();
    }

    public function __immediateStop() returns error? {
        var result = self.stop();
        if (result is error) {
            panic result;
        }
        lock {
            self.started = false;
        }
    }

    function init() returns ListenerError? = external;

    function register(service s, map<any> config) returns ListenerError? = external;

    function start() returns ListenerError? = external;

    function stop() returns ListenerError? = external;

    function detach(service attachedService) returns ListenerError? = external;

    function isStarted() returns boolean {
        return self.started;
    }

    # Pauses the task listener.
    #
    # + return - Returns `task:ListenerError` if an error is occurred while resuming, nil Otherwise.
    public function pause() returns ListenerError? = external;

    # Resumes a paused task listener.
    #
    # + return - Returns `task:ListenerError` when an error occurred while pausing, nil Otherwise.
    public function resume() returns ListenerError? = external;
};
