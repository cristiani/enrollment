package com.test.enrollment.course;

import com.test.enrollment.student.Student;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.Hibernate;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Accessors(chain=true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "course",
       uniqueConstraints={
        @UniqueConstraint(columnNames = {"title", "subject"})
})
public class Course  implements Serializable {
    private static final long serialVersionUID = 2L;
    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE)
    private Long id;

    @NotEmpty(message = "Title is required")
    private String title;
    @NotEmpty(message = "Subject is required")
    private String subject;
    @NotEmpty(message = "Teacher is required")
    private String teacher;
    @Column(name = "description")
    private String description;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, mappedBy = "courses")
    private Set<Student> students = new HashSet<>();

    // cannot use lombok's as it conflicts with jpa
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Course course = (Course) o;

        //we test only these two columns because there is a uniqueness condition on them
        return new EqualsBuilder()
                .append(this.title, course.title)
                .append(this.subject, course.subject).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.title)
                .append(this.subject).toHashCode();
    }
}
