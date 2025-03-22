package com.example.container2;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@RestController
public class ProcessController {

    private static final Logger LOGGER = Logger.getLogger(ProcessController.class.getName());
    private static final String STORAGE_DIR = "/anupam_PV_dir";

    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> process(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        String filename = request.get("file");
        String product = request.get("product");

        System.out.println("B00990999_anupam"); // ✅ Updated log message

        // ✅ Validate input
        if (filename == null || filename.trim().isEmpty() || product == null || product.trim().isEmpty()) {
            response.put("error", "Invalid JSON input.");
            return ResponseEntity.badRequest().body(response);
        }

        Path filepath = Paths.get(STORAGE_DIR, filename);

        // ✅ Check if file exists
        if (!Files.exists(filepath)) {
            response.put("file", filename);
            response.put("error", "File not found.");
            return ResponseEntity.badRequest().body(response);
        }

        int total = 0;

        try (BufferedReader br = Files.newBufferedReader(filepath)) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                String[] columns = line.split(",");

                if (isFirstLine) {
                    isFirstLine = false;
                    // ✅ Check if header is valid
                    if (columns.length != 2 || !columns[0].trim().equalsIgnoreCase("product")
                            || !columns[1].trim().equalsIgnoreCase("amount")) {
                        response.put("file", filename);
                        response.put("error", "Input file not in CSV format.");
                        return ResponseEntity.badRequest().body(response);
                    }
                    continue; // Skip header
                }

                // ✅ Ensure correct row format
                if (columns.length != 2) {
                    response.put("file", filename);
                    response.put("error", "Input file not in CSV format.");
                    return ResponseEntity.badRequest().body(response);
                }

                // ✅ Process data
                if (columns[0].trim().equalsIgnoreCase(product)) {
                    try {
                        total += Integer.parseInt(columns[1].trim());
                    } catch (NumberFormatException e) {
                        response.put("file", filename);
                        response.put("error", "Input file not in CSV format.");
                        return ResponseEntity.badRequest().body(response);
                    }
                }
            }

            // ✅ Return final sum
            response.put("file", filename);
            response.put("sum", total);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("file", filename);
            response.put("error", "Error reading file.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
