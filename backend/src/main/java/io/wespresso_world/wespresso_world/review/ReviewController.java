package io.wespresso_world.wespresso_world.review;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.wespresso_world.wespresso_world.user.JwtService;

import java.util.List;

@Tag(name = "Reviews API", description = "Endpoints for managing coffee bean reviews")
@RestController
@RequestMapping("/beans/{beansId}/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private JwtService jwtService;

    @Operation(summary = "Get all reviews for a coffee bean")
    @GetMapping
    public List<Review> getReviews(@PathVariable Long beansId) {
        return reviewService.getReviewsForBean(beansId);
    }

    @Operation(summary = "Submit a review for a coffee bean")
    @PostMapping
    public Review submitReview(
            @PathVariable Long beansId,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ReviewRequest request) {
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        Long userId = jwtService.extractUserId(token);
        return reviewService.submitReview(beansId, userId, username, request.getRating(), request.getComment());
    }

    @Operation(summary = "Delete a review (admin only)")
    @PreAuthorize("hasRole('admin')")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long beansId, @PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}

class ReviewRequest {
    private Integer rating;
    private String comment;

    public Integer getRating() { return rating; }
    public String getComment() { return comment; }
    public void setRating(Integer rating) { this.rating = rating; }
    public void setComment(String comment) { this.comment = comment; }
}
