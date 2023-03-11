package com.transmute.configuration;

import com.transmute.docx.to.pdf.DocxToPDF;
import com.transmute.docx.to.pdf.impl.DocxToPDFImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The {@code DocxToPDFBeanConfig} class represents the Configuration class that initializes
 * {@code DocxToPDF} class and it's implementations.
 *
 * @author Bishow Pandey
 * @since 0.0.1-SNAPSHOT
 */
@Configuration
public class DocxToPDFBeanConfig {

    @Bean
    DocxToPDF docxToPDF() {
        return new DocxToPDFImpl();
    }
}
