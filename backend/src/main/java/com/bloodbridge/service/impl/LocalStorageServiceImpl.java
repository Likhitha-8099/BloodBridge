package com.bloodbridge.service.impl;

import com.bloodbridge.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Local & Mock implementation of StorageService.
 * Can be replaced seamlessly by CloudinaryStorageService or S3StorageService.
 */
@Slf4j
@Service
public class LocalStorageServiceImpl implements StorageService {

    @Override
    public String uploadProfileImage(MultipartFile file, Long userId) {
        if (file == null || file.isEmpty()) {
            log.warn("Empty file uploaded for userId: {}", userId);
            throw new IllegalArgumentException("Uploaded file cannot be empty");
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String generatedFileName = "user_" + userId + "_" + UUID.randomUUID().toString().substring(0, 8) + fileExtension;
        String simulatedUrl = "https://images.bloodbridge.com/profiles/" + generatedFileName;

        log.info("Simulated profile image upload for userId: {}. Public URL: {}", userId, simulatedUrl);
        return simulatedUrl;
    }

    @Override
    public String storeImageUrl(String imageUrl, Long userId) {
        log.info("Setting direct profile image URL for userId: {}. URL: {}", userId, imageUrl);
        return imageUrl;
    }
}
