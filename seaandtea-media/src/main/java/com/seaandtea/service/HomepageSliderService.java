package com.seaandtea.service;

import com.seaandtea.dto.HomepageSliderImageResponse;
import com.seaandtea.entity.HomepageSliderImage;
import com.seaandtea.exception.ResourceNotFoundException;
import com.seaandtea.repository.HomepageSliderImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HomepageSliderService {

    private final HomepageSliderImageRepository repository;
    private final FileUploadService fileUploadService;

    public List<HomepageSliderImageResponse> getAll() {
        return repository.findAllByOrderBySortOrderAsc().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public HomepageSliderImageResponse add(String imageUrl, Integer sortOrder, String altText) {
        int order = sortOrder != null ? sortOrder : (int) repository.count();
        HomepageSliderImage entity = HomepageSliderImage.builder()
            .imageUrl(imageUrl)
            .sortOrder(order)
            .altText(altText)
            .build();
        entity = repository.save(entity);
        log.info("Homepage slider image added: id={}", entity.getId());
        return toResponse(entity);
    }

    @Transactional
    public void deleteById(Long id) {
        HomepageSliderImage image = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("HomepageSliderImage", id));
        try {
            fileUploadService.deleteImage(image.getImageUrl());
        } catch (Exception e) {
            log.warn("Could not delete image from Cloudinary: {}", image.getImageUrl(), e);
        }
        repository.delete(image);
        log.info("Homepage slider image deleted: id={}", id);
    }

    private HomepageSliderImageResponse toResponse(HomepageSliderImage entity) {
        return HomepageSliderImageResponse.builder()
            .id(entity.getId())
            .imageUrl(entity.getImageUrl())
            .sortOrder(entity.getSortOrder())
            .altText(entity.getAltText())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}
