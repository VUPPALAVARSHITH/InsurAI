package com.insurai.backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    // Single source of truth for upload directory
    private static final String UPLOAD_DIR =
            "C:/Users/varsh/OneDrive/Documents/InsurAI_Uploads/";

    /**
     * Store multiple files safely
     */
    public List<String> storeFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }

        return files.stream()
                .map(this::storeSingleFile)
                .collect(Collectors.toList());
    }

    /**
     * Store a single file safely
     */
    private String storeSingleFile(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            Path uploadPath = Paths.get(UPLOAD_DIR);
            Files.createDirectories(uploadPath);

            Path filePath = uploadPath.resolve(fileName);

            Files.copy(
                    file.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            // Path returned to frontend
            return "/uploads/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to store file: " + file.getOriginalFilename(), e
            );
        }
    }
}
