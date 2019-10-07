/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.packaging;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Testing pushing a module from central by specifying the access token as an environment variable.
 *
 * @since 0.990.5
 */
public class ModulePushTestCase extends BaseTest {
    private Path tempProjectDirectory;
    private String moduleName = "test";
    private Map<String, String> envVariables;
    private BMainInstance balClient;

    @BeforeClass()
    public void setUp() throws IOException, BallerinaTestException {
        tempProjectDirectory = Files.createTempDirectory("bal-test-integration-packaging-project-");
        moduleName = moduleName + PackerinaTestUtils.randomModuleName(8);
        envVariables = addEnvVariables(PackerinaTestUtils.getEnvVariables());
        balClient = new BMainInstance(balServer);
    }

    @Test(description = "Test pushing a package to central by specifying the access token as an environment variable",
            groups = "brokenOnPackerinaChanges")
    public void testPush() throws Exception {
        Path projectPath = tempProjectDirectory.resolve("initPushProject");
        
        // Org-name
        String orgName = "bcintegrationtest";
    
        // Create project
        balClient.runMain("new", new String[]{"initPushProject"}, envVariables, new String[]{}, new LogLeecher[]{},
                projectPath.getParent().toString());
    
        // Update org name
        PackerinaTestUtils.updateManifestOrgName(projectPath, orgName);
    
        Assert.assertTrue(Files.exists(projectPath));
        Assert.assertTrue(Files.isDirectory(projectPath));
    
        // Create module
        balClient.runMain("add", new String[]{moduleName}, envVariables, new String[]{}, new LogLeecher[]{},
                projectPath.toString());
    
        Assert.assertTrue(Files.exists(projectPath.resolve("src").resolve(moduleName)));
        Assert.assertTrue(Files.isDirectory(projectPath.resolve("src").resolve(moduleName)));
    
        balClient.runMain("build", new String[]{"-c", moduleName}, envVariables, new String[]{},
                new LogLeecher[]{}, projectPath.toString());
        
        // Push the module to Ballerina Central
        String msg = orgName + "/" + moduleName + ":0.1.0 [project repo -> central]";
        LogLeecher logLeecher = new LogLeecher(msg);
        balClient.runMain("push", new String[]{moduleName}, envVariables, new String[]{}, new LogLeecher[]{logLeecher},
                projectPath.toString());
        logLeecher.waitForText(5000);
    }

    /**
     * Get environment variables.
     *
     * @return env directory variable array
     */
    private Map<String, String> addEnvVariables(Map<String, String> envVariables) {
        envVariables.put("BALLERINA_DEV_STAGE_CENTRAL", "true");
        envVariables.put("BALLERINA_CENTRAL_ACCESS_TOKEN", PackerinaTestUtils.getToken());
        return envVariables;
    }

    @AfterClass
    private void cleanup() throws Exception {
        PackerinaTestUtils.deleteFiles(tempProjectDirectory);
    }
}
