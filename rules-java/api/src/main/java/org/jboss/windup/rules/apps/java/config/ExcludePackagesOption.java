package org.jboss.windup.rules.apps.java.config;

import org.jboss.windup.config.AbstractConfigurationOption;
import org.jboss.windup.config.InputType;
import org.jboss.windup.config.ValidationResult;

/**
 * Windup option for excluding packages.s
 */
public class ExcludePackagesOption extends AbstractConfigurationOption {

    public static final String NAME = "excludePackages";

    @Override
    public String getDescription() {
        return "A list of java package name prefixes to exclude (eg, com.myapp.subpackage). Multiple options may be specified, separated by a space.";
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return "Exclude Java Packages";
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public InputType getUIType() {
        return InputType.MANY;
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    public ValidationResult validate(Object value) {
        return ValidationResult.SUCCESS;
    }
}
