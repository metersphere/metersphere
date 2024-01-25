package io.metersphere.functional.constants;

public interface CaseEvent {

    interface Event {
        String ASSOCIATE = "associate";
        String DISASSOCIATE = "disassociate";
        String DELETE_FUNCTIONAL_CASE = "deleteFunctionalCase";
        String DELETE_TRASH_FUNCTIONAL_CASE = "deleteTrashFunctionalCase";
        String RECOVER_FUNCTIONAL_CASE = "recoverFunctionalCase";
        String REVIEW_FUNCTIONAL_CASE = "reviewFunctionalCase";
    }

    interface Param {
        String REVIEW_ID = "reviewId";
        String CASE_IDS = "caseIds";
        String USER_ID = "userId";
        String EVENT_NAME = "eventName";
        String COUNT_MAP = "countMap";
        String STATUS_MAP = "statusMap";
    }

}
