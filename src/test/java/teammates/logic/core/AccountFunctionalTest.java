package teammates.logic.core;

import org.testng.annotations.Test;

import teammates.common.datatransfer.InstructorPrivileges;
import teammates.common.datatransfer.attributes.InstructorAttributes;
import teammates.common.datatransfer.attributes.StudentAttributes;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.util.Const;
import teammates.storage.api.InstructorsDb;
import teammates.storage.api.StudentsDb;

/**
 * SUT: {@link AccountFunctionalTest}.
 */
public class AccountFunctionalTest extends BaseLogicTest {
    private final StudentsDb studentsDb = StudentsDb.inst();
    private final InstructorsLogic instructorsLogic = InstructorsLogic.inst();
    private final InstructorsDb instructorsDb = InstructorsDb.inst();
    private final AccountsLogic accountsLogic = AccountsLogic.inst();

    @Test
    public void testCreateAndDeleteStudent() throws Exception {

        StudentAttributes s = StudentAttributes
                .builder("course id", "valid-fresh@email.com")
                .withName("valid student")
                .withComment("")
                .withTeamName("validTeamName")
                .withSectionName("validSectionName")
                .withGoogleId("validGoogleId")
                .build();

        ______TS("success : valid params");
        s.setCourse("valid-course");

        studentsDb.deleteStudent(s.getCourse(), s.getEmail());

        studentsDb.createEntity(s);
        verifyPresentInDatabase(s);
        assertNull(studentsDb.getStudentForGoogleId(s.getCourse() + "not existing", s.getGoogleId()));
        assertNull(studentsDb.getStudentForGoogleId(s.getCourse(), s.getGoogleId() + "not existing"));
        assertNull(studentsDb.getStudentForGoogleId(s.getCourse() + "not existing", s.getGoogleId() + "not existing"));

        ______TS("success: delete student");
        studentsDb.deleteStudent(s.getCourse(), s.getEmail());
        verifyAbsentInDatabase(s);

        ______TS("null params check");
        assertThrows(AssertionError.class, () -> studentsDb.createEntity(null));

        studentsDb.deleteStudent(s.getCourse(), s.getEmail());
    }

    @Test
    public void checkStudentStatus() throws Exception {
        StudentAttributes s = StudentAttributes
                .builder("valid-course", "valid@email.com")
                .withName("valid student")
                .withComment("")
                .withTeamName("validTeamName")
                .withSectionName("validSectionName")
                .withGoogleId("")
                .build();

        studentsDb.deleteStudent(s.getCourse(), s.getEmail());
        studentsDb.createEntity(s);

        ______TS("success: check student status by course enrollment");
        assertEquals(1, studentsDb.getStudentsForCourse(s.getCourse()).size());

        // clean up
        studentsDb.deleteStudent(s.getCourse(), s.getEmail());

        ______TS("failure: null params check");
        StudentAttributes[] finalStudent = new StudentAttributes[] {s};
        assertThrows(AssertionError.class,
                () -> studentsDb.deleteStudent(null, finalStudent[0].getEmail()));

        assertThrows(AssertionError.class,
                () -> studentsDb.deleteStudent(finalStudent[0].getCourse(), null));
    }

    @Test
    public void testCreateAndDeleteInstructor() throws Exception {
        ______TS("success: add an instructor");

        String courseId = "testing-course";
        String name = "My Instructor";
        String email = "test.instr@email.tmt";
        String role = Const.InstructorPermissionRoleNames.INSTRUCTOR_PERMISSION_ROLE_COOWNER;
        String displayedName = Const.DEFAULT_DISPLAY_NAME_FOR_INSTRUCTOR;
        InstructorPrivileges privileges =
                new InstructorPrivileges(Const.InstructorPermissionRoleNames.INSTRUCTOR_PERMISSION_ROLE_COOWNER);
        InstructorAttributes instr = InstructorAttributes.builder(courseId, email)
                .withName(name)
                .withRole(role)
                .withDisplayedName(displayedName)
                .withPrivileges(privileges)
                .build();

        instructorsLogic.createInstructor(instr);

        verifyPresentInDatabase(instr);

        ______TS("success: delete instructor");
        instructorsDb.deleteInstructor(instr.getCourseId(), instr.getEmail());
        verifyAbsentInDatabase(instr);

        ______TS("failure: null parameters");
        assertThrows(AssertionError.class, () -> instructorsLogic.createInstructor(null));
    }

    @Test
    public void makeStudentAnInstructor() throws Exception {
        //using an already made student
        ______TS("success: test make account instructor");
        accountsLogic.makeAccountInstructor("student2InCourse1");
        assertTrue(accountsLogic.isAccountAnInstructor("student2InCourse1"));

        ______TS("success: test change status back to student");
        accountsLogic.downgradeInstructorToStudentCascade("student2InCourse1");
        assertFalse(accountsLogic.isAccountAnInstructor("student2InCourse1"));

        ______TS("failure: test for bad id");
        assertThrows(EntityDoesNotExistException.class, () -> {
            accountsLogic.makeAccountInstructor("id-does-not-exist");
        });
    }

    @Test
    public void makeInstructorAStudent() throws Exception {
        //using an already made Instructor
        ______TS("success: test account is instructor");
        assertTrue(accountsLogic.isAccountAnInstructor("idOfInstructor2OfCourse1"));

        ______TS("success: test change account status to student");
        accountsLogic.downgradeInstructorToStudentCascade("idOfInstructor2OfCourse1");
        assertFalse(accountsLogic.isAccountAnInstructor("idOfInstructor2OfCourse1"));

        ______TS("success: test make back to instructor");
        accountsLogic.makeAccountInstructor("idOfInstructor2OfCourse1");
        assertTrue(accountsLogic.isAccountAnInstructor("idOfInstructor2OfCourse1"));

        ______TS("failure: test for bad id");
        assertThrows(EntityDoesNotExistException.class, () -> {
            accountsLogic.makeAccountInstructor("id-does-not-exist");
        });
    }

    @Test
    public void checkInstructorStatus() throws Exception {
        ______TS("success: check status is instructor");
        assertTrue(accountsLogic.isAccountAnInstructor("idOfInstructor2OfCourse1"));

        ______TS("success: student should not have instructor status");
        assertFalse(accountsLogic.isAccountAnInstructor("student2InCourse1"));
    }
}
