package com.finflow.ai.common.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String store(MultipartFile file, String subFolder);
    void delete(String fileUrl);
}
