package com.test.enrollment.course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    // Query method
    List<Course> findByTitle(String title);
    List<Course> findBySubject(String subject);
    Course findByTitleAndSubject(String title, String subject);
}