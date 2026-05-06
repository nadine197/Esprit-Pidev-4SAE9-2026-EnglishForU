package tn.spring.course.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer contentid;

    private String title;
    private String type;
    private String url;

    @JsonBackReference("content-ref")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courseid")
    private Course course;
    @Column(nullable = false, columnDefinition = "uuid")
    private UUID authorId;
}