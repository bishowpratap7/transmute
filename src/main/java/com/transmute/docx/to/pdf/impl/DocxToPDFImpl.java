package com.transmute.docx.to.pdf.impl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.transmute.docx.to.pdf.DocxToPDF;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.docx4j.Docx4J;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * The {@code DocxToPDFImpl} is the implementation class to convert DOCX to PDF.
 * This takes MultipartFile, byte[] and an option to save the converted PDF as file.
 *
 * @author Bishow Pandey
 * @since 0.0.1-SNAPSHOT
 */
public class DocxToPDFImpl implements DocxToPDF {
    @Value("${windows.default.chrome.driver.path}")
    private String windowsDefaultChromeDriverPath;
    @Value("${linux.default.chrome.driver.path}")
    private String linuxDefaultChromeDriverPath;
    @Value("${temp.html.path}")
    private String tempHtmlPath;
    @Value("${temp.png.path}")
    private String tempPngPath;
    @Value("${docx4j.html.temp.path}")
    private String docx4jHtmlTempPath;
    @Value("${save.converted.pdf.path}")
    private String saveConvertedPdfPath;
    @Value("${chrome.specific.url}")
    private String chromeSpecificUrl;

    @Value("${static.temp.path}")
    private String staticTempPath;

    @Override
    public byte[] wordToPdfBytes(MultipartFile[] files, byte[] fileAsBytes, boolean saveAsFile) throws IOException, DocumentException {
        byte[] fileContent;

        if (fileAsBytes != null && fileAsBytes.length > 0) {
            fileContent = fileAsBytes;
        } else {
            //TODO: Check for no files.
            fileContent = Arrays.stream(files).findFirst().get().getBytes();
        }

        InputStream bufferedInputStream = new BufferedInputStream(new ByteArrayInputStream(fileContent));

        try (ByteArrayOutputStream htmlOut = new ByteArrayOutputStream()) {
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
                    .load(bufferedInputStream);
            HTMLSettings htmlSettings = Docx4J.createHTMLSettings();
            htmlSettings.setImageDirPath(docx4jHtmlTempPath);
            htmlSettings.setWmlPackage(wordMLPackage);

            Docx4J.toHTML(htmlSettings, htmlOut, Docx4J.FLAG_EXPORT_PREFER_XSL);

            Path path = Paths.get(tempHtmlPath);
            Files.write(path, htmlOut.toByteArray());

        } catch (Exception e) {
            //IGNORE
        }

        //Initialize Chromedriver.
        WebDriverManager.chromedriver().setup();
        ChromeDriverService.createServiceWithConfig(chromeOptions());
        ChromeDriver driver = new ChromeDriver(chromeOptions());
        driver.get(chromeSpecificUrl);

        // capture screenshot and store the image
        Screenshot s = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000)).takeScreenshot(driver);
        ImageIO.write(s.getImage(), "PNG", new File(tempPngPath));

        //closing the webdriver
        driver.close();

        Image image = Image.getInstance(tempPngPath);
        image.scaleToFit(PageSize.A0);
        //Get Size of Original Image for conversion
        float origWidth = image.getWidth();
        float origHeight = image.getHeight();
        image.scaleToFit(origWidth, origHeight);
        //Set position of image in top left corner
        image.setAbsolutePosition(0, 0);
        //Create Rectangle in support of new page size
        Rectangle rectangle = new Rectangle(origWidth, origHeight);

        Document document = new Document(PageSize.A4, 10, 10, 10, 10);

        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, pdfOut);
        document.open();
        //Set page size before adding new page
        document.setPageSize(rectangle);
        document.newPage();
        document.add(image);
        document.close();
        if (saveAsFile) {
            DateTimeFormatter timeStampPattern = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            Files.write(Path.of(saveConvertedPdfPath + timeStampPattern.format(LocalDateTime.now()) + ".pdf"), pdfOut.toByteArray());

        }
        FileUtils.cleanDirectory(new File(staticTempPath));
        return pdfOut.toByteArray();

    }

    private ChromeOptions chromeOptions() {

        //set the location of chrome browser
        if (SystemUtils.IS_OS_WINDOWS) {
            System.setProperty("webdriver.chrome.driver", windowsDefaultChromeDriverPath);
        } else if (SystemUtils.IS_OS_LINUX) {
            System.setProperty("webdriver.chrome.driver", linuxDefaultChromeDriverPath);
        } else {
            //TODO : Need to throw dedicated Exception - TransmuteNoOperatingSystemException
            throw new RuntimeException("OPERATING SYSTEM NOT SUPPORTED YET.");
        }

        // Setting your Chrome options. #TODO : Move to application.properties on weekend
        System.setProperty("webdriver.chrome.whitelistedIps", "");
        System.setProperty("--allowed-ips", "");
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--no-sandbox"); // Bypass OS security model
        chromeOptions.addArguments("--headless");
        chromeOptions.addArguments("--start-maximized");
        chromeOptions.addArguments("--start-fullscreen");
        chromeOptions.addArguments("--hide-scrollbars");
        chromeOptions.addArguments("disable-infobars"); // disabling infobars
        chromeOptions.addArguments("--disable-extensions"); // disabling extensions
        chromeOptions.addArguments("--disable-gpu"); // applicable to windows os only*/
        chromeOptions.addArguments("--window-size=1100x1100");
        chromeOptions.addArguments("--disable-dev-shm-usage"); // overcome limited resource problems
        chromeOptions.addArguments("--remote-debugging-port=9222");

        return chromeOptions;
    }
}
