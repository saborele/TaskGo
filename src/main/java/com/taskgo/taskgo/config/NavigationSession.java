package com.taskgo.taskgo.config;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class NavigationSession {
    private Playwright playwright;
    private Browser browser;
    private Page page;
    private String url;
    private String query;

    public NavigationSession(Playwright playwright, Browser browser, Page page, String url, String query) {
        this.playwright = playwright;
        this.browser = browser;
        this.page = page;
        this.url = url;
        this.query = query;
    }

    public Playwright getPlaywright() {
        return playwright;
    }

    public Browser getBrowser() {
        return browser;
    }

    public Page getPage() {
        return page;
    }

    public String getUrl() {
        return url;
    }

    public String getQuery() {
        return query;
    }

    public void close() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}