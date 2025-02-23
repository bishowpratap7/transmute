package com.docker.example;

import org.jodconverter.DocumentConverter;
import org.jodconverter.LocalConverter;
import org.jodconverter.office.LocalOfficeManager;
import org.jodconverter.office.OfficeManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Configuration
public class JodConverterConfig {
    @Bean
    public OfficeManager officeManager() throws IOException {

        // Run the command to find LibreOffice path
        Process process = Runtime.getRuntime().exec("which libreoffice"); // Linux & Mac
        // Process process = Runtime.getRuntime().exec("where libreoffice"); // Windows

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        if ((line = reader.readLine()) != null) {
            System.out.println("✅ LibreOffice is installed at: " + line);
        } else {
            System.out.println("❌ LibreOffice is not found!");
        }

        OfficeManager officeManager = LocalOfficeManager.builder()
                .install()
                .officeHome("/usr/lib/libreoffice")
                .build();

        return officeManager;
    }

    @Bean
    public DocumentConverter documentConverter(OfficeManager officeManager) {
        return LocalConverter.make(officeManager);
    }
}
