package org.jboss.windup.rules.apps.java.reporting.freemarker;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;
import org.jboss.windup.rules.apps.java.model.AbstractJavaSourceModel;
import org.jboss.windup.rules.apps.java.model.AmbiguousJavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;


/**
 * A Freemarker method for finding a source for a class by it's name.
 */
public class FindSourceFilesByClassNameMethod implements WindupFreeMarkerMethod {
    private JavaClassService javaClassService;

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        if (arguments.size() != 1) {
            throw new TemplateModelException("Error, method expects one argument (String)");
        }
        SimpleScalar arg = (SimpleScalar) arguments.get(0);
        String qualifedClassName = arg.getAsString();
        JavaClassModel classModel = javaClassService.getByName(qualifedClassName);
        List<AbstractJavaSourceModel> results = new ArrayList<>();
        if (classModel instanceof AmbiguousJavaClassModel) {
            AmbiguousJavaClassModel ambiguousJavaClassModel = (AmbiguousJavaClassModel) classModel;
            for (JavaClassModel referencedClass : ambiguousJavaClassModel.getReferences()) {
                addSourceFilesToResult(results, referencedClass);
            }
        } else {
            addSourceFilesToResult(results, classModel);
        }
        return results;
    }

    private void addSourceFilesToResult(List<AbstractJavaSourceModel> results, JavaClassModel referencedClass) {
        AbstractJavaSourceModel decompiledSource = referencedClass.getDecompiledSource();
        AbstractJavaSourceModel originalSource = referencedClass.getOriginalSource();
        if (decompiledSource != null) {
            results.add(decompiledSource);
        }
        if (originalSource != null) {
            results.add(originalSource);
        }
    }

    @Override
    public String getDescription() {
        return "Finds all " + JavaSourceFileModel.class.getSimpleName() + "s for the given fully qualified class name";
    }

    @Override
    public void setContext(GraphRewrite event) {
        this.javaClassService = new JavaClassService(event.getGraphContext());
    }
}
