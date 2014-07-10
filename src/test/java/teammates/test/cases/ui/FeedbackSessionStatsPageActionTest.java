package teammates.test.cases.ui;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import teammates.common.datatransfer.DataBundle;
import teammates.common.datatransfer.FeedbackSessionAttributes;
import teammates.common.datatransfer.InstructorAttributes;
import teammates.common.exception.UnauthorizedAccessException;
import teammates.common.util.Const;
import teammates.ui.controller.AjaxResult;
import teammates.ui.controller.FeedbackSessionStatsPageAction;
import teammates.ui.controller.FeedbackSessionStatsPageData;

public class FeedbackSessionStatsPageActionTest extends BaseActionTest {
    DataBundle dataBundle;
    
    
    @BeforeClass
    public static void classSetUp() throws Exception {
        printTestClassHeader();
        uri = Const.ActionURIs.INSTRUCTOR_FEEDBACK_STATS_PAGE;
    }

    @BeforeMethod
    public void caseSetUp() throws Exception {
        dataBundle = getTypicalDataBundle();
        restoreTypicalDataInDatastore();
    }
    
    @Test
    public void testAccessControl() throws Exception{
        InstructorAttributes instructor1OfCourse1 = dataBundle.instructors.get("instructor1OfCourse1");
        FeedbackSessionAttributes accessableFeedbackSession = dataBundle.feedbackSessions.get("session1InCourse1");
        String[] submissionParams = new String[] { Const.ParamsNames.FEEDBACK_SESSION_NAME, accessableFeedbackSession.feedbackSessionName,
                                            Const.ParamsNames.COURSE_ID, instructor1OfCourse1.courseId};
        
        verifyOnlyInstructorsOfTheSameCourseCanAccess(submissionParams);

    }
    
    @Test
    public void testExecuteAndPostProcess() throws Exception{
        
        InstructorAttributes instructor1OfCourse1 = dataBundle.instructors.get("instructor1OfCourse1");
        String instructorId = instructor1OfCourse1.googleId;
        String[] submissionParams;
        
        gaeSimulation.loginAsInstructor(instructorId);
        
        ______TS("typical: instructor accesses feedback stats of his/her course");
        FeedbackSessionAttributes accessableFeedbackSession = dataBundle.feedbackSessions.get("session1InCourse1");
        submissionParams = new String[] { Const.ParamsNames.FEEDBACK_SESSION_NAME, accessableFeedbackSession.feedbackSessionName,
                                            Const.ParamsNames.COURSE_ID, instructor1OfCourse1.courseId};
        
        FeedbackSessionStatsPageAction a = getAction(addUserIdToParams(instructorId, submissionParams));
        AjaxResult  r = (AjaxResult)a.executeAndPostProcess();
        FeedbackSessionStatsPageData data = (FeedbackSessionStatsPageData) r.data;
        
        assertEquals(Const.ViewURIs.INSTRUCTOR_FEEDBACK_STATS+"?error=false&user=idOfInstructor1OfCourse1", r.getDestinationWithParams());
        assertEquals(9,data.sessionDetails.stats.expectedTotal);
        assertEquals(4,data.sessionDetails.stats.submittedTotal);
        assertEquals("", r.getStatusMessage());
        
        ______TS("fail: instructor accesses stats of non-existent feedback session");
        String nonexistentFeedbackSession = "nonexistentFeedbackSession";
        submissionParams = new String[] { Const.ParamsNames.FEEDBACK_SESSION_NAME, nonexistentFeedbackSession,
                                          Const.ParamsNames.COURSE_ID, instructor1OfCourse1.courseId};
        boolean doesThrowUnauthorizedAccessException = false;
        String exceptionMessage = "";
        
        a = getAction(addUserIdToParams(instructorId, submissionParams));
        try {
            r = (AjaxResult)a.executeAndPostProcess();
        } catch (UnauthorizedAccessException e) {
            doesThrowUnauthorizedAccessException = true;
            exceptionMessage = e.getMessage();
        }
        
        assertEquals(true, doesThrowUnauthorizedAccessException);
        assertEquals("Trying to access system using a non-existent feedback session entity", exceptionMessage);
        assertEquals("", r.getStatusMessage());
        
    }
    
    private FeedbackSessionStatsPageAction getAction(String... params) throws Exception{
        return (FeedbackSessionStatsPageAction) (gaeSimulation.getActionObject(uri, params));
    }
}
