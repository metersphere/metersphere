package io.metersphere.request.testcase;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestCaseExportRequest extends TestCaseBatchRequest {
  private List<TestCaseExportHeader> baseHeaders;
  private List<TestCaseExportHeader> customHeaders;
  private List<TestCaseExportHeader> otherHeaders;

  /**
   * exportAll: 导出全部或者批量导出(v2.6)
   */
  private Boolean exportAll = false;

  @Getter
  @Setter
  public static class TestCaseExportHeader {
    private String id;
    private String name;
  }
}
