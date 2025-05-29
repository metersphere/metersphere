package io.metersphere.ai.engine.common;

import lombok.Data;

@Data
public class AIChatOptions {
    private String baseUrl;
    private String apiKey;
    private String modelType;
    // 关闭日志顾问，默认开启，true 关闭
    private boolean disableLoggingAdvisor;
    // todo add more options
    // 用来控制生成的完成的表观创造性的采样温度。较高的值将使输出更加随机，而较低的值将使结果更加集中和确定。不建议修改同一完成请求的温度和 top_p，因为这两个设置的相互作用很难预测。
    private double temperature;
    // 介于 -2.0 和 2.0 之间的数字。正值会根据新标记在文本中出现的频率对其进行惩罚，从而降低模型逐字重复同一行的可能性。
    private double frequencyPenalty;
    // 聊天完成中生成的最大标记数。输入标记和生成的标记的总长度受模型的上下文长度限制。
    private Integer maxTokens;
    // 另一种使用温度进行采样的替代方法称为核采样，其中模型会考虑概率质量排名前 p 位的 token 的结果。因此，0.1 表示仅考虑概率质量排名前 10% 的 token。我们通常建议更改此值或温度，但不要同时更改两者。
    private double topP;


}
