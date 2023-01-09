package com.transmute.docx.to.pdf.impl;

import com.transmute.docx.to.pdf.DocxToPDF;
import org.springframework.web.multipart.MultipartFile;

public class ConvertWordDocxToPDF implements DocxToPDF {
    @Override
    public byte[] wordToPdfBytes(byte[] bytesOfWordDocx) {
        return new byte[0];
    }

    @Override
    public byte[] readDocxFile(MultipartFile multipartDocxFile) {
        return new byte[0];
    }
}
