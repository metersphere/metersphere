package io.metersphere.job.sechedule;

import io.metersphere.commons.utils.FileUtils;
import io.metersphere.xmind.utils.FileUtil;
import org.quartz.JobExecutionContext;

import java.io.File;

public class ClearJob extends MsScheduleJob {

    @Override
    void businessExecute(JobExecutionContext context) {
        // 清理调试产生的body文件
        FileUtil.deleteDir(new File(FileUtils.BODY_FILE_DIR + "/tmp"));
    }
}
