package com.google.idea.sdkcompat.util;

import com.intellij.ide.actions.CreateDesktopEntryAction;
import javax.annotation.Nullable;

/**
 * Compat for {@link com.intellij.ide.actions.CreateDesktopEntryAction}. Remove when #api192 is no
 * longer supported.
 */
public class CreateDesktopEntryActionCompat {
  private CreateDesktopEntryActionCompat() {}

  @Nullable
  public static String getLauncherScript() {
    return CreateDesktopEntryAction.getLauncherScript();
  }
}
