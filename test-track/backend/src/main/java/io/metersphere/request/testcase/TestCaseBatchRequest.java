package io.metersphere.request.testcase;

import io.metersphere.base.domain.TestCaseWithBLOBs;
import io.metersphere.dto.CustomFieldResourceDTO;
import io.metersphere.request.OrderRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestCaseBatchRequest extends TestCaseWithBLOBs {
    private List<String> ids;
    private List<OrderRequest> orders;
    private String projectId;
    private CustomFieldResourceDTO customField;
    private QueryTestCaseRequest condition;
    private String customTemplateFieldId;
    private List<String> tagList;
    private boolean appendTag = false;

    /**
     * exportAll: 导出全部或者批量导出(v2.6)
     */
    private Boolean exportAll = false;

    /**
     * v2.9 大批量导出分页参数
     * pageStart: 偏移量
     * pageCount: 数目
     */
    private int pageStart = 0;

    private int pageCount = 0;

    @Getter
    @Setter
    public static class CustomFiledRequest {
        private String id;
        private String name;
        private Object value;
    }
}
