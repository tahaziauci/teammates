package teammates.logic.core;

import org.junit.Before;
import org.junit.Test;

import teammates.common.datatransfer.attributes.StudentAttributes;
import org.junit.*;
import teammates.common.exception.EntityAlreadyExistsException;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.exception.InvalidParametersException;

import java.util.Collections;

import static org.mockito.Mockito.*;

public class StudentLogicMockitoTest extends BaseLogicTest{
    private StudentsLogic studentsLogic;
    private CoursesLogic coursesLogic;
    String courseId = "";
    String teamName= "";
    StudentAttributes studentData;

    @Before
    public void setup() throws EntityAlreadyExistsException, InvalidParametersException {
        studentsLogic = mock(StudentsLogic.class);
        coursesLogic = CoursesLogic.inst();

        courseId = "idOfTypicalCourse1";
        teamName = "Team 1.2";

        studentData = StudentAttributes
                .builder(courseId, "new@gmail.com")
                .withName("name")
                .withGoogleId("googleIdTest")
                .withSectionName("sectionName")
                .withTeamName(teamName)
                .withComment("")
                .build();
        studentsLogic.createStudent(studentData);
    }

    @Test
    public void testStudentTeamsExistsInCourse() throws EntityDoesNotExistException {
        when(studentsLogic.getStudentsForCourse(courseId)).thenReturn(Collections.singletonList(studentData));
        Assert.assertTrue(coursesLogic.getTeamsForCourse(courseId).contains(teamName));

    }
}