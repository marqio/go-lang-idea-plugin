package com.goide.runconfig.testing;

import com.goide.jps.model.JpsGoSdkType;
import com.goide.runconfig.GoRunningState;
import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.testframework.autotest.ToggleAutoTestAction;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class GoTestRunningState extends GoRunningState {
  private GoTestRunConfiguration myConfiguration;

  @NotNull
  @Override
  public ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner runner) throws ExecutionException {
    ProcessHandler processHandler = startProcess();
    TextConsoleBuilder consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(myModule.getProject());
    setConsoleBuilder(consoleBuilder);

    GoTestConsoleProperties consoleProperties = new GoTestConsoleProperties(myConfiguration, executor);
    ConsoleView consoleView = SMTestRunnerConnectionUtil.createAndAttachConsole("Go", processHandler, consoleProperties, getEnvironment());

    DefaultExecutionResult executionResult = new DefaultExecutionResult(consoleView, processHandler);
    executionResult.setRestartActions(new ToggleAutoTestAction(getEnvironment()));
    return executionResult;
  }

  public GoTestRunningState(ExecutionEnvironment env, Module module, GoTestRunConfiguration configuration) {
    super(env, module);
    myConfiguration = configuration;
  }

  @Override
  protected GeneralCommandLine getCommand(@NotNull Sdk sdk) throws ExecutionException {
    String homePath = sdk.getHomePath();
    assert homePath != null;

    String executable = JpsGoSdkType.getGoExecutableFile(sdk.getHomePath()).getAbsolutePath();

    GeneralCommandLine installDependencies = new GeneralCommandLine();
    installDependencies.setExePath(executable);
    installDependencies.addParameter("test");
    installDependencies.addParameter("-i");
    fillCommandLineWithParameters(installDependencies);
    try {
      installDependencies.createProcess().waitFor();
      VirtualFileManager.getInstance().syncRefresh();
    }
    catch (InterruptedException ignore) {
      Thread.currentThread().interrupt();
    }

    GeneralCommandLine runTests = new GeneralCommandLine();
    runTests.setExePath(executable);
    runTests.addParameter("test");
    runTests.addParameter("-v");
    fillCommandLineWithParameters(runTests);
    runTests.getParametersList().addParametersString(myConfiguration.getParams());
    return runTests;
  }

  private void fillCommandLineWithParameters(@NotNull GeneralCommandLine commandLine) {
    commandLine.setWorkDirectory(myConfiguration.getWorkingDirectory());
    switch (myConfiguration.getKind()) {
      case DIRECTORY:
        String relativePath = FileUtil.getRelativePath(myConfiguration.getWorkingDirectory(),
                                                       myConfiguration.getDirectoryPath(),
                                                       File.separatorChar);
        if (relativePath != null) {
          commandLine.addParameter("./" + relativePath);
        }
        else {
          commandLine.addParameter(myConfiguration.getDirectoryPath());
          commandLine.setWorkDirectory(myConfiguration.getDirectoryPath());
        }
        break;
      case PACKAGE:
        commandLine.addParameter(myConfiguration.getPackage());
        break;
      case FILE:
        commandLine.addParameter(myConfiguration.getFilePath());
        break;
    }
    String pattern = myConfiguration.getPattern();
    if (!StringUtil.isEmpty(pattern)) {
      commandLine.addParameter("--run='" + pattern + "'");
    }
  }
}