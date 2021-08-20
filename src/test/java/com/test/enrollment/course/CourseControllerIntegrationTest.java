package com.test.enrollment.course;

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

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
public class CourseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseController courseController;

    @MockBean
    private CourseService courseService;

    private List<CourseDto>  courseDtos;
    private List<Course>  courses;
    private Course first, second;

    @BeforeEach
    void setUp() {
        CourseDto firstDto = new CourseDto(1L, "Databases", "Human knowledge", "John Travolta", "Introduction to databases");
        CourseDto secondDto = new CourseDto(2L, "Carpentry", "Human skills", "Mary Scott", "Introduction to carpentry");
        CourseDto thirdDto = new CourseDto(3L, "Chemistry", "Human knowledge", "Marie Curie", "Introduction to chemistry");
        courseDtos = new ArrayList<>();
        courseDtos.add(firstDto);
        courseDtos.add(secondDto);
        courseDtos.add(thirdDto);

        first = new Course();
        first.setId(1L);
        first.setSubject("Human knowledge");
        first.setTitle("Databases");
        first.setTeacher("John Travolta");
        first.setDescription("Introduction to databases");
        second = new Course();
        second.setId(2L);
        second.setSubject("Human skills");
        second.setTitle("Carpentry");
        second.setTeacher("Mary Scott");
        second.setDescription("Introduction to carpentry");
        Course third = new Course();
        third.setId(3L);
        third.setSubject("Human knowledge");
        third.setTitle("Chemistry");
        third.setTeacher("Marie Curie");
        third.setDescription("Introduction to chemistry");
        courses = new ArrayList<>();
        courses.add(first);
        courses.add(second);
        courses.add(third);
    }

    @Test
    public void contextLoads() throws Exception {
        assertThat(courseController).isNotNull();
    }

    @Test
    public void findAll_ShouldReturnAllFoundCourses() throws Exception {
        when(courseService.getAllCourses()).thenReturn(courses);

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].description", is("Introduction to databases")))
                .andExpect(jsonPath("$[0].title", is("Databases")))
                .andExpect(jsonPath("$[0].subject", is("Human knowledge")))
                .andExpect(jsonPath("$[0].teacher", is("John Travolta")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].description", is("Introduction to carpentry")))
                .andExpect(jsonPath("$[1].title", is("Carpentry")))
                .andExpect(jsonPath("$[1].subject", is("Human skills")))
                .andExpect(jsonPath("$[1].teacher", is("Mary Scott")))
                .andExpect(jsonPath("$[2].id", is(3)))
                .andExpect(jsonPath("$[2].description", is("Introduction to chemistry")))
                .andExpect(jsonPath("$[2].title", is("Chemistry")))
                .andExpect(jsonPath("$[2].subject", is("Human knowledge")))
                .andExpect(jsonPath("$[2].teacher", is("Marie Curie")));

        verify(courseService, times(1)).getAllCourses();
        verifyNoMoreInteractions(courseService);
    }

    @Test
    public void findAll_ShouldReturnAllFoundSubjectFilteredCourses() throws Exception {
        courses.remove(second);
        when(courseService.getCoursesBySubject("Human knowledge")).thenReturn(courses);

        mockMvc.perform(get("/api/courses/filters")
                .param("name", "subject")
                .param("value","Human knowledge"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].description", is("Introduction to databases")))
                .andExpect(jsonPath("$[0].title", is("Databases")))
                .andExpect(jsonPath("$[1].id", is(3)))
                .andExpect(jsonPath("$[1].description", is("Introduction to chemistry")))
                .andExpect(jsonPath("$[1].title", is("Chemistry")));


        verify(courseService, times(1)).getCoursesBySubject("Human knowledge");
        verifyNoMoreInteractions(courseService);
    }

    @Test
    public void findAll_ShouldReturnAllFoundCapacityFilteredCourses() throws Exception {
        when(courseService.getCoursesByCapacity(0)).thenReturn(courses);

        mockMvc.perform(get("/api/courses/filters")
                        .param("name", "capacity")
                        .param("value","0"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)));

        verify(courseService, times(1)).getCoursesByCapacity(0);
        verifyNoMoreInteractions(courseService);
    }

    @Test
    public void findAll_ShouldReturnSpecificCourses() throws Exception {
        Optional<Course> courseOptional = Optional.of(first);
        when(courseService.findById(1l)).thenReturn(courseOptional);

        mockMvc.perform(get("/api/courses/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("id", is(1)))
                .andExpect(jsonPath("description", is("Introduction to databases")))
                .andExpect(jsonPath("title", is("Databases")))
                .andExpect(jsonPath("subject", is("Human knowledge")));

        verify(courseService, times(1)).findById(1l);
        verifyNoMoreInteractions(courseService);
    }
}
