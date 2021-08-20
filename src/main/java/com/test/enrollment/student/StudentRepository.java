package com.test.enrollment.student;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    // Query methods
    List<Student> findByEmail(String email);
    List<Student> findByLastname(String lastname);
    Student findByLastnameAndEmail(String lastname, String email);
}
