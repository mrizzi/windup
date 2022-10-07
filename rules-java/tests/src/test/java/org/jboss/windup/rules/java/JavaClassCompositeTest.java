package org.jboss.windup.rules.java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.engine.predicates.RuleProviderWithDependenciesPredicate;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.AnalyzeJavaFilesRuleProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

@RunWith(Arquillian.class)
public class JavaClassCompositeTest {
    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    @Inject
    JavaCompositeClassTestRuleProvider provider;

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testJavaClassCondition() throws IOException, InstantiationException, IllegalAccessException {
        try (GraphContext context = factory.create(getDefaultPath(), true)) {
            final String inputDir = "src/test/resources/org/jboss/windup/rules/java";

            final Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(),
                    "windup_" + RandomStringUtils.randomAlphanumeric(6));
            FileUtils.deleteDirectory(outputPath.toFile());
            Files.createDirectories(outputPath);

            final WindupConfiguration processorConfig = new WindupConfiguration().setOutputDirectory(outputPath);
            processorConfig.setRuleProviderFilter(new RuleProviderWithDependenciesPredicate(
                    JavaCompositeClassTestRuleProvider.class));
            processorConfig.setGraphContext(context);
            processorConfig.addInputPath(Paths.get(inputDir));
            processorConfig.setOutputDirectory(outputPath);
            processorConfig.setOptionValue(ScanPackagesOption.NAME, Collections.singletonList(""));
            processorConfig.setOptionValue(SourceModeOption.NAME, true);

            processor.execute(processorConfig);

            GraphService<JavaTypeReferenceModel> typeRefService = new GraphService<>(context,
                    JavaTypeReferenceModel.class);
            Iterable<JavaTypeReferenceModel> typeReferences = typeRefService.findAll();
            Assert.assertTrue(typeReferences.iterator().hasNext());

            Assert.assertEquals(2, provider.getFirstRuleMatchCount());
            Assert.assertEquals(1, provider.getSecondRuleMatchCount());
        }
    }

    private Path getDefaultPath() {
        return FileUtils.getTempDirectory().toPath().resolve("Windup")
                .resolve("windupgraph_javaclasstest_" + RandomStringUtils.randomAlphanumeric(6));
    }

    @Singleton
    public static class JavaCompositeClassTestRuleProvider extends AbstractRuleProvider {
        private int firstRuleMatchCount = 0;
        private int secondRuleMatchCount = 0;

        public JavaCompositeClassTestRuleProvider() {
            super(MetadataBuilder.forProvider(JavaCompositeClassTestRuleProvider.class)
                    .addExecuteAfter(AnalyzeJavaFilesRuleProvider.class));
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {

            return ConfigurationBuilder.begin()
                    .addRule().when(
                            JavaClass.references("org.apache.commons.{*}").inType("{type}2").at(TypeReferenceLocation.IMPORT)
                                    .and(JavaClass.references("org.ocpsoft.rewrite.{*}").inType("{type}1").at(TypeReferenceLocation.IMPORT).as("2"))
                    ).perform(
                            Iteration.over("2").perform(new AbstractIterationOperation<JavaTypeReferenceModel>() {
                                @Override
                                public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload) {
                                    firstRuleMatchCount++;
                                }
                            }).endIteration()
                    )

                    .addRule().when(
                            JavaClass.references("{type}").inType("org.{*}1").at(TypeReferenceLocation.IMPORT)
                                    .and(JavaClass.references("{type}").inType("org.{*}2").at(TypeReferenceLocation.IMPORT).as("2"))
                    ).perform(
                            Iteration.over("2").perform(new AbstractIterationOperation<JavaTypeReferenceModel>() {
                                @Override
                                public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload) {
                                    secondRuleMatchCount++;
                                }
                            }).endIteration()
                    )
                    .where("type").matches("org\\.jboss\\.forge\\.furnace\\.repositories\\.AddonDependencyEntry");
        }
        // @formatter:on

        public int getFirstRuleMatchCount() {
            return firstRuleMatchCount;
        }

        public int getSecondRuleMatchCount() {
            return secondRuleMatchCount;
        }
    }
}
