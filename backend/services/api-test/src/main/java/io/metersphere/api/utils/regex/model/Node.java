package io.metersphere.api.utils.regex.model;

import io.metersphere.api.utils.regex.exception.*;

public interface Node {

    String getExpression();

    String random() throws UninitializedException, RegexpIllegalException;

    boolean test();

    void init() throws RegexpIllegalException, TypeNotMatchException;

    boolean isInitialized();
}
