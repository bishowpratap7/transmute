package com.transmute.docx.to.pdf.api;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.transmute.docx.to.pdf.DocxToPDF;
import org.docx4j.Docx4J;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.openqa.selenium.WebDriver;
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
import io.github.bonigarcia.wdm.WebDriverManager;

@RestController
public class DocxToPDFController {

    private final DocxToPDF docxToPDF;

    public DocxToPDFController(DocxToPDF docxToPDF) {
        this.docxToPDF = docxToPDF;
    }

    @PostMapping("/convertToPDF")
    public ResponseEntity<byte[]> responseEntity(@RequestParam("file") MultipartFile[] files) throws IOException, DocumentException, ParserConfigurationException {
        byte[] fileContent = Arrays.stream(files).findFirst().get().getBytes();

        InputStream is = new BufferedInputStream(new ByteArrayInputStream(fileContent));


        byte[] pdfDocBytes = new byte[0];

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
                    .load(is);
            HTMLSettings htmlSettings = Docx4J.createHTMLSettings();
            htmlSettings.setImageDirPath("C:\\ui_be_projects\\transmute\\converted_documents\\");
            htmlSettings.setWmlPackage(wordMLPackage);

            Docx4J.toHTML(htmlSettings, out, Docx4J.FLAG_EXPORT_PREFER_XSL);

            Path path = Paths.get("C:\\ui_be_projects\\transmute\\test.html");
            Files.write(path, out.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
        }

        WebDriverManager.chromedriver().setup();

        //set the location of chrome browser
        System.setProperty("webdriver.chrome.driver", "C:\\ui_be_projects\\transmute\\src\\main\\resources\\static\\chromedriver.exe");
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

        ChromeDriverService.createServiceWithConfig(options);

        // Initialize browser
        ChromeDriver driver = new ChromeDriver(options);

        //navigate to url
        driver.get("C:\\ui_be_projects\\transmute\\test.html");

        // capture screenshot and store the image
        Screenshot s = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000)).takeScreenshot(driver);
        ImageIO.write(s.getImage(), "PNG", new File("C:\\ui_be_projects\\transmute\\test.png"));

        //closing the webdriver
        driver.close();


        Image image = Image.getInstance("C:\\ui_be_projects\\transmute\\test.png");
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
        PdfWriter.getInstance(document, new FileOutputStream("C:\\ui_be_projects\\transmute\\test.pdf"));
        document.open();
        //Set page size before adding new page
        document.setPageSize(rectangle);
        document.newPage();
        document.add(image);
        document.close();
        return null;
    }

}
