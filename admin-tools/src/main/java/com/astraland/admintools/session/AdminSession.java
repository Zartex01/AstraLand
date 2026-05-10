package com.astraland.admintools.session;

import java.util.UUID;

public class AdminSession {

    private int page = 0;
    private FilterMode filterMode = FilterMode.CONTAINS;
    private String filterText = "";
    private PendingInput pendingInput = null;
    private UUID targetPlayerUUID = null;

    public int getPage()                           { return page; }
    public void setPage(int page)                  { this.page = Math.max(0, page); }

    public FilterMode getFilterMode()              { return filterMode; }
    public void setFilterMode(FilterMode mode)     { this.filterMode = mode; }

    public String getFilterText()                  { return filterText; }
    public void setFilterText(String text)         { this.filterText = text == null ? "" : text; }
    public boolean hasFilter()                     { return !filterText.isEmpty(); }

    public PendingInput getPendingInput()          { return pendingInput; }
    public void setPendingInput(PendingInput p)    { this.pendingInput = p; }
    public boolean isWaitingInput()                { return pendingInput != null; }
    public void clearPendingInput()                { this.pendingInput = null; }

    public UUID getTargetPlayerUUID()              { return targetPlayerUUID; }
    public void setTargetPlayerUUID(UUID uuid)     { this.targetPlayerUUID = uuid; }
}
