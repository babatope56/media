package com.media.media.controller;

import com.media.media.model.Media;
import com.media.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {
    
    private final MediaService mediaService;
    
    @GetMapping
    public ResponseEntity<List<Media>> getAllMedia() {
        return ResponseEntity.ok(mediaService.getAllMedia());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Media> getMediaById(@PathVariable Long id) {
        return mediaService.getMediaById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Media> createMedia(@RequestBody Media media) {
        try {
            Media createdMedia = mediaService.createMedia(media);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMedia);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Media> createMediaWithUpload(
            @RequestPart("metadata") Media media,
            @RequestPart("file") MultipartFile file
    ) {
        try {
            Media createdMedia = mediaService.createMedia(media, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMedia);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Media> updateMedia(@PathVariable Long id, @RequestBody Media media) {
        try {
            Media updatedMedia = mediaService.updateMedia(id, media);
            return ResponseEntity.ok(updatedMedia);
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
            @RequestPart("file") MultipartFile file
    ) {
        try {
            Media updatedMedia = mediaService.updateMedia(id, media, file);
            return ResponseEntity.ok(updatedMedia);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedia(@PathVariable Long id) {
        mediaService.deleteMedia(id);
        return ResponseEntity.noContent().build();
    }
}
