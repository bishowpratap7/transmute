package com.transmute.docx.to.pdf.api;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.transmute.docx.to.pdf.DocxToPDF;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
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
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@RestController
public class DocxToPDFController {

    private final DocxToPDF docxToPDF;

    public DocxToPDFController(DocxToPDF docxToPDF) {
        this.docxToPDF = docxToPDF;
    }

    @PostMapping("/convertToPDF")
    public ResponseEntity<byte[]> responseEntity(@RequestParam("file") MultipartFile[] files) throws IOException, DocumentException {
        byte[] fileContent = Arrays.stream(files).findFirst().get().getBytes();

        InputStream is = new BufferedInputStream(new ByteArrayInputStream(fileContent));

        byte[] pdfDocBytes = new byte[0];

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
                    .load(is);
            Docx4J.toHTML(wordMLPackage, "src/main/resources/assets", "src/main/resources/assets", out);

            Path path = Paths.get("src/main/resources/static/test.html");
            Files.write(path, out.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
        }


        //set the location of chrome browser
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\bisho\\Downloads\\chromedriver_win32\\chromedriver.exe");
// Setting your Chrome options (Desired capabilities)
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--start-fullscreen");
        options.addArguments("--headless");
        options.addArguments("--hide-scrollbars");

        // Initialize browser
        WebDriver driver = new ChromeDriver(options);

        //navigate to url
        driver.get("C:\\ui_be_projects\\transmute\\src\\main\\resources\\static\\test.html");

        // capture screenshot and store the image
        Screenshot s = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000)).takeScreenshot(driver);
        ImageIO.write(s.getImage(), "PNG", new File("src/main/resources/static/test.png"));

        //closing the webdriver
        driver.close();


        Image image = Image.getInstance("src/main/resources/static/test.png");
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
        PdfWriter.getInstance(document, new FileOutputStream("src/main/resources/static/test.pdf"));
        document.open();
        //Set page size before adding new page
        document.setPageSize(rectangle);
        document.newPage();
        document.add(image);
        document.close();
        return null;
    }

}
