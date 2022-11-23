package com.veracode.scamitigation.selenium;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public final class SeleniumHelper {
    private static final int POLLING_TIMEOUT = 15;

    private SeleniumHelper() {
        throw new IllegalStateException("This class should not be instantiated");
    }

    public static boolean checkIfElementIsPresent(WebDriver webDriver, By elementToCheck) {
        try {
            webDriver.findElement(elementToCheck);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public static void waitForElementPresent(WebDriver webDriver, By elementToCheck) throws TimeoutException {
        boolean hasFound = false;
        Instant start = Instant.now();

        while(!hasFound) {
            try {
                webDriver.findElement(elementToCheck);
                hasFound = true;
            } catch (NoSuchElementException e) {
                checkTimeout(start);
            }
        }

    }

    private static void checkTimeout(Instant start) throws TimeoutException {
        if (getTimeElapsed(start) > 15L) {
            timeout();
        }

        try {
            TimeUnit.SECONDS.sleep(1L);
        } catch (InterruptedException e) {
        }

    }

    public static void clickElement(WebDriver webDriver, By elementToClick) throws TimeoutException {
        boolean hasClicked = false;
        Instant start = Instant.now();

        while(!hasClicked) {
            try {
                webDriver.findElement(elementToClick).click();
                hasClicked = true;
            } catch (ElementNotInteractableException e) {
                checkTimeout(start);
            }
        }

    }

    public static boolean waitForCondition(WebDriver webDriver, By elementToEvaluate, Predicate<WebElement> conditionEvaluation) throws TimeoutException {
        Instant start = Instant.now();

        while(true) {
            try {
                WebElement foundElement = webDriver.findElement(elementToEvaluate);
                if (foundElement != null && conditionEvaluation.test(foundElement)) {
                    return true;
                }
            } catch (NoSuchElementException | ElementNotInteractableException e) {
                checkTimeout(start);
                return false;
            }
        }
    }

    public static void sendKeysToElement(WebDriver webDriver, By elementToSendKeys, String keysToSend) throws TimeoutException {
        boolean hasSentKeys = false;
        Instant start = Instant.now();

        while(!hasSentKeys) {
            try {
                webDriver.findElement(elementToSendKeys).sendKeys(keysToSend);
                hasSentKeys = true;
            } catch (NoSuchElementException | ElementNotInteractableException e) {
                checkTimeout(start);
            }
        }

    }

    private static void timeout() throws TimeoutException {
        throw new TimeoutException("Timed out when running command");
    }

    private static long getTimeElapsed(Instant start) {
        return Duration.between(start, Instant.now()).getSeconds();
    }
}