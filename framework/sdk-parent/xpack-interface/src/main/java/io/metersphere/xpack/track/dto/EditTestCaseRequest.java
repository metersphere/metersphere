package io.metersphere.xpack.track.dto;
import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.domain.TestCaseWithBLOBs;
import io.metersphere.dto.CustomFieldResourceDTO;
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
    private List<CustomFieldResourceDTO> addFields;
    private List<CustomFieldResourceDTO> editFields;
    /**
     * 复制测试用例后，要进行复制的文件Id list
     */
    private List<String> fileIds = new ArrayList<>();
    private List<List<String>> selected = new ArrayList<>();
    /**
     * 复制测试用例时原始用例ID
     */
    private String copyCaseId;
    // 是否处理附件文件
    private boolean handleAttachment = true;
    // 关联文件管理引用ID
    private List<String> relateFileMetaIds = new ArrayList<>();
    // 取消关联文件应用ID
    private List<String> unRelateFileMetaIds = new ArrayList<>();

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
