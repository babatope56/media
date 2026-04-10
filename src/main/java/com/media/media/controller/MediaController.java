package com.media.media.controller;

import com.media.media.dto.AudioAnalysisResult;
import com.media.media.dto.ChordComparisonResult;
import com.media.media.model.Media;
import com.media.media.service.AudioAnalysisService;
import com.media.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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

    @PostMapping(value = "/analyze/compare", consumes = "multipart/form-data")
    public ResponseEntity<?> compareUploads(
            @RequestPart("fileA") MultipartFile fileA,
            @RequestPart("fileB") MultipartFile fileB) {
        try {
            ChordComparisonResult result = audioAnalysisService.compareUploads(fileA, fileB);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Comparison failed: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/analyze/compare/{otherId}")
    public ResponseEntity<?> compareSavedMedia(@PathVariable Long id,
                                               @PathVariable Long otherId,
                                               Authentication authentication) {
        Optional<Media> sourceOpt = mediaService.getMediaById(id);
        Optional<Media> otherOpt = mediaService.getMediaById(otherId);

        if (sourceOpt.isEmpty() || otherOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Media source = sourceOpt.get();
        Media other = otherOpt.get();
        String username = authentication.getName();
        if (!username.equals(source.getOwnerUsername()) || !username.equals(other.getOwnerUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            ChordComparisonResult result = audioAnalysisService.compareMedia(source, other);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Comparison failed: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/analyze/matches")
    public ResponseEntity<?> findMatchesInLibrary(@PathVariable Long id,
                                                  Authentication authentication) {
        Optional<Media> sourceOpt = mediaService.getMediaById(id);
        if (sourceOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Media source = sourceOpt.get();
        String username = authentication.getName();
        if (!username.equals(source.getOwnerUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<Media> userMedia = mediaService.getAllMedia(username);
            List<ChordComparisonResult> matches = audioAnalysisService.findMatchesInMediaLibrary(source, userMedia);
            return ResponseEntity.ok(matches);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Match scan failed: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/analyze/compare/{otherId}/piano-midi")
    public ResponseEntity<?> renderMatchingChordsAsPianoMidi(@PathVariable Long id,
                                                             @PathVariable Long otherId,
                                                             Authentication authentication) {
        Optional<Media> sourceOpt = mediaService.getMediaById(id);
        Optional<Media> otherOpt = mediaService.getMediaById(otherId);

        if (sourceOpt.isEmpty() || otherOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Media source = sourceOpt.get();
        Media other = otherOpt.get();
        String username = authentication.getName();
        if (!username.equals(source.getOwnerUsername()) || !username.equals(other.getOwnerUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            byte[] midi = audioAnalysisService.renderMatchingChordsAsPianoMidi(source, other);
            String filename = "matching-chords-" + id + "-vs-" + otherId + ".mid";
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf("audio/midi"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(midi);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("MIDI rendering failed: " + e.getMessage());
        }
    }
}
