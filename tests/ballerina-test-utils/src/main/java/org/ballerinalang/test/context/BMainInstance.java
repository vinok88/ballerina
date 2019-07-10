/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.context;

import org.apache.commons.lang3.ArrayUtils;
import org.ballerinalang.test.util.BCompileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class hold the server information and manage the a server instance.
 *
 * @since 0.982.0
 */
public class BMainInstance implements BMain {
    private static final Logger log = LoggerFactory.getLogger(BMainInstance.class);
    private static final String JAVA_OPTS = "JAVA_OPTS";
    private String agentArgs = "";
    private BalServer balServer;

    public BMainInstance(BalServer balServer) throws BallerinaTestException {
        this.balServer = balServer;
        initialize();
    }

    /**
     * Initialize the server instance with properties.
     *
     * @throws BallerinaTestException when an exception is thrown while initializing the server
     */
    private void initialize() throws BallerinaTestException {
        configureAgentArgs();
    }

    private void configureAgentArgs() throws BallerinaTestException {
        String jacocoArgLine = System.getProperty("jacoco.agent.argLine");
        if (jacocoArgLine == null || jacocoArgLine.isEmpty()) {
            log.warn("Running integration test without jacoco test coverage");
            return;
        }
        agentArgs = jacocoArgLine + " ";
    }

    @Override
    public void runMain(String balFile) throws BallerinaTestException {
        runMain(balFile, new String[]{}, new String[]{});
    }

    @Override
    public void runMain(String balFile, LogLeecher[] leechers) throws BallerinaTestException {
        runMain(balFile, new String[]{}, new String[]{}, leechers);
    }

    @Override
    public void runMain(String balFile, String[] flags, String[] args) throws BallerinaTestException {
        runMain(balFile, flags, args, null, null);
    }

    @Override
    public void runMain(String balFile, String[] flags,
                        String[] args, LogLeecher[] leechers) throws BallerinaTestException {
        runMain(balFile, flags, args, null, new String[]{}, leechers);
    }

    @Override
    public void runMain(String balFile, String[] flags, String[] args, Map<String, String> envProperties,
                        String[] clientArgs) throws BallerinaTestException {
        runMain(balFile, flags, args, envProperties, clientArgs, null);
    }

    @Override
    public void runMain(String balFile, String[] flags, String[] args, Map<String, String> envProperties,
                        String[] clientArgs, LogLeecher[] leechers) throws BallerinaTestException {
        if (balFile == null || balFile.isEmpty()) {
            throw new IllegalArgumentException("Invalid ballerina program file name provided, name - " + balFile);
        }

        if (args == null) {
            args = new String[]{};
        }

        if (envProperties == null) {
            envProperties = new HashMap<>();
        }

        String[] newArgs = ArrayUtils.addAll(flags, balFile);
        newArgs = ArrayUtils.addAll(newArgs, args);

        addJavaAgents(envProperties);

        runMain("run", newArgs, envProperties, clientArgs, leechers, balServer.getServerHome());
    }

    @Override
    public void runMain(String sourceRoot, String packagePath) throws BallerinaTestException {
        runMain(sourceRoot, packagePath, new String[]{}, new String[]{});
    }

    @Override
    public void runMain(String sourceRoot, String packagePath, LogLeecher[] leechers) throws BallerinaTestException {
        runMain(sourceRoot, packagePath, new String[]{}, new String[]{}, leechers);
    }

    @Override
    public void runMain(String sourceRoot, String packagePath,
                        String[] flags, String[] args) throws BallerinaTestException {
        runMain(sourceRoot, packagePath, flags, args, null, null);
    }

    @Override
    public void runMain(String sourceRoot, String packagePath, String[] flags, String[] args,
                        LogLeecher[] leechers) throws BallerinaTestException {
        runMain(sourceRoot, packagePath, flags, args, null, new String[]{}, leechers);
    }

    @Override
    public void runMain(String sourceRoot, String packagePath, String[] flags, String[] args,
                        Map<String, String> envProperties, String[] clientArgs) throws BallerinaTestException {
        runMain(sourceRoot, packagePath, flags, args, envProperties, clientArgs, null);
    }

    @Override
    public void runMain(String sourceRoot, String packagePath,
                        String[] flags, String[] args, Map<String, String> envProperties,
                        String[] clientArgs, LogLeecher[] leechers) throws BallerinaTestException {
        if (sourceRoot == null || sourceRoot.isEmpty() || packagePath == null || packagePath.isEmpty()) {
            throw new IllegalArgumentException("Invalid ballerina program file provided, sourceRoot - "
                    + sourceRoot + " packagePath - " + packagePath);
        }

        if (args == null) {
            args = new String[]{};
        }

        if (envProperties == null) {
            envProperties = new HashMap<>();
        }

        String[] newArgs = ArrayUtils.addAll(flags, "--sourceroot", sourceRoot, packagePath);
        newArgs = ArrayUtils.addAll(newArgs, args);

        addJavaAgents(envProperties);

        runMain("run", newArgs, envProperties, clientArgs, leechers, balServer.getServerHome());
    }

