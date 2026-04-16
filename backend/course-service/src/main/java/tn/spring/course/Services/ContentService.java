package tn.spring.course.Services;

import tn.spring.course.DTO.ContentRequestDTO;
import tn.spring.course.DTO.ContentResponseDTO;
import tn.spring.course.DTO.CourseResponseDTO;

import java.util.List;

public interface ContentService {

    ContentResponseDTO createContent(ContentRequestDTO dto);

    ContentResponseDTO updateContent(int contentid, ContentRequestDTO dto);

    ContentResponseDTO getContent(int contentid);

    List<ContentResponseDTO> getAllContents();

    void deleteContent(int contentid);
    List<ContentResponseDTO> searchContents(String keyword);
    byte[] exportContentsPdf();
    List<Object[]> getStatsByType();
    List<String> getHistory();
    byte[] exportHistoryTxt();

}