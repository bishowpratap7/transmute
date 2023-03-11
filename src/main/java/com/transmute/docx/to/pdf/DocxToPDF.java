package com.transmute.docx.to.pdf;

import com.itextpdf.text.DocumentException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * The {@code DocxToPDF} is the interface class. W
 * We have {@code wordToPdfBytes }.
 * This takes MultipartFile, byte[] and an option to save the converted PDF as file.
 *
 * @author Bishow Pandey
 * @since 0.0.1-SNAPSHOT
 */
public interface DocxToPDF {

    byte[] wordToPdfBytes(MultipartFile[] files, byte[] fileAsBytes, boolean saveAsFile) throws IOException, DocumentException, URISyntaxException, InterruptedException;

}
