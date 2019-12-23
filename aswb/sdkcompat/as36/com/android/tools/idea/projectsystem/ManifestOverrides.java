package com.android.tools.idea.projectsystem;

import com.android.manifmerger.ManifestSystemProperty;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import kotlin.text.Regex;

public class ManifestOverrides {
  private static Regex PLACEHOLDER_REGEX = new Regex("${([^}]*)}");

  private final Map<ManifestSystemProperty, String> directOverrides;
  private final Map<String, String> placeHolders;

  public Map<ManifestSystemProperty, String> getDirectOverrides() {
    return directOverrides;
  }

  public Map<String, String> getPlaceHolders() {
    return placeHolders;
  }

  public ManifestOverrides(
      @Nullable Map<ManifestSystemProperty, String> directOverrides,
      @Nullable Map<String, String> placeHolders) {
    this.directOverrides = directOverrides == null ? new HashMap<>() : directOverrides;
    this.placeHolders = placeHolders == null ? new HashMap<>() : placeHolders;
  }

  public ManifestOverrides() {
    this(null, null);
  }

  public String resolvePlaceholders(String string) {
    return PLACEHOLDER_REGEX.replace(
        string, matchResult -> placeHolders.getOrDefault(matchResult.getGroupValues().get(1), ""));
  }
}
