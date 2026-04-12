package tn.spring.course.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.spring.course.Controllers.StudyGroupAlertController;
import tn.spring.course.DTO.ContentRequestDTO;
import tn.spring.course.DTO.ContentResponseDTO;
import tn.spring.course.Mappers.ContentMapper;
import tn.spring.course.Models.Content;
import tn.spring.course.Models.Course;
import tn.spring.course.Repositories.ContentRepository;
import tn.spring.course.Repositories.CourseRepository;
import tn.spring.course.Repositories.StudyGroupRepository;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {

    private final ContentRepository        contentRepository;
    private final CourseRepository         courseRepository;
    private final ContentMapper            mapper;
    private final StudyGroupRepository     studyGroupRepository;  // ✅ ajouté
    private final StudyGroupAlertController alertController;       // ✅ ajouté

    private final List<String> history = new ArrayList<>();

    private void addHistory(String action) {
        history.add(action + " - " + LocalDateTime.now());
    }

    @Override
    public ContentResponseDTO createContent(ContentRequestDTO dto) {

        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Content content = mapper.toEntity(dto, course);
        Content saved   = contentRepository.save(content);

        addHistory("CREATE Content : " + saved.getTitle());

        // ✅ Notifier tous les StudyGroups liés à ce cours
        studyGroupRepository.findByCourse(course).forEach(group ->
                alertController.notifyNewContent(
                        group,
                        saved.getTitle(),
                        saved.getType()
                )
        );

        return mapper.toResponseDTO(saved);
    }

    @Override
    public ContentResponseDTO updateContent(int contentid, ContentRequestDTO dto) {

        Content existing = contentRepository.findById(contentid)
                .orElseThrow(() -> new RuntimeException("Content not found"));

        existing.setTitle(dto.getTitle());
        existing.setType(dto.getType());
        existing.setUrl(dto.getUrl());

        Content updated = contentRepository.save(existing);
        addHistory("UPDATE Content : " + updated.getTitle());

        return mapper.toResponseDTO(updated);
    }

    @Override
    public ContentResponseDTO getContent(int contentid) {
        return mapper.toResponseDTO(
                contentRepository.findById(contentid)
                        .orElseThrow(() -> new RuntimeException("Content not found"))
        );
    }

    @Override
    public List<ContentResponseDTO> getAllContents() {
        return contentRepository.findAll()
                .stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteContent(int contentid) {
        Content content = contentRepository.findById(contentid)
                .orElseThrow(() -> new RuntimeException("Content not found"));
        contentRepository.deleteById(contentid);
        addHistory("DELETE Content : " + content.getTitle());
    }

    @Override
    public List<ContentResponseDTO> searchContents(String keyword) {
        return contentRepository
                .findByTitleContainingIgnoreCase(keyword)
                .stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    @Override
    public byte[] exportContentsPdf() {
        try {
            List<Content> contents = contentRepository.findAll();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("Contents List"));
            PdfPTable table = new PdfPTable(4);
            table.addCell("ID");
            table.addCell("Title");
            table.addCell("Type");
            table.addCell("URL");
            for (Content c : contents) {
                table.addCell(String.valueOf(c.getContentid()));
                table.addCell(c.getTitle());
                table.addCell(c.getType());
                table.addCell(c.getUrl());
            }
            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF");
        }
    }

    @Override
    public List<Object[]> getStatsByType() {
        return contentRepository.countByType();
    }

    @Override
    public byte[] exportHistoryTxt() {
        StringBuilder builder = new StringBuilder();
        for (String h : history) {
            builder.append(h).append("\n");
        }
        return builder.toString().getBytes();
    }

    @Override
    public List<String> getHistory() {
        return history;
    }
}