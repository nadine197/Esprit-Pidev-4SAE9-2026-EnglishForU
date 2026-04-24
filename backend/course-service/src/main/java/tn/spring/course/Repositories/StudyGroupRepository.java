package tn.spring.course.Repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.history.RevisionRepository;
import tn.spring.course.Models.Course;
import tn.spring.course.Models.StudyGroup;
import tn.spring.course.Models.StudyGroupStatus;

import java.util.Date;
import java.util.List;

public interface StudyGroupRepository
        extends JpaRepository<StudyGroup, Long>,
        RevisionRepository<StudyGroup, Long, Long> {

    @Query("SELECT g FROM StudyGroup g WHERE :date BETWEEN g.startdate AND g.enddate")
    List<StudyGroup> findByDate(@Param("date") Date date);

    @Query("SELECT g FROM StudyGroup g WHERE g.startdate <= :endOfMonth AND g.enddate >= :startOfMonth")
    List<StudyGroup> findByMonthRange(
            @Param("startOfMonth") Date startOfMonth,
            @Param("endOfMonth") Date endOfMonth
    );

    List<StudyGroup> findByStartdate(Date startdate);
    List<StudyGroup> findByEnddate(Date enddate);

    // ✅ Groupes liés à un cours — pour notifier lors d'un nouveau contenu
    List<StudyGroup> findByCourse(Course course);

    @Query("SELECT g.status, COUNT(g) FROM StudyGroup g GROUP BY g.status")
    List<Object[]> countByStatus();

    @Query("SELECT g.level, COUNT(g) FROM StudyGroup g GROUP BY g.level")
    List<Object[]> countByLevel();

    @Query("SELECT g.level, AVG(SIZE(g.studentsIds) * 100.0 / g.maxCapacity) " +
            "FROM StudyGroup g WHERE g.maxCapacity > 0 GROUP BY g.level")
    List<Object[]> avgFillRateByLevel();

    @Query("SELECT MONTH(g.startdate), COUNT(g) FROM StudyGroup g " +
            "GROUP BY MONTH(g.startdate) ORDER BY MONTH(g.startdate)")
    List<Object[]> countByMonth();

    @Query("SELECT g FROM StudyGroup g ORDER BY SIZE(g.studentsIds) DESC")
    List<StudyGroup> findTopByFillRate(Pageable pageable);

    @Query("SELECT g.level, SUM(g.maxCapacity), SUM(SIZE(g.studentsIds)) " +
            "FROM StudyGroup g GROUP BY g.level")
    List<Object[]> capacityVsEnrolledByLevel();

    @Query("SELECT g FROM StudyGroup g WHERE " +
            "(:name IS NULL OR LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:level IS NULL OR g.level = :level) AND " +
            "(:status IS NULL OR g.status = :status) AND " +
            "(:location IS NULL OR LOWER(g.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:courseId IS NULL OR g.course.courseid = :courseId)")
    List<StudyGroup> searchGroups(
            @Param("name")     String name,
            @Param("level")    String level,
            @Param("status")   StudyGroupStatus status,
            @Param("location") String location,
            @Param("courseId") Integer courseId
    );
    @Query("SELECT g FROM StudyGroup g WHERE g.status = 'PLANNED' " +
            "AND g.startdate <= :now")
    List<StudyGroup> findPlannedToActivate(@Param("now") Date now);
    @Query("SELECT g FROM StudyGroup g WHERE g.status = 'ACTIVE' " +
            "AND g.enddate < :now")
    List<StudyGroup> findActiveToComplete(@Param("now") Date now);
    @Query("SELECT DISTINCT g FROM StudyGroup g LEFT JOIN FETCH g.studentsIds")
    List<StudyGroup> findAllWithStudents();
}