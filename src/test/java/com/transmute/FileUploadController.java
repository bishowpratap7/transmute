package com.docker.example.controller;

import org.jodconverter.DocumentConverter;
import org.jodconverter.JodConverter;
import org.jodconverter.office.LocalOfficeManager;
import org.jodconverter.office.OfficeManager;
import org.jodconverter.office.OfficeUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/convert")
public class FileUploadController {

    private static final String TEMP_DIR = "/tmp/libreoffice/";


    private final DocumentConverter documentConverter;

    public FileUploadController(DocumentConverter documentConverter) {
        this.documentConverter = documentConverter;
    }

    @PostMapping("/to-pdf")
    public ResponseEntity<byte[]> convertToPdf(@RequestParam("file") MultipartFile file) {
        try {
            // Ensure the temp directory exists
            Files.createDirectories(Paths.get(TEMP_DIR));

            // Generate unique file names
            String inputFilePath = TEMP_DIR + UUID.randomUUID() + "-" + file.getOriginalFilename();
            String outputFilePath = inputFilePath.replaceAll("\\.\\w+$", ".pdf");

            // Save the uploaded file
            file.transferTo(new File(inputFilePath));

            // Invoke LibreOffice for conversion
            Process process = new ProcessBuilder(
                    "libreoffice", "--headless", "--convert-to", "pdf", inputFilePath, "--outdir", TEMP_DIR
            ).start();

            // Wait for conversion to complete
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return ResponseEntity.badRequest().body(("Conversion failed. Exit code: " + exitCode).getBytes());
            }

            // Read the converted PDF file
            byte[] pdfBytes = Files.readAllBytes(Paths.get(outputFilePath));

            // Delete temporary files after use
            Files.deleteIfExists(Paths.get(inputFilePath));
            Files.deleteIfExists(Paths.get(outputFilePath));

            // Return the PDF as a response for direct download
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + new File(outputFilePath).getName())
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(("Error: " + e.getMessage()).getBytes());
        }


    }


    @PostMapping("/to-pdf-jod")
    public ResponseEntity<byte[]> convertToPdfJod(@RequestParam("file") MultipartFile file) {
        try {
            // Create temp files for input and output
            File inputFile = File.createTempFile("input-", "-" + file.getOriginalFilename());
            File outputFile = new File(inputFile.getParent(), inputFile.getName().replaceAll("\\.\\w+$", ".pdf"));

            // Save uploaded file to temp location
            file.transferTo(inputFile);

            // Convert to PDF using JodConverter
            OfficeManager officeManager = LocalOfficeManager.builder()
                    .install()
                    .officeHome("/usr/lib/libreoffice")
                    .build();
            try {
                // Start an office process and connect to the started instance (on port 2002).
                officeManager.start();
                // Convert
                JodConverter
                        .convert(inputFile)
                        .to(outputFile)
                        .execute();
            } finally {
                // Stop the office process
                OfficeUtils.stopQuietly(officeManager);
            }

            // Read converted PDF
            byte[] pdfBytes = new FileInputStream(outputFile).readAllBytes();

            // Clean up temp files
            inputFile.delete();
            outputFile.delete();

            // Return the PDF as a response
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + outputFile.getName())
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(("Error: " + e.getMessage()).getBytes());
        }
    }
}
