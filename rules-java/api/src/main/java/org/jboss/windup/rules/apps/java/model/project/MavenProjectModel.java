package org.jboss.windup.rules.apps.java.model.project;

import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;

import java.util.List;

/**
 * Extends ProjectModel to support maven specific properties.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(MavenProjectModel.TYPE)
public interface MavenProjectModel extends ProjectModel {
    String MAVEN_POM = "mavenPom";
    String PARENT_MAVEN_POM = "parentMavenPOM";
    String TYPE = "MavenProjectModel";
    String ARTIFACT_ID = "artifactId";
    String GROUP_ID = "groupId";
    String SPECIFICATION_VERSION = "specificationVersion";
    String MAVEN_IDENTIFIER = "mavenIdentifier";

    /**
     * Contains the parent POM (if available).
     */
    @Adjacency(label = PARENT_MAVEN_POM, direction = Direction.OUT)
    MavenProjectModel getParentMavenPOM();

    /**
     * Contains the parent POM (if available).
     */
    @Adjacency(label = PARENT_MAVEN_POM, direction = Direction.OUT)
    void setParentMavenPOM(MavenProjectModel parentMavenProject);

    /**
     * Gets projects that have this POM set as their maven parent
     */
    @Adjacency(label = PARENT_MAVEN_POM, direction = Direction.IN)
    List<MavenProjectModel> getMavenChildProjects();

    /**
     * Contains the maven pom {@link XmlFileModel}.
     */
    @Adjacency(label = MAVEN_POM, direction = Direction.OUT)
    List<XmlFileModel> getMavenPom();

    /**
     * Contains the maven pom {@link XmlFileModel}.
     */
    @Adjacency(label = MAVEN_POM, direction = Direction.OUT)
    void addMavenPom(XmlFileModel pom);

    /**
     * The full maven id (groupid, artifactid, and version).
     */
    @Indexed
    @Property(MAVEN_IDENTIFIER)
    String getMavenIdentifier();

    /**
     * The full maven id (groupid, artifactid, and version).
     */
    @Property(MAVEN_IDENTIFIER)
    void setMavenIdentifier(String identifier);

    /**
     * The Maven specification version.
     */
    @Property(SPECIFICATION_VERSION)
    String getSpecificationVersion();

    /**
     * The Maven specification version.
     */
    @Property(SPECIFICATION_VERSION)
    void setSpecificationVersion(String version);

    /**
     * Contains the maven group id.
     */
    @Property(GROUP_ID)
    String getGroupId();

    /**
     * Contains the maven group id.
     */
    @Property(GROUP_ID)
    void setGroupId(String version);

    /**
     * Contains the maven artifact id.
     */
    @Property(ARTIFACT_ID)
    String getArtifactId();

    /**
     * Contains the maven artifact id.
     */
    @Property(ARTIFACT_ID)
    void setArtifactId(String artifactId);
}
