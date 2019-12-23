package com.android.tools.idea.run.editor;

import com.intellij.openapi.project.Project;

public class AndroidProfilersPanelCompat {
  private AndroidProfilersPanelCompat() {}

  public static AndroidProfilersPanel getNewAndroidProfilersPanel(
      Project project, ProfilerState state) {
    return new AndroidProfilersPanel(project, state);
  }

  public static void resetFrom(AndroidProfilersPanel profilersPanel, ProfilerState state) {
    profilersPanel.resetFrom(state);
  }

  public static void applyTo(AndroidProfilersPanel profilersPanel, ProfilerState state) {
    profilersPanel.applyTo(state);
  }
}
