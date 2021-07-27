package io.metersphere.track.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class TestPlanPreviewsDTO {

    private static final Map<Integer, Preview> previewMap = new HashMap(6);

    static {
        previewMap.put(1, new Preview(1, "基础信息", "system"));
        previewMap.put(2, new Preview(2, "测试结果", "system"));
        previewMap.put(3, new Preview(3, "测试结果分布", "system"));
        previewMap.put(4, new Preview(4, "失败用例", "system"));
        previewMap.put(5, new Preview(5, "缺陷列表", "system"));
        previewMap.put(6, new Preview(6, "自定义模块", "custom"));
    }

    public static Preview get(Integer id) {
        return previewMap.get(id);
    }

    @Getter
    @Setter
    public static class Preview {
        private String name;
        private Integer id;
        private String type;

        public Preview(Integer id, String name, String type) {
            this.name = name;
            this.id = id;
            this.type = type;
        }
    }
}
