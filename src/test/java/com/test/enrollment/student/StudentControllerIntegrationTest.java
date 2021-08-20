package com.test.enrollment.student;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class StudentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentController studentController;


    @MockBean
    private StudentService studentService;

    private List<StudentDto> studentDtos;
    private List<Student>  students;
    private Student first, second;

    @BeforeEach
    void setUp() {
        StudentDto firstDto = new StudentDto(1L, "John", "Doe", "john.doe@aol.com");
        StudentDto secondDto = new StudentDto(2L, "Jane", "Doe", "jane.doe@aol.com");
        StudentDto thirdDto = new StudentDto(3L, "john", "wayne", "john.wayne@aol.com");
        studentDtos = new ArrayList<>();
        studentDtos.add(firstDto);
        studentDtos.add(secondDto);
        studentDtos.add(thirdDto);

        first = new Student();
        first.setId(1L);
        first.setFirstname("John");
        first.setLastname("Doe");
        first.setEmail("john.doe@aol.com");
        second = new Student();
        second.setId(2L);
        second.setFirstname("Jane");
        second.setLastname("Doe");
        second.setEmail("jane.doe@aol.com");
        Student third = new Student();
        third.setId(3L);
        third.setFirstname("John");
        third.setLastname("Wayne");
        third.setEmail("john.wayne@aol.com");
        students = new ArrayList<>();
        students.add(first);
        students.add(second);
        students.add(third);
    }

    @Test
    public void contextLoads() throws Exception {
        assertThat(studentController).isNotNull();
    }

    @Test
    public void findAll_ShouldReturnAllFoundCourses() throws Exception {
        when(studentService.getAllStudents()).thenReturn(students);

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].firstname", is("John")))
                .andExpect(jsonPath("$[0].lastname", is("Doe")))
                .andExpect(jsonPath("$[0].email", is("john.doe@aol.com")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].firstname", is("Jane")))
                .andExpect(jsonPath("$[1].lastname", is("Doe")))
                .andExpect(jsonPath("$[1].email", is("jane.doe@aol.com")))
                .andExpect(jsonPath("$[2].id", is(3)))
                .andExpect(jsonPath("$[2].firstname", is("John")))
                .andExpect(jsonPath("$[2].lastname", is("Wayne")))
                .andExpect(jsonPath("$[2].email", is("john.wayne@aol.com")));

        verify(studentService, times(1)).getAllStudents();
        verifyNoMoreInteractions(studentService);
    }

    @Test
    public void findAll_ShouldReturnAllFoundLastnameFilteredStudents() throws Exception {
        students.remove(first);
        students.remove(second);
        when(studentService.getStudentsByLastname("Wayne")).thenReturn(students);

        mockMvc.perform(get("/api/students/filters")
                        .param("name", "lastname")
                        .param("value","Wayne"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(3)))
                .andExpect(jsonPath("$[0].firstname", is("John")))
                .andExpect(jsonPath("$[0].lastname", is("Wayne")))
                .andExpect(jsonPath("$[0].email", is("john.wayne@aol.com")));


        verify(studentService, times(1)).getStudentsByLastname("Wayne");
        verifyNoMoreInteractions(studentService);
    }

    @Test
    public void findAll_ShouldReturnAllFoundCapacityFilteredCourses() throws Exception {
        when(studentService.getStudentsByEnrollmentCapacity(0)).thenReturn(students);

        mockMvc.perform(get("/api/students/filters")
                        .param("name", "capacity")
                        .param("value","0"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)));

        verify(studentService, times(1)).getStudentsByEnrollmentCapacity(0);
        verifyNoMoreInteractions(studentService);
    }

    @Test
    public void findAll_ShouldReturnSpecificCourses() throws Exception {
        Optional<Student> courseOptional = Optional.of(first);
        when(studentService.findById(1l)).thenReturn(courseOptional);

        mockMvc.perform(get("/api/students/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("id", is(1)))
                .andExpect(jsonPath("firstname", is("John")))
                .andExpect(jsonPath("lastname", is("Doe")))
                .andExpect(jsonPath("email", is("john.doe@aol.com")));

        verify(studentService, times(1)).findById(1l);
        verifyNoMoreInteractions(studentService);
    }
}

