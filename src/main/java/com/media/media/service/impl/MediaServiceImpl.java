package com.media.media.service.impl;

import com.media.media.model.Media;
import com.media.media.service.MediaService;
import jakarta.annotation.PostConstruct;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class MediaServiceImpl implements MediaService {

    private static final String UPLOADS_DIRECTORY = "uploads";

    // Safe extensions for stored files. Unknown extensions are rejected.
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp", ".avif",
            ".mp3", ".wav", ".ogg", ".flac", ".aac", ".m4a", ".opus"
    );

    private final List<Media> mediaRepository = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    private final Path uploadsPath = Path.of(UPLOADS_DIRECTORY).toAbsolutePath().normalize();
    private final Tika tika = new Tika();

    @Value("${app.uploads.base-url:http://localhost:8080/uploads/}")
    private String uploadsBaseUrl;

    @PostConstruct
    void initializeUploadsDirectory() {
        try {
            Files.createDirectories(uploadsPath);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create uploads directory", e);
        }
    }

    @Override
    public List<Media> getAllMedia(String ownerUsername) {
        return mediaRepository.stream()
                .filter(m -> ownerUsername.equals(m.getOwnerUsername()))
                .collect(Collectors.toList());
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
    public Media updateMedia(Long id, Media media, String ownerUsername) {
        return mediaRepository.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .map(existingMedia -> {
                    checkOwnership(existingMedia, ownerUsername);
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
    public Media updateMedia(Long id, Media media, MultipartFile file, String ownerUsername) {
        return mediaRepository.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .map(existingMedia -> {
                    checkOwnership(existingMedia, ownerUsername);
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
    public void deleteMedia(Long id, String ownerUsername) {
        mediaRepository.removeIf(media -> {
            if (media.getId().equals(id)) {
                checkOwnership(media, ownerUsername);
                deleteUploadedFileIfPresent(media.getUrl());
                return true;
            }
            return false;
        });
    }

    private void checkOwnership(Media media, String ownerUsername) {
        if (!ownerUsername.equals(media.getOwnerUsername())) {
            throw new SecurityException("Access denied: you do not own this media item");
        }
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

        // Detect real content type from file bytes — ignores the client-supplied Content-Type
        String detectedType;
        try {
            detectedType = tika.detect(file.getInputStream());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to inspect uploaded file", e);
        }

        if ("image".equalsIgnoreCase(media.getMediaType()) && !detectedType.startsWith("image/")) {
            throw new IllegalArgumentException("File content does not match the declared image type");
        }
        if ("audio".equalsIgnoreCase(media.getMediaType()) && !detectedType.startsWith("audio/")) {
            throw new IllegalArgumentException("File content does not match the declared audio type");
        }

        // Validate file extension against allowlist
        String extension = extractExtension(file.getOriginalFilename());
        if (!extension.isEmpty() && !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("File extension '" + extension + "' is not permitted");
        }
    }

    private boolean isUploadSupported(String mediaType) {
        return "image".equalsIgnoreCase(mediaType) || "audio".equalsIgnoreCase(mediaType);
    }

    private String storeFile(MultipartFile file) {
        String extension = extractExtension(file.getOriginalFilename());
        String storedFilename = UUID.randomUUID() + extension;
        Path destination = uploadsPath.resolve(storedFilename);

        try {
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store uploaded file", e);
        }

        String base = uploadsBaseUrl.endsWith("/") ? uploadsBaseUrl : uploadsBaseUrl + "/";
        return base + storedFilename;
    }

    private void deleteUploadedFileIfPresent(String url) {
        if (url == null || !url.startsWith(uploadsBaseUrl)) {
            return;
        }
        String filename = url.substring(uploadsBaseUrl.endsWith("/")
                ? uploadsBaseUrl.length()
                : uploadsBaseUrl.length() + 1);
        Path filePath = uploadsPath.resolve(filename).normalize();
        if (!filePath.startsWith(uploadsPath)) {
            return; // path traversal guard
        }
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
            // Best-effort cleanup
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }
}
