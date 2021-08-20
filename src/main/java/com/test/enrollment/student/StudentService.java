package com.test.enrollment.student;

import com.test.enrollment.course.Course;

import java.util.List;
import java.util.Optional;

public interface StudentService {
    List<Student> getAllStudents();
    List<Student> getStudentsByEmail(String email);
    List<Student> getStudentsByLastname(String lastname);
    List<Student> getNonRegisteredStudents();
    List<Student> getStudentsByEnrollmentCapacity(Integer capacity);
    List<Course> getRegisteredCoursesByStudent(Long id);
    Optional<Student> findById(Long id);
    Student save(Student std);
    void deleteById(Long id);
}
