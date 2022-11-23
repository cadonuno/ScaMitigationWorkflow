package com.veracode.scamitigation.forms;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.veracode.scamitigation.DateHandler;
import com.veracode.scamitigation.dialogs.MessageDialog;
import com.veracode.scamitigation.models.Comment;
import com.veracode.scamitigation.models.ScaMitigation;
import com.veracode.scamitigation.selenium.SeleniumWrapper;
import com.veracode.scamitigation.ui.components.CursorHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Objects;

public class CommentsForm  extends BaseForm {
    public static final int FORM_WIDTH = 1024;
    public static final int VERTICAL_GAP = 5;
    public static final int HORIZONTAL_GAP = 10;
    public static final int MINIMUM_HEIGHT = 400;
    public static final int SPACE_BETWEEN_LINES = 1;
    private JScrollPane scrollPane;
    private JPanel rootPanel;
    private JTextArea commentTextArea;
    private JButton postCommentButton;
    private JPanel buttonsPanel;
    private JPanel footerPanel;
    private int totalHeight;
    private static CommentsForm instance;
    private static JFrame form;
    private String issueLink;

    protected CommentsForm(JFrame frame) {
        super(frame);
        this.$$$setupUI$$$();
        instance = this;
    }

    public static void openPage() {
        form = FormHandler.handleInitialize(CommentsForm.class, "Comments", form);
        form.setDefaultCloseOperation(1);
    }

    public static void setScaMitigation(ScaMitigation scaMitigation) {
        instance.setComments(scaMitigation.getComments());
        instance.issueLink = scaMitigation.getIssueLink();
        instance.postCommentButton.addActionListener(instance::tryPostComment);
    }

    private void setComments(List<Comment> comments) {
        this.totalHeight = 0;
        JPanel contentsPanel = new JPanel();
        this.populateAllRows(comments, contentsPanel);
        this.adjustSizes(contentsPanel);
        form.revalidate();
        form.repaint();
    }

    private void tryPostComment(ActionEvent actionEvent) {
        String commentToAdd = this.commentTextArea.getText();
        if (commentToAdd.length() == 0) {
            MessageDialog.showWarningDialog("To add a comment, enter its value in the field above.");
        } else {
            boolean hasAdded = CursorHandler.runWithWaitCursor(this.frame,
                    () -> SeleniumWrapper.tryPostComment(commentToAdd, this.issueLink));
            if (!hasAdded) {
                MessageDialog.showErrorDialog("Unable to post comment!");
            }
        }

    }

    private void adjustSizes(JPanel contentsPanel) {
        this.scrollPane.add(contentsPanel);
        this.scrollPane.setViewportView(contentsPanel);
        contentsPanel.setSize(1024, this.totalHeight);
        contentsPanel.setPreferredSize(new Dimension(1024, this.totalHeight));
        int scrollPaneHeight = Math.min(Math.max(this.totalHeight, 400), 768);
        this.scrollPane.setSize(1024, scrollPaneHeight);
        this.scrollPane.setPreferredSize(new Dimension(1024, scrollPaneHeight));
        contentsPanel.revalidate();
        contentsPanel.repaint();
        this.scrollPane.revalidate();
        this.scrollPane.repaint();
        form.setSize(1024, scrollPaneHeight + this.footerPanel.getHeight());
        form.setPreferredSize(new Dimension(1024, scrollPaneHeight + this.footerPanel.getHeight()));
    }

    private void populateAllRows(List<Comment> comments, JPanel contentsPanel) {
        GroupLayout groupLayout = new GroupLayout(contentsPanel);
        contentsPanel.setLayout(groupLayout);
        GroupLayout.ParallelGroup horizontalGroup = groupLayout.createParallelGroup();
        groupLayout.setHorizontalGroup(horizontalGroup);
        GroupLayout.SequentialGroup verticalGroup = groupLayout.createSequentialGroup();
        groupLayout.setVerticalGroup(verticalGroup);
        comments.forEach((comment) -> {
            this.addRow(horizontalGroup, verticalGroup, comment);
        });
        this.totalHeight += 10;
    }

    private void addRow(GroupLayout.ParallelGroup horizontalGroup, GroupLayout.SequentialGroup verticalGroup, Comment comment) {
        JPanel rowPanel = new JPanel();
        JTextArea dateTextArea = this.getDateTextArea(DateHandler.getDateAsString(comment.getDateCreated()));
        JTextArea commentTextArea = this.getCommentTextArea(comment.getText(), dateTextArea.getWidth());
        rowPanel.add(dateTextArea);
        rowPanel.add(commentTextArea);
        horizontalGroup.addComponent(rowPanel);
        verticalGroup.addComponent(rowPanel, -2, -2, -2);
        verticalGroup.addGap(5);
        this.totalHeight += 5;
    }

    private JTextArea getDateTextArea(String dateAsString) {
        JTextArea textArea = this.getTextArea(dateAsString);
        int areaWidth = 100;
        int textAreaHeight = this.getTextAreaHeight(textArea, 1);
        textArea.setSize(areaWidth, textAreaHeight);
        textArea.setPreferredSize(new Dimension(areaWidth, textAreaHeight));
        return textArea;
    }

    private JTextArea getCommentTextArea(String comment, int dateAreaWidth) {
        JTextArea textArea = this.getTextArea(comment);
        int numberOfLines = 1 + comment.split("\r\n|\r|\n").length;
        int textAreaHeight = this.getTextAreaHeight(textArea, numberOfLines);
        int textAreaWidth = 1024 - dateAreaWidth - 10;
        textArea.setSize(textAreaWidth, textAreaHeight);
        textArea.setPreferredSize(new Dimension(textAreaWidth, textAreaHeight));
        this.totalHeight += textAreaHeight;
        return textArea;
    }

    private int getTextAreaHeight(JTextArea textArea, int numberOfLines) {
        return numberOfLines * (textArea.getFont().getSize() + 1) + 2;
    }

    private JTextArea getTextArea(String text) {
        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        return textArea;
    }

    protected Container getRootPanel() {
        return this.rootPanel;
    }

    private void $$$setupUI$$$() {
        this.rootPanel = new JPanel();
        this.rootPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        this.scrollPane = new JScrollPane();
        this.rootPanel.add(this.scrollPane, new GridConstraints(0, 0, 1, 1, 0, 3, 5, 5, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        this.footerPanel = new JPanel();
        this.footerPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        this.rootPanel.add(this.footerPanel, new GridConstraints(1, 0, 1, 1, 0, 3, 3, 3, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        this.commentTextArea = new JTextArea();
        this.commentTextArea.setText("");
        this.footerPanel.add(this.commentTextArea, new GridConstraints(0, 0, 1, 1, 0, 3, 4, 4, (Dimension)null, new Dimension(150, 50), (Dimension)null, 0, false));
        this.buttonsPanel = new JPanel();
        this.buttonsPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        this.footerPanel.add(this.buttonsPanel, new GridConstraints(1, 0, 1, 1, 0, 3, 3, 3, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        this.postCommentButton = new JButton();
        this.postCommentButton.setText("Post Comment");
        this.buttonsPanel.add(this.postCommentButton, new GridConstraints(0, 0, 1, 1, 0, 1, 3, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
    }

    public JComponent $$$getRootComponent$$$() {
        return this.rootPanel;
    }
}
