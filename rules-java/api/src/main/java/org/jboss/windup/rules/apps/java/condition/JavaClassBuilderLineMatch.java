package org.jboss.windup.rules.apps.java.condition;

import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.ConditionBuilder;

public interface JavaClassBuilderLineMatch {
    /**
     * Specify one or more {@link TypeReferenceLocation} where the specified regex is of interest.
     */
    JavaClassBuilderAt at(TypeReferenceLocation... locations);

    /**
     * Specify the the variable in which to store matching {@link JavaTypeReferenceModel} results, and complete the
     * builder, returning a fully configured {@link Condition} object.
     */
    ConditionBuilder as(String variable);

    /**
     * Specify a regex that filters against {@link JavaSourceFileModel#getPrettyPathWithinProject()}. (E.g. The fully
     * qualified type name.)
     */
    JavaClassBuilderInFile inType(String regex);
}
