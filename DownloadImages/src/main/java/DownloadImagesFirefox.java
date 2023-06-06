import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.io.FileHandler;

import org.testng.annotations.*;

public class DownloadImagesFirefox {
    WebDriver driver;
    String baseDir = System.getProperty("user.dir");
    ArrayList<String> al = new ArrayList<String>();
    Object screenRecordObj = null;

    @BeforeSuite
    public void setup() throws IOException {
        // Delete all Files from results directory
        cleanupResultsDirectory();

        // Iterate over data from CSV
        getDimensionsFromCsvData();
    }

    @Test
    @Parameters("browser")
    public void captureScreenshots(String browser) throws IOException, InterruptedException, AWTException {
        for (String i : al) {
                // Create result directories
                if (browser.equalsIgnoreCase("Chrome"))
                    createResultsDirectory("chrome" + i);
                else if (browser.equalsIgnoreCase("Firefox"))
                    createResultsDirectory("firefox" + i);
                else
                    System.out.println("Running tests with invalid browser: " + browser);

                //Get width and height
                String width = i.substring(i.indexOf("W") + 1, i.indexOf("H"));
                String height = i.substring(i.indexOf("H") + 1);

                // Launch Browser with dimensions
                if (browser.equalsIgnoreCase("chrome")) {
                    //Added for version 111 and above
                    ChromeOptions options = new ChromeOptions();

                    options.addArguments("--remote-allow-origins=*");
                    options.addArguments("--disable-gpu");
                    options.addArguments("--disable-extensions");
                    options.addArguments("--no-sandbox");
                    options.addArguments("--disable-dev-shm-usage");
                    if (utils.readProperties_File(Boolean.parseBoolean("headless"))) {
                        options.addArguments("--headless=new");
                    }
                    options.addArguments("--window-size=" + Integer.parseInt(width) + "," + Integer.parseInt(height));
                    driver = new ChromeDriver(options);

                    driver.get(utils.readPropertiesFile("app.url"));
                    //utils.StartScreenRecorder("./results");
                    Thread.sleep(5);
                } else if (browser.equalsIgnoreCase("firefox")) {
                    System.setProperty("webdriver.firefox.marionette", baseDir + "\\drivers\\geckodriver.exe");

                    FirefoxBinary firefoxBinary = new FirefoxBinary();
                    FirefoxOptions options = new FirefoxOptions();
                    options.setBinary(firefoxBinary);
                    options.addArguments("--disable-notifications");
                    if (utils.readProperties_File(Boolean.parseBoolean("headless"))) {
                        options.addArguments("-headless");
                    }

                    driver = new FirefoxDriver(options);
                    Dimension dim = new Dimension(Integer.parseInt(width), Integer.parseInt(height));
                    driver.manage().window().setSize(dim);
                    driver.get(utils.readPropertiesFile("app.url"));
                    //utils.StartScreenRecorder("./results");
                    Thread.sleep(5);
                } else
                    System.out.println("Logic to be implemented for new browser");

                //Capture Screenshots under specific folder structure
                captureScreenshotsToEndOfPage(browser + i);

                tearDown();
        }
    }

    public void tearDown() throws IOException{
        //utils.StopScreenRecorder();
        driver.quit();
    }

    public void getDimensionsFromCsvData() {
        String csv = baseDir + "\\Dimensions.csv";
        BufferedReader br = null;
        String line = "";
        String csvSplit = ",";
        String[] cellValue = new String[0];

        al = new ArrayList<String>();

        try {
            br = new BufferedReader(new FileReader(csv));
            while ((line = br.readLine()) != null) {
                cellValue = line.split(csvSplit);
                al.add("W" + cellValue[0] + "H" + cellValue[1]);
            }
            al.remove(0);
        }catch(IOException io) {
            System.out.println(io.getMessage());
        }

    }

    public void cleanupResultsDirectory() throws IOException {
        String resultsDir = baseDir + "\\results\\";
        File fd = new File(resultsDir);
        FileUtils.cleanDirectory(fd);
    }

    public void createResultsDirectory(String folderName) throws IOException {
        File directory = new File(baseDir + "\\results\\" + folderName);
        if(directory.exists())
            System.out.println("Results Directory already exists: "+folderName);
        else {
            directory.mkdir();
            System.out.println("Results Directory Created: "+folderName);
        }
    }

    public void captureScreenshotsToEndOfPage(String browserFolder) throws IOException {
        // Capture Screenshots till end of page
        JavascriptExecutor jsExec = (JavascriptExecutor) driver;
        jsExec.executeScript("window.scrollTo(0, 0);"); //Scroll To Top
        Long innerHeight = (Long) jsExec.executeScript("return window.innerHeight;");
        Long scroll = innerHeight;
        Long scrollHeight = (Long) jsExec.executeScript("return document.body.scrollHeight;");
        scrollHeight = scrollHeight + scroll;

        String resultsDir = baseDir + "\\results\\" +browserFolder +"\\";

        do {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

            //Unique file name for each screenshot
            File destination = new File(resultsDir + String.join("_", LocalDateTime.now().toString().split("[^A-Za-z0-9]")) + ".jpg");

            FileHandler.copy(screenshot, destination);
            jsExec.executeScript("window.scrollTo(0, " + innerHeight + ");");
            innerHeight = innerHeight + scroll;
        } while (scrollHeight >= innerHeight);

    }

    public void getLineCountFromCsv() throws IOException {
        String inputDir = baseDir + "\\Dimensions.csv";
        BufferedReader bufferedReader = new BufferedReader(new FileReader(inputDir));
        String input;
        int count = 0;
        while((input = bufferedReader.readLine()) != null)
        {
            count++;
        }
    }

}