package tn.spring.course.mappers;

import org.springframework.stereotype.Component;
import tn.spring.course.DTO.ContentRequestDTO;
import tn.spring.course.DTO.ContentResponseDTO;
import tn.spring.course.Models.Content;

@Component
public class ContentMapper {

    // DTO -> ENTITY
    public Content toEntity(ContentRequestDTO dto){
        Content content = new Content();
        content.setTitle(dto.getTitle());
        content.setType(dto.getType());
        content.setUrl(dto.getUrl());
        return content;
    }

    // ENTITY -> RESPONSE DTO
    public ContentResponseDTO toResponseDTO(Content content){
        ContentResponseDTO dto = new ContentResponseDTO();

        dto.setContentId(content.getContentid());
        dto.setTitle(content.getTitle());
        dto.setType(content.getType());
        dto.setUrl(content.getUrl());

        // très important : on renvoie seulement l'id du course
        if(content.getCourse()!=null)
            dto.setCourseId(content.getCourse().getCourseid());

        return dto;
    }
}