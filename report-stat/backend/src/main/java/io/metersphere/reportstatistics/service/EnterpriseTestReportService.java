package io.metersphere.reportstatistics.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.EnterpriseTestReportMapper;
import io.metersphere.base.mapper.EnterpriseTestReportSendRecordMapper;
import io.metersphere.base.mapper.ext.BaseUserGroupMapper;
import io.metersphere.base.mapper.ext.ExtEnterpriseTestReportMapper;
import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.commons.constants.ScheduleStatus;
import io.metersphere.commons.constants.ScheduleType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.dto.UserDTO;
import io.metersphere.dto.UserGroupInfoDTO;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.reportstatistics.EnterpriseTestReportReference;
import io.metersphere.notice.domain.Receiver;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.sender.impl.MailNoticeSender;
import io.metersphere.reportstatistics.dto.EnterpriseReportContentStep;
import io.metersphere.reportstatistics.dto.request.EnterpriseTestReportRequest;
import io.metersphere.reportstatistics.dto.request.emun.EnterpriseReportStatus;
import io.metersphere.reportstatistics.dto.response.EnterpriseTestReportDTO;
import io.metersphere.reportstatistics.dto.response.UserGroupResponse;
import io.metersphere.reportstatistics.job.SendReportJob;
import io.metersphere.reportstatistics.utils.EmailPageInfoUtil;
import io.metersphere.reportstatistics.utils.JSONUtil;
import io.metersphere.reportstatistics.utils.ScheduleUtil;
import io.metersphere.request.ScheduleRequest;
import io.metersphere.service.BaseScheduleService;
import io.metersphere.service.BaseUserService;
import jakarta.annotation.Resource;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class EnterpriseTestReportService {
    @Resource
    private EnterpriseTestReportMapper enterpriseTestReportMapper;
    @Resource
    private ExtEnterpriseTestReportMapper extEnterpriseTestReportMapper;
    @Resource
    private EnterpriseTestReportSendRecordMapper enterpriseTestReportSendRecordMapper;
    @Resource
    private MailNoticeSender mailNoticeSender;
    @Resource
    private BaseUserGroupMapper baseUserGroupMapper;
    @Resource
    private BaseScheduleService baseScheduleService;
    @Resource
    private BaseUserService baseUserService;
    @Resource
    private ReportStatisticsService reportStatisticsService;

    public String getLogDetails(EnterpriseTestReportRequest request) {
        List<EnterpriseTestReport> reportList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(request.getIds())) {
            EnterpriseTestReportExample example = new EnterpriseTestReportExample();
            example.createCriteria().andIdIn(request.getIds());
            reportList = enterpriseTestReportMapper.selectByExample(example);
        } else if (StringUtils.isNotEmpty(request.getId())) {
            EnterpriseTestReport report = enterpriseTestReportMapper.selectByPrimaryKey(request.getId());
            if (report != null) {
                reportList.add(report);
            }
        }
        if (!CollectionUtils.isEmpty(reportList)) {
            List<String> idList = new ArrayList<>();
            List<String> nameList = new ArrayList<>();
            reportList.forEach(el -> {
                idList.add(el.getId());
                nameList.add(el.getName());
            });
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(idList), reportList.get(0).getProjectId(), String.join(",", nameList), reportList.get(0).getCreateUser(), new LinkedList<>());
            return JSON.toJSONString(details);
        } else {
            return null;
        }
    }

    public String getLogDetails(String id) {
        EnterpriseTestReport report = enterpriseTestReportMapper.selectByPrimaryKey(id);
        if (report != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(report, EnterpriseTestReportReference.enterpriseTestReportColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(id), report.getProjectId(), report.getName(), report.getCreateUser(), columns);
            return JSON.toJSONString(details);
        } else {
            return null;
        }
    }

    public List<EnterpriseTestReport> list(EnterpriseTestReportRequest request) {
        this.initSelectRequest(request);
        if (MapUtils.isNotEmpty(request.getFilters())) {
            return extEnterpriseTestReportMapper.selectByRequest(request);
        } else {
            EnterpriseTestReportExample example = new EnterpriseTestReportExample();
            if (StringUtils.isNotEmpty(request.getName())) {
                example.createCriteria().andNameLike(request.getName());
            }
            if (StringUtils.isNotEmpty(request.getProjectId())) {
                example.createCriteria().andProjectIdEqualTo(request.getProjectId());
            }
            example.setOrderByClause(" create_time DESC");
            return enterpriseTestReportMapper.selectByExample(example);
        }
    }

    private void initSelectRequest(EnterpriseTestReportRequest request) {
        if (MapUtils.isNotEmpty(request.getFilters())) {
            if (request.getFilters().containsKey("schedule_status") && CollectionUtils.isNotEmpty(request.getFilters().get("schedule_status"))) {
                if (CollectionUtils.isNotEmpty(request.getFilters().get("schedule_status"))
                        && request.getFilters().get("schedule_status").containsAll(new ArrayList<String>() {{
                    this.add("OPEN");
                    this.add("SHUT");
                    this.add("NOTSET");
                }})) {
                    //查询条件全都存在既意味着不需要查询这一项
                    request.getFilters().remove("schedule_status");
                }
            }
        }
    }

    public EnterpriseTestReport save(EnterpriseTestReportWithBLOBs request) {
        request.setId(UUID.randomUUID().toString());
        if (StringUtils.isEmpty(request.getCreateUser())) {
            request.setCreateUser(SessionUtils.getUserId());
            request.setUpdateUser(SessionUtils.getUserId());
        }
        long time = System.currentTimeMillis();
        request.setCreateTime(time);
        request.setUpdateTime(time);

        boolean sendSuccess = false;
        if (StringUtils.isEmpty(request.getStatus())) {
            request.setStatus(EnterpriseReportStatus.NEW.name());
        } else if (StringUtils.equalsIgnoreCase(request.getStatus(), "send")) {
            request.setStatus(EnterpriseReportStatus.SENDED.name());
            try {
                this.sendEmail(request, false);
                request.setLastSendTime(time);
                sendSuccess = true;
            } catch (Exception e) {
                request.setStatus(EnterpriseReportStatus.SEND_FAILED.name());
                LogUtil.error(e);
            }
        }
        enterpriseTestReportMapper.insert(request);
        if (sendSuccess) {
            this.insertEnterpriseSendRecord(request);
        }
        return request;
    }

    public EnterpriseTestReport send(EnterpriseTestReportWithBLOBs param) {
        EnterpriseTestReportWithBLOBs bloBs = enterpriseTestReportMapper.selectByPrimaryKey(param.getId());
        boolean sendSuccess = false;
        if (bloBs != null) {
            try {
                this.sendEmail(bloBs, true);
                bloBs.setStatus(EnterpriseReportStatus.SENDED.name());
                bloBs.setLastSendTime(System.currentTimeMillis());
                sendSuccess = true;
            } catch (Exception e) {
                bloBs.setStatus(EnterpriseReportStatus.SEND_FAILED.name());
                LogUtil.error("Send email error!", e);
                MSException.throwException("Send email error!");
            }
            enterpriseTestReportMapper.updateByPrimaryKeySelective(bloBs);
            if (sendSuccess) {
                this.insertEnterpriseSendRecord(bloBs);
            }
        }
        return bloBs;
    }

    public EnterpriseTestReport update(EnterpriseTestReportWithBLOBs request) {
        if (StringUtils.isEmpty(request.getId())) {
            return null;
        }
        if (StringUtils.isEmpty(request.getUpdateUser())) {
            request.setUpdateUser(SessionUtils.getUserId());
        }
        long time = System.currentTimeMillis();
        request.setUpdateTime(time);

        boolean sendSuccess = false;
        if (StringUtils.equalsIgnoreCase(request.getStatus(), "send")) {
            request.setStatus(EnterpriseReportStatus.SENDED.name());
            try {
                this.sendEmail(request, false);
                request.setLastSendTime(time);
                sendSuccess = true;
            } catch (Exception e) {
                request.setStatus(EnterpriseReportStatus.SEND_FAILED.name());
                LogUtil.error(e);
            }
        }
        enterpriseTestReportMapper.updateByPrimaryKeySelective(request);
        if (sendSuccess) {
            this.insertEnterpriseSendRecord(request);
        }
        return request;
    }

    private void insertEnterpriseSendRecord(EnterpriseTestReportWithBLOBs request) {
        EnterpriseTestReportSendRecordWithBLOBs sendRecord = new EnterpriseTestReportSendRecordWithBLOBs();
        sendRecord.setDuplicated(request.getDuplicated());
        sendRecord.setAddressee(request.getAddressee());
        sendRecord.setCreateTime(request.getLastSendTime());
        sendRecord.setCreateUser(request.getUpdateUser());
        sendRecord.setReportContent(request.getReportContent());
        sendRecord.setEnterpriseTestReportId(request.getId());
        sendRecord.setId(UUID.randomUUID().toString());
        enterpriseTestReportSendRecordMapper.insert(sendRecord);

    }

    public EnterpriseTestReportWithBLOBs get(String id) {
        return enterpriseTestReportMapper.selectByPrimaryKey(id);
    }

    public void deleteByRequest(EnterpriseTestReportRequest request) {
        if (request.isSelectAll() || !CollectionUtils.isEmpty(request.getIds())) {
            this.initAllIds(request);
            this.delete(request.getIds());
        } else if (StringUtils.isNotEmpty(request.getId())) {
            this.delete(request.getId());
        }
    }

    private void initAllIds(EnterpriseTestReportRequest request) {
        this.initSelectRequest(request);
        request.setIds(this.extEnterpriseTestReportMapper.selectIdByRequest(request));
        if (CollectionUtils.isNotEmpty(request.getUnSelectIds()) && CollectionUtils.isNotEmpty(request.getIds())) {
            request.getIds().removeAll(request.getUnSelectIds());
        }
    }

    private void delete(List<String> ids) {
        if (!CollectionUtils.isEmpty(ids)) {
            EnterpriseTestReportSendRecordExample sendRecordExample = new EnterpriseTestReportSendRecordExample();
            sendRecordExample.createCriteria().andEnterpriseTestReportIdIn(ids);
            this.enterpriseTestReportSendRecordMapper.deleteByExample(sendRecordExample);

            EnterpriseTestReportExample example = new EnterpriseTestReportExample();
            example.createCriteria().andIdIn(ids);
            this.enterpriseTestReportMapper.deleteByExample(example);

            //删除定时任务
            ids.forEach(id -> {
                baseScheduleService.deleteByResourceId(id, ScheduleGroup.SCHEDULE_SEND_REPORT.name());
            });
        }
    }

    private void delete(String id) {
        this.enterpriseTestReportMapper.deleteByPrimaryKey(id);
    }

    public void copy(EnterpriseTestReportRequest request) {
        if (StringUtils.isNotEmpty(request.getId())) {
            EnterpriseTestReportWithBLOBs reCopyData = this.enterpriseTestReportMapper.selectByPrimaryKey(request.getId());
            if (reCopyData != null) {
                EnterpriseTestReportWithBLOBs newData = reCopyData;
                newData.setName("COPY_" + reCopyData.getName());
                newData.setId(UUID.randomUUID().toString());
                if (StringUtils.isEmpty(reCopyData.getCreateUser())) {
                    newData.setCreateUser(SessionUtils.getUserId());
                    newData.setUpdateUser(SessionUtils.getUserId());
                }
                long time = System.currentTimeMillis();
                newData.setCreateTime(time);
                newData.setUpdateTime(time);
                newData.setStatus(EnterpriseReportStatus.NEW.name());
                newData.setLastSendTime(null);
                newData.setSendFreq(null);
                newData.setSendCron(null);
                this.enterpriseTestReportMapper.insert(newData);
            }
        }
    }

    public void sendEmail(String resourceId, boolean isSchedule) throws Exception {
        EnterpriseTestReportWithBLOBs bloBs = enterpriseTestReportMapper.selectByPrimaryKey(resourceId);
        this.sendEmail(bloBs, isSchedule);
    }

    public void sendEmail(EnterpriseTestReportWithBLOBs report, boolean isSchedule) throws Exception {
        if (report == null) {
            return;
        }
        URL resource3 = this.getClass().getResource("/template/enterprise/mail/enterprise-report.html");
        String context = IOUtils.toString(resource3, StandardCharsets.UTF_8);
        String reportContent = this.genReportContent(report, isSchedule);
        context = StringUtils.replace(context, "#{reportData}", reportContent);
        String subject = report.getName();

        List<Receiver> list = new ArrayList<>();
        if (StringUtils.isNotEmpty(report.getAddressee())) {
            List<String> userEmails = JSON.parseArray(report.getAddressee(), String.class);
            userEmails.forEach(item -> {
                Receiver receiver = new Receiver(item, "EMAIL");
                list.add(receiver);
            });
        }
        List<Receiver> ccList = new ArrayList<>();
        if (StringUtils.isNotEmpty(report.getDuplicated())) {
            List<String> userEmails = JSON.parseArray(report.getDuplicated(), String.class);
            userEmails.forEach(item -> {
                Receiver receiver = new Receiver(item, "EMAIL");
                ccList.add(receiver);
            });
        }

        if (list.isEmpty()) {
            return;
        }
        NoticeModel noticeModel = NoticeModel.builder()
                .subject(subject)
                .receivers(list)
                .recipients(ccList)
                .build();
        mailNoticeSender.sendExternalMail(context, noticeModel);
    }

    private String formatPreviewContent(String previewContent) {
        previewContent = StringUtils.replace(previewContent, "class=\"hljs-center\"", "style=\"text-align: center\"");
        previewContent = StringUtils.replace(previewContent, "class=\"hljs-right\"", "style=\"text-align: right\"");
        previewContent = StringUtils.replace(previewContent, "class=\"hljs-left\"", "style=\"text-align: left\"");
        return previewContent;
    }

    private boolean isReportStep(String stepType) {
        return StringUtils.equalsIgnoreCase(stepType, EmailPageInfoUtil.STEP_TYPE_REPORT);
    }

    private boolean isRichTextStep(String stepType) {
        return StringUtils.equalsIgnoreCase(stepType, EmailPageInfoUtil.STEP_TYPE_TXT);
    }

    private String genReportContent(EnterpriseTestReportWithBLOBs report, boolean isSchedule) {
        StringBuilder reportContent = new StringBuilder();
        List<EnterpriseReportContentStep> stepList = JSONUtil.parseArray(report.getReportContent(), EnterpriseReportContentStep.class);
        for (EnterpriseReportContentStep step : stepList) {
            String title = EmailPageInfoUtil.generateEmailStepTitle(step.getName());
            reportContent.append(title);
            if (isRichTextStep(step.getType())) {
                String previweContent = this.formatPreviewContent(step.getPreviewContext());
                //判断富文本内容中是否含有图片
                if (StringUtils.contains(previweContent, EmailPageInfoUtil.IMG_DATA_PREFIX)
                        && StringUtils.contains(previweContent, EmailPageInfoUtil.IMG_DATA_CONTAINS)) {
                    previweContent = this.formatStepImageInfo(previweContent);
                }
                reportContent.append(previweContent);
            } else if (isReportStep(step.getType())) {
                if (StringUtils.isNotEmpty(step.getRecordImageContent())) {
                    reportContent.append(EmailPageInfoUtil.generatePicInfo(step.getReportRecordId(), step.getRecordImageContent()));
                }

                if (step.getReportRecordData() != null && step.getReportRecordData().containsKey("showTable")) {
                    reportContent.append(EmailPageInfoUtil.generateTableInfo(step.getReportRecordData()));
                }
            }
        }

        return reportContent.toString();
    }

    private String formatStepImageInfo(String previewContent) {
        String[] previewContentArr = StringUtils.splitByWholeSeparator(StringUtils.SPACE + previewContent, EmailPageInfoUtil.IMG_DATA_PREFIX);
        if (previewContentArr.length > 1) {
            for (int i = 1; i < previewContentArr.length; i++) {
                String itemStr = previewContentArr[i];
                int containsIndex = StringUtils.indexOf(itemStr, EmailPageInfoUtil.IMG_DATA_CONTAINS);
                if (containsIndex > 0) {
                    String fileNameStr = StringUtils.substring(itemStr, 0, containsIndex);
                    String decodeFileName = StringUtils.startsWith(fileNameStr, "?fileName=") ? fileNameStr.substring(10) : fileNameStr;
                    String encodedFileName = URLDecoder.decode(decodeFileName, StandardCharsets.UTF_8);
                    String dataType = StringUtils.endsWithIgnoreCase(encodedFileName, ".png") ?
                            EmailPageInfoUtil.IMG_DATA_TYPE_PNG : EmailPageInfoUtil.IMG_DATA_TYPE_JPG;
                    try {
                        String fileContent = "<img src=\"" + dataType + "base64," + this.getImageContext(encodedFileName) + "\"";
                        previewContent = StringUtils.replace(previewContent, EmailPageInfoUtil.IMG_DATA_PREFIX + fileNameStr + "\"", fileContent);
                    } catch (Exception exception) {
                        LogUtil.error(exception);
                    }

                }
            }
        }
        return previewContent;
    }

    private Map<String, String> getSyncReportMap(List<EnterpriseReportContentStep> stepList, User user, boolean isSchedule) {
        Map<String, String> returnMap = new HashMap<>();
        if (isSchedule) {
            //定时任务触发的数据，需要检查图片是否需要重新生成
            String language = null;
            if (user != null) {
                language = user.getLanguage();
            }
            List<ReportStatisticsWithBLOBs> bloBsList = new ArrayList<>();
            for (EnterpriseReportContentStep step : stepList) {
                ReportStatisticsWithBLOBs reportStatisticsWithBLOBs = reportStatisticsService.selectById(step.getReportRecordId(), false);
                if (reportStatisticsWithBLOBs != null) {
                    boolean needSyncImage = reportStatisticsService.reportHasUpdated(reportStatisticsWithBLOBs);
                    if (needSyncImage) {
                        bloBsList.add(reportStatisticsWithBLOBs);
                    }
                }
            }
            if (!CollectionUtils.isEmpty(bloBsList)) {
                returnMap = reportStatisticsService.getImageContentById(bloBsList, language);
            }
        }
        return returnMap;
    }

    public String getImageContext(String fileName) {
        File file = new File(FileUtils.MD_IMAGE_DIR + "/" + fileName);
        FileSystemResource fileSystemResource = new FileSystemResource(file);

        byte[] data = null;
        // 读取图片字节数组
        try {
            InputStream in = fileSystemResource.getInputStream();
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (Exception e) {
            LogUtil.error(e);
        }
        // 对字节数组进行Base64编码，得到Base64编码的字符串
        return org.apache.commons.codec.binary.StringUtils.newStringUsAscii(Base64.encodeBase64(data));
    }

    public List<UserGroupResponse> initUserGroupInfo(String projectId) {
        List<UserGroupInfoDTO> userGroupInfoDTOS = baseUserGroupMapper.getUserGroupInfoByProjectId(projectId);

        List<UserGroupResponse> returnList = new ArrayList<>();
        Map<String, List<UserDTO>> map = new LinkedHashMap<>();
        for (UserGroupInfoDTO dto : userGroupInfoDTOS) {
            String groupName = dto.getGroupName();
            UserDTO user = new UserDTO();
            user.setId(dto.getUserId());
            user.setName(dto.getUserName());
            user.setEmail(dto.getUserEmail());
            if (map.containsKey(groupName)) {
                map.get(groupName).add(user);
            } else {
                List<UserDTO> list = new ArrayList<>();
                list.add(user);
                map.put(groupName, list);
            }
        }
        for (Map.Entry<String, List<UserDTO>> entry : map.entrySet()) {
            UserGroupResponse response = new UserGroupResponse();
            response.setGroupName(entry.getKey());
            response.setUsers(entry.getValue());
            returnList.add(response);
        }
        return returnList;
    }

    public List<EnterpriseTestReportDTO> parseDTO(List<EnterpriseTestReport> modelList) {
        if (CollectionUtils.isEmpty(modelList)) {
            return new ArrayList<>(0);
        } else {
            List<String> userIdList = new ArrayList<>();
            List<String> idList = new ArrayList<>();
            modelList.forEach(item -> {
                idList.add(item.getId());
                if (!userIdList.contains(item.getCreateUser())) {
                    userIdList.add(item.getCreateUser());
                }
            });
            List<Schedule> scheduleByResourceIds = baseScheduleService.getScheduleByResourceIds(idList, ScheduleGroup.SCHEDULE_SEND_REPORT.name());
            Map<String, Schedule> scheduleMap = scheduleByResourceIds.stream().collect(Collectors.toMap(Schedule::getResourceId, Schedule -> Schedule));
            List<EnterpriseTestReportDTO> returnList = new ArrayList<>();
            Map<String, User> userMap = baseUserService.queryNameByIds(userIdList);
            for (EnterpriseTestReport model : modelList) {
                EnterpriseTestReportDTO dto = new EnterpriseTestReportDTO();
                BeanUtils.copyBean(dto, model);
                if (userMap.containsKey(dto.getCreateUser())) {
                    dto.setCreateUser(userMap.get(dto.getCreateUser()).getName());
                }
                Schedule schedule = scheduleMap.get(model.getId());
                if (schedule != null) {
                    dto.setScheduleId(schedule.getId());
                    if (schedule.getEnable()) {
                        dto.setScheduleIsOpen(true);
                        dto.setScheduleStatus(ScheduleStatus.OPEN.name());
                        dto.setScheduleExecuteTime(ScheduleUtil.getNextTriggerTime(schedule.getValue()));
                    } else {
                        dto.setScheduleStatus(ScheduleStatus.SHUT.name());
                    }
                } else {
                    dto.setScheduleStatus(ScheduleStatus.NOTSET.name());
                }
                returnList.add(dto);
            }
            return returnList;
        }
    }

    public void createSchedule(ScheduleRequest request) {
        Schedule schedule = baseScheduleService.buildApiTestSchedule(request);
        EnterpriseTestReport report = enterpriseTestReportMapper.selectByPrimaryKey(request.getResourceId());
        schedule.setName(report.getName());   //  add场景定时任务时，设置新增的数据库表字段的值
        schedule.setProjectId(report.getProjectId());
        schedule.setJob(SendReportJob.class.getName());
        schedule.setGroup(ScheduleGroup.SCHEDULE_SEND_REPORT.name());
        schedule.setType(ScheduleType.CRON.name());
        baseScheduleService.addSchedule(schedule);
        this.addOrUpdateApiScenarioCronJob(request);
    }

    public void updateSchedule(Schedule request) {
        Schedule schedule = baseScheduleService.getSchedule(request.getId());
        if (schedule != null) {
            schedule.setEnable(request.getEnable());
            if (StringUtils.isNotEmpty(request.getValue())) {
                schedule.setValue(request.getValue());
            }
            schedule.setUpdateTime(System.currentTimeMillis());
            baseScheduleService.editSchedule(schedule);
            this.addOrUpdateApiScenarioCronJob(schedule);
        }
    }

    private void addOrUpdateApiScenarioCronJob(Schedule request) {
        baseScheduleService.addOrUpdateCronJob(
                request, SendReportJob.getJobKey(request.getResourceId()), SendReportJob.getTriggerKey(request.getResourceId()), SendReportJob.class);

    }
}
