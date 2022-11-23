package com.veracode.scamitigation.api;

import com.google.common.net.UrlEscapers;
import com.veracode.scamitigation.ExceptionHandler;
import com.veracode.scamitigation.models.ScaMitigation;
import com.veracode.scamitigation.models.WorkspaceInfo;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class VeracodeApi {

    public VeracodeApi() {
    }

    public static boolean validateCredentials() {
        return ApiCaller.runApi("/healthcheck/status", "GET", null).isPresent();
    }

    public static List<ScaMitigation> getAllIgnoredFindings(String workspaceName,
                                                            String projectName, boolean getOpenIssues) {
        return ApiCaller.runApi("/srcclr/v3/workspaces?" +
                        getWorkspaceFilters(workspaceName, projectName), "GET", null)
                .filter(ApiResults::hasResponse)
                .map((apiResults) -> getWorkspacesFromPayload(apiResults.getApiResponse()))
                .map((allWorkspaces) -> allWorkspaces.stream()
                        .flatMap((workspaceInfo) ->
                                getIssuesForWorkspace(workspaceInfo, projectName, getOpenIssues).stream())
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    private static String getWorkspaceFilters(String workspaceName, String projectName) {
        String filters = "";
        if (workspaceName != null && workspaceName.length() > 0) {
            filters = filters + "filter%5Bworkspace%5D=" + encodeUrl(workspaceName) + "&";
        }

        if (projectName != null && projectName.length() > 0) {
            filters = filters + "filter%5Bproject%5D=" + encodeUrl(projectName) + "&";
        }

        return filters + "size=2000";
    }

    private static String encodeUrl(String additionalFilter) {
        return UrlEscapers.urlFragmentEscaper().escape(additionalFilter);
    }

    private static List<ScaMitigation> getIssuesForWorkspace(WorkspaceInfo workspaceInfo, String projectName, boolean getOpenIssues) {
        return ApiCaller.runApi("/srcclr/v3/workspaces/" + workspaceInfo.getWorkspaceGuid() +
                        "/issues/?" + getTypeOfIssueFilter(getOpenIssues) +
                        "size=2000", "GET", null)
                .filter(ApiResults::hasResponse)
                .map(ApiResults::getApiResponse)
                .flatMap(VeracodeApi::getEmbeddedNode)
                .flatMap(VeracodeApi::getIssuesNode)
                .map(issues -> readAllIssuesFromJsonArray(issues, workspaceInfo).stream()
                        .filter((scaMitigation) -> projectName == null
                                || scaMitigation.getProject().startsWith(projectName))
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    private static String getTypeOfIssueFilter(boolean getOpenIssues) {
        return getOpenIssues ? "ignored=false&" : "ignored=true&";
    }

    private static List<ScaMitigation> readAllIssuesFromJsonArray(JSONArray issues, WorkspaceInfo workspaceInfo) {
        List<ScaMitigation> foundMitigations = new ArrayList<>();

        for (int currentIndex = 0; currentIndex < issues.length(); ++currentIndex) {
            tryGetElementAtJsonArrayIndex(issues, currentIndex)
                    .map(jsonObject -> ScaMitigation.from(jsonObject, workspaceInfo))
                    .ifPresent(foundMitigations::add);
        }

        return foundMitigations;
    }

    private static List<WorkspaceInfo> getWorkspacesFromPayload(JSONObject apiResponse) {
        return Optional.of(apiResponse)
                .flatMap(VeracodeApi::getEmbeddedNode)
                .flatMap(VeracodeApi::getWorkspacesNode)
                .map(VeracodeApi::getWorkspaceFromNode)
                .orElse(Collections.emptyList());
    }

    private static List<WorkspaceInfo> getWorkspaceFromNode(JSONArray allWorkspaces) {
        List<WorkspaceInfo> foundIds = new ArrayList<>();

        for (int currentIndex = 0; currentIndex < allWorkspaces.length(); ++currentIndex) {
            tryGetElementAtJsonArrayIndex(
                    allWorkspaces, currentIndex)
                    .map(VeracodeApi::instantiateWorkspace)
                    .ifPresent(foundIds::add);
        }

        return foundIds;
    }

    private static WorkspaceInfo instantiateWorkspace(JSONObject workspaceNode) {
        return new WorkspaceInfo(tryGetElementAsString(workspaceNode, "id").orElse(""),
                tryGetElementAsString(workspaceNode, "name").orElse(""),
                tryGetElementAsString(workspaceNode, "site_id").orElse(""));
    }

    public static Optional<String> tryGetElementAsString(JSONObject jsonObject, String elementToGet) {
        return tryGetElementFromJsonObject(jsonObject, elementToGet)
                .filter(result -> result instanceof String)
                .map(result -> (String) result);
    }

    public static Optional<String> tryGetElementFromInteger(JSONObject jsonObject, String elementToGet) {
        return tryGetElementFromJsonObject(jsonObject, elementToGet)
                .filter(result -> result instanceof Integer)
                .map(result -> ((Integer) result).toString());
    }

    private static Optional<JSONObject> getEmbeddedNode(JSONObject baseNode) {
        return tryGetElementFromJsonObject(baseNode, "_embedded")
                .filter(result -> result instanceof JSONObject)
                .map(VeracodeApi::mapToJsonObject);
    }

    private static Optional<JSONArray> getIssuesNode(JSONObject baseNode) {
        return tryGetElementFromJsonObject(baseNode, "issues")
                .filter(result -> result instanceof JSONArray)
                .map(VeracodeApi::mapToJsonArray);
    }

    public static Optional<JSONObject> getVulnerabilityNode(JSONObject baseNode) {
        return tryGetElementFromJsonObject(baseNode, "vulnerability")
                .filter(result -> result instanceof JSONObject)
                .map(VeracodeApi::mapToJsonObject);
    }

    public static Optional<JSONObject> getLibraryNode(JSONObject baseNode) {
        return tryGetElementFromJsonObject(baseNode, "library")
                .filter(result -> result instanceof JSONObject)
                .map(VeracodeApi::mapToJsonObject);
    }

    private static Optional<JSONArray> getWorkspacesNode(JSONObject embeddedNode) {
        return tryGetElementFromJsonObject(embeddedNode, "workspaces")
                .filter(result -> result instanceof JSONArray)
                .map(VeracodeApi::mapToJsonArray);
    }

    private static Optional<JSONObject> tryGetElementAtJsonArrayIndex(JSONArray allApplications, int currentIndex) {
        try {
            Object element = allApplications.get(currentIndex);
            if (element instanceof JSONObject) {
                return Optional.of((JSONObject) element);
            }
        } catch (JSONException e) {
            ExceptionHandler.logException(e);
        }

        return Optional.empty();
    }

    private static Optional<Object> tryGetElementFromJsonObject(JSONObject jsonObject, String elementToGet) {
        try {
            return Optional.of(jsonObject.get(elementToGet));
        } catch (JSONException e) {
            return Optional.empty();
        }
    }

    private static JSONObject mapToJsonObject(Object jsonResult) {
        return (JSONObject) jsonResult;
    }

    private static JSONArray mapToJsonArray(Object jsonResult) {
        return (JSONArray) jsonResult;
    }
}
