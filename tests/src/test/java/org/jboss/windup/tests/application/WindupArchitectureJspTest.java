package org.jboss.windup.tests.application;

import java.io.File;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Singleton;

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
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.ReportModel;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.model.AbstractJavaSourceModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.model.PhantomJavaClassModel;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.apps.javaee.model.JspSourceFileModel;
import org.jboss.windup.testutil.html.TestJavaApplicationOverviewUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

@RunWith(Arquillian.class)
public class WindupArchitectureJspTest extends WindupArchitectureTest {

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-ee"),
            @AddonDependency(name = "org.jboss.windup.tests:test-util"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config-groovy"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addAsResource(new File("src/test/xml/XmlExample.windup.xml"))
                .addClass(WindupArchitectureTest.class);
    }

    @Inject
    private JspRulesProvider provider;

    @Test
    public void testRunWindupJsp() throws Exception {
        final String path = "../test-files/jsptest";

        try (GraphContext context = createGraphContext()) {
            super.runTest(context, path, false);

            Assert.assertEquals(2, provider.taglibsFound);
            Assert.assertEquals(2, provider.enumerationRuleHitCount);

            validateReports(context);
            validateClassModels(context);
        }
    }

    /**
     * Validates JavaClassModel classes belonging to .jsp files
     */
    private void validateClassModels(GraphContext context) {
        Iterable<JspSourceFileModel> jspFiles = context.service(JspSourceFileModel.class).findAll();

        int countJspFiles = 0;

        for (JspSourceFileModel jspFile : jspFiles) {
            countJspFiles++;
            int countClasses = 0;

            for (JavaClassModel classModel : jspFile.getJavaClasses()) {
                countClasses++;

                Assert.assertTrue(classModel.getClassName().endsWith(".jsp") || classModel.getClassName().endsWith(".tag"));
                Assert.assertEquals(jspFile.getFileName(), classModel.getClassName());
                Assert.assertEquals(jspFile.getFileName(), classModel.getQualifiedName());
                Assert.assertEquals("", classModel.getPackageName());
                Assert.assertNull(classModel.getDecompiledSource());
                Assert.assertNull(classModel.getClassFile());

                JavaClassModel parentClass = classModel.getExtends();

                Assert.assertEquals("javax.servlet.http.HttpServlet", parentClass.getQualifiedName());
                Assert.assertTrue(parentClass instanceof PhantomJavaClassModel);

                AbstractJavaSourceModel sourceModel = classModel.getOriginalSource();

                Assert.assertNotNull(sourceModel);
                Assert.assertTrue(sourceModel instanceof JspSourceFileModel);
            }

            Assert.assertEquals(1, countClasses);
        }

        Assert.assertEquals(5, countJspFiles);
    }

    /**
     * Validate that the report pages were generated correctly
     */
    private void validateReports(GraphContext context) {
        ReportService reportService = new ReportService(context);
        ReportModel reportModel = super.getMainApplicationReport(context);
        Path appReportPath = reportService.getReportDirectory().resolve(reportModel.getReportFilename());

        TestJavaApplicationOverviewUtil util = new TestJavaApplicationOverviewUtil();
        util.loadPage(appReportPath);
        util.checkFilePathAndIssues("jsptest", "src/example-with-taglib.jsp", "Other Taglib Import");
    }

    @Singleton
    public static class JspRulesProvider extends AbstractRuleProvider {
        private int taglibsFound = 0;
        private int enumerationRuleHitCount = 0;

        public JspRulesProvider() {
            super(MetadataBuilder.forProvider(JspRulesProvider.class)
                    .setHaltOnException(true));
        }

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return ConfigurationBuilder.begin()
                    .addRule()
                    .when(JavaClass.references("http://www.example.com/custlib").at(TypeReferenceLocation.TAGLIB_IMPORT))
                    .perform(
                            new AbstractIterationOperation<JavaTypeReferenceModel>() {
                                @Override
                                public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload) {
                                    FileModel source = payload.getFile();
                                    if (!(source instanceof JspSourceFileModel))
                                        Assert.fail("File was not a jsp file!");
                                    taglibsFound++;
                                }
                            })
                    .addRule()
                    .when(JavaClass.references("java.util.Enumeration").at(TypeReferenceLocation.IMPORT))
                    .perform(new AbstractIterationOperation<JavaTypeReferenceModel>() {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload) {
                            FileModel source = payload.getFile();
                            if (!(source instanceof JspSourceFileModel))
                                Assert.fail("File was not a jsp file!");
                            enumerationRuleHitCount++;
                        }
                    });
        }
    }
}
