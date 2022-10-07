package org.jboss.windup.config.iteration.payload.when;

import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

import org.jboss.windup.graph.Property;

@TypeValue("TestWhenModel")
public interface TestWhenModel extends WindupVertexFrame {

    public static final String SECOND_NAME = "secondName";
    public static final String NAME = "name";

    @Property(NAME)
    String getName();

    @Property(NAME)
    void setName(String name);

    @Property(SECOND_NAME)
    String getSecondName();

    @Property(SECOND_NAME)
    void setSecondName(String name);
}
