package io.metersphere.sechedule;

import io.metersphere.commons.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;

public class ClearJob extends MsScheduleJob {

    @Override
    protected void businessExecute(JobExecutionContext context) {
        // 清理调试产生的body文件
        FileUtils.deleteDir(StringUtils.join(FileUtils.BODY_FILE_DIR, "/tmp"));
    }
}
