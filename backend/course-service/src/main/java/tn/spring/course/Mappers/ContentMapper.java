package tn.spring.course.Mappers;

import org.springframework.stereotype.Component;
import tn.spring.course.DTO.ContentRequestDTO;
import tn.spring.course.DTO.ContentResponseDTO;
import tn.spring.course.Models.Content;
import tn.spring.course.Models.Course;

@Component
public class ContentMapper {

    public Content toEntity(ContentRequestDTO dto, Course course){
        Content content = new Content();

        content.setTitle(dto.getTitle());
        content.setType(dto.getType());
        content.setUrl(dto.getUrl());
        content.setAuthorId(dto.getAuthorId());
        content.setCourse(course);

        return content;
    }

    public ContentResponseDTO toResponseDTO(Content content){
        return ContentResponseDTO.builder()
                .contentId(content.getContentid())
                .title(content.getTitle())
                .type(content.getType())
                .url(content.getUrl())
                .authorId(content.getAuthorId())
                .courseId(content.getCourse().getCourseid())
                .build();
    }
}