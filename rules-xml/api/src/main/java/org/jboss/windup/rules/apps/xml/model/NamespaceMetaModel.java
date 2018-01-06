package org.jboss.windup.rules.apps.xml.model;

import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

import org.apache.tinkerpop.gremlin.structure.Direction;
import com.syncleus.ferma.annotations.Adjacency;
import com.syncleus.ferma.annotations.Property;

@TypeValue(NamespaceMetaModel.TYPE)
public interface NamespaceMetaModel extends WindupVertexFrame
{
    public static final String SCHEMA_LOCATION = "schemaLocation";
    public static final String NAMESPACE_URI = "namespaceURI";
    public static final String TYPE = "NamespaceMetaModel";

    @Adjacency(label = XmlFileModel.NAMESPACE, direction = Direction.IN)
    public void addXmlResource(XmlFileModel facet);

    @Adjacency(label = XmlFileModel.NAMESPACE, direction = Direction.IN)
    public Iterable<XmlFileModel> getXmlResources();

    @Property(NAMESPACE_URI)
    public String getURI();

    @Indexed
    @Property(NAMESPACE_URI)
    public void setURI(String uri);

    @Indexed
    @Property(SCHEMA_LOCATION)
    public String getSchemaLocation();

    @Property(SCHEMA_LOCATION)
    public void setSchemaLocation(String schemaLocation);

}
