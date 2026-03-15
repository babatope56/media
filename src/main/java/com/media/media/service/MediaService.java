package com.media.media.service;

import com.media.media.model.Media;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface MediaService {
    List<Media> getAllMedia();
    Optional<Media> getMediaById(Long id);
    Media createMedia(Media media);
    Media createMedia(Media media, MultipartFile file);
    Media updateMedia(Long id, Media media);
    Media updateMedia(Long id, Media media, MultipartFile file);
    void deleteMedia(Long id);
}
