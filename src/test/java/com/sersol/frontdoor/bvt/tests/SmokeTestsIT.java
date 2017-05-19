package com.sersol.frontdoor.bvt.tests;

import net.sourceforge.htmlunit.corejs.javascript.tools.debugger.Main;
import org.junit.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileReader;
import java.net.URLDecoder;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class SmokeTestsIT {
    WebDriver driver;
    private static String username = "proquestexlibirsqa@gmail.com";
    private static String password = "proquest2016";
    final static private String frontDoorAdminConsole = "https://clientcenter.serialssolutions.com/CC/Login/Default.aspx";

    @Before
    public void initializeDriver() {
        long MAX_PAGE_LOAD_TIMEOUT_IN_MINUTES = 1;
        long MAX_WAIT_TIMEOUT_IN_SECONDS = 40;

        if (driver == null) driver = new FirefoxDriver();
        driver.manage().timeouts().pageLoadTimeout(MAX_PAGE_LOAD_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
        driver.manage().timeouts().implicitlyWait(MAX_WAIT_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
    }

    @After
    public void cleanup() {
        driver.quit();
    }

    @Test
    public void testFrontDoors() {
        String file = "";
        int errorCount = 0;
        file = Main.class.getResource("/frontDoors.txt").getFile();

        try {
            file = URLDecoder.decode(file, "UTF-8");
            Scanner scan = new Scanner(new FileReader(file));

            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                String[] frontDoor = line.split("\t");
                String application = frontDoor[0];
                String url = frontDoor[1];
                String expectedPageTitle = frontDoor[2];
                String pageTitle = "";

                System.out.println(String.format(String.format("Verifying %s url: %s", application, url)));
                driver.get(url);
                try {
                    pageTitle = driver.getTitle();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (!pageTitle.contentEquals(expectedPageTitle)) {
                    System.out.println(String.format("Failed: Expected Page Title %s. Actual; %s",
                            expectedPageTitle, pageTitle));
                    errorCount++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue(String.format("Failed: Error count: %d", errorCount), errorCount == 0);
    }

    @Test
    public void testUlrichsXmlApi1() {
        String url = "http://xml.serialssolutions.com/docs/Ulrichsweb/v1.0/index.html";
        String expectedPageTitle = "Ulrichsweb API";
        System.out.println(String.format(String.format("Verifying UrlichsXmlApi1 url: %s", url)));
        logIntoUlrichsXmlApi1(url);
        String pageTitle = driver.getTitle();
        assertThat(pageTitle, is(expectedPageTitle));
    }

    private void logIntoUlrichsXmlApi1(String url){
        String userName = "sersolxml";
        String password = "apidocs";

        driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS);
        try {
            driver.get(url);
        }catch(TimeoutException e){
            e.printStackTrace();
        }
        System.out.println("Logging in");
        Alert alert = driver.switchTo().alert();
        alert.sendKeys(userName);
        driver.switchTo().alert();
        alert.sendKeys("\t");
        alert.sendKeys(password);
        alert.accept();
    }

    @Test
    public void testUlrichsXmlApi2() {
        String url = "http://ulrichsweb.serialssolutions.com/api/AO0SJWQGUN/search?query";

        System.out.println(String.format(String.format("Verifying UrlichsXmlApi2 url: %s", url)));
        driver.get(url);
        assertThat(ulrichsXmlApi2FrontDoorDisplays(), is(true));
    }

    public Boolean ulrichsXmlApi2FrontDoorDisplays() {
        WebElement status = driver.findElement(By.xpath("searchResults/status[.='Success']"));
        return status != null;
    }

    @Test
    public void testSolr() {
        String url = "http://solr002.sea.sersol.lib:8080/solr/ulrichs/dataimport?command";

        System.out.println(String.format(String.format("Verifying Solr url: %s", url)));
        driver.get(url);
        assertThat(solrFrontDoorDisplays(), is(true));
    }

    public Boolean solrFrontDoorDisplays() {
        WebElement responseHeader = driver.findElement(By.cssSelector("[name=responseHeader]"));
        return responseHeader != null;
    }

    @Test
    public void testSoa() {
        String url = "";

        System.out.println(String.format(String.format("Verifying Soa url: %s", url)));
        Assert.fail("TBD");
        //need url
    }

    @Test
    public void frontDoorAdminConsole(){
        driver.get(frontDoorAdminConsole);
        driver.findElement(By.id("_login__login_UserName")).sendKeys(username);
        driver.findElement(By.id("_login__login_Password")).sendKeys(password);
        driver.findElement(By.id("_login__login_LoginButton")).click();
        driver.findElement(By.className("LinkButtonItalic")).click();
        WebDriverWait waiter = new WebDriverWait(driver, 10);
        WebElement link = waiter.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//span[text()='360 Core']/ancestor::node()[1]/following-sibling::table/tbody/tr/td/a[text()='Administration Console']")));
        link.click();
        String text = driver.findElement(By.id("HeaderTitle")).getText();
        System.out.println(String.format(String.format("Verifying Admin Console url: %s", frontDoorAdminConsole)));
        Assert.assertTrue(text.contains("Configure E-Journal Portal"));
    }
}


