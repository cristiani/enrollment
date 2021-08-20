package com.test.enrollment.student;

import com.test.enrollment.course.CourseDto;
import com.test.enrollment.exception.InvalidNumberException;
import com.test.enrollment.exception.StudentEnrolmentCapacityExceeded;
import com.test.enrollment.course.Course;
import com.test.enrollment.exception.StudentNotFoundException;
import com.test.enrollment.validation.ValuesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Validated
@RestController
@RequestMapping("/api")
public class StudentController {
    @Value( "${student_max_enrollment}" )
    private Integer maxCapacity;

    @Autowired
    StudentService studentService;

    @GetMapping(value="/students", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<StudentDto> getAllStudents(){
        return toDtoList(studentService.getAllStudents());
    }

    @GetMapping(value="/students/filters", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<StudentDto> getStudentsByFilter(@RequestParam(value="name")
                                                   @NotNull @NotEmpty
                                                   @ValuesAllowed(values = {
                                                           "email",
                                                           "lastname",
                                                           "capacity"
                                                   })
                                                           String name,
                                               @RequestParam(value="value") @NotNull @NotEmpty String value) {
        if ("email".equals(name)) return toDtoList(studentService.getStudentsByEmail(value));
        else if ("lastname".equals(name)) return  toDtoList(studentService.getStudentsByLastname(value));
        int capacity;
        try {
            capacity = Integer.parseInt(value);
        } catch (NumberFormatException ne) {
            throw new InvalidNumberException(ne.getMessage());
        }
        if (capacity > maxCapacity) {
            throw new StudentEnrolmentCapacityExceeded("Requested capacity of " + capacity +
                    " is higher than maximum accepted " + maxCapacity);
        }
        return toDtoList(studentService.getStudentsByEnrollmentCapacity(capacity));
    }


    @GetMapping(value="/students/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public StudentDto getStudentById(@PathVariable("id") @Min(1) Long id) {
        return toDto(studentService.findById(id)
                .orElseThrow(()->new StudentNotFoundException("Student with " + id + " is Not Found!")));
    }

    //TO DO: transform it into a general method like in CourseController
    @PostMapping(value="/students", produces = MediaType.APPLICATION_JSON_VALUE)
    public StudentDto addNonRegisteredStudent(@Valid @RequestBody StudentDto std) {
        return toDto(studentService.save(toEntity(std)));
    }

    //TO DO: Update list of courses too
    @PutMapping(value="/students/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public StudentDto updateStudent(@PathVariable("id") @Min(1) Long id, @Valid @RequestBody StudentDto newStudent) {
        Student stdu = studentService.findById(id)
                .orElseThrow(()->new StudentNotFoundException("Student with " + id +" is Not Found!"));
        stdu.setFirstname(newStudent.getFirstname());
        stdu.setLastname(newStudent.getLastname());
        stdu.setEmail(newStudent.getEmail());
        return toDto(studentService.save(stdu));
    }

    @DeleteMapping(value="/students/{id}")
    public String deleteStudent(@PathVariable("id") @Min(1) Long id) {
        Student std = studentService.findById(id)
                .orElseThrow(()->new StudentNotFoundException("Student with " + id + " is Not Found!"));
        studentService.deleteById(std.getId());
        return "Student with ID : " + id + " is deleted";
    }

    @GetMapping(value="/students/non-registered", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<StudentDto> getNonRegisteredStudents() {
        return toDtoList(studentService.getNonRegisteredStudents());
    }

    @GetMapping(value="/students/{id}/courses", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CourseDto> getRegisteredCourses(@PathVariable("id") @Min(1) Long id) {
        return toCourseDtoList(studentService.getRegisteredCoursesByStudent(id));
    }

    private StudentDto toDto(Student student) {
        StudentDto dto =  new StudentDto(student.getId(), student.getFirstname(), student.getLastname(), student.getEmail());
        Set<Course> courses = student.getCourses();
        if (courses == null || courses.isEmpty()) return dto;
        List<CourseDto> courseDtos = new ArrayList<>();
        for (Course course : courses) {
            CourseDto coursedto = new CourseDto(course.getId(), course.getTitle(), course.getSubject(),
                    course.getTeacher(), course.getDescription());
            courseDtos.add(coursedto);
        }
        dto.setCourses(courseDtos);
        return dto;
    }

    private List<StudentDto> toDtoList(List<Student> students) {
        List<StudentDto> dtos = new ArrayList<>();
        for (Student student: students) {
            dtos.add(toDto(student));
        }
        return dtos;
    }

    /**
     * For this controller we are not interested in list of enrolled students
     * @param course entity
     * @return dto
     */
    private CourseDto toCourseDto(Course course) {
        return new CourseDto(course.getId(), course.getTitle(), course.getSubject(), course.getTeacher(),
                course.getDescription());
    }

    private List<CourseDto> toCourseDtoList(List<Course> courses) {
        List<CourseDto> dtos = new ArrayList<>();
        for (Course course: courses) {
            dtos.add(toCourseDto(course));
        }
        return dtos;
    }

    private Student toEntity(StudentDto dto) {
        Student student = new Student();
        student.setFirstname(dto.getFirstname());
        student.setLastname(dto.getLastname());
        student.setEmail(dto.getEmail());
        List<CourseDto> courseDtos = dto.getCourses();
        if (courseDtos == null || courseDtos.isEmpty()) return student;
        Set<Course> courses = new HashSet<>();
        for (CourseDto courseDto : courseDtos) {
            Course course = new Course();
            course.setDescription(courseDto.getDescription());
            course.setTeacher(courseDto.getTeacher());
            course.setSubject(courseDto.getSubject());
            course.setTitle(courseDto.getTitle());
            Set<Student> courseStudents = new HashSet<>();
            courseStudents.add(student);
            course.setStudents(courseStudents);
            courses.add(course);
        }
        student.setCourses(courses);
        return student;
    }
}

