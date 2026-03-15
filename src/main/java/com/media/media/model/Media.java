package com.media.media.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Media {
    private Long id;
    private String title;
    private String description;
    private String mediaType;
    private String url;
}

