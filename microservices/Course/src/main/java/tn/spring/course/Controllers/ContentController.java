package tn.spring.course.Controllers; // Changé de "Controllers" à "tn.spring.course.controllers"

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.spring.course.DTO.ContentRequestDTO;  // Changé "DTO" en "dto" (minuscules)
import tn.spring.course.DTO.ContentResponseDTO; // Supprimé le "src.main.java" qui n'a rien à faire ici
import tn.spring.course.Services.ContentService; // Changé "Services" en "services"

import java.util.List;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @PostMapping("/addcontent")
    public ContentResponseDTO createContent(@RequestBody ContentRequestDTO dto) {
        return contentService.createContent(dto);
    }

    @GetMapping("/{id}")
    public ContentResponseDTO getContent(@PathVariable int id) {
        return contentService.getContent(id);
    }

    @GetMapping
    public List<ContentResponseDTO> getAllContents() {
        return contentService.getAllContents();
    }

    @PutMapping("/{id}")
    public ContentResponseDTO updateContent(@PathVariable int id,
                                            @RequestBody ContentRequestDTO dto) {
        return contentService.updateContent(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteContent(@PathVariable int id) {
        contentService.deleteContent(id);
    }
}