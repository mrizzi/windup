package org.jboss.windup.rules.apps.mavenize;

import java.util.Map;
import java.util.logging.Logger;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.ArchiveExtractionPhase;
import org.jboss.windup.config.phase.ArchiveMetadataExtractionPhase;
import org.jboss.windup.config.phase.DependentPhase;
import org.jboss.windup.config.phase.DiscoverProjectStructurePhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.archives.config.ArchiveIdentificationConfigLoadingRuleProvider;
import org.jboss.windup.rules.apps.java.archives.model.ArchiveCoordinateModel;
import org.jboss.windup.rules.apps.java.archives.model.IdentifiedArchiveModel;
import org.jboss.windup.rules.apps.java.condition.SourceMode;
import org.jboss.windup.rules.apps.java.scan.provider.DiscoverMavenHierarchyRuleProvider;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;


/**
 * Creates a stub of Maven project structure, including pom.xml's and the proper directory structure and dependencies,
 * based on the project structure determined by prior Windup rules (nested deployments) and the libraries included in them.
 *
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, ozizka at seznam.cz</a>
 */
@RuleMetadata(after = {
        ArchiveMetadataExtractionPhase.class,
        ArchiveIdentificationConfigLoadingRuleProvider.class,
        ArchiveExtractionPhase.class,
        DiscoverMavenHierarchyRuleProvider.class,
        DiscoverProjectStructurePhase.class
}, phase = DependentPhase.class)
public class MavenizeRuleProvider extends AbstractRuleProvider {
    private static final Logger LOG = Logging.get(MavenizeRuleProvider.class);
    public static final MavenCoord JBOSS_PARENT = new MavenCoord("org.jboss", "jboss-parent", "20");
    public static final MavenCoord JBOSS_BOM_JAVAEE6_WITH_ALL = new MavenCoord("org.jboss.bom", "jboss-javaee-6.0-with-all", "1.0.7.Final");
    public static final MavenCoord JBOSS_BOM_JAVAEE7_WITH_ALL = new MavenCoord("org.jboss.bom", "wildfly-javaee7-with-tools", "10.0.1.Final");

    // @formatter:off
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        ConditionBuilder applicationProjectModels = Query.fromType(WindupConfigurationModel.class);

        return ConfigurationBuilder.begin()
                // Create the BOM frame
                .addRule()
                .perform(new GraphOperation() {
                    public void perform(GraphRewrite event, EvaluationContext context) {
                        if (!isPerformMavenization(event.getGraphContext()))
                            return;

                        GlobalBomModel bom = event.getGraphContext().getFramed().addFramedVertex(GlobalBomModel.class);
                        ArchiveCoordinateModel jbossParent = event.getGraphContext().getFramed().addFramedVertex(ArchiveCoordinateModel.class);
                        copyTo(JBOSS_PARENT, jbossParent);
                        bom.setParent(jbossParent);
                    }
                })
                .withId("Mavenize-BOM-data-collection")

                // For each IdentifiedArchive, add it to the global BOM.
                .addRule()
                .when(Query.fromType(IdentifiedArchiveModel.class))
                .perform(new MavenizePutNewerVersionToGlobalBomOperation())
                .withId("Mavenize-BOM-file-creation")

                // For each application given to Windup as input, mavenize it.
                .addRule()
                .when(applicationProjectModels, SourceMode.isDisabled())
                .perform(new MavenizeApplicationOperation())
                .withId("Mavenize-projects-mavenization")
                ;
    }
    // @formatter:on


    /**
     * This operation puts the given IdentifiedArchiveModel to the global BOM frame.
     * If there's already one of such G:A:P, then the newer version is used.
     * Eventual version collisions are overridden in pom.xml's.
     */
    class MavenizePutNewerVersionToGlobalBomOperation extends AbstractIterationOperation<IdentifiedArchiveModel> {
        @Override
        public void perform(GraphRewrite event, EvaluationContext context, IdentifiedArchiveModel archive) {
            if (!isPerformMavenization(event.getGraphContext()))
                return;

            if (archive.getCoordinate() == null) {
                LOG.info("Warning: archive.getCoordinate() is null: " + archive.toPrettyString());
                return;
            }

            LOG.info("Adding to global BOM: " + archive.getCoordinate().toPrettyString());
            // BOM
            GraphService<GlobalBomModel> bomServ = new GraphService<>(event.getGraphContext(), GlobalBomModel.class);
            GlobalBomModel bom = bomServ.getUnique();

            // Check for an existing coord, add the new one
            bom.addNewerDependency(archive.getCoordinate());
        }
    }

    /**
     * Create a stub of Maven project structure, including pom.xml's and the proper directory structure and dependencies,
     * based on the project structure determined by prior Windup rules (nested deployments) and the libraries included in them.
     */
    private class MavenizeApplicationOperation extends AbstractIterationOperation<WindupConfigurationModel> {
        public MavenizeApplicationOperation() {
        }

        @Override
        public void perform(GraphRewrite event, EvaluationContext evalContext, WindupConfigurationModel config) {
            if (!isPerformMavenization(event.getGraphContext()))
                return;

            for (FileModel inputPath : config.getInputPaths()) {
                ProjectModel projectModel = inputPath.getProjectModel();
                if (projectModel == null)
                    throw new WindupException("Error, no project found in: " + inputPath.getFilePath());

                new MavenizationService(event.getGraphContext()).mavenizeApp(projectModel);
            }
        }
    }


    private static void copyTo(MavenCoord from, ArchiveCoordinateModel to) {
        to.setArtifactId(from.getArtifactId());
        to.setGroupId(from.getGroupId());
        to.setVersion(from.getVersion());
        to.setClassifier(from.getClassifier());
        to.setPackaging(from.getPackaging());
    }

    /**
     * @return the value of the option with given name. null if the value was null.
     * @throws IllegalStateException if the value is not Boolean.
     */
    public static Boolean getBooleanOption(GraphContext graphContext, String name) {
        Map<String, Object> options = graphContext.getOptionMap();
        final Object value = options.get(name);
        if (value != null && !(value instanceof Boolean))
            throw new IllegalStateException("Option value expected to be Boolean, but was: " + value.getClass());
        return (Boolean) options.get(name);
    }

    /**
     * @return the boolean value of the option with given name. Given default_ value if the value was null.
     * @throws IllegalStateException if the value is not Boolean.
     */
    public static boolean getBooleanOption(GraphContext graphContext, String name, boolean default_) {
        Boolean val = getBooleanOption(graphContext, name);
        if (val == null)
            return default_;
        return val;
    }

    private static boolean isPerformMavenization(GraphContext graphContext) {
        return getBooleanOption(graphContext, MavenizeOption.NAME, false);
    }
}
