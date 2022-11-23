package com.veracode.scamitigation.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class WebDriverProvider {
    private static final boolean IS_HEADLESS = false;
    private static final String CHROME_DRIVER = "webdriver.chrome.driver";
    private static final String FIREFOX_DRIVER = "webdriver.gecko.driver";

    public WebDriverProvider() {
    }

    public static WebDriver getDriver(String driverName) {
        switch (driverName) {
            case CHROME_DRIVER:
                return getChromeDriver();
            case FIREFOX_DRIVER:
                return getFirefoxDriver();
            default:
                throw new IllegalArgumentException("Invalid driver: " + driverName);
        }
    }

    private static WebDriver getFirefoxDriver() {
        FirefoxOptions firefoxOptions = new FirefoxOptions();
        firefoxOptions.setHeadless(IS_HEADLESS);
        return new FirefoxDriver(firefoxOptions);
    }

    private static WebDriver getChromeDriver() {
        ChromeOptions chromeOptions = new ChromeOptions();
        if (IS_HEADLESS) {
            chromeOptions.addArguments("--headless");
        }
        return new ChromeDriver(chromeOptions);
    }
}
