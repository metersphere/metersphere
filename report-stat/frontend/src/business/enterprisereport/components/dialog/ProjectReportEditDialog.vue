<template>
  <div>
    <el-dialog
      :close-on-click-modal="false"
      :destroy-on-close="true"
      :visible.sync="dialogFormVisible"
      @close="close"
      top="8vh"
      width="70%">

      <el-form v-loading="loading" :model="form" :rules="rules" ref="planFrom" v-if="isStepTableAlive">
        <!--基本信息-->
        <ms-form-divider :title="$t('test_track.plan_view.base_info')"/>
        <el-row type="flex" :gutter="20">
          <el-col :span="24">
            <el-form-item
              :label="$t('mail.mail_subject')"
              :label-width="formLabelWidth"
              prop="name">
              <el-input v-model="form.name" :placeholder="$t('mail.mail_subject')" size="medium"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row type="flex" :gutter="20">
          <el-col :span="20">
            <el-form-item
              :label="$t('mail.mail_addressee')"
              :label-width="formLabelWidth"
              prop="addressee">
              <ms-input-tag :currentScenario="form" prop="addressee"
                            :error-infor="$t('commons.report_statistics.alert.mail_is_exist')"
                            :placeholder="$t('mail.enter_mail_addressee')"
                            @onblur="validateForm">
              </ms-input-tag>
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-popover
              placement="left-start"
              v-model="showAddrAddUserGroup">
              <div>
                <p>{{ $t('commons.report_statistics.option.select_user_group') }}</p>
                <div class="select-user-group">
                  <el-checkbox-group v-model="selectAddrUserGroupList"
                                     style="margin: 5px; max-height: 500px;overflow: auto">
                    <el-row v-for="item in userGroups" :key="item.groupName">
                      <el-checkbox :label="item.groupName"></el-checkbox>
                    </el-row>
                  </el-checkbox-group>
                </div>

              </div>
              <div style="text-align: right; margin: 0">
                <el-button size="mini" type="text" @click="closeUserGroupAddPopover('addr')">
                  {{ $t("commons.cancel") }}
                </el-button>
                <el-button type="primary" size="mini" @click="addUserGroupAddPopover('addr')">
                  {{ $t("commons.confirm") }}
                </el-button>
              </div>
              <el-button slot="reference" icon="el-icon-plus" type="primary" size="small"
                         style="margin-top: 4px"></el-button>
            </el-popover>
          </el-col>
        </el-row>

        <el-row type="flex" :gutter="20">
          <el-col :span="20">
            <el-form-item
              :label="$t('mail.mail_duplicate')"
              :label-width="formLabelWidth"
              prop="duplicated">
              <ms-input-tag :currentScenario="form" prop="duplicated"
                            :error-infor="$t('commons.report_statistics.alert.mail_is_exist')"
                            :placeholder="$t('mail.enter_mail_duplicate')"
                            @onblur="validateForm"/>
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-popover
              placement="left-start"
              v-model="showDuplAddUserGroup">
              <div>
                <p>{{ $t('commons.report_statistics.option.select_user_group') }}</p>
                <div class="select-user-group">
                  <el-checkbox-group v-model="selectDuplUserGroupList"
                                     style="margin: 5px; max-height: 500px; overflow: auto">
                    <el-row v-for="item in userGroups" :key="item.groupName">
                      <el-checkbox :label="item.groupName"></el-checkbox>
                    </el-row>
                  </el-checkbox-group>
                </div>
              </div>
              <div style="text-align: right; margin: 0">
                <el-button size="mini" type="text" @click="closeUserGroupAddPopover('dupl')">
                  {{ $t("commons.cancel") }}
                </el-button>
                <el-button type="primary" size="mini" @click="addUserGroupAddPopover('dupl')">
                  {{ $t("commons.confirm") }}
                </el-button>
              </div>
              <el-button slot="reference" icon="el-icon-plus" type="primary" size="small"
                         style="margin-top: 4px"></el-button>
            </el-popover>
          </el-col>
        </el-row>

        <ms-form-divider :title="$t('commons.report_statistics.project_report.report_detail')">
          <el-dropdown @command="addReportDetail">
            <span class="el-dropdown-link">
              {{ $t('commons.report_statistics.option.add_report') }}
              <i class="el-icon-arrow-down el-icon--right"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="txt">{{ $t('commons.report_statistics.text') }}</el-dropdown-item>
              <el-dropdown-item command="report">{{ $t('commons.report_statistics.report') }}</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
          <el-popover
            placement="right-start"
            width="200"
            trigger="hover">
            <img src="/assets/enterpriseReportExample.png"/>
            <el-link type="primary" slot="reference">{{ $t('commons.example') }}</el-link>
          </el-popover>

        </ms-form-divider>
        <email-compnent :read-only="false" :data.sync="form.reportContent" ref="emailCompnent"/>
      </el-form>

      <template v-slot:footer>
        <div class="email-footer">
          <el-button
            v-prevent-re-click
            @click="previewReport">
            {{ $t('commons.preview') }}
          </el-button>
          <el-button
            v-prevent-re-click
            v-permission="['PROJECT_ENTERPRISE_REPORT:READ+EXPORT']"
            @click="saveTemplate('send')">
            {{ $t('commons.report_statistics.option.send') }}
          </el-button>
          <el-button
            type="primary"
            v-prevent-re-click
            v-permission="['PROJECT_ENTERPRISE_REPORT:READ+EDIT']"
            @click="saveTemplate('NEW')">
            {{ $t('commons.report_statistics.option.save_as_draft') }}
          </el-button>
          <el-button
            v-prevent-re-click
            @click="dialogFormVisible = false">
            {{ $t('test_track.cancel') }}
          </el-button>
        </div>
      </template>
    </el-dialog>
    <report-statistics-dialog @addReportRecord="addReportRecord" ref="reportStatisticsDialog"/>
    <email-preview-dialog ref="emailPreviewDialog"/>
  </div>
