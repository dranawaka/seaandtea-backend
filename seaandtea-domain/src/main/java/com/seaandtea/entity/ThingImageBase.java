package com.seaandtea.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class ThingImageBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String url;

    @Column(name = "alt_text", length = 300)
    private String altText;

    @Column(length = 300)
    private String credit;

    @Column(length = 300)
    private String licence;

    @Column(name = "source_url", length = 1000)
    private String sourceUrl;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}
