package io.wespresso_world.wespresso_world.review;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByBeansIdOrderByCreatedAtDesc(Long beansId);
}
