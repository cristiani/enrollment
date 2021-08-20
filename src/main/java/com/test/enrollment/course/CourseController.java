package com.test.enrollment.course;

import com.test.enrollment.exception.CourseCapacityExceeded;
import com.test.enrollment.exception.CourseNotFoundException;
import com.test.enrollment.exception.InvalidNumberException;
import com.test.enrollment.student.Student;
import com.test.enrollment.student.StudentDto;
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
public class CourseController {
    @Value( "${course_max_capacity}" )
    private Integer maxCapacity;

    @Autowired
    CourseService courseService;

    @GetMapping(value="/courses", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CourseDto>  getAllCourses(){
        return toDtoList(courseService.getAllCourses());
    }

    @GetMapping(value="/courses/filters", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CourseDto> getCoursesByFilter(@RequestParam(value="name")
                                             @NotNull @NotEmpty
                                             @ValuesAllowed(values = {
                                                     "title",
                                                     "subject",
                                                     "capacity"
                                             })
                                                     String name,
                                             @RequestParam(value="value") @NotNull @NotEmpty String value) {
        if ("title".equals(name)) return toDtoList(courseService.getCoursesByTitle(value));
        else if ("subject".equals(name)) return toDtoList(courseService.getCoursesBySubject(value));
        int capacity;
        try {
            capacity = Integer.parseInt(value);
        } catch (NumberFormatException ne) {
            throw new InvalidNumberException(ne.getMessage());
        }
        if (capacity > maxCapacity) {
            throw new CourseCapacityExceeded("Requested capacity of " + capacity +
                    " is higher than maximum accepted " + maxCapacity);
        }
        return toDtoList(courseService.getCoursesByCapacity(capacity));
    }

    @GetMapping(value="/courses/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CourseDto getCourseById(@PathVariable("id") @Min(1) @NotNull Long id) {
        return  toDto(courseService.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Course with " + id + " is Not Found!")));
    }

    @PostMapping(value="/courses", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CourseDto> addCourses(@Valid @RequestBody List<CourseDto> courses) {
        return toDtoList(courseService.addCourses(toEntityList(courses)));
    }

    //TO DO: Update list of students too
    @PutMapping(value="/courses/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CourseDto updateCourse(@PathVariable("id") @Min(1) @NotNull Long id, @Valid @RequestBody CourseDto newCourse) {
        Course course = courseService.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Course with " + id +" is Not Found!"));
        course.setTitle(newCourse.getTitle());
        course.setDescription(newCourse.getDescription());
        course.setTeacher(newCourse.getTeacher());
        course.setSubject(newCourse.getSubject());
        return toDto(courseService.save(course));
    }

    @DeleteMapping(value="/courses/{id}")
    public String deleteCourse(@PathVariable("id") @Min(1) @NotNull Long id) {
        Course std = courseService.findById(id)
                .orElseThrow(()->new CourseNotFoundException("Course with " + id + " is Not Found!"));
        courseService.deleteById(std.getId());
        return "Course with ID : " + id + " is deleted";
    }

    @GetMapping(value="/courses/empty", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CourseDto> geEmptyCourses() {
        return toDtoList(courseService.getEmptyCourses());
    }

    @GetMapping(value="/courses/{id}/students", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<StudentDto> getEnrolledStudentsPerCourse(@PathVariable("id") @Min(1) @NotNull Long id) {
        return toStudentDtoList(courseService.getEnrolledStudentsByCourse(id));
    }


    @PutMapping(value="/courses/{course_id}/students/{student_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean studentRegistration(@PathVariable("course_id") @Min(1) @NotNull Long courseId,
                                       @PathVariable("student_id") @Min(1) @NotNull Long studentId) {
        return courseService.enrollStudent(courseId, studentId);
    }

    private CourseDto toDto(Course course) {
        CourseDto dto =  new CourseDto(course.getId(), course.getTitle(), course.getSubject(), course.getTeacher(),
                course.getDescription());
        Set<Student> students = course.getStudents();
        if (students == null || students.isEmpty()) return dto;
        List<StudentDto> studentDtos = new ArrayList<>();
        for (Student student : students) {
            StudentDto studentDto = new StudentDto(student.getId(), student.getFirstname(), student.getLastname(),
                    student.getEmail());
            studentDtos.add(studentDto);
        }
        dto.setStudents(studentDtos);
        return dto;
    }

    private List<CourseDto> toDtoList(List<Course> courses) {
        List<CourseDto> dtos = new ArrayList<>();
        for (Course course: courses) {
            dtos.add(toDto(course));
        }
        return dtos;
    }

    /**
     * For this controller we are not interested in list of enrolled students
     * @param student entity
     * @return dto
     */
    private StudentDto toStudentDto(Student student) {
        return new StudentDto(student.getId(), student.getFirstname(), student.getLastname(), student.getEmail());
    }

    private List<StudentDto> toStudentDtoList(List<Student> students) {
        List<StudentDto> dtos = new ArrayList<>();
        for (Student student: students) {
            dtos.add(toStudentDto(student));
        }
        return dtos;
    }

    private Course toEntity(CourseDto dto) {
        Course course = new Course();
        course.setSubject(dto.getSubject());
        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        course.setTeacher(dto.getTeacher());
        List<StudentDto> studentDtos = dto.getStudents();
        if (studentDtos == null || studentDtos.isEmpty()) return course;
        Set<Student> students = new HashSet<>();
        for (StudentDto studentDto : studentDtos) {
            Student student = new Student();
            student.setFirstname(studentDto.getFirstname());
            student.setLastname(studentDto.getLastname());
            student.setEmail(studentDto.getEmail());
            Set<Course> studentCourses = new HashSet<>();
            studentCourses.add(course);
            student.setCourses(studentCourses);
            students.add(student);
        }
        course.setStudents(students);
        return course;
    }

    private List<Course> toEntityList (List<CourseDto> courseDtos) {
        List<Course> courses = new ArrayList<>();
        for (CourseDto courseDto: courseDtos) {
            courses.add(toEntity(courseDto));
        }
        return courses;
    }
}
