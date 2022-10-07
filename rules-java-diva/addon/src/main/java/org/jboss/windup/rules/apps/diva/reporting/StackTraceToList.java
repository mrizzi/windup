package org.jboss.windup.rules.apps.diva.reporting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;
import org.jboss.windup.rules.apps.diva.model.DivaStackTraceModel;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;

public class StackTraceToList implements WindupFreeMarkerMethod {

    private static final String NAME = "stackTraceToList";

    @Override
    public String getMethodName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Takes a " + DivaStackTraceModel.class.getSimpleName() + " object, and returns a list of stack traces.";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        if (arguments.size() != 1) {
            throw new TemplateModelException("Error, method expects one argument");
        }

        List<DivaStackTraceModel> list = new ArrayList<>();
        StringModel stringModel = (StringModel) arguments.get(0);
        if (stringModel == null)
            return list;

        if (stringModel.getWrappedObject() != null
                && !(stringModel.getWrappedObject() instanceof DivaStackTraceModel)) {
            throw new TemplateModelException(
                    "Error, method expects argument of type " + DivaStackTraceModel.class.getSimpleName());
        }

        DivaStackTraceModel model = (DivaStackTraceModel) stringModel.getWrappedObject();

        while (model != null) {
            list.add(model);
            model = model.getParent();
        }
        Collections.reverse(list);

        return list;
    }

}
