package com.finflow.ai.common.storage;

import com.finflow.ai.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class LocalStorageServiceImpl implements StorageService {

    @Value("${finflow.storage.local-dir}")
    private String localDir;

    @Override
    public String store(MultipartFile file, String subFolder) {
        try {
            if (file.isEmpty()) {
                throw new BusinessException("Failed to store empty file.");
            }

            Path uploadPath = Paths.get(localDir, subFolder);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String newFilename = UUID.randomUUID().toString() + extension;
            Path destinationFile = uploadPath.resolve(Paths.get(newFilename)).normalize().toAbsolutePath();

            // Security check to ensure the file isn't written outside the base directory
            if (!destinationFile.getParent().equals(uploadPath.toAbsolutePath())) {
                throw new BusinessException("Cannot store file outside current directory.");
            }

            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

            log.info("Stored file locally at: {}", destinationFile);
            return "/api/uploads/" + subFolder + "/" + newFilename;

        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new BusinessException("Failed to store file: " + e.getMessage());
        }
    }

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith("/api/uploads/")) {
            return;
        }

        try {
            String relativePath = fileUrl.substring("/api/uploads/".length());
            Path filePath = Paths.get(localDir).resolve(relativePath).normalize().toAbsolutePath();

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Deleted file locally at: {}", filePath);
            }
        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileUrl, e);
        }
    }
}