    private synchronized void addJavaAgents(Map<String, String> envProperties) throws BallerinaTestException {
//        String javaOpts = "";
//        if (envProperties.containsKey(JAVA_OPTS)) {
//            javaOpts = envProperties.get(JAVA_OPTS);
//        }
//        if (javaOpts.contains("jacoco.agent")) {
//            return;
//        }
//        javaOpts = agentArgs + javaOpts;
//        if ("".equals(javaOpts)) {
//            return;
//        }
//        envProperties.put(JAVA_OPTS, javaOpts);
    }

    /**
     * Executing the sh or bat file to start the server.
     *
     * @param command       command to run
     * @param args          command line arguments to pass when executing the sh or bat file
     * @param envProperties environmental properties to be appended to the environment
     * @param clientArgs    arguments which program expects
     * @param leechers      log leechers to check the log if any
     * @param commandDir    where to execute the command
     * @throws BallerinaTestException if starting services failed
     */
    public void runMain(String command, String[] args, Map<String, String> envProperties, String[] clientArgs,
                         LogLeecher[] leechers, String commandDir) throws BallerinaTestException {
        String scriptName;
        if (BCompileUtil.jBallerinaTestsEnabled()) {
            scriptName = Constant.JBALLERINA_SERVER_SCRIPT_NAME;
        } else {
            scriptName = Constant.BALLERINA_SERVER_SCRIPT_NAME;
        }
        String[] cmdArray;
        try {

            if (Utils.getOSName().toLowerCase(Locale.ENGLISH).contains("windows")) {
                cmdArray = new String[]{"cmd.exe", "/c", balServer.getServerHome() +
                        File.separator + "bin" + File.separator + scriptName + ".bat", "build"};
            } else {
                cmdArray = new String[]{"bash", balServer.getServerHome() +
                        File.separator + "bin/" + scriptName, "build"};
            }

            Path jarLocation = Files.createTempDirectory("temp");
//            List<String> argsList = Arrays.asList(args);
            String[] outputDir = new String[]{"-o", jarLocation.toString()};
            String[] argsArray = Stream.concat(Arrays.stream(args), Arrays.stream(outputDir)).toArray(String[]::new);
            String[] cmdArgs =
                    Stream.concat(Arrays.stream(cmdArray), Arrays.stream(argsArray)).toArray(String[]::new);
            ProcessBuilder processBuilder = new ProcessBuilder(cmdArgs).directory(new File(commandDir));
            if (envProperties != null) {
                Map<String, String> env = processBuilder.environment();
                for (Map.Entry<String, String> entry : envProperties.entrySet()) {
                    env.put(entry.getKey(), entry.getValue());
                }
            }
            processBuilder.redirectErrorStream();
            Process process = processBuilder.start();

            ServerLogReader serverInfoLogReader = new ServerLogReader("inputStream", process.getInputStream());
            ServerLogReader serverErrorLogReader = new ServerLogReader("errorStream", process.getErrorStream());
            if (leechers == null) {
                leechers = new LogLeecher[]{};
            }
            for (LogLeecher leecher : leechers) {
                switch (leecher.getLeecherType()) {
                    case INFO:
                        serverInfoLogReader.addLeecher(leecher);
                        break;
                    case ERROR:
                        serverErrorLogReader.addLeecher(leecher);
                        break;
                }
            }
            serverInfoLogReader.start();
            serverErrorLogReader.start();
            if (clientArgs != null && clientArgs.length > 0) {
                writeClientArgsToProcess(clientArgs, process);
            }
            process.waitFor();

            if (Utils.getOSName().toLowerCase(Locale.ENGLISH).contains("windows")) {
                cmdArray = new String[]{"cmd.exe", "/c", balServer.getServerHome() +
                        File.separator + "bin" + File.separator + scriptName + ".bat", command};
            } else {
                cmdArray = new String[]{"bash", balServer.getServerHome() +
                        File.separator + "bin/" + scriptName, command};
            }

            Path jarPath = Paths.get(jarLocation.toString(), getJarNameFromBal(args[0]));
            args[0] = jarPath.toString();

            cmdArgs =
                    Stream.concat(Arrays.stream(cmdArray), Arrays.stream(args)).toArray(String[]::new);
            processBuilder = new ProcessBuilder(cmdArgs).directory(new File(commandDir));

            process = processBuilder.start();

            serverInfoLogReader = new ServerLogReader("inputStream", process.getInputStream());
            serverErrorLogReader = new ServerLogReader("errorStream", process.getErrorStream());

            if (leechers == null) {
                leechers = new LogLeecher[]{};
            }
            for (LogLeecher leecher : leechers) {
                switch (leecher.getLeecherType()) {
                    case INFO:
                        serverInfoLogReader.addLeecher(leecher);
                        break;
                    case ERROR:
                        serverErrorLogReader.addLeecher(leecher);
                        break;
                }
            }

            serverInfoLogReader.start();
            serverErrorLogReader.start();
            if (clientArgs != null && clientArgs.length > 0) {
                writeClientArgsToProcess(clientArgs, process);
            }
            process.waitFor();

            serverInfoLogReader.stop();
            serverInfoLogReader.removeAllLeechers();

            serverErrorLogReader.stop();
            serverErrorLogReader.removeAllLeechers();
        } catch (IOException e) {
            throw new BallerinaTestException("Error executing ballerina", e);
        } catch (InterruptedException e) {
            throw new BallerinaTestException("Error waiting for execution to finish", e);
        }
    }

