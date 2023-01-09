package com.transmute.docx.to.pdf;

import org.springframework.web.multipart.MultipartFile;

public interface DocxToPDF {

    byte[] wordToPdfBytes(byte[] bytesOfWordDocx);

    byte[] readDocxFile(MultipartFile multipartDocxFile);

}
