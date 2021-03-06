package io.eroshenkoam.selenoid;

import org.aeonbits.owner.Config;

import java.net.URL;

public interface ProjectConfig extends Config {

    @Key("webdriver.remote.url")
    URL remoteUrl();

    @Key("webdriver.browser.name")
    String browserName();

    @Key("webdriver.browser.version")
    String browserVersion();

    @DefaultValue("https://auto.ru")
    @Key("webdriver.base.url")
    String baseUrl();

    @DefaultValue("true")
    @Key("webdriver.video.enabled")
    boolean videoEnabled();

    @DefaultValue("5")
    @Key("test.count")
    int testCount();

}
