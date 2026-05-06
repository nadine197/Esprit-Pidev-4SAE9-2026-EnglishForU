package tn.spring.course.Services;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import tn.spring.course.DTO.CourseRequestDTO;
import tn.spring.course.DTO.CourseResponseDTO;
import tn.spring.course.Mappers.CourseMapper;
import tn.spring.course.Models.Course;
import tn.spring.course.Repositories.CourseRepository;

import java.util.List;
import java.util.stream.Collectors;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    @Override
    public CourseResponseDTO createCourse(CourseRequestDTO dto) {

        Course course = courseMapper.toEntity(dto);
        return courseMapper.toResponseDTO(courseRepository.save(course));
    }

    @Override
    public CourseResponseDTO updateCourse(int id, CourseRequestDTO dto) {

        Course existing = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // update simple fields seulement
        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());
        existing.setDuration(dto.getDuration());
        existing.setAdminId(dto.getAdminId());

        return courseMapper.toResponseDTO(courseRepository.save(existing));
    }

    @Override
    public CourseResponseDTO getCourse(int id) {
        return courseMapper.toResponseDTO(
                courseRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Course not found"))
        );
    }

    @Override
    public List<CourseResponseDTO> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(courseMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCourse(int id) {
        courseRepository.deleteById(id);
    }

    public List<CourseResponseDTO> getCoursesSortedByDuration() {

        return courseRepository.findAllByOrderByDurationAsc()
                .stream()
                .map(courseMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    public List<CourseResponseDTO> searchCourses(String keyword){

        return courseRepository.findByTitleContainingIgnoreCase(keyword)
                .stream()
                .map(courseMapper::toResponseDTO)
                .toList();
    }
    public byte[] exportCoursesPdf() {

        try {

            List<Course> courses = courseRepository.findAll();

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            Document document = new Document();

            PdfWriter.getInstance(document, out);

            document.open();

            document.add(new Paragraph("Courses List"));

            PdfPTable table = new PdfPTable(4);

            table.addCell("ID");
            table.addCell("Title");
            table.addCell("Description");
            table.addCell("Duration");

            for(Course c : courses){

                table.addCell(String.valueOf(c.getCourseid()));
                table.addCell(c.getTitle());
                table.addCell(c.getDescription());
                table.addCell(String.valueOf(c.getDuration()));

            }

            document.add(table);

            document.close();

            return out.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException("Error generating PDF");

        }

    }
    public byte[] exportCoursesExcel(){

        try {

            List<Course> courses = courseRepository.findAll();

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Courses");

            Row header = sheet.createRow(0);

            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Title");
            header.createCell(2).setCellValue("Description");
            header.createCell(3).setCellValue("Duration");

            int rowIndex = 1;

            for(Course c : courses){

                Row row = sheet.createRow(rowIndex++);

                row.createCell(0).setCellValue(c.getCourseid());
                row.createCell(1).setCellValue(c.getTitle());
                row.createCell(2).setCellValue(c.getDescription());
                row.createCell(3).setCellValue(c.getDuration());

            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            workbook.write(out);
            workbook.close();

            return out.toByteArray();

        } catch (Exception e){

            throw new RuntimeException("Error generating Excel");

        }

    }
}