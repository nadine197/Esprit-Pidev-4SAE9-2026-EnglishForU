package tn.spring.quiz.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contentid;

    private String title;
    private String type;
    private String url;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    private Course course;
}