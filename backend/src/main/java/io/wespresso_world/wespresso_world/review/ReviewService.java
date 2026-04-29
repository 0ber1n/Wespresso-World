package io.wespresso_world.wespresso_world.review;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.wespresso_world.wespresso_world.InputSanitizer;
import io.wespresso_world.wespresso_world.VulnConfig;

import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private InputSanitizer inputSanitizer;

    @Autowired
    private VulnConfig vulnConfig;

    public List<Review> getReviewsForBean(Long beansId) {
        return reviewRepository.findByBeansIdOrderByCreatedAtDesc(beansId);
    }

    public Review submitReview(Long beansId, Long userId, String username, Integer rating, String comment) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        String cleanComment = comment != null ? comment.trim() : "";
        String cleanUsername = username != null ? username.trim() : "";

        if (!vulnConfig.getStoredXss().isEnabled()) {
            cleanComment = inputSanitizer.sanitize(cleanComment, "comment");
            cleanUsername = inputSanitizer.sanitize(cleanUsername, "username");
        }

        Review review = new Review();
        review.setBeansId(beansId);
        review.setUserId(userId);
        review.setUsername(cleanUsername);
        review.setRating(rating);
        review.setComment(cleanComment);
        return reviewRepository.save(review);
    }

    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }
}
