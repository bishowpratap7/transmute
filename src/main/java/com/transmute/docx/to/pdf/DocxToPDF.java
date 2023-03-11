package com.transmute.docx.to.pdf;

import com.itextpdf.text.DocumentException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;

public interface DocxToPDF {

    byte[] wordToPdfBytes(MultipartFile[] files, boolean saveAsFile, boolean saveAsByteArray) throws IOException, DocumentException, URISyntaxException, InterruptedException;

}
