package io.metersphere.track.request.testcase;
import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.TestCaseWithBLOBs;
import io.metersphere.base.domain.ext.CustomFieldResource;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class EditTestCaseRequest extends TestCaseWithBLOBs {
    private List<FileMetadata> updatedFileList;
    private List<String> follows;
    private OtherInfoConfig otherInfoConfig;
    private String oldDataId;
    private List<CustomFieldResource> addFields;
    private List<CustomFieldResource> editFields;
    /**
     * 复制测试用例后，要进行复制的文件Id list
     */
    private List<String> fileIds = new ArrayList<>();
    private List<List<String>> selected = new ArrayList<>();

    /**
     * 创建新版本时 是否连带复制其他信息的配置类
     */
    @Getter
    @Setter
    public static class OtherInfoConfig {
        //是否复制备注
        private boolean remark;
        //是否复制关联测试
        private boolean relateTest;
        //是否复制关联需求
        private boolean relateDemand;
        //是否复制关联缺陷
        private boolean relateIssue;
        //是否复制附件
        private boolean archive;
        //是否复制依赖
        private boolean dependency;

        public static OtherInfoConfig createDefault() {
            OtherInfoConfig o = new OtherInfoConfig();
            o.setArchive(true);
            o.setRemark(true);
            o.setRelateTest(true);
            o.setDependency(true);
            o.setRelateDemand(true);
            o.setRelateIssue(true);
            return o;
        }
    }
}
