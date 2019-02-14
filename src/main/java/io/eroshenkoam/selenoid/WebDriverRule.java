package io.eroshenkoam.selenoid;

import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import org.apache.commons.io.IOUtils;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

public class WebDriverRule extends TestWatcher {

    private static final Logger LOGGER = Logger.getLogger(WebDriverRule.class.getName());

    private final AllureLifecycle lifecycle = Allure.getLifecycle();

    private final ProjectConfig config;
    private WebDriver driver;

    public WebDriver getDriver() {
        return driver;
    }

    public WebDriverRule(ProjectConfig config) {
        this.config = config;
    }

    protected void starting(Description description) {
        final DesiredCapabilities capabilities = new DesiredCapabilities("chrome", "", Platform.ANY);
        capabilities.setCapability("enableVideo", config.videoEnabled());
        capabilities.setCapability("name", description.getDisplayName());
        capabilities.setCapability("enableVNC", true);
        capabilities.setBrowserName(config.browserName());
        capabilities.setVersion(config.browserVersion());

        this.driver = new RemoteWebDriver(config.remoteUrl(), capabilities);
    }

    protected void finished(Description description) {
        if (config.videoEnabled()) {
            getSelenoidVideoUrl().ifPresent(videoUrl -> {
                this.driver.quit();
                attachVideo(videoUrl);
            });
        } else {
            this.driver.quit();
        }
    }

    private void attachVideo(String videoUrl) {
        try {
            HttpURLConnection connection = getConnection(videoUrl);
            byte[] bytes = IOUtils.toByteArray(connection);
            lifecycle.addAttachment("Video", "video/mp4", "mp4", bytes);
        } catch (Exception e) {
            LOGGER.info(String.format("Can't attach video file: %s", e.getMessage()));
        }
    }

    private HttpURLConnection getConnection(String videoUrl) {
        return await().timeout(20, TimeUnit.SECONDS).until(() -> {
            return (HttpURLConnection) new URL(videoUrl).openConnection();
        }, hasProperty("responseCode", equalTo(200)));
    }

    private Optional<String> getSelenoidVideoUrl() {
        if (Objects.nonNull(driver) && driver instanceof RemoteWebDriver) {
            RemoteWebDriver remote = ((RemoteWebDriver) driver);
            return Optional.of(remote).map(this::getSelenoidVideoUrl);
        }
        return Optional.empty();
    }

    private String getSelenoidVideoUrl(RemoteWebDriver driver) {
        String sessionId = driver.getSessionId().toString();
        URL url = ((HttpCommandExecutor) driver.getCommandExecutor()).getAddressOfRemoteServer();
        return String.format("%s://%s:%s/video/%s.mp4", url.getProtocol(), url.getHost(), url.getPort(), sessionId);
    }

}
