package main.java;

import org.monte.media.Format;
import org.monte.media.math.Rational;

import java.awt.*;
import java.io.*;
import java.util.Properties;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

//import org.monte.media.math.Rational;
//import org.monte.media.Format;
import org.monte.screenrecorder.ScreenRecorder;

import static org.monte.media.AudioFormatKeys.*;
import static org.monte.media.VideoFormatKeys.*;


public class utils {
    static String baseDir = System.getProperty("user.dir");
    private static final int BUFFER_SIZE = 4096;
    private static ScreenRecorder screenRecorder;

    public static String readPropertiesFile(String name) {
        String value = "";
        try (FileReader reader = new FileReader(baseDir + "\\config.properties")) {
            Properties p = new Properties();
            p.load(reader);
            value = p.getProperty(name);
        } catch (Exception e) {
            System.out.println("Error in reading properties file: " + e.getMessage());
        }
        return value;
    }

    /**
     * Compresses a list of files to a destination zip file
     * @param listFiles A collection of files and directories
     * @param destZipFile The path of the destination zip file
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void zip(List<File> listFiles, String destZipFile) throws FileNotFoundException,
            IOException {
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destZipFile));
        for (File file : listFiles) {
            if (file.isDirectory()) {
                zipDirectory(file, file.getName(), zos);
            } else {
                zipFile(file, zos);
            }
        }
        zos.flush();
        zos.close();
    }

    /**
     * Compresses files represented in an array of paths
     * @param files a String array containing file paths
     * @param destZipFile The path of the destination zip file
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void zip(String[] files, String destZipFile) throws FileNotFoundException, IOException {
        List<File> listFiles = new ArrayList<File>();
        for (int i = 0; i < files.length; i++) {
            listFiles.add(new File(files[i]));
        }
        zip(listFiles, destZipFile);
    }

    /**
     * Adds a directory to the current zip output stream
     * @param folder the directory to be  added
     * @param parentFolder the path of parent directory
     * @param zos the current zip output stream
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static void zipDirectory(File folder, String parentFolder,
                                     ZipOutputStream zos) throws FileNotFoundException, IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                zipDirectory(file, parentFolder + "/" + file.getName(), zos);
                continue;
            }
            zos.putNextEntry(new ZipEntry(parentFolder + "/" + file.getName()));
            BufferedInputStream bis = new BufferedInputStream(
                    new FileInputStream(file));
            long bytesRead = 0;
            byte[] bytesIn = new byte[BUFFER_SIZE];
            int read = 0;
            while ((read = bis.read(bytesIn)) != -1) {
                zos.write(bytesIn, 0, read);
                bytesRead += read;
            }
            zos.closeEntry();
        }
    }

    /**
     * Adds a file to the current zip output stream
     * @param file the file to be added
     * @param zos the current zip output stream
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static void zipFile(File file, ZipOutputStream zos)
            throws FileNotFoundException, IOException {
        zos.putNextEntry(new ZipEntry(file.getName()));
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(
                file));
        long bytesRead = 0;
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = bis.read(bytesIn)) != -1) {
            zos.write(bytesIn, 0, read);
            bytesRead += read;
        }
        zos.closeEntry();
    }

    public static void zipResultsDirectories() throws IOException {
        List<String> al = new ArrayList<String>();
        File file = new File(baseDir + "\\results");
        String[] names = file.list();

        for(String name : names)
        {
            if (new File(baseDir + "\\results\\" + name).isDirectory())
            {
                System.out.println("Directory adding to zip:" +name);
                al.add(baseDir + "\\results\\"+name);
            }
        }

        String[] myFiles = al.stream().toArray(String[]::new);
        String zipFile = baseDir + "\\results\\Output.zip";
        zip(myFiles, zipFile);
    }

    public static void StartScreenRecorder(String Path) throws IOException, AWTException
    {
        try {
            // This is needed for ScreenRecorder class.
            GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

            // Create an instance of ScreenRecorder with the required configurations
            screenRecorder = new ScreenRecorder(gc, null, new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_QUICKTIME),
                    new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, ENCODING_QUICKTIME_JPEG, CompressorNameKey, ENCODING_QUICKTIME_JPEG, DepthKey, (int) 24, FrameRateKey, Rational.valueOf(15), QualityKey, 0.2f, KeyFrameIntervalKey, (int) (15 * 60)),
                    new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, "black", FrameRateKey, Rational.valueOf(30)), null, new File(Path));

            screenRecorder.start();
        }

        catch (Exception e) {
            System.out.println("Exception handled for Method - Screenrecorder..." + e);
        }
    }

    public static void StopScreenRecorder() throws IOException
    {
        screenRecorder.stop();
    }
}