package org.jboss.windup.reporting.export;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.DependentPhase;
import org.jboss.windup.config.phase.PostReportGenerationPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.reporting.category.IssueCategoryModel;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.EffortReportModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.rules.AttachApplicationReportsToIndexRuleProvider;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.opencsv.CSVWriter;

/**
 * RuleProvider generating optional SARIF file for every application. This file will contain the main reporting information.
 *
 */
@RuleMetadata(phase = DependentPhase.class,
        after = PostReportGenerationPhase.class,
        before = AttachApplicationReportsToIndexRuleProvider.class,
        haltOnException = true)
public class ExportSarifFileRuleProvider extends AbstractRuleProvider
{
    private static final Logger LOG = Logger.getLogger(ExportSarifFileRuleProvider.class.getCanonicalName());
    private static final String MERGED_SARIF_FILENAME = "AllIssues";
    private static final String APP_FILE_TECH_SARIF_FILENAME = "ApplicationFileTechnologies";

    // @formatter:off
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
    {
        return ConfigurationBuilder.begin()
                .addRule()
                .when(Query.fromType(WindupConfigurationModel.class))
                .perform(new ExportSarifReportOperation());
    }
    // @formatter:on

    private final class ExportSarifReportOperation extends AbstractIterationOperation<WindupConfigurationModel>
    {
        @Override
        public void perform(GraphRewrite event, EvaluationContext context, WindupConfigurationModel config)
        {
            JSONObject root = new JSONObject();
            root.put("$schema", "https://json.schemastore.org/sarif-2.1.0.json");
            root.put("version", "2.1.0");
            
            InlineHintService hintService = new InlineHintService(event.getGraphContext());
            String outputFolderPath = config.getOutputPath().getFilePath() + File.separator;
            ClassificationService classificationService = new ClassificationService(event.getGraphContext());
            final Map<String, CSVWriter> projectToFile = new HashMap<>();
            final List<InlineHintModel> hints = hintService.findAll();
            final List<ClassificationModel> classifications = classificationService.findAll();
            List<EffortReportModel> reportableEvents = new ArrayList<>();
            reportableEvents.addAll(hints);
            reportableEvents.addAll(classifications);

            Map<String, JSONObject> rulesFound = new HashMap<>();
            JSONArray results = new JSONArray();
            //try{} in case something bad happens, we need to close files
            try
            {
                reportableEvents.stream().sorted((o1,o2) ->

                        ((Comparator<EffortReportModel>) (o11, o21) -> {
                            IssueCategoryModel c1 = o11.getIssueCategory();
                            IssueCategoryModel c2 = o21.getIssueCategory();
                            Comparator comparator = new IssueCategoryModel.IssueSummaryPriorityComparator();
                            return comparator.compare(c1, c2);
                        }).thenComparing(((Comparator<EffortReportModel>) (o112, o212) -> {
                            int i1 = o112.getEffort();
                            int i2 = o212.getEffort();

                            return Integer.compare(i1, i2);
                        }).reversed()).compare(o1,o2)).forEachOrdered((Object reportableEvent) ->


                {
                    JSONObject result = new JSONObject();
                    if (reportableEvent instanceof InlineHintModel)
                    {
                        InlineHintModel hint = (InlineHintModel)reportableEvent;
                        final ProjectModel parentRootProjectModel = hint.getFile().getProjectModel().getRootProjectModel();
                        String links = buildLinkString(hint.getLinks());
                        String ruleId = hint.getRuleID() != null ? hint.getRuleID() : "";
                        String title = hint.getTitle() != null ? hint.getTitle() : "";
//                        String description = hint.getDescription() != null ? hint.getDescription() : "";
                        String hintMessage = hint.getHint() != null ? hint.getHint() : "";

                        JSONObject rule = new JSONObject();
                        rule.put("id", ruleId);
                        JSONObject shortDescriptionText = new JSONObject();
                        shortDescriptionText.put("text", title);
                        rule.put("shortDescription", shortDescriptionText);
                        JSONObject help = new JSONObject();
                        help.put("text", hintMessage);
                        rulesFound.putIfAbsent(ruleId, rule);

                        result.put("ruleId", ruleId);
                        JSONObject message = new JSONObject();
                        message.put("text", title);
                        message.put("markdown", hintMessage);
                        result.put("message", message);

                        String projectNameString = "";
                        String fileName = "";
                        String filePath = "";
                        if (hint.getFile() != null) {
                            if (hint.getFile().getProjectModel() != null) {
                                projectNameString = hint.getFile().getProjectModel().getName();
                            }
                            fileName = hint.getFile().getFileName();
                            filePath = hint.getFile().getFilePath();
                        }
                        JSONObject artifactLocation = new JSONObject();
                        artifactLocation.put("uri", hint.getFile().getPrettyPath());
                        artifactLocation.put("uriBaseId", "%SRCROOT%");
                        JSONObject physicalLocation = new JSONObject();
                        physicalLocation.put("artifactLocation", artifactLocation);
                        JSONObject region = new JSONObject();
                        region.put("startLine", hint.getLineNumber());
                        region.put("endLine", hint.getLineNumber());
                        final int columnNumber = hint.getColumnNumber();
                        final int length = hint.getLength();
                        if (columnNumber == 1 && length == 1) {
                            region.put("startColumn", columnNumber);
                            region.put("endColumn", length);
                        } else {
                            region.put("startColumn", columnNumber + 1);
                            if (hint.getFile().getFileName().endsWith(".xml")) {
                                region.put("endColumn", length + columnNumber);
                            } else {
                                region.put("endColumn", length);
                            }
                        }
                        physicalLocation.put("region", region);
                        JSONObject location = new JSONObject();
                        location.put("physicalLocation", physicalLocation);
                        JSONArray locations = new JSONArray();
                        locations.put(location);
                        result.put("locations", locations);
                        results.put(result);
                    }
                    if (reportableEvent instanceof ClassificationModel)
                    {
/*
                        ClassificationModel classification = (ClassificationModel)reportableEvent;
                        for (FileModel fileModel : classification.getFileModels())
                        {
                            final ProjectModel parentRootProjectModel = fileModel.getProjectModel().getRootProjectModel();
                            String links = buildLinkString(classification.getLinks());
                            String ruleId = classification.getRuleID() != null ? classification.getRuleID() : "";
                            String classificationText = classification.getClassification() != null ? classification.getClassification() : "";
                            String description = classification.getDescription() != null ? classification.getDescription() : "";
                            String projectNameString = "";
                            if (fileModel.getProjectModel() != null)
                            {
                                projectNameString = fileModel.getProjectModel().getName();
                            }
                            String fileName = fileModel.getFileName();
                            String filePath = fileModel.getFilePath();
                            String[] strings = new String[] {
                                    ruleId, classification.getIssueCategory().getCategoryID(), classificationText,
                                    description, links,
                                    projectNameString, fileName, filePath, "N/A",
                                    String.valueOf(
                                            classification.getEffort()) };
                            writeCsvRecordForProject(projectToFile, outputFolderPath, parentRootProjectModel, strings, null,false);

                        }
*/
                    }
//                    results.put(result);
                }


                );

                JSONObject driver = new JSONObject();
                driver.put("name", "MTA");
                driver.put("fullName", "MTA - Windup");
                driver.put("version", "5.3.0-SNAPSHOT");
                driver.put("informationUri", "https://developers.redhat.com/products/mta/");
                JSONArray rules = new JSONArray();
                rulesFound.values().forEach(rules::put);
                driver.put("rules", rules);
                JSONObject tool = new JSONObject();
                tool.put("driver", driver);
                JSONObject run = new JSONObject();
                run.put("tool", tool);
                run.put("results", results);
                JSONArray runs = new JSONArray();
                runs.put(run);
                root.put("runs", runs);
                System.out.println(root.toString(2));
            }
            finally
            {
            }

        }

        private String buildLinkString(Iterable<LinkModel> links)
        {
            StringBuilder linksString = new StringBuilder();
            for (LinkModel linkModel : links)
            {
                linksString.append("[");
                linksString.append(linkModel.getLink()).append(",");
                linksString.append(linkModel.getDescription());
                linksString.append("]");
            }
            return linksString.toString();
        }


    }
}
