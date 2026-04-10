package com.media.media.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    /** Set by the controller from the authenticated principal — never sent by the client. */
    @JsonIgnore
    private String ownerUsername;
}

