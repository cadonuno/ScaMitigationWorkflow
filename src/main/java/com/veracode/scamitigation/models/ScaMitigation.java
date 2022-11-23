package com.veracode.scamitigation.models;

import com.veracode.scamitigation.TypeOfSearchEnum;
import com.veracode.scamitigation.api.VeracodeApi;
import com.veracode.scamitigation.selenium.SeleniumWrapper;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.sling.commons.json.JSONObject;
import org.openqa.selenium.WebDriver;

public class ScaMitigation {
    private final String issueGuid;
    private final String workspace;
    private final String project;
    private final String issueId;
    private final String cveId;
    private final String description;
    private final String library;
    private final String version;
    private final String issueLink;
    private List<Comment> comments;
    private Date dateLastIgnore;

    private ScaMitigation(String issueGuid, String workspace, String project, String issueId, String cveId, String description, String library, String version, String issueLink) {
        this.issueGuid = issueGuid;
        this.workspace = workspace;
        this.project = project;
        this.issueId = issueId;
        this.cveId = cveId;
        this.description = description;
        this.library = library;
        this.version = version;
        this.issueLink = issueLink;
    }

    public String getIssueGuid() {
        return this.issueGuid;
    }

    public String getVersion() {
        return this.version;
    }

    public String getLibrary() {
        return this.library;
    }

    public String getDescription() {
        return this.description;
    }

    public String getCveId() {
        return this.cveId;
    }

    public String getIssueId() {
        return this.issueId;
    }

    public String getProject() {
        return this.project;
    }

    public String getWorkspace() {
        return this.workspace;
    }

    public String getIssueLink() {
        return this.issueLink;
    }

    public List<Comment> getComments() {
        return this.comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public static ScaMitigation from(JSONObject issueNode, WorkspaceInfo workspaceInfo) {
        JSONObject vulnerabilityNode = VeracodeApi.getVulnerabilityNode(issueNode).orElse(null);
        JSONObject libraryNode = VeracodeApi.getLibraryNode(issueNode).orElse(null);
        String siteId = VeracodeApi.tryGetElementFromInteger(issueNode, "site_id").orElse("");
        String issueId = VeracodeApi.tryGetElementAsString(issueNode, "id").orElse("");
        String workspaceName = workspaceInfo.getWorkspaceName();
        String projectName = VeracodeApi.tryGetElementAsString(issueNode, "project_name").orElse("");
        String cveId = vulnerabilityNode == null ? "" : VeracodeApi.tryGetElementAsString(vulnerabilityNode, "cve").orElse("");
        String vulnerabilityTitle = vulnerabilityNode == null ? "" : VeracodeApi.tryGetElementAsString(vulnerabilityNode, "title").orElse("");
        String libraryName = libraryNode == null ? "" : VeracodeApi.tryGetElementAsString(libraryNode, "name").orElse("");
        String libraryVersion = libraryNode == null ? "" : VeracodeApi.tryGetElementAsString(libraryNode, "version").orElse("");
        return new ScaMitigation(issueId, workspaceName, projectName, siteId, cveId,
                vulnerabilityTitle, libraryName, libraryVersion,
                "https://sca.analysiscenter.veracode.com/workspaces/" +
                        workspaceInfo.getWorkspaceSiteId() +
                        "/issues/vulnerabilities/" + siteId);
    }

    public ScaMitigation readCommentsFromPlatform(WebDriver webDriver) {
        this.setComments(SeleniumWrapper.getComments(this, webDriver));
        return this;
    }

    public void populateDateLastIgnore(TypeOfSearchEnum typeOfSearch) {
        boolean foundRequiredAction = false;
        Iterator<Comment> iterator = this.comments.iterator();

        //TODO: review this
        do {
            Comment comment;
            label36:
            do {
                while (iterator.hasNext()) {
                    comment = iterator.next();
                    if (typeOfSearch != TypeOfSearchEnum.REJECTED_ISSUES || !this.isMitigationRejection(comment.getText())) {
                        if (comment.getText().startsWith("Action: ignored")) {
                            this.dateLastIgnore = comment.getDateCreated();
                            return;
                        }
                        continue label36;
                    }

                    foundRequiredAction = true;
                }

                if (!foundRequiredAction) {
                    this.dateLastIgnore = null;
                    return;
                }

                this.dateLastIgnore = (this.comments.get(this.comments.size() - 1)).getDateCreated();
                return;
            } while (!this.isMitigationApproval(comment.getText()));

            this.dateLastIgnore = null;
            if (typeOfSearch != TypeOfSearchEnum.APPROVED_ISSUES) {
                break;
            }

            foundRequiredAction = true;
        } while(typeOfSearch == TypeOfSearchEnum.REJECTED_ISSUES && foundRequiredAction);
    }

    private boolean isMitigationRejection(String commentText) {
        return this.isSpecificCustomAction(commentText, "Comment: Mitigation Rejected:");
    }

    private boolean isMitigationApproval(String commentText) {
        return this.isSpecificCustomAction(commentText, "Comment: Mitigation Approved:");
    }

    private boolean isSpecificCustomAction(String commentText, String prefix) {
        String[] commentSplitInLines = commentText.split("\\n");
        return commentSplitInLines.length == 3 && commentSplitInLines[2].startsWith(prefix);
    }

    public boolean isMitigationOpenBetween(Date startDateFilter, Date endDateFilter) {
        return this.dateLastIgnore != null && this.filterByStartDate(startDateFilter) && this.filterByEndDate(endDateFilter);
    }

    private boolean filterByStartDate(Date startDateFilter) {
        return startDateFilter == null || startDateFilter.compareTo(this.dateLastIgnore) <= 0;
    }

    private boolean filterByEndDate(Date endDateFilter) {
        return endDateFilter == null || endDateFilter.compareTo(this.dateLastIgnore) >= 0;
    }

    public Date getDateLastIgnore() {
        return this.dateLastIgnore;
    }
}