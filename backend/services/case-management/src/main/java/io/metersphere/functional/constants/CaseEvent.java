package io.metersphere.functional.constants;

public interface CaseEvent {

    interface Event {
        String ASSOCIATE = "associate";
        String DISASSOCIATE = "disassociate";
        String BATCH_DISASSOCIATE = "batchDisassociate";
        String DELETE_FUNCTIONAL_CASE = "deleteFunctionalCase";
        String DELETE_TRASH_FUNCTIONAL_CASE = "deleteTrashFunctionalCase";
        String RECOVER_FUNCTIONAL_CASE = "recoverFunctionalCase";
        String REVIEW_FUNCTIONAL_CASE = "reviewFunctionalCase";
    }

    interface Param {
        String REVIEW_ID = "reviewId";
        String CASE_IDS = "caseIds";
        String PASS_COUNT = "passCount";
        String CASE_COUNT = "caseCount";
        String STATUS = "status";
        String USER_ID = "userId";
        String UN_COMPLETED_COUNT = "unCompletedCount";
        String EVENT_NAME = "eventName";
    }

}
