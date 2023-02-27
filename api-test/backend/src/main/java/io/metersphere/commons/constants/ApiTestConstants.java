package io.metersphere.commons.constants;

import io.metersphere.commons.enums.ApiTestDataStatus;

import java.util.List;

public class ApiTestConstants {
    public static final String JAR_PATH = "JAR_PATH";
    public static final String ROOT = "root";
    public static final String LAST_RESULT = "last_result";
    public static final String FAKE_ERROR = "FakeError";
    public static final String STATUS = "status";

    public static final List<String> STATUS_ALL = List.of(
            ApiTestDataStatus.PREPARE.getValue(), ApiTestDataStatus.UNDERWAY.getValue(), ApiTestDataStatus.COMPLETED.getValue()
    );
}
