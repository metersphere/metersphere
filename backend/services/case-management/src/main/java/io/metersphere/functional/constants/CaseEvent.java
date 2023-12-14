package io.metersphere.functional.constants;

public interface CaseEvent {

    interface Event {
        String ASSOCIATE = "associate";
        String DISASSOCIATE = "disassociate";
        String BATCH_DISASSOCIATE = "batchDisassociate";
        String DELETE_FUNCTIONAL_CASE = "deleteFunctionalCase";
        String DELETE_TRASH_FUNCTIONAL_CASE = "deleteTrashFunctionalCase";
        String RECOVER_FUNCTIONAL_CASE = "recoverFunctionalCase";
    }

    interface Param {
        String REVIEW_ID = "reviewId";
        String CASE_IDS = "caseIds";
        String PASS_COUNT = "passCount";
        String CASE_COUNT = "caseCount";

    }

}
