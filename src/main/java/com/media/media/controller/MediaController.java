package com.media.media.controller;

import com.media.media.dto.AudioAnalysisResult;
import com.media.media.model.Media;
import com.media.media.service.AudioAnalysisService;
import com.media.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;
    private final AudioAnalysisService audioAnalysisService;

    @GetMapping
    public ResponseEntity<List<Media>> getAllMedia(Authentication authentication) {
        return ResponseEntity.ok(mediaService.getAllMedia(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Media> getMediaById(@PathVariable Long id) {
        return mediaService.getMediaById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Media> createMedia(@RequestBody Media media,
                                             Authentication authentication) {
        try {
            media.setOwnerUsername(authentication.getName());
            Media createdMedia = mediaService.createMedia(media);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMedia);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Media> createMediaWithUpload(
            @RequestPart("metadata") Media media,
            @RequestPart("file") MultipartFile file,
            Authentication authentication) {
        try {
            media.setOwnerUsername(authentication.getName());
            Media createdMedia = mediaService.createMedia(media, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMedia);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Media> updateMedia(@PathVariable Long id,
                                             @RequestBody Media media,
                                             Authentication authentication) {
        try {
            Media updatedMedia = mediaService.updateMedia(id, media, authentication.getName());
            return ResponseEntity.ok(updatedMedia);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<Media> updateMediaWithUpload(
            @PathVariable Long id,
            @RequestPart("metadata") Media media,
            @RequestPart("file") MultipartFile file,
            Authentication authentication) {
        try {
            Media updatedMedia = mediaService.updateMedia(id, media, file, authentication.getName());
            return ResponseEntity.ok(updatedMedia);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedia(@PathVariable Long id,
                                            Authentication authentication) {
        try {
            mediaService.deleteMedia(id, authentication.getName());
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping(value = "/analyze", consumes = "multipart/form-data")
    public ResponseEntity<?> analyzeUpload(
            @RequestPart("file") MultipartFile file,
            @RequestParam("timestamp") double timestamp) {
        try {
            AudioAnalysisResult result = audioAnalysisService.analyzeAtTimestamp(file, timestamp);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Analysis failed: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/analyze")
    public ResponseEntity<?> analyzeMedia(@PathVariable Long id) {
        return mediaService.getMediaById(id)
                .map(media -> {
                    try {
                        AudioAnalysisResult result = audioAnalysisService.analyze(media);
                        return ResponseEntity.ok(result);
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().body(e.getMessage());
                    } catch (Exception e) {
                        return ResponseEntity.internalServerError().body("Analysis failed: " + e.getMessage());
                    }
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
