package org.jboss.windup.rules.apps.mavenize;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Logger;

import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.Service;
import org.jboss.windup.rules.apps.java.archives.model.ArchiveCoordinateModel;
import org.jboss.windup.rules.apps.java.scan.ast.TypeInterestFactory;
import org.jboss.windup.util.Logging;

/**
 * This is a service class that provides information about which artifacts contain the given package.
 *
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 * <p>
 * TODO: This could also be achived through WINDUP-1028, depends on what gets merged when.
 */
public class PackagesToContainingMavenArtifactsIndex {
    private static final Logger LOG = Logging.get(PackagesToContainingMavenArtifactsIndex.class);
    public static final String EDGE_USES = "uses";

    private final GraphContext graphContext;


    public PackagesToContainingMavenArtifactsIndex(GraphContext graphContext) {
        this.graphContext = graphContext;
    }


    /**
     * Which projects contain classes which reference the given package (in their imports).
     */
    private Iterable<ProjectModel> getProjectsContainingClassesReferencingPackage(String pkg) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    /**
     * For given API artifact, finds the projects whose Java classes use artifact's classes,
     * and links them in the graph.
     * <p>
     * TODO:
     * Because the graph doesn't contain data about all Java classes,
     * this will likely need to use TypeInterestFactory -
     * register all the classes (or just packages?) from the API jars.
     * This is executed in a separate rule.
     */
    public void registerPackagesFromAPI(MavenCoord apiCoords) {
        Iterable<String> packages = this.getPackagesInArtifact(apiCoords);
        for (String pkg : packages) {
            this.registerPackageInTypeInterestFactory(pkg);
        }
    }

    /**
     * After the packages are registered and Java scanning done,
     * we can link the ProjectModel and API packages together.
     * ProjectModel --uses--> ArchiveCoordinateModel
     */
    public void markProjectsUsingPackagesFromAPI(MavenCoord apiCoords) {
        final Service<ArchiveCoordinateModel> coordsService = graphContext.service(ArchiveCoordinateModel.class);

        Iterable<String> packages = this.getPackagesInArtifact(apiCoords);
        for (String pkg : packages) {
            Iterable<ProjectModel> projects = this.getProjectsContainingClassesReferencingPackage(pkg);
            for (ProjectModel project : projects) {
                ArchiveCoordinateModel apiArchiveRepresentant = new ArchiveCoordinateService(graphContext, ArchiveCoordinateModel.class)
                        .getSingleOrCreate(apiCoords.getGroupId(), apiCoords.getArtifactId(), null); // We specifically want null.
                project.getElement().addEdge(EDGE_USES, apiArchiveRepresentant.getElement());
            }
        }
    }


    /**
     * For given API artifact, finds the projects whose Java classes use artifact's classes,
     * and links them in the graph.
     */
    public boolean moduleContainsPackagesFromAPI(ProjectModel projectModel, MavenCoord apiCoords) {
        ArchiveCoordinateModel archive = new ArchiveCoordinateService(graphContext, ArchiveCoordinateModel.class).findSingle(apiCoords.getGroupId(), apiCoords.getArtifactId(), null);
        if (archive == null)
            return false;
        //return graphContext.testIncidence(projectModel.asVertex(), archive.asVertex(), EDGE_USES);
        Iterator<Vertex> projectsVerts = archive.getElement().vertices(Direction.IN, EDGE_USES);
        Iterator<ProjectModel> projects = (Iterator<ProjectModel>) graphContext.getFramed().frame(projectsVerts, ProjectModel.class);
        while (projects.hasNext()) {
            ProjectModel project = projects.next();
            if (projectModel.equals(project))
                return true;
        }
        return false;
    }

    private Iterable<String> getPackagesInArtifact(MavenCoord apiCoords) {
        // TODO: WINDUP-1028, WINDUP-984 - Either take from index, or download and scan (Jandex?).
        return Collections.EMPTY_LIST;
    }


    /**
     * So that we get these packages caught Java class analysis.
     */
    private void registerPackageInTypeInterestFactory(String pkg) {
        TypeInterestFactory.registerInterest(pkg + "_pkg", pkg.replace(".", "\\."), pkg, TypeReferenceLocation.IMPORT);
        // TODO: Finish the implementation
    }

}
