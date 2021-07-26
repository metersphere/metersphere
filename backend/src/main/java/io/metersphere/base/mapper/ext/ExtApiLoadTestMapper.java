package io.metersphere.base.mapper.ext;

public interface ExtApiLoadTestMapper {

    int countNeedUpdateApiCase(String loadTestId);

    int countNeedUpdateApiScenario(String loadTestId);
}
