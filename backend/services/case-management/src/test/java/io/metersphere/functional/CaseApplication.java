package io.metersphere.functional;


import io.metersphere.system.config.MinioProperties;
import org.springframework.ai.model.openai.autoconfigure.*;
import org.springframework.ai.model.zhipuai.autoconfigure.ZhiPuAiChatAutoConfiguration;
import org.springframework.ai.model.zhipuai.autoconfigure.ZhiPuAiEmbeddingAutoConfiguration;
import org.springframework.ai.model.zhipuai.autoconfigure.ZhiPuAiImageAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.ldap.LdapAutoConfiguration;
import org.springframework.boot.autoconfigure.neo4j.Neo4jAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {
        QuartzAutoConfiguration.class,
        LdapAutoConfiguration.class,
        Neo4jAutoConfiguration.class,
        OpenAiModerationAutoConfiguration.class,
        OpenAiImageAutoConfiguration.class,
        OpenAiEmbeddingAutoConfiguration.class,
        OpenAiAudioSpeechAutoConfiguration.class,
        OpenAiAudioTranscriptionAutoConfiguration.class,
        OpenAiChatAutoConfiguration.class,
        ZhiPuAiChatAutoConfiguration.class,
        ZhiPuAiEmbeddingAutoConfiguration.class,
        ZhiPuAiImageAutoConfiguration.class,

})
@EnableConfigurationProperties({
        MinioProperties.class
})
@ServletComponentScan
@ComponentScan(basePackages = {"io.metersphere.sdk", "io.metersphere.system", "io.metersphere.project", "io.metersphere.functional"})
public class CaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(CaseApplication.class, args);
    }
}
