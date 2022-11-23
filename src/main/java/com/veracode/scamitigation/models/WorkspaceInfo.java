package com.veracode.scamitigation.models;

public class WorkspaceInfo {
    private final String workspaceGuid;
    private final String workspaceName;
    private final String workspaceSiteId;

    public WorkspaceInfo(String workspaceGuid, String workspaceName, String workspaceSiteId) {
        this.workspaceGuid = workspaceGuid;
        this.workspaceName = workspaceName;
        this.workspaceSiteId = workspaceSiteId;
    }

    public String getWorkspaceName() {
        return this.workspaceName;
    }

    public String getWorkspaceGuid() {
        return this.workspaceGuid;
    }

    public String getWorkspaceSiteId() {
        return this.workspaceSiteId;
    }
}
