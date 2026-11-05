package com.seaandtea.repository;

import com.seaandtea.entity.HomepageSliderImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomepageSliderImageRepository extends JpaRepository<HomepageSliderImage, Long> {

    List<HomepageSliderImage> findAllByOrderBySortOrderAsc();
}
