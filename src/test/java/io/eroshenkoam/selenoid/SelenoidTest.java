package io.eroshenkoam.selenoid;

import com.googlecode.junittoolbox.ParallelParameterized;
import org.aeonbits.owner.ConfigFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;

@RunWith(ParallelParameterized.class)
public class SelenoidTest {

    private static final ProjectConfig config = ConfigFactory.create(ProjectConfig.class, System.getProperties());

    @Rule
    public WebDriverRule webDriverRule = new WebDriverRule(config);

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[config.testCount()][0]);
    }

    @Test
    public void selenoidSessionTest() {
        webDriverRule.getDriver().get(config.baseUrl());
        assertThat(webDriverRule.getDriver().getTitle(), startsWith("Авто.ру"));
    }

}
