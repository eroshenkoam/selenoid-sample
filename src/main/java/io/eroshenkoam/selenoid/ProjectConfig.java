package io.eroshenkoam.selenoid;

import org.aeonbits.owner.Config;

import java.net.URL;

public interface ProjectConfig extends Config {

    @Key("webdriver.remote.url")
    URL remoteUrl();

    @DefaultValue("https://auto.ru")
    @Key("webdriver.base.url")
    String baseUrl();

    @DefaultValue("5")
    @Key("test.count")
    int testCount();

}
