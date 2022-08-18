package io.metersphere.track.request.testcase;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestCaseExportRequest extends TestCaseBatchRequest {
  private List<TestCaseExportHeader> baseHeaders;
  private List<TestCaseExportHeader> customHeaders;
  private List<TestCaseExportHeader> otherHeaders;

  @Getter
  @Setter
  public static class TestCaseExportHeader {
    private String id;
    private String name;
  }
}
