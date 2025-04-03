package io.metersphere.system.constants;

public class AIConfigConstants {
    /**
     * @Author: guoyuqi
     */
    public enum AiPermissionType {
        PUBLIC,PRIVATE; // 公有/私有
    }

    public enum AiModelType {
        LLM, VISION, AUDIO; // 大语言/视觉/音频
    }

    public enum AiOwnerType {
        ORGANIZATION, PERSONAL; // 企业/个人
    }
}
