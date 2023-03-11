package com.transmute.docx.to.pdf.api;

import com.itextpdf.text.DocumentException;
import com.transmute.docx.to.pdf.DocxToPDF;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;

@RestController
public class DocxToPDFController {
    private final DocxToPDF docxToPDF;


    public DocxToPDFController(DocxToPDF docxToPDF) {
        this.docxToPDF = docxToPDF;
    }


    @PostMapping("/convertToPDF")
    public byte[] responseEntity(@RequestParam("file") MultipartFile[] files, @RequestParam("saveAsFile") boolean saveAsFile) throws IOException, DocumentException, ParserConfigurationException, URISyntaxException, InterruptedException {

        return docxToPDF.wordToPdfBytes(files, saveAsFile);

    }

}
