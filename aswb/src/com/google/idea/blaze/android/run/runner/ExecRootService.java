/*
 * Copyright 2020 The Bazel Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.idea.blaze.android.run.runner;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.idea.blaze.base.command.info.BlazeInfo;
import com.google.idea.blaze.base.command.info.BlazeInfoRunner;
import com.google.idea.blaze.base.model.primitives.WorkspaceRoot;
import com.google.idea.blaze.base.scope.BlazeContext;
import com.google.idea.blaze.base.scope.output.IssueOutput;
import com.google.idea.blaze.base.scope.output.StatusOutput;
import com.google.idea.blaze.base.settings.Blaze;
import com.google.idea.common.experiments.BoolExperiment;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import java.io.File;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;

/**
 * Utility for fetching execroot.
 *
 * <p>Note: This utility caches the default execroot and assumes the default execroot will stay the
 * same per IDE session. Fetching the execroot using blaze info is fast (~1-2sec), but it's still
 * time saved every build for most users.
 *
 * <p>The default execroot cache can be disabled using {@link #useDefaultExecrootCache} flag.
 */
public final class ExecRootService {
  private static final BoolExperiment useDefaultExecrootCache =
      new BoolExperiment("enable.default.execroot.cache", true);
  private Project project;
  private File defaultExecroot = null;

  public static ExecRootService getInstance(Project project) {
    return ServiceManager.getService(project, ExecRootService.class);
  }

  public ExecRootService(Project project) {
    this.project = project;
  }

  /**
   * Returns the execroot of the given project and a set of build flags to check for possible custom
   * execroots. Errors are exposed through the given context.
   */
  @Nullable
  public File getExecutionRoot(ImmutableList<String> buildFlags, BlazeContext context) {
    boolean useDefaultExecroot = false;

    if (useDefaultExecrootCache.getValue()) {
      useDefaultExecroot = true;
      for (String buildFlag : buildFlags) {
        if (buildFlag.contains("--output_base")) {
          useDefaultExecroot = false;
          break;
        }
      }

      if (useDefaultExecroot && defaultExecroot != null) {
        return defaultExecroot;
      }
    }

    context.output(new StatusOutput("Fetching project output directory..."));
    ListenableFuture<String> execRootFuture =
        BlazeInfoRunner.getInstance()
            .runBlazeInfo(
                context,
                Blaze.getBuildSystemProvider(project).getBinaryPath(project),
                WorkspaceRoot.fromProject(project),
                buildFlags,
                BlazeInfo.EXECUTION_ROOT_KEY);
    try {
      File execroot = new File(execRootFuture.get());
      if (useDefaultExecroot) {
        defaultExecroot = execroot;
      }
      return execroot;
    } catch (InterruptedException e) {
      IssueOutput.warn("Build cancelled.").submit(context);
      context.setCancelled();
    } catch (ExecutionException e) {
      IssueOutput.error(e.getMessage()).submit(context);
      context.setHasError();
    }
    return null;
  }
}
