<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <title>${getWindupBrandName()} Rule Providers</title>
    <link href="resources/css/bootstrap.min.css" rel="stylesheet"/>
    <link href="resources/css/font-awesome.min.css" rel="stylesheet" />
    <link href="resources/css/windup.css" rel="stylesheet" media="screen"/>
    <link href="resources/css/windup.java.css" rel="stylesheet" media="screen"/>

    <#assign basePath="resources">
    <#include "include/favicon.ftl">

    <script src="resources/js/jquery-3.3.1.min.js"></script>
</head>
<body role="document">

    <div id="main-navbar" class="navbar navbar-inverse navbar-fixed-top">
        <div class="wu-navbar-header navbar-header">
            <#include "include/navheader.ftl">
        </div>
        <div class="navbar-collapse collapse navbar-responsive-collapse">
            <ul class="nav navbar-nav">
                <li><a href="../index.html"><i class="glyphicon glyphicon-arrow-left"></i> All Applications</a></li>
            </ul>
        </div><!-- /.nav-collapse -->
        <div class="navbar-collapse collapse navbar-responsive-collapse">
            <ul class="nav navbar-nav"></ul>
        </div><!-- /.nav-collapse -->
    </div>

    <div class="container-fluid" role="main">
        <div class="row">
            <div class="page-header page-header-no-border">
                <h1>
                    <div class="main">Rule Providers Execution Overview
                    <i class="glyphicon glyphicon-info-sign" data-toggle="tooltip" data-placement=right title="This report lists 'rule providers', or sets of ${getWindupBrandName()} rules. They may originate from a '.windup.xml', a '.rhamt.xml', or a '.mta.xml' file
                    or a Java class implementing 'RuleProvider'."></i></div>
                </h1>
            </div>
        </div>

        <div class="row container-fluid">

            <!-- All Rule Providers -->
            <#list getAllRuleProviders() as ruleProvider>
                <#if isRulePhase(ruleProvider)>
                    <div class="alert alert-info">
                        <h3 class="panel-title">Phase: ${ruleProvider.metadata.ID}</h3>
                    </div>
                <#else>
                    <div class="panel panel-primary">
                        <div class="panel-heading">
                            <h3 class="panel-title">${ruleProvider.metadata.ID}</h3>
                            Phase: ${ruleProvider.metadata.phase.simpleName}
                        </div>
                        <table class="table table-striped table-bordered">
                            <tr>
                                <th>Rule-ID</th>
                                <th>Rule</th>
                                <th>Statistics</th>
                                <th>Status?</th>
                                <th>Result?</th>
                                <th>Failure Cause</th>
                            </tr>
                            <#list getRuleExecutionResults(ruleProvider) as ruleExecutionInfo>
                                <#if ruleExecutionInfo??>
                                <tr>
                                    <td>
                                        ${ruleExecutionInfo.rule.id}
                                    </td>
                                    <td>
                                        <a name="${ruleExecutionInfo.rule.id}" class="anchor"></a>
                                        <span style="white-space: pre-wrap">${formatRule(ruleExecutionInfo.rule)?html}</span>
                                    </td>
                                    <td>
                                        <div>Vertices Created: ${ruleExecutionInfo.vertexIDsAdded}</div>
                                        <div>Edges Created: ${ruleExecutionInfo.edgeIDsAdded}</div>
                                        <div>Vertices Removed: ${ruleExecutionInfo.vertexIDsRemoved}</div>
                                        <div>Edges Removed: ${ruleExecutionInfo.edgeIDsRemoved}</div>
                                    </td>
                                    <td>
                                        ${ruleExecutionInfo.executed?string("Condition met.", "Condition not met.")}
                                    </td>
                                    <td>
                                        ${ruleExecutionInfo.failed?string("failed", "success")}
                                    </td>
                                    <td>
                                        <#if ruleExecutionInfo.failureCause?? && ruleExecutionInfo.failureCause.message??>
                                            ${ruleExecutionInfo.failureCause.message}
                                        </#if>
                                    </td>
                                </tr>
                                <#else>
                                    <tr>
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                    </tr>
                                </#if>
                            </#list>
                        </table>
                    </div>
                </#if>
            </#list>
        </div> <!-- /row -->
    </div> <!-- /container main -->



    <script src="resources/libraries/flot/jquery.flot.min.js"></script>
    <script src="resources/libraries/flot/jquery.flot.pie.min.js"></script>

    <script src="resources/js/bootstrap.min.js"></script>
    <script type="text/javascript" src="resources/js/navbar.js"></script>
    <script>$(document).ready(function(){$('[data-toggle="tooltip"]').tooltip();});</script>
</body>
</html>
