package com.test.enrollment.student.impl;

import com.test.enrollment.student.StudentRepository;
import com.test.enrollment.course.Course;
import com.test.enrollment.student.Student;
import com.test.enrollment.student.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudentServiceImpl implements StudentService {
    @Autowired
    StudentRepository repository;

    @Override
    public List<Student> getAllStudents() {
        return repository.findAll();
    }

    @Override
    public List<Student> getStudentsByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Override
    public List<Student> getStudentsByLastname(String lastname) {
        return repository.findByLastname(lastname);
    }

    @Override
    public List<Student> getNonRegisteredStudents() {
        return repository.findAll().stream().filter(course ->
        {
            Set<Course> courses = course.getCourses();
            return courses == null || courses.size() == 0;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Student> getStudentsByEnrollmentCapacity(Integer capacity) {
        return repository.findAll().stream().filter(course ->
        {
            Set<Course> courses = course.getCourses();
            return courses != null && courses.size() == capacity;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Course> getRegisteredCoursesByStudent(Long id) {
        return new ArrayList<>(repository.findById(id).map(Student::getCourses).orElse(new HashSet<>()));
    }

    @Override
    public Optional<Student> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Student save(Student std) {
        return repository.save(std);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

}
