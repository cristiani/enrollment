package com.test.enrollment.student;

import com.test.enrollment.course.CourseDto;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
public class StudentDto {
    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private List<CourseDto> courses;

    public StudentDto (Long id, String firstname, String lastname, String email) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
    }
}
