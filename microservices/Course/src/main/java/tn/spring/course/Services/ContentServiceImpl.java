package tn.spring.course.Services;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.spring.course.Repositories.CourseRepository;
import tn.spring.course.Models.Content;
import tn.spring.course.DTO.ContentRequestDTO;
import tn.spring.course.DTO.ContentResponseDTO;
import tn.spring.course.mappers.ContentMapper;
import tn.spring.course.Repositories.ContentRepository;
import tn.spring.course.Services.ContentService;
import tn.spring.course.Models.Course;

import java.util.List;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {

    private final ContentRepository contentRepository;
    private final CourseRepository courseRepository;
    private final ContentMapper contentMapper;

    @Override
    public ContentResponseDTO createContent(ContentRequestDTO dto){

        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Content content = contentMapper.toEntity(dto);
        content.setCourse(course);

        return contentMapper.toResponseDTO(contentRepository.save(content));
    }

    @Override
    public ContentResponseDTO updateContent(int contentid, ContentRequestDTO dto) {

        Content existing = contentRepository.findById(contentid)
                .orElseThrow(() -> new RuntimeException("Content not found"));

        existing.setTitle(dto.getTitle());
        existing.setType(dto.getType());
        existing.setUrl(dto.getUrl());

        return contentMapper.toResponseDTO(contentRepository.save(existing));
    }

    @Override
    public ContentResponseDTO getContent(int contentid) {
        return contentMapper.toResponseDTO(
                contentRepository.findById(contentid)
                        .orElseThrow(() -> new RuntimeException("Content not found"))
        );
    }

    @Override
    public List<ContentResponseDTO> getAllContents() {
        return contentRepository.findAll()
                .stream()
                .map(contentMapper::toResponseDTO)
                .toList();
    }

    @Override
    public void deleteContent(int contentid) {
        contentRepository.deleteById(contentid);
    }
}