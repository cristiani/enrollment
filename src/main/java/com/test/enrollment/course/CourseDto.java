package com.test.enrollment.course;

import com.test.enrollment.student.StudentDto;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
public class CourseDto {
    private Long id;
    private String title;
    private String subject;
    private String teacher;
    private String description;
    private List<StudentDto> students;

    public CourseDto (Long id, String title, String subject, String teacher, String description) {
        this.id = id;
        this.title = title;
        this.subject= subject;
        this.description = description;
        this.teacher = teacher;
    }
}
