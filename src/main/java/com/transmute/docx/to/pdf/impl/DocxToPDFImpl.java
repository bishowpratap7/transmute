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
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

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
    public byte[] wordToPdfBytes(MultipartFile[] files, boolean saveAsFile) throws IOException, DocumentException, URISyntaxException, InterruptedException {
        byte[] fileContent = Arrays.stream(files).findFirst().get().getBytes();

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
            e.printStackTrace();
        }

        WebDriverManager.chromedriver().setup();


        ChromeDriverService.createServiceWithConfig(chromeOptions());

        // Initialize browser
        ChromeDriver driver = new ChromeDriver(chromeOptions());

        //navigate to url
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
        FileUtils.cleanDirectory(new File(staticTempPath));
        if (saveAsFile) {
            DateTimeFormatter timeStampPattern = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            Files.write(Path.of(saveConvertedPdfPath + timeStampPattern.format(LocalDateTime.now()) + ".pdf"), pdfOut.toByteArray());

        }
        return pdfOut.toByteArray();

    }

    private ChromeOptions chromeOptions() {

        //set the location of chrome browser
        if (SystemUtils.IS_OS_WINDOWS) {
            System.setProperty("webdriver.chrome.driver", windowsDefaultChromeDriverPath);
        } else if (SystemUtils.IS_OS_LINUX) {
            System.setProperty("webdriver.chrome.driver", linuxDefaultChromeDriverPath);
        } else {
            throw new RuntimeException("OPERATING SYSTEM NOT SUPPORTED YET.");
        }
        System.setProperty("webdriver.chrome.whitelistedIps", "");
        System.setProperty("--allowed-ips", "");

        // Setting your Chrome options (Desired capabilities)
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox"); // Bypass OS security model
        options.addArguments("--headless");
        options.addArguments("--start-maximized");
        options.addArguments("--start-fullscreen");
        options.addArguments("--hide-scrollbars");
        options.addArguments("disable-infobars"); // disabling infobars
        options.addArguments("--disable-extensions"); // disabling extensions
        options.addArguments("--disable-gpu"); // applicable to windows os only*/
        options.addArguments("--window-size=1100x1100");

        options.addArguments("--disable-dev-shm-usage"); // overcome limited resource problems
        options.addArguments("--remote-debugging-port=9222");

        return options;
    }
}