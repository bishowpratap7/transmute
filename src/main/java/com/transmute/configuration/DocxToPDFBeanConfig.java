package com.transmute.configuration;

import com.transmute.docx.to.pdf.DocxToPDF;
import com.transmute.docx.to.pdf.impl.DocxToPDFImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocxToPDFBeanConfig {

    @Bean
    DocxToPDF docxToPDF() {
        return new DocxToPDFImpl();
    }
}
