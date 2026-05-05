package tn.spring.course.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.course.DTO.ContentRequestDTO;
import tn.spring.course.DTO.ContentResponseDTO;
import tn.spring.course.Services.ContentService;

import java.util.List;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @PostMapping
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
    @GetMapping("/search")
    public List<ContentResponseDTO> searchContents(@RequestParam String keyword){
        return contentService.searchContents(keyword);
    }
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> exportPdf(){

        byte[] pdf = contentService.exportContentsPdf();

        return ResponseEntity.ok()
                .header("Content-Disposition","attachment; filename=contents.pdf")
                .header("Content-Type","application/pdf")
                .body(pdf);
    }
    @GetMapping("/stats/type")
    public List<Object[]> statsByType(){
        return contentService.getStatsByType();
    }
    @GetMapping("/history/txt")
    public ResponseEntity<byte[]> downloadHistory(){

        byte[] file = contentService.exportHistoryTxt();

        return ResponseEntity.ok()
                .header("Content-Disposition","attachment; filename=content-history.txt")
                .header("Content-Type","text/plain")
                .body(file);
    }
}