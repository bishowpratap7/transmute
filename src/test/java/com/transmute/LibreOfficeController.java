package com.docker.example.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@RestController
@RequestMapping("/libreoffice")
public class LibreOfficeController {

    private static final Logger logger = LoggerFactory.getLogger(LibreOfficeController.class);

    @DeleteMapping("/stop-long-running-process")
    public String stopLongRunningLibreOfficeProcesses() {
        StringBuilder response = new StringBuilder();

        try {
            // Step 1: Find all LibreOffice process PIDs using `pgrep`
            Process pgrepProcess = Runtime.getRuntime().exec("pgrep -f soffice.bin");
            BufferedReader reader = new BufferedReader(new InputStreamReader(pgrepProcess.getInputStream()));

            String line;
            boolean foundProcess = false;
            response.append("🔍 Found LibreOffice processes: ");

            while ((line = reader.readLine()) != null) {
                foundProcess = true;
                response.append(line).append(" ");
                logger.info("Found LibreOffice process with PID: {}", line.trim());
            }

            reader.close();
            pgrepProcess.waitFor();

            if (!foundProcess) {
                return "✅ No long-running LibreOffice processes found!";
            }

            // Step 2: Stop all LibreOffice processes using `pkill`
            Process pkillProcess = Runtime.getRuntime().exec("pkill -9 -f soffice.bin");
            pkillProcess.waitFor();
            response.append("\n✅ Stopped all long-running LibreOffice processes successfully.");

            return response.toString();

        } catch (IOException | InterruptedException e) {
            logger.error("❌ Error stopping LibreOffice processes", e);
            return "❌ Error stopping LibreOffice processes: " + e.getMessage();
        }
    }

}
