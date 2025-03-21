package com.example.container2;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ProcessController {

    private static final String MOUNTED_VOLUME_PATH = "/data"; // PV mount path, same as Container 1

    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> calculateProductSum(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        // Extract file content and product from the request
        String fileContent = request.get("file_content");
        String product = request.get("product");

        // Validate input
        if (fileContent == null || product == null || fileContent.trim().isEmpty() || product.trim().isEmpty()) {
            response.put("error", "Invalid input: file content or product missing.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // Parse the CSV content and calculate the sum
            int totalSum = calculateSumFromCSV(fileContent, product);

            // Return success response with the sum
            response.put("sum", totalSum);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // Return error if CSV format is invalid
            response.put("error", "input file not in CSV format.");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            // Handle unexpected errors
            response.put("error", "Error processing file content.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private int calculateSumFromCSV(String fileContent, String targetProduct) {
        // Split the content into lines
        String[] lines = fileContent.split("\n");

        // Check if the first line is a header and validate format
        if (lines.length < 2 || !lines[0].trim().equals("product, amount")) {
            throw new IllegalArgumentException("Invalid CSV header or insufficient data.");
        }

        int sum = 0;
        // Process each line starting from index 1 (skip header)
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue; // Skip empty lines
            }

            // Split line into product and amount
            String[] parts = line.split(",");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid CSV line format.");
            }

            String product = parts[0].trim();
            String amountStr = parts[1].trim();

            // Validate and sum if the product matches
            if (product.equalsIgnoreCase(targetProduct)) {
                try {
                    int amount = Integer.parseInt(amountStr);
                    sum += amount;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Amount is not a valid integer.");
                }
            }
        }

        return sum;
    }
}