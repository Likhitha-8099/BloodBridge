package com.bloodbridge.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Storage Service interface for profile image uploads.
 * Extension point for Cloudinary, AWS S3, Azure Blob, or Local file storage.
 */
public interface StorageService {

    /**
     * Uploads a profile image and returns the accessible public image URL.
     *
     * @param file uploaded multipart file
     * @param userId ID of the user uploading the image
     * @return public image URL string
     */
    String uploadProfileImage(MultipartFile file, Long userId);

    /**
     * Stores an image URL string (for direct URL inputs or third-party avatars).
     *
     * @param imageUrl direct image URL
     * @param userId user ID
     * @return processed image URL
     */
    String storeImageUrl(String imageUrl, Long userId);
}
