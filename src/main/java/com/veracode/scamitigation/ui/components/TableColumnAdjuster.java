package com.veracode.scamitigation.ui.components;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class TableColumnAdjuster implements PropertyChangeListener, TableModelListener {
    private static final int MINIMUM_WIDTH = 1110;
    private final JTable table;
    private final int spacing;
    private boolean isColumnHeaderIncluded;
    private boolean isColumnDataIncluded;
    private boolean isOnlyAdjustLarger;
    private boolean isDynamicAdjustment;
    private final Map<TableColumn, Integer> columnSizes = new HashMap();
    private int totalWidth;

    private TableColumnAdjuster(JTable table) {
        this.table = table;
        this.spacing = 6;
        this.setColumnHeaderIncluded(true);
        this.setColumnDataIncluded(true);
        this.setOnlyAdjustLarger(true);
        this.setDynamicAdjustment(false);
        this.installActions();
    }

    public static Dimension adjustTable(JTable table) {
        TableColumnAdjuster adjuster = new TableColumnAdjuster(table);
        adjuster.adjustColumns();
        return new Dimension(adjuster.getTotalWidth(), getTableHeight(table));
    }

    private static int getTableHeight(JTable table) {
        int height = table.getTableHeader().getHeight();
        if (table.getRowCount() > 0) {
            height += table.getRowHeight() * (table.getRowCount() + 1) + table.getIntercellSpacing().height * table.getRowCount();
        }

        return height;
    }

    public void adjustColumns() {
        TableColumnModel tableColumnModel = this.table.getColumnModel();
        this.totalWidth = 0;

        for(int columnIndex = 0; columnIndex < tableColumnModel.getColumnCount(); ++columnIndex) {
            this.adjustColumn(columnIndex);
        }

        this.table.getTableHeader().setResizingColumn((TableColumn)null);
        this.table.setPreferredSize(new Dimension(Math.max(1110, (int)this.table.getPreferredSize().getWidth()), (int)this.table.getPreferredSize().getHeight()));
        this.table.setSize(new Dimension(Math.max(1110, (int)this.table.getSize().getWidth()), (int)this.table.getSize().getHeight()));
    }

    public void adjustColumn(int columnIndex) {
        TableColumn tableColumn = this.table.getColumnModel().getColumn(columnIndex);
        if (tableColumn.getResizable()) {
            int columnHeaderWidth = this.getColumnHeaderWidth(columnIndex, tableColumn);
            int columnDataWidth = this.getColumnDataWidth(columnIndex, tableColumn);
            this.updateTableColumn(tableColumn, Math.max(columnHeaderWidth, columnDataWidth));
        }
    }

    private int getColumnHeaderWidth(int column, TableColumn tableColumn) {
        if (!this.isColumnHeaderIncluded) {
            return 0;
        } else {
            Object value = tableColumn.getHeaderValue();
            TableCellRenderer renderer = tableColumn.getHeaderRenderer();
            if (renderer == null) {
                renderer = this.table.getTableHeader().getDefaultRenderer();
            }

            return renderer.getTableCellRendererComponent(this.table, value, false, false, -1, column).getPreferredSize().width;
        }
    }

    private int getColumnDataWidth(int column, TableColumn tableColumn) {
        if (!this.isColumnDataIncluded) {
            return 0;
        } else {
            int preferredWidth = 0;
            int maxWidth = tableColumn.getMaxWidth();

            for(int row = 0; row < this.table.getRowCount(); ++row) {
                preferredWidth = Math.max(preferredWidth, this.getCellDataWidth(row, column));
                if (preferredWidth >= maxWidth) {
                    break;
                }
            }

            return preferredWidth;
        }
    }

    private int getCellDataWidth(int row, int column) {
        return column >= 8 ? this.getCellDataWidthForButton(row, column) : this.getCellDataWidthForText(row, column);
    }

    private int getCellDataWidthForButton(int row, int column) {
        TableCellRenderer cellRenderer = this.table.getCellRenderer(row, column);
        Component renderedComponent = this.table.prepareRenderer(cellRenderer, row, column);
        return renderedComponent.getWidth() + this.table.getIntercellSpacing().width;
    }

    private int getCellDataWidthForText(int row, int column) {
        TableCellRenderer cellRenderer = this.table.getCellRenderer(row, column);
        Component renderedComponent = this.table.prepareRenderer(cellRenderer, row, column);
        return renderedComponent.getPreferredSize().width + this.table.getIntercellSpacing().width;
    }

    private void updateTableColumn(TableColumn tableColumn, int width) {
        if (!tableColumn.getResizable()) {
            this.totalWidth += tableColumn.getPreferredWidth();
        } else {
            width += this.spacing;
            if (this.isOnlyAdjustLarger) {
                width = Math.max(width, tableColumn.getPreferredWidth());
            }

            this.columnSizes.put(tableColumn, tableColumn.getWidth());
            this.table.getTableHeader().setResizingColumn(tableColumn);
            tableColumn.setPreferredWidth(width);
            tableColumn.setWidth(width);
            this.totalWidth += width;
        }
    }

    public void restoreColumns() {
        TableColumnModel tcm = this.table.getColumnModel();

        for(int i = 0; i < tcm.getColumnCount(); ++i) {
            this.restoreColumn(i);
        }

    }

    private void restoreColumn(int column) {
        TableColumn tableColumn = this.table.getColumnModel().getColumn(column);
        Optional.ofNullable((Integer)this.columnSizes.get(tableColumn)).ifPresent((width) -> {
            this.table.getTableHeader().setResizingColumn(tableColumn);
            tableColumn.setWidth(width);
        });
    }

    public void setColumnHeaderIncluded(boolean isColumnHeaderIncluded) {
        this.isColumnHeaderIncluded = isColumnHeaderIncluded;
    }

    public void setColumnDataIncluded(boolean isColumnDataIncluded) {
        this.isColumnDataIncluded = isColumnDataIncluded;
    }

    public void setOnlyAdjustLarger(boolean isOnlyAdjustLarger) {
        this.isOnlyAdjustLarger = isOnlyAdjustLarger;
    }

    public void setDynamicAdjustment(boolean isDynamicAdjustment) {
        if (this.isDynamicAdjustment != isDynamicAdjustment) {
            if (isDynamicAdjustment) {
                this.table.addPropertyChangeListener(this);
                this.table.getModel().addTableModelListener(this);
            } else {
                this.table.removePropertyChangeListener(this);
                this.table.getModel().removeTableModelListener(this);
            }
        }

        this.isDynamicAdjustment = isDynamicAdjustment;
    }

    public void propertyChange(PropertyChangeEvent e) {
        if ("model".equals(e.getPropertyName())) {
            TableModel model = (TableModel)e.getOldValue();
            model.removeTableModelListener(this);
            model = (TableModel)e.getNewValue();
            model.addTableModelListener(this);
            this.adjustColumns();
        }

    }

    public void tableChanged(TableModelEvent e) {
        if (this.isColumnDataIncluded) {
            if (e.getType() == 0) {
                int column = this.table.convertColumnIndexToView(e.getColumn());
                if (this.isOnlyAdjustLarger) {
                    int row = e.getFirstRow();
                    TableColumn tableColumn = this.table.getColumnModel().getColumn(column);
                    if (tableColumn.getResizable()) {
                        int width = this.getCellDataWidthForText(row, column);
                        this.updateTableColumn(tableColumn, width);
                    }
                } else {
                    this.adjustColumn(column);
                }
            } else {
                this.adjustColumns();
            }

        }
    }

    private void installActions() {
        this.installColumnAction(true, true, "adjustColumn", "control ADD");
        this.installColumnAction(false, true, "adjustColumns", "control shift ADD");
        this.installColumnAction(true, false, "restoreColumn", "control SUBTRACT");
        this.installColumnAction(false, false, "restoreColumns", "control shift SUBTRACT");
        this.installToggleAction(true, false, "toggleDynamic", "control MULTIPLY");
        this.installToggleAction(false, true, "toggleLarger", "control DIVIDE");
    }

    private void installColumnAction(boolean isSelectedColumn, boolean isAdjust, String key, String keyStroke) {
        Action action = new ColumnAction(isSelectedColumn, isAdjust);
        KeyStroke ks = KeyStroke.getKeyStroke(keyStroke);
        this.table.getInputMap().put(ks, key);
        this.table.getActionMap().put(key, action);
    }

    private void installToggleAction(boolean isToggleDynamic, boolean isToggleLarger, String key, String keyStroke) {
        Action action = new ToggleAction(isToggleDynamic, isToggleLarger);
        KeyStroke ks = KeyStroke.getKeyStroke(keyStroke);
        this.table.getInputMap().put(ks, key);
        this.table.getActionMap().put(key, action);
    }

    public int getTotalWidth() {
        return this.totalWidth;
    }

    class ToggleAction extends AbstractAction {
        private final boolean isToggleDynamic;
        private final boolean isToggleLarger;

        public ToggleAction(boolean isToggleDynamic, boolean isToggleLarger) {
            this.isToggleDynamic = isToggleDynamic;
            this.isToggleLarger = isToggleLarger;
        }

        public void actionPerformed(ActionEvent e) {
            if (this.isToggleDynamic) {
                TableColumnAdjuster.this.setDynamicAdjustment(!TableColumnAdjuster.this.isDynamicAdjustment);
            } else if (this.isToggleLarger) {
                TableColumnAdjuster.this.setOnlyAdjustLarger(!TableColumnAdjuster.this.isOnlyAdjustLarger);
            }

        }
    }

    class ColumnAction extends AbstractAction {
        private final boolean isSelectedColumn;
        private final boolean isAdjust;

        public ColumnAction(boolean isSelectedColumn, boolean isAdjust) {
            this.isSelectedColumn = isSelectedColumn;
            this.isAdjust = isAdjust;
        }

        public void actionPerformed(ActionEvent e) {
            if (this.isSelectedColumn) {
                int[] columns = TableColumnAdjuster.this.table.getSelectedColumns();

                for (int column : columns) {
                    if (this.isAdjust) {
                        TableColumnAdjuster.this.adjustColumn(column);
                    } else {
                        TableColumnAdjuster.this.restoreColumn(column);
                    }
                }
            } else if (this.isAdjust) {
                TableColumnAdjuster.this.adjustColumns();
            } else {
                TableColumnAdjuster.this.restoreColumns();
            }

        }
    }
}
