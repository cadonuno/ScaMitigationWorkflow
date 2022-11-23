package com.veracode.scamitigation.selenium;

import com.veracode.scamitigation.ExceptionHandler;
import com.veracode.scamitigation.models.Comment;
import com.veracode.scamitigation.models.ScaMitigation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public final class SeleniumWrapper {
    private static final String BASE_URL = "https://web.analysiscenter.veracode.com/login/";
    private static final String USERNAME_FIELD_ID = "okta-signin-username";
    private static final String PASSWORD_FIELD_ID = "okta-signin-password";
    private static final String LOGIN_BUTTON_ID = "okta-signin-submit";
    private static final String USER_NAME_ICON_ID = "icon_user";
    private static final String COMMENTS_RED_ICON_XPATH = "//*[@class='fas fa-circle color--danger']";
    private static final String VIEW_COMMENTS_BUTTON_XPATH = "//*[@class='popover--dark']/div";
    private static final String COMMENT_HISTORY_CLOCK_ICON = "//*[@class='fas fa-clock mr- font--h4 color--primary']";
    private static final String ALL_COMMENT_ROWS_XPATH = "//div[@class='mh']/div[@class='pr']/div[@class='grid']";
    private static final String EACH_COMMENT_TEXT_XPATH = "//div[@class='mh']/div[@class='pr']/div[@class='grid']/div[@class='grid__item col-2-3 pt pb bg-color--white']";
    private static final String EACH_COMMENT_DATE_XPATH = "//div[@class='mh']/div[@class='pr']/div[@class='grid']/div[@class='grid__item text--right position--relative pr col-1-3 bo-r--1 border-style--dashed border-color--muted-light pb pt color--black-light']";
    private static final String POST_COMMENTS_BUTTON_XPATH = "//div[@class='flex grid__item flex--justify-content--end align-item--center col-2-5 pt--']/div[@class='link--obvious link--no-underline font--16 pb--- pt-- flex flex--align-items--center mr']/span[@class='pl--']";
    private static final String COMMENT_TEXT_AREA_XPATH = "//textarea[@name='comment']";
    private static final String COMMENT_POST_BUTTON_XPATH = "//button[@type='submit']";
    private static final String COMMENT_ADDED_MESSAGE_XPATH = "//div[@class='toastr toastr--success']/div[@class='flex flex--justify-content--space-between align-items--center pv-- ph- toastr--bg-color--success']/div[@class='flex align-items--center']";
    public static final String IGNORE_ISSUE_INPUT_FIELD_XPATH = "//input[@name='ignore-issue']";
    public static final String IGNORE_ISSUE_CHECKBOX_XPATH = "//span[@class='control--checkbox']";
    private static int numberOfAttempts = 0;
    private static final int MAX_ATTEMPTS = 10;

    private SeleniumWrapper() {
    }

    public static List<Comment> getComments(ScaMitigation scaMitigation, WebDriver webDriver) {
        return runOnIssue(scaMitigation.getIssueLink(), webDriver,
                seleniumWebDriver -> getCommentsInternal(webDriver, scaMitigation.getIssueLink()));
    }

    private static List<Comment> getCommentsInternal(WebDriver webDriver, String issueLink) {
        try {
            return tryOpenComments(webDriver) ? readComments(webDriver) : Collections.emptyList();
        } catch (TimeoutException e) {
            return handleTimeout(webDriver, issueLink,
                    () -> getCommentsInternal(webDriver, issueLink),
                    Collections.emptyList());
        }
    }

    public static boolean tryPostComment(String commentToAdd, String issueLink) {
        return runOnIssue(issueLink, (webDriver ->
                tryPostCommentInternal(commentToAdd, issueLink, webDriver)), false);
    }

    private static boolean tryPostCommentInternal(String commentToAdd, String issueLink, WebDriver webDriver) {
        try {
            postComment(commentToAdd, webDriver);
            return true;
        } catch (TimeoutException e) {
            return handleTimeout(webDriver, issueLink,
                    () -> tryPostCommentInternal(commentToAdd, issueLink, webDriver), false);
        }
    }

    public static boolean approveMitigation(String approvalMessage, String issueLink) {
        return runOnIssue(issueLink,
                webDriver -> approveMitigationInternal(approvalMessage, issueLink, webDriver), false);
    }

    private static boolean approveMitigationInternal(String approvalMessage, String issueLink, WebDriver webDriver) {
        try {
            postComment("Mitigation Approved:\n" + approvalMessage, webDriver);
            return true;
        } catch (TimeoutException e) {
            return handleTimeout(webDriver, issueLink,
                    () -> approveMitigationInternal(approvalMessage, issueLink, webDriver),
                    false);
        }
    }

    public static boolean rejectMitigation(String rejectMessage, String issueLink) {
        return runOnIssue(issueLink, webDriver -> {
            try {
                SeleniumHelper.waitForElementPresent(webDriver, By.xpath("//input[@name='ignore-issue']"));
                SeleniumHelper.clickElement(webDriver, By.xpath("//span[@class='control--checkbox']"));
                typeInComment("Mitigation Rejected:\n" + rejectMessage, webDriver);
                return SeleniumHelper.waitForCondition(webDriver,
                        By.xpath("//input[@name='ignore-issue']"),
                        checkbox -> !checkbox.isSelected());
            } catch (TimeoutException e) {
                ExceptionHandler.logException(e);
                return false;
            }
        }, false);
    }

    private static void postComment(String commentToAdd, WebDriver webDriver) throws TimeoutException {
        SeleniumHelper.waitForElementPresent(webDriver, By.xpath("//div[@class='flex grid__item flex--justify-content--end align-item--center col-2-5 pt--']/div[@class='link--obvious link--no-underline font--16 pb--- pt-- flex flex--align-items--center mr']/span[@class='pl--']"));
        SeleniumHelper.clickElement(webDriver, By.xpath("//div[@class='flex grid__item flex--justify-content--end align-item--center col-2-5 pt--']/div[@class='link--obvious link--no-underline font--16 pb--- pt-- flex flex--align-items--center mr']/span[@class='pl--']"));
        typeInComment(commentToAdd, webDriver);
        SeleniumHelper.waitForElementPresent(webDriver, By.xpath("//div[@class='toastr toastr--success']/div[@class='flex flex--justify-content--space-between align-items--center pv-- ph- toastr--bg-color--success']/div[@class='flex align-items--center']"));
    }

    private static void typeInComment(String commentToAdd, WebDriver webDriver) throws TimeoutException {
        SeleniumHelper.sendKeysToElement(webDriver, By.xpath("//textarea[@name='comment']"), commentToAdd);
        SeleniumHelper.clickElement(webDriver, By.xpath("//button[@type='submit']"));
    }

    private static void loginToPlatform(String username, String password, WebDriver webDriver) throws TimeoutException {
        webDriver.manage().window().setSize(new Dimension(1920, 1080));
        SeleniumHelper.waitForElementPresent(webDriver, By.id("okta-signin-submit"));
        SeleniumHelper.sendKeysToElement(webDriver, By.id("okta-signin-username"), username);
        SeleniumHelper.sendKeysToElement(webDriver, By.id("okta-signin-password"), password);
        SeleniumHelper.clickElement(webDriver, By.id("okta-signin-submit"));
        SeleniumHelper.waitForElementPresent(webDriver, By.id("icon_user"));
    }

    private static boolean tryOpenComments(WebDriver webDriver) throws TimeoutException {
        SeleniumHelper.waitForElementPresent(webDriver, By.xpath("//div[@class='flex grid__item flex--justify-content--end align-item--center col-2-5 pt--']/div[@class='link--obvious link--no-underline font--16 pb--- pt-- flex flex--align-items--center mr']/span[@class='pl--']"));
        if (!SeleniumHelper.checkIfElementIsPresent(webDriver, By.xpath(COMMENTS_RED_ICON_XPATH))) {
            return false;
        }
        SeleniumHelper.clickElement(webDriver, By.xpath(COMMENTS_RED_ICON_XPATH));
        SeleniumHelper.clickElement(webDriver, By.xpath("//*[@class='popover--dark']/div"));
        SeleniumHelper.waitForElementPresent(webDriver, By.xpath("//*[@class='fas fa-clock mr- font--h4 color--primary']"));
        SeleniumHelper.waitForElementPresent(webDriver, By.xpath("//div[@class='mh']/div[@class='pr']/div[@class='grid']"));
        return true;
    }

    private static List<Comment> readComments(WebDriver webDriver) {
        List<WebElement> allDateElements = webDriver.findElements(By.xpath("//div[@class='mh']/div[@class='pr']/div[@class='grid']/div[@class='grid__item text--right position--relative pr col-1-3 bo-r--1 border-style--dashed border-color--muted-light pb pt color--black-light']"));
        List<WebElement> allCommentElements = webDriver.findElements(By.xpath("//div[@class='mh']/div[@class='pr']/div[@class='grid']/div[@class='grid__item col-2-3 pt pb bg-color--white']"));
        List<Comment> allComments = new ArrayList<>();

        for (int currentIndex = 0; currentIndex < allCommentElements.size() || currentIndex < allDateElements.size(); ++currentIndex) {
            Date dateCreated = currentIndex < allDateElements.size() ? getDateFromField(((WebElement) allDateElements.get(currentIndex)).getText()) : null;
            String commentText = currentIndex < allCommentElements.size() ? ((WebElement) allCommentElements.get(currentIndex)).getText() : "";
            allComments.add(new Comment(dateCreated, commentText));
        }

        return allComments;
    }

    private static <T> T runOnIssue(String issueLink, Function<WebDriver, T> toRun, T defaultReturn) {
        numberOfAttempts = 0;
        return tryLoginWithNewDriver()
                .map(webDriver -> {
                    T returnValue;
                    try {
                        webDriver.get(issueLink);
                        returnValue = toRun.apply(webDriver);
                    } finally {
                        webDriver.close();
                    }
                    return returnValue;
                })
                .orElse(defaultReturn);
    }

    private static <T> T handleTimeout(WebDriver webDriver, String baseUrl, Supplier<T> retryMethod, T defaultValue) {
        tryLoginWithDriver(webDriver);
        webDriver.get(baseUrl);
        return numberOfAttempts++ < 10 ? retryMethod.get() : defaultValue;
    }

    private static <T> T runOnIssue(String issueLink, WebDriver webDriver, Function<WebDriver, T> toRun) {
        numberOfAttempts = 0;
        System.setProperty(ExecutionParameters.getInstance().getWebDriverName(),
                ExecutionParameters.getInstance().getWebDriverLocation());
        webDriver.get(issueLink);
        return toRun.apply(webDriver);
    }

    public static Optional<WebDriver> tryLoginWithNewDriver() {
        System.setProperty(ExecutionParameters.getInstance().getWebDriverName(),
                ExecutionParameters.getInstance().getWebDriverLocation());
        WebDriver webDriver = WebDriverProvider.getDriver(ExecutionParameters.getInstance().getWebDriverName());
        return tryLoginWithDriver(webDriver);
    }

    private static Optional<WebDriver> tryLoginWithDriver(WebDriver webDriver) {
        try {
            webDriver.get("https://web.analysiscenter.veracode.com/login/");
            loginToPlatform(ExecutionParameters.getInstance().getUsername(),
                    ExecutionParameters.getInstance().getPassword(), webDriver);
            return Optional.of(webDriver);
        } catch (TimeoutException e) {
            return Optional.empty();
        }
    }

    private static Date getDateFromField(String dateFieldValue) {
        String firstLine = dateFieldValue.split("\\n")[0];
        return new Date(firstLine.substring(0, firstLine.lastIndexOf(" ") - 2));
    }
}
