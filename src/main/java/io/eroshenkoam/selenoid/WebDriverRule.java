package io.eroshenkoam.selenoid;

import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.Factory;
import org.apache.commons.io.IOUtils;
import org.junit.rules.ExternalResource;
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

public class WebDriverRule extends ExternalResource {

    private static final Logger LOGGER = Logger.getLogger(WebDriverRule.class.getName());

    private final AllureLifecycle lifecycle = Allure.getLifecycle();

    private final WebDriverConfig config;
    private WebDriver driver;

    public WebDriver getDriver() {
        return driver;
    }

    public WebDriverRule() {
        this.config = ConfigFactory.create(WebDriverConfig.class, System.getProperties());
    }

    protected void before() {
        final DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        capabilities.setCapability("enableVideo", true);

        this.driver = new RemoteWebDriver(config.remoteUrl(), capabilities);
    }

    protected void after() {
        getSelenoidVideoUrl().ifPresent(videoUrl -> {
            this.driver.quit();
            attachVideo(videoUrl);
        });
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
