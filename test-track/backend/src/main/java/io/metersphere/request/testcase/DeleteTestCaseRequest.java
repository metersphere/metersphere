package io.metersphere.request.testcase;

import io.metersphere.base.domain.TestCase;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DeleteTestCaseRequest extends TestCase {
    private List<String> ids;
}
