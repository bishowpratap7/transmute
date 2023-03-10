package com.transmute.docx.to.pdf.api;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.transmute.docx.to.pdf.DocxToPDF;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.docx4j.Docx4J;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@RestController
public class DocxToPDFController {

    private static final String CHROME_DRIVER_PATH = "src/main/resources/static/chromedriver_windows/chromedriver.exe";
    private static final ChromeOptions CHROME_OPTIONS = chromeOptions();
    private static final String TEMP_HTML_PATH = "C:\\ui_be_projects\\transmute\\temp\\test.html";
    private static final String TEMP_PNG_PATH = "C:\\ui_be_projects\\transmute\\temp\\test.png";
    private static final String PDF_FILE_PATH = "C:\\ui_be_projects\\transmute\\converted_docx_to_pdf\\test.pdf";

    private static final String HTML_SETTINGS_IMG = "C:\\ui_be_projects\\transmute\\temp\\";
    private final DocxToPDF docxToPDF;

    public DocxToPDFController(DocxToPDF docxToPDF) {
        this.docxToPDF = docxToPDF;
    }

    private static ChromeOptions chromeOptions() {

        //set the location of chrome browser
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_PATH);
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

    @PostMapping("/convertToPDF")
    public ResponseEntity<byte[]> responseEntity(@RequestParam("file") MultipartFile[] files, boolean saveAsFile, boolean saveAsByteArray) throws IOException, DocumentException, ParserConfigurationException {
        byte[] fileContent = Arrays.stream(files).findFirst().get().getBytes();

        InputStream is = new BufferedInputStream(new ByteArrayInputStream(fileContent));


        byte[] pdfDocBytes = new byte[0];

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
                    .load(is);
            HTMLSettings htmlSettings = Docx4J.createHTMLSettings();
            htmlSettings.setImageDirPath(HTML_SETTINGS_IMG);
            htmlSettings.setWmlPackage(wordMLPackage);

            Docx4J.toHTML(htmlSettings, out, Docx4J.FLAG_EXPORT_PREFER_XSL);

            Path path = Paths.get(TEMP_HTML_PATH);
            Files.write(path, out.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
        }

        WebDriverManager.chromedriver().setup();


        ChromeDriverService.createServiceWithConfig(CHROME_OPTIONS);

        // Initialize browser
        ChromeDriver driver = new ChromeDriver(CHROME_OPTIONS);

        //navigate to url
        driver.get(TEMP_HTML_PATH);

        // capture screenshot and store the image
        Screenshot s = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000)).takeScreenshot(driver);
        ImageIO.write(s.getImage(), "PNG", new File(TEMP_PNG_PATH));

        //closing the webdriver
        driver.close();


        Image image = Image.getInstance(TEMP_PNG_PATH);
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
        PdfWriter.getInstance(document, new FileOutputStream(PDF_FILE_PATH));
        document.open();
        //Set page size before adding new page
        document.setPageSize(rectangle);
        document.newPage();
        document.add(image);
        document.close();
        return null;
    }

}
