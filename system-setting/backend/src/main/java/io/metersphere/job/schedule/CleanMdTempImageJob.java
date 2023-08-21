package io.metersphere.job.schedule;

import com.fit2cloud.quartz.anno.QuartzScheduled;
import io.metersphere.service.MdFileService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 定时清理markdown临时图片，默认7天清理一次
 * @author jainxing
 */
@Component
public class CleanMdTempImageJob {

    @Resource
    private MdFileService mdFileService;

    @QuartzScheduled(cron = "0 0 6 1/7 * ?")
    public void cleanMdTempImage() {
        mdFileService.deleteTempImages();
    }
}
