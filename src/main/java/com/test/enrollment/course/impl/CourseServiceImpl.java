package com.test.enrollment.course.impl;

import com.test.enrollment.course.CourseRepository;
import com.test.enrollment.student.StudentRepository;
import com.test.enrollment.exception.CourseCapacityExceeded;
import com.test.enrollment.exception.CourseNotFoundException;
import com.test.enrollment.exception.StudentEnrolmentCapacityExceeded;
import com.test.enrollment.exception.StudentNotFoundException;
import com.test.enrollment.course.Course;
import com.test.enrollment.student.Student;
import com.test.enrollment.course.CourseService;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {
    @Value( "${student_max_enrollment}" )
    private Integer courseMaxCapacityPerStudent;

    @Value( "${course_max_capacity}" )
    private Integer studentEnrollmentMaxCapacityPerCourse;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    StudentRepository studentRepository;

    @Override
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Override
    public List<Course> getEmptyCourses() {
        return courseRepository.findAll().stream().filter(course ->
        {
            Set<Student> students = course.getStudents();
            return students == null || students.size() == 0;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Course> getCoursesByCapacity(Integer capacity) {
        return courseRepository.findAll().stream().filter(course ->
        {
            Set<Student> students = course.getStudents();
            return students != null && students.size() == capacity;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Course> getCoursesByTitle(String title) {
        return courseRepository.findByTitle(title);
    }

    @Override
    public List<Course> getCoursesBySubject(String subject) {
        return courseRepository.findBySubject(subject);
    }

    @Override
    public List<Student> getEnrolledStudentsByCourse(Long id) {
        return new ArrayList<>(courseRepository.findById(id).map(Course::getStudents).orElse(new HashSet<>()));
    }

    @Override
    public Optional<Course> findById(Long id) {
        return courseRepository.findById(id);
    }

    @Override
    public Course save(Course std) {
        return courseRepository.save(std);
    }

    @Override
    public void deleteById(Long id) {
        courseRepository.deleteById(id);
    }

    @Override
    public Boolean enrollStudent(Long courseId, Long studentId) throws StudentEnrolmentCapacityExceeded,
            CourseCapacityExceeded, CourseNotFoundException, StudentNotFoundException {
        Course course = courseRepository.findById(courseId).orElseThrow(() ->
                new CourseNotFoundException("Course with " + courseId +" is Not Found!"));
        Student student = studentRepository.findById(studentId).orElseThrow(() ->
                new StudentNotFoundException("Student with " + studentId +" is Not Found!"));
        Set<Student> currentlyEnrolledStudents = course.getStudents();
        Set<Course> currentlyEnrolledCourses = student.getCourses();
        //check if number of courses per student is not reaching maximum allowed
        if (currentlyEnrolledStudents != null && currentlyEnrolledStudents.size() == courseMaxCapacityPerStudent) {
            throw new StudentEnrolmentCapacityExceeded("Student with " + studentId +
                    " is already enrolled in maximum number of allowed courses = " + courseMaxCapacityPerStudent);
        }
        //check if number of students per course is not reaching maximum allowed
        if (currentlyEnrolledCourses != null && currentlyEnrolledCourses.size() == studentEnrollmentMaxCapacityPerCourse ) {
            throw new CourseCapacityExceeded("Course with " + courseId +
                    " is already enrolled to maximum number of students = " + studentEnrollmentMaxCapacityPerCourse);
        }
        if (currentlyEnrolledStudents == null) {
            currentlyEnrolledStudents = new HashSet<>(courseMaxCapacityPerStudent);
        }
        boolean addedStudent = false;
        if (!currentlyEnrolledStudents.contains(student)) {
            currentlyEnrolledStudents.add(student);
            courseRepository.save(course);
            addedStudent = true;
        }
        if (currentlyEnrolledCourses == null) {
            currentlyEnrolledCourses = new HashSet<>(studentEnrollmentMaxCapacityPerCourse);
        }
        boolean addedCourse = false;
        if (!currentlyEnrolledCourses.contains(course)) {
            currentlyEnrolledCourses.add(course);
            studentRepository.save(student);
            addedCourse = true;
        }

        return addedStudent || addedCourse;
    }

    /**
     * Adds multiple courses via batch inserting, if there are students enrolled to different courses these are added
     * separately for extra courses to avoid DataIntegrityViolation exception.
     * This method uses only batch inserts if all courses and students from the list are not in db, and each student is
     * allocated to one course (this will run fast - could included 100K objects quickly). To the other extreme,
     * if all objects are in db, it will run very slow and return db situation.
     * @param courses list of course entities to be inserted
     * @return list of inserted entities
     */
    @Override
    public List<Course> addCourses(List<Course> courses) {
        if (courses == null) return new ArrayList<>();
        if (courses.isEmpty()) return new ArrayList<>();
        //for limit verification
        Map<Pair<String, String>, Integer> studentCourses = new HashMap<>();
        //keeps reference between students and courses that need to be added manually
        Map<Pair<String, String>, Pair<String, String>> studentToExtraCourses = new HashMap<>();
        List<Course> savedCourses = new ArrayList<>();


        //validation for new inserts
        for (Course course: courses) {
            Set<Student> students = course.getStudents();
            if (students.size() > studentEnrollmentMaxCapacityPerCourse) {
                throw new CourseCapacityExceeded("Course " + course.getTitle() + " " + course.getSubject() +
                        " has too many enrolled students = " + students.size());
            }
            for (Student student : students) {
                studentCourses.merge(Pair.with(student.getLastname(), student.getEmail()), 1, Integer::sum);
            }
        }
        Set<Pair<String, String>> studentData = studentCourses.keySet();
        for (Pair<String, String> currentStudentData: studentData) {
            Integer numberOfCourses = studentCourses.get(currentStudentData);
            if (numberOfCourses > courseMaxCapacityPerStudent) {
                throw new StudentEnrolmentCapacityExceeded("Student " + currentStudentData.getValue0() + " "
                        + currentStudentData.getValue1() +
                        " will be enrolled in more courses than allowed = " + numberOfCourses);
            }
        }

        //course verification
        Iterator<Course> courseIt = courses.iterator();
        Set<Student> allDbStudents = new HashSet<>();
        while(courseIt.hasNext()) {
            Course currentCourse = courseIt.next();
            Course dbCourse = courseRepository.findByTitleAndSubject(currentCourse.getTitle(), currentCourse.getSubject());
            if (dbCourse != null) {
                //already in DB we need to insert students manually to avoid constraint violation exception
                Set<Student> students = currentCourse.getStudents();
                if (students != null) {
                    Iterator<Student> studentIt = students.iterator();
                    List<Student> foundStudents = new ArrayList<>();
                    while (studentIt.hasNext()) {
                        Student currentStudent = studentIt.next();
                        Student dbStudent = studentRepository.findByLastnameAndEmail(currentStudent.getLastname(), currentStudent.getEmail());
                        foundStudents.add(dbStudent);
                        if (dbStudent == null) {
                            //course in db but student not in db
                            studentToExtraCourses.put(Pair.with(currentStudent.getLastname(), currentStudent.getEmail()),
                                    Pair.with(currentCourse.getTitle(), currentCourse.getSubject()));
                        }
                        studentIt.remove();
                    }
                    students.addAll(foundStudents);
                    allDbStudents.addAll(foundStudents);
                }
                savedCourses.add(dbCourse);
                courseIt.remove();
            }
        }

        //second student verification for new courses (not in DB)
        List<Student> allStudents = new ArrayList<>();
        for (Course course: courses) {
            Set<Student> students = course.getStudents();
            Iterator<Student> it = students.iterator();
            while(it.hasNext()) {
                Student currentStudent = it.next();
                if (allDbStudents.contains(currentStudent)) {
                    //student in db but course not in db
                    studentToExtraCourses.put(Pair.with(currentStudent.getLastname(), currentStudent.getEmail()),
                            Pair.with(course.getTitle(), course.getSubject()));
                    //remove to avoid exception being thrown
                    it.remove();
                } else {
                    //not in db but maybe all ready present in current list
                    if (allStudents.contains(currentStudent)) {
                        studentToExtraCourses.put(Pair.with(currentStudent.getLastname(), currentStudent.getEmail()),
                                Pair.with(course.getTitle(), course.getSubject()));
                        //remove to avoid exception being thrown
                        it.remove();
                    } else allStudents.add(currentStudent);
                }
            }
        }

        // save courses and students not in db in batch mode
        if (!courses.isEmpty()) {
            savedCourses.addAll(courseRepository.saveAll(courses));
        }
        // add the rest
        studentData = studentToExtraCourses.keySet();
        for (Pair<String, String> currentStudentData: studentData) {
            Student student = getStudentFromSavedData(currentStudentData, savedCourses);
            Pair<String, String> courseToEnroll = studentToExtraCourses.get(currentStudentData);
            Course course = getCourseFromSavedData(courseToEnroll, savedCourses);

            if (course != null && student != null) {
                enrollStudent(course.getId(), student.getId());
            }
            //add student back to main structure
            for(Course savedCourse: savedCourses) {
                if (savedCourse.getTitle().equals(courseToEnroll.getValue0()) &&
                        savedCourse.getSubject().equals(courseToEnroll.getValue1())) {
                    savedCourse.getStudents().add(student);
                }
            }

        }

        //maybe none was saved
        return savedCourses;
    }

    private Student getStudentFromSavedData(Pair<String, String> studentData, List<Course> courses) {
        for(Course course: courses) {
            Set<Student> students = course.getStudents();
            if (students != null && !students.isEmpty()) {
                for (Student student: students) {
                    if (student.getLastname().equals(studentData.getValue0()) &&
                            student.getEmail().equals(studentData.getValue1())) return student;
                }
            }
        }
        return null;
    }

    private Course getCourseFromSavedData(Pair<String, String> courseData, List<Course> courses) {
        for (Course course : courses) {
            if (course.getTitle().equals(courseData.getValue0()) &&
                    course.getSubject().equals(courseData.getValue1())) return course;
        }
        return null;
    }
}
