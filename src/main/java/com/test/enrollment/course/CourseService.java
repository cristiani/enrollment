package com.test.enrollment.course;

import com.test.enrollment.student.Student;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CourseService {
    List<Course> getAllCourses();
    List<Course> getEmptyCourses();
    List<Course> getCoursesByCapacity(Integer capacity);
    List<Course> getCoursesByTitle(String title);
    List<Course> getCoursesBySubject(String subject);
    List<Student> getEnrolledStudentsByCourse(Long id);
    Optional<Course> findById(Long id);
    Course save(Course std);
    void deleteById(Long id);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    Boolean enrollStudent(Long courseId, Long studentId);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    List<Course> addCourses (List<Course> courses);
}
