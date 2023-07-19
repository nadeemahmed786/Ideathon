package main.java;

import java.awt.*;
import java.io.*;
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

public class DownloadImagesChrome {

    WebDriver driver;
    String baseDir = System.getProperty("user.dir");
    ArrayList<String> al = new ArrayList<String>();

    Object screenRecordObj = null;

    RenameFiles renameFiles = new RenameFiles();

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
                    if (utils.readPropertiesFile("headless").contains("true")) {
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
                    if (utils.readPropertiesFile("headless").contains("true")) {
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

    @AfterSuite
    public void zipDestination() throws Exception {
        //rename results directories
        renameFiles.listAllDirectories();

        //Iterate over directories
        System.out.println("Dir List:" +RenameFiles.directories.toString());
        int dirCount = RenameFiles.directories.length;

        for(int dir=0; dir<dirCount; dir++) {

            String actualPath = System.getProperty("user.dir") + "\\results\\" + RenameFiles.directories[dir];
            String expectedPath = System.getProperty("user.dir") + "\\expectedresults\\" + RenameFiles.directories[dir];

            File path1 = new File(actualPath);
            File[] files1 = path1.listFiles();

            File path2 = new File(expectedPath);
            File[] files2 = path2.listFiles();

            for (int i = 0; i < files1.length; i++) {

                if (files1[i].isFile()) {
                    double percentage = ImageCheck.imageCheck(files1[i], files2[i]);
                    if (percentage != 0.00) {
                        System.out.println("percentage=" + percentage);
                        System.out.println("Images not equal: " + files1[i]);

                        String[] cmd = {
                                "python",
                                "C:\\MBT\\idea\\Latest\\DownloadImages\\src\\main\\java\\Image.py",
                                String.valueOf(path1)+"\\"+files1[i].getName(),
                                String.valueOf(path2)+"\\"+files2[i].getName(),
                                String.valueOf(path1),
                                files1[i].getName()
                        };

                        String s = null;
                        Process p = Runtime.getRuntime().exec(cmd);
                        BufferedReader stdInput = new BufferedReader(new
                                InputStreamReader(p.getInputStream()));

                        BufferedReader stdError = new BufferedReader(new
                                InputStreamReader(p.getErrorStream()));

                        // read the output from the command
                        System.out.println("Here is the standard output of the command:\n");
                        while ((s = stdInput.readLine()) != null) {
                            System.out.println(s);
                        }

                        // read any errors from the attempted command
                        System.out.println("Here is the standard error of the command (if any):\n");
                        while ((s = stdError.readLine()) != null) {
                            System.out.println(s);
                        }
                    }else{
                        System.out.println("Images are equal.....");
                    }
                }

            }
        }

        //zip directories
        utils.zipResultsDirectories();
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

    public void captureScreenshotsToEndOfPage(String browserFolder) throws IOException, InterruptedException {
        // Capture Screenshots till end of page
        JavascriptExecutor jsExec = (JavascriptExecutor) driver;
        jsExec.executeScript("window.scrollTo(0, 0);"); //Scroll To Top
        Long innerHeight = (Long) jsExec.executeScript("return window.innerHeight;");
        Long scroll = innerHeight;
        Long scrollHeight = (Long) jsExec.executeScript("return document.body.scrollHeight;");
        scrollHeight = scrollHeight + scroll;

        String resultsDir = baseDir + "\\results\\" +browserFolder +"\\";
        jsExec.executeScript("return document.readyState").toString().equals("complete");
        Thread.sleep(3000);

        do {
            Thread.sleep(2000);
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