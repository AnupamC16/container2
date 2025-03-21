package com.example.container2;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

        LOGGER.info("B00990335_Anupam");

        if (filename == null || filename.trim().isEmpty() || product == null || product.trim().isEmpty()) {
            response.put("file", filename);
            response.put("error", "Invalid JSON input.");
            return ResponseEntity.badRequest().body(response);
        }

        File file = new File(STORAGE_DIR, filename);

        // Check if file exists
        if (!file.exists()) {
            response.put("file", filename);
            response.put("error", "File not found.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String header = br.readLine();
            if (header == null || !header.trim().equals("product,amount")) {
                response.put("file", filename);
                response.put("error", "Input file not in CSV format.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            int totalSum = 0;
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 2) {
                    response.put("file", filename);
                    response.put("error", "Input file not in CSV format.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }

                String rowProduct = parts[0].trim();
                String amountStr = parts[1].trim();

                if (rowProduct.equalsIgnoreCase(product)) {
                    try {
                        totalSum += Integer.parseInt(amountStr);
                    } catch (NumberFormatException e) {
                        response.put("file", filename);
                        response.put("error", "Input file not in CSV format.");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                    }
                }
            }

            response.put("file", filename);
            response.put("sum", totalSum);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("file", filename);
            response.put("error", "Error processing file.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