</template>

<script>

import {getUUID, listenGoBack, removeGoBackListener} from "metersphere-frontend/src/utils"
import {getCurrentProjectID, getCurrentUserId} from "metersphere-frontend/src/utils/token";
import MsInputTag from "@/business/compnent/form/MsInputTag";
import MsFormDivider from "metersphere-frontend/src/components/MsFormDivider";
import EmailCompnent from "@/business/enterprisereport/components/container/EmailComponent";
import ReportStatisticsDialog from "@/business/enterprisereport/components/dialog/SelectReportStatisticsDialog";
import EmailPreviewDialog from "@/business/enterprisereport/components/dialog/EmailPreviewDialog";
import {getUesrGroup} from "@/api/user";
import {getProject} from "@/api/project";
import {createEnterpriseReport, updateEnterpriseReport} from "@/api/enterprise-report";

export default {
  name: "ProjectReportEditDialog",
  components: {MsFormDivider, MsInputTag, EmailCompnent, ReportStatisticsDialog, EmailPreviewDialog},
  data() {
    var checkEmail = (rule, value, callback) => {
      if (value.length === 0) {
        if (rule.required) {
          callback(new Error(this.$t('member.input_email'),));
        } else {
          callback();
        }
      } else {
        const regEmail = /^([a-zA-Z0-9._-])+@([a-zA-Z0-9_-])+(\.[a-zA-Z0-9_-])+/;
        value.forEach(item => {
          if (!regEmail.test(item)) {
            callback(new Error(this.$t('user.email_format_is_incorrect'),));
          }
        });
        callback();
      }
    };

    return {
      loading: false,
      dialogFormVisible: false,
      isStepTableAlive: true,
      formLabelWidth: '80px',
      projectId: getCurrentProjectID(),
      form: {},
      projectName: "",
      userGroups: [],
      showAddrAddUserGroup: false,
      showDuplAddUserGroup: false,
      selectAddrUserGroupList: [],
      selectDuplUserGroupList: [],
      baseForm: {
        projectId: getCurrentProjectID(),
        createUser: getCurrentUserId(),
        name: '',
        status: 'NEW',
        addressee: [],
        duplicated: [],
        reportContent: [],
      },
      reportDetail: {
        name: "",
        nameIsEdit: false,
        type: "",
        previewContext: "",
        reportRecordId: "",
        reportRecordData: {},
        recordImageContent: "",
        showTable: "",
      },
      rules: {
        name: [
          {required: true, message: this.$t('mail.input_mail_subject'), trigger: 'blur'},
          {max: 30, message: this.$t('test_track.length_less_than') + '30', trigger: 'blur'}
        ],
        addressee: [
          {
            required: true,
            validator: checkEmail,
            trigger: 'blur'
          }
        ],
        duplicated: [
          {
            validator: checkEmail,
            message: this.$t('user.email_format_is_incorrect'),
            trigger: 'blur'
          }
        ],
      },
    };
  },
  created() {
    this.form = this.baseForm;
    this.getProjectName();
  },
  methods: {
    reload() {
      this.isStepTableAlive = false;
      this.$nextTick(() => {
        this.isStepTableAlive = true;
        this.loading = false;
      });
    },
    initUserGroup() {
      getUesrGroup(getCurrentProjectID()).then(
        response => {
          let data = response.data;
          if (data) {
            this.userGroups = data;
          }
        }
      );
    },
    open(projectReport) {
      this.clearForm();
      this.initUserGroup();
      if (projectReport) {
        this.form = projectReport;
      }
      this.form.projectName = this.projectName;
      listenGoBack(this.close);
      this.dialogFormVisible = true;
      this.reload();
    },
    closeUserGroupAddPopover(type) {
      if (type === 'addr') {
        this.showAddrAddUserGroup = false;
        this.selectAddrUserGroupList = [];
      } else if (type === 'dupl') {
        this.showDuplAddUserGroup = false;
        this.selectDuplUserGroupList = [];
      }
    },
    addUserGroupAddPopover(type) {
      let selectUserGroups = [];
      let formEmails = [];

      if (type === 'addr') {
        selectUserGroups = this.selectAddrUserGroupList;
        formEmails = this.form.addressee;
      } else if (type === 'dupl') {
        selectUserGroups = this.selectDuplUserGroupList;
        formEmails = this.form.duplicated;
      }
      let selectEmails = [];
      selectUserGroups.forEach(item => {
        this.userGroups.forEach(group => {
          if (group.groupName === item) {
            let users = group.users;
            users.forEach(user => {
              if (user.email) {
                selectEmails.push(user.email);
              }
            });
          }
        });
      });

      selectEmails.forEach(itemEmail => {
        if (formEmails.indexOf(itemEmail) < 0) {
          formEmails.push(itemEmail);
        }
      });
      if (type === 'addr') {
        this.form.addressee = formEmails;
      } else if (type === 'dupl') {
        this.form.duplicated = formEmails;
      }

      this.isStepTableAlive = false;
      this.$nextTick(() => {
        this.isStepTableAlive = true;
      });
      this.closeUserGroupAddPopover(type);
    },
    getProjectName() {
      getProject(this.projectId).then(response => {
        let project = response.data;
        if (project) {
          this.projectName = project.name;
        }
      });
    },
    addReportDetail(command) {
      if (command === 'txt') {
        let detail = JSON.parse(JSON.stringify(this.reportDetail));
        detail.name = this.form.reportContent.length + 1 + ". " + this.$t('commons.subject');
        detail.id = getUUID();
        detail.type = command;
        this.form.reportContent.push(detail);
      } else if (command === 'report') {
        let detail = JSON.parse(JSON.stringify(this.reportDetail));
        detail.id = getUUID();
        detail.name = this.form.reportContent.length + 1 + ". " + this.$t('commons.subject');
        detail.type = command;
        detail.reportRecordData = {};
        detail.reportRecordId = "";
        detail.showTable = "";
        this.$refs.reportStatisticsDialog.open(detail);
      }
    },
    addReportRecord(reportRecord) {
      this.form.reportContent.push(reportRecord);
    },
    validateForm() {
      this.$refs['planFrom'].validate();
    },
    saveTemplate(saveType) {
      //检查是否符合邮件规范
      this.$refs['planFrom'].validate((valid) => {
        if (valid) {
          if (this.$refs.emailCompnent) {
            this.$refs.emailCompnent.initDatas();
          }

          let param = JSON.parse(JSON.stringify(this.form));
          if (this.form.addressee) {
            param.addressee = JSON.stringify(this.form.addressee);
          } else {
            param.addressee = "[]";
          }

          if (this.form.duplicated) {
            param.duplicated = JSON.stringify(this.form.duplicated);
          } else {
            param.duplicated = "[]";
          }

          if (this.form.reportContent) {
            param.reportContent = JSON.stringify(this.form.reportContent);
          } else {
            param.reportContent = "[]";
          }
          param.status = saveType;
          this.loading = true;

          if (this.form.id) {
            updateEnterpriseReport(param).then(request =>
              this.afterSaveEnterpriseReport(request)
            ).catch(() => {
              this.loading = false;
            });
          } else {
            createEnterpriseReport(param).then(request =>
              this.afterSaveEnterpriseReport(request)
            ).catch(() => {
              this.loading = false;
            });
          }
        } else {
          return false;
        }
      });
    },
    afterSaveEnterpriseReport(returnData) {
      this.$success(this.$t('commons.save_success'));
      this.dialogFormVisible = false;
      this.$emit("refresh");
      this.loading = false;
    },
    validate(param) {
      if (param.name === '') {
        this.$warning(this.$t('test_track.plan.input_plan_name'));
        return false;
      }
      if (param.plannedStartTime > param.plannedEndTime) {
        this.$warning(this.$t('commons.date.data_time_error'));
        return false;
      }
      return true;
    },
    close() {
      removeGoBackListener(this.close);
      this.clearForm();
      this.dialogFormVisible = false;
    },
    clearForm() {
      this.form = JSON.parse(JSON.stringify(this.baseForm));
    },
    previewReport() {
      if (this.$refs.emailCompnent) {
        this.$refs.emailCompnent.initDatas();
      }
      this.$refs.emailPreviewDialog.open(this.form)
    },
    selectUserGroup(selectType) {

    },
  }
}
</script>

<style scoped>
.email-footer {
  text-align: left;
}

.select-user-group :deep(.el-checkbox__label) {
  font-size: 12px;
  font-weight: 0;
}
</style>