    /**
     * Executing the sh or bat file to start the server and returns the logs printed to stdout.
     *
     * @param command    command to run
     * @param args       command line arguments to pass when executing the sh or bat file
     * @param commandDir where to execute the command
     * @return logs printed to std out
     * @throws BallerinaTestException if starting services failed or if an error occurs when reading the stdout
     */
    public String runMainAndReadStdOut(String command, String[] args, String commandDir) throws BallerinaTestException {
        return runMainAndReadStdOut(command, args, new HashMap<>(), commandDir, false);
    }

    /**
     * Executing the sh or bat file to start the server and returns the logs printed to stdout.
     *
     * @param command       command to run
     * @param args          command line arguments to pass when executing the sh or bat file
     * @param envProperties environmental properties to be appended to the environment
     * @param commandDir    where to execute the command
     * @param readErrStream whether to read the error stream or input stream
     * @return logs printed to std out
     * @throws BallerinaTestException if starting services failed or if an error occurs when reading the stdout
     */
    public String runMainAndReadStdOut(String command, String[] args, Map<String, String> envProperties,
                                       String commandDir, boolean readErrStream) throws BallerinaTestException {

        String scriptName;
        if (BCompileUtil.jBallerinaTestsEnabled()) {
            scriptName = Constant.JBALLERINA_SERVER_SCRIPT_NAME;
        } else {
            scriptName = Constant.BALLERINA_SERVER_SCRIPT_NAME;
        }
        String[] cmdArray;
        try {

            if (Utils.getOSName().toLowerCase(Locale.ENGLISH).contains("windows")) {
                cmdArray = new String[]{"cmd.exe", "/c", balServer.getServerHome() +
                        File.separator + "bin" + File.separator + scriptName + ".bat", command};
            } else {
                cmdArray = new String[]{"bash", balServer.getServerHome() +
                        File.separator + "bin/" + scriptName, command};
            }

            String[] cmdArgs = Stream.concat(Arrays.stream(cmdArray), Arrays.stream(args)).toArray(String[]::new);
            ProcessBuilder processBuilder = new ProcessBuilder(cmdArgs).directory(new File(commandDir));

            Map<String, String> env = processBuilder.environment();
            env.putAll(envProperties);

            Process process = processBuilder.start();

            // Give a small timeout so that the output is given.
            Thread.sleep(5000);

            String output = "";
            InputStream inputStream = readErrStream ? process.getErrorStream() : process.getInputStream();
            try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader buffer = new BufferedReader(inputStreamReader)) {
                output = buffer.lines().collect(Collectors.joining("\n"));
            } catch (Exception e) {
                throw new BallerinaTestException("Error when reading from the stdout ", e);
            }

            process.waitFor();
            return output;
        } catch (IOException e) {
            throw new BallerinaTestException("Error executing ballerina", e);
        } catch (InterruptedException e) {
            throw new BallerinaTestException("Error waiting for execution to finish", e);
        }
    }

    /**
     * Write client clientArgs to process.
     *
     * @param clientArgs client clientArgs
     * @param process    process executed
     * @throws IOException if something goes wrong
     */
    private void writeClientArgsToProcess(String[] clientArgs, Process process) throws IOException {
        try {
            // Wait until the options are prompted TODO find a better way
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            //Ignore
        }
        OutputStream stdin = process.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));

        for (String arguments : clientArgs) {
            writer.write(arguments);
        }
        writer.flush();
        writer.close();
    }

    private String getJarNameFromBal(String balPath) {
        assert balPath.endsWith(".bal") : "Invalid file extention, expected '.bal'";
        String fileName = Paths.get(balPath).getFileName().toString();
        return fileName.substring(0, fileName.lastIndexOf(".bal")).concat(".jar");
    }
}
