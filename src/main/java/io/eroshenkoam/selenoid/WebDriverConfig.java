package io.eroshenkoam.selenoid;

import org.aeonbits.owner.Config;

import java.net.URL;

public interface WebDriverConfig extends Config {

    @Key("webdriver.remote.url")
    URL remoteUrl();

}
