package io.wespresso_world.wespresso_world.review;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reviews")
@Schema(description = "A review left for a coffee bean product")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Schema(description = "Unique identifier for the review")
    private Long id;

    @Schema(description = "ID of the coffee bean being reviewed")
    private Long beansId;

    @Schema(description = "ID of the user who wrote the review")
    private Long userId;

    @Schema(description = "Username of the reviewer")
    private String username;

    @Schema(description = "Rating from 1 to 5")
    private Integer rating;

    @Column(length = 1000)
    @Schema(description = "Review comment")
    private String comment;

    @Schema(description = "Timestamp when the review was created")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
