package com.media.media.service.impl;

import com.media.media.model.Media;
import com.media.media.service.MediaService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MediaServiceImpl implements MediaService {
    private static final String UPLOADS_DIRECTORY = "uploads";

    private final List<Media> mediaRepository = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    private final Path uploadsPath = Path.of(UPLOADS_DIRECTORY).toAbsolutePath().normalize();

    @PostConstruct
    void initializeUploadsDirectory() {
        try {
            Files.createDirectories(uploadsPath);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create uploads directory", e);
        }
    }

    @Override
    public List<Media> getAllMedia() {
        return new ArrayList<>(mediaRepository);
    }
    
    @Override
    public Optional<Media> getMediaById(Long id) {
        return mediaRepository.stream()
                .filter(media -> media.getId().equals(id))
                .findFirst();
    }
    
    @Override
    public Media createMedia(Media media) {
        validateExternalUrlMedia(media);
        media.setId(idCounter.getAndIncrement());
        mediaRepository.add(media);
        return media;
    }

    @Override
    public Media createMedia(Media media, MultipartFile file) {
        validateUploadRequest(media, file);
        media.setId(idCounter.getAndIncrement());
        media.setUrl(storeFile(file));
        mediaRepository.add(media);
        return media;
    }

    @Override
    public Media updateMedia(Long id, Media media) {
        return mediaRepository.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .map(existingMedia -> {
                    validateExternalUrlMedia(media);
                    deleteUploadedFileIfPresent(existingMedia.getUrl());
                    existingMedia.setTitle(media.getTitle());
                    existingMedia.setDescription(media.getDescription());
                    existingMedia.setMediaType(media.getMediaType());
                    existingMedia.setUrl(media.getUrl());
                    return existingMedia;
                })
                .orElseThrow(() -> new RuntimeException("Media not found with id: " + id));
    }

    @Override
    public Media updateMedia(Long id, Media media, MultipartFile file) {
        return mediaRepository.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .map(existingMedia -> {
                    validateUploadRequest(media, file);
                    deleteUploadedFileIfPresent(existingMedia.getUrl());
                    existingMedia.setTitle(media.getTitle());
                    existingMedia.setDescription(media.getDescription());
                    existingMedia.setMediaType(media.getMediaType());
                    existingMedia.setUrl(storeFile(file));
                    return existingMedia;
                })
                .orElseThrow(() -> new RuntimeException("Media not found with id: " + id));
    }

    @Override
    public void deleteMedia(Long id) {
        mediaRepository.removeIf(media -> {
            if (media.getId().equals(id)) {
                deleteUploadedFileIfPresent(media.getUrl());
                return true;
            }
            return false;
        });
    }

    private void validateExternalUrlMedia(Media media) {
        if (!StringUtils.hasText(media.getTitle())) {
            throw new IllegalArgumentException("Title is required");
        }
        if (!StringUtils.hasText(media.getUrl())) {
            throw new IllegalArgumentException("URL is required");
        }
    }

    private void validateUploadRequest(Media media, MultipartFile file) {
        if (!StringUtils.hasText(media.getTitle())) {
            throw new IllegalArgumentException("Title is required");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("A file is required");
        }
        if (!isUploadSupported(media.getMediaType())) {
            throw new IllegalArgumentException("Only image and audio files can be uploaded");
        }

        String contentType = file.getContentType();
        if ("image".equalsIgnoreCase(media.getMediaType())
                && (contentType == null || !contentType.startsWith("image/"))) {
            throw new IllegalArgumentException("Selected file must be an image");
        }
        if ("audio".equalsIgnoreCase(media.getMediaType())
                && (contentType == null || !contentType.startsWith("audio/"))) {
            throw new IllegalArgumentException("Selected file must be audio");
        }
    }

    private boolean isUploadSupported(String mediaType) {
        return "image".equalsIgnoreCase(mediaType) || "audio".equalsIgnoreCase(mediaType);
    }

    private String storeFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }

        String storedFilename = UUID.randomUUID() + extension;
        Path destination = uploadsPath.resolve(storedFilename);

        try {
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store uploaded file", e);
        }

        return "http://localhost:8080/uploads/" + storedFilename;
    }

    private void deleteUploadedFileIfPresent(String url) {
        String prefix = "http://localhost:8080/uploads/";
        if (url == null || !url.startsWith(prefix)) {
            return;
        }

        Path filePath = uploadsPath.resolve(url.substring(prefix.length())).normalize();
        if (!filePath.startsWith(uploadsPath)) {
            return;
        }

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
            // Best-effort cleanup for replaced/deleted uploads.
        }
    }
}
