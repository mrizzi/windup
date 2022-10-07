package org.jboss.windup.rules.apps.javaee.model;

import org.jboss.windup.graph.Indexed;

import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;

/**
 * Represents a JMS Message destination.
 */
@TypeValue(JmsDestinationModel.TYPE)
public interface JmsDestinationModel extends JNDIResourceModel {
    public static final String JMS_DESTINATION = "jmsDestination";
    public static final String TYPE = "JmsDestinationModel";
    public static final String DESTINATION_TYPE = "destinationType";
    public static final String NAME = "destinationName";

    /**
     * Contains JMS destination type (queue / topic)
     */
    @Property(DESTINATION_TYPE)
    public JmsDestinationType getDestinationType();

    /**
     * Contains JMS destination type (queue / topic)
     */
    @Property(DESTINATION_TYPE)
    public void setDestinationType(JmsDestinationType type);

    /**
     * Contains the name of the destination.
     */
    @Indexed
    @Property(NAME)
    public String getDestinationName();

    /**
     * Contains the name of the destination.
     */
    @Property(NAME)
    public void setDestinationName(String destinationName);
}
