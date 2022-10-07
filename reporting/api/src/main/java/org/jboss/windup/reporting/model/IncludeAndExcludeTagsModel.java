package org.jboss.windup.reporting.model;

import java.util.Set;

import org.jboss.windup.graph.SetInProperties;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface IncludeAndExcludeTagsModel {
    String INCLUDE_TAGS = "includeTags";
    String EXCLUDE_TAGS = "excludeTags";

    /**
     * Set the set of tags to include in this report.
     */
    @SetInProperties(propertyPrefix = INCLUDE_TAGS)
    IncludeAndExcludeTagsModel setIncludeTags(Set<String> tags);

    /**
     * Get the set of tags to include in this report.
     */
    @SetInProperties(propertyPrefix = INCLUDE_TAGS)
    Set<String> getIncludeTags();

    /**
     * Set the set of tags to exclude from this report.
     */
    @SetInProperties(propertyPrefix = EXCLUDE_TAGS)
    IncludeAndExcludeTagsModel setExcludeTags(Set<String> tags);

    /**
     * Get the set of tags to exclude from this report.
     */
    @SetInProperties(propertyPrefix = EXCLUDE_TAGS)
    Set<String> getExcludeTags();
}
