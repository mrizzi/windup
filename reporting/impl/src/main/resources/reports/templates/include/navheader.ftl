<#if index_page??>
    <#assign logoPrefix = navUrlPrefix>
<#else>
    <#assign logoPrefix = "">
</#if>
            <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-responsive-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <span class="wu-navbar-header">
              <#include "navbar_left_brand.ftl">
            </span>