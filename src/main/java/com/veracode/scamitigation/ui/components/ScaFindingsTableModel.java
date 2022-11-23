package com.veracode.scamitigation.ui.components;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class ScaFindingsTableModel extends DefaultTableModel {
    private static final Object[] TABLE_HEADERS = new Object[]{"Date Last Ignored", "Workspace", "Project", "Issue ID", "CVE ID", "Description", "Library", "Version", "Open in Platform", "Approve", "Reject", "Comments"};
    private final JTable table;

    public ScaFindingsTableModel(JTable table, int rowCount) {
        super(TABLE_HEADERS, rowCount);
        this.table = table;
        table.setFillsViewportHeight(true);
    }

    public ScaFindingsTableModel(JTable table) {
        super(TABLE_HEADERS, 0);
        this.table = table;
    }

    public void initializeRenderers() {
        this.table.getColumn("Open in Platform").setCellRenderer(new TableWithButtonsRenderer());
        this.table.getColumn("Approve").setCellRenderer(new TableWithButtonsRenderer());
        this.table.getColumn("Reject").setCellRenderer(new TableWithButtonsRenderer());
        this.table.getColumn("Comments").setCellRenderer(new TableWithButtonsRenderer());
    }

    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
