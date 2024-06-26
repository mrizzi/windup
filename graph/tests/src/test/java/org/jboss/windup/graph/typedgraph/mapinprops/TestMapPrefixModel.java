package org.jboss.windup.graph.typedgraph.mapinprops;

import java.util.Map;

import org.jboss.windup.graph.MapInProperties;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

@TypeValue("MapInPropsPrefixModel")
public interface TestMapPrefixModel extends WindupVertexFrame {
    @MapInProperties(propertyPrefix = "map")
    void setMap(Map<String, String> map);

    @MapInProperties(propertyPrefix = "map")
    Map<String, String> getMap();
}
