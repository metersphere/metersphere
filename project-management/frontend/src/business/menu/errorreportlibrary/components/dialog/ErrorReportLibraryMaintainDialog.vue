<template>
  <div>
    <el-dialog
      :close-on-click-modal="false"
      :destroy-on-close="true"
      :visible.sync="dialogFormVisible"
      @close="close"
      top="8vh"
      width="70%">

      <el-form v-loading="loading" :model="libraryData" :rules="rules" ref="planFrom" v-if="isStepTableAlive">
        <!--基本信息-->
        <ms-form-divider :title="$t('test_track.plan_view.base_info')"/>
        <el-row type="flex" :gutter="20">
          <el-col :span="10">
            <el-form-item
              :label="$t('error_report_library.option.error_code')"
              :label-width="formLabelWidth"
              prop="errorCode">
              <el-input v-model="libraryData.errorCode" :placeholder="$t('commons.input_content')"
                        size="medium"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              :label="$t('error_report_library.option.is_open')"
              :label-width="formLabelWidth"
              prop="status">
              <el-switch v-model="libraryData.status"/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row type="flex" :gutter="20">
          <el-col :span="24">
            <el-form-item
              :label="$t('commons.description')"
              :label-width="formLabelWidth"
              prop="description">
              <el-input
                type="textarea"
                :rows="2"
                :placeholder="$t('commons.input_content')"
                maxlength="1000"
                show-word-limit
                v-model="libraryData.description">
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row type="flex" :gutter="20">
          <el-col :span="6">
            <el-form-item
              :label="$t('error_report_library.option.match_rule')"
              :label-width="formLabelWidth"
              prop="description">
              <el-select :disabled="isReadOnly" class="assertion-item" v-model="libraryData.matchType"
                         :placeholder="$t('api_test.request.assertions.select_type')" size="small">
                <el-option :label="$t('api_test.request.assertions.text')" :value="options.TEXT"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="20">
            <el-row :gutter="10" type="flex" justify="space-between" align="middle" style="margin-top: 5px"
                    v-if="libraryData.matchType === options.TEXT">
              <el-col :span="7" class="assertion-select">
                <el-select :disabled="isReadOnly" class="assertion-item"
                           v-model="libraryData.content.regexConfig.subject" size="small"
                           :placeholder="$t('api_test.request.assertions.select_subject')">
                  <el-option label="Response Code" :value="subjects.RESPONSE_CODE"/>
                  <el-option label="Response Headers" :value="subjects.RESPONSE_HEADERS"/>
                  <el-option label="Response Data" :value="subjects.RESPONSE_DATA"/>
                </el-select>
              </el-col>
              <el-col :span="5" class="assertion-select">
                <el-select :disabled="isReadOnly" class="assertion-item"
                           v-model="libraryData.content.regexConfig.condition" size="small"
                           :placeholder="$t('api_test.request.assertions.select_condition')">
                  <el-option :label="$t('api_test.request.assertions.contains')" value="CONTAINS"/>
                  <el-option :label="$t('api_test.request.assertions.not_contains')" value="NOT_CONTAINS"/>
                  <el-option :label="$t('api_test.request.assertions.equals')" value="EQUALS"/>
                  <el-option :label="$t('api_test.request.assertions.start_with')" value="START_WITH"/>
                  <el-option :label="$t('api_test.request.assertions.end_with')" value="END_WITH"/>
                </el-select>
              </el-col>
              <el-col :span="12">
                <el-input :disabled="isReadOnly" v-model="libraryData.content.regexConfig.value" maxlength="200"
                          size="small" show-word-limit
                          :placeholder="$t('api_test.request.assertions.value')"/>
              </el-col>
            </el-row>
          </el-col>
        </el-row>
      </el-form>

      <template v-slot:footer>
        <div class="email-footer">
          <el-button
            type="primary"
            v-prevent-re-click
            @click="save()">
            {{ $t('commons.save') }}
          </el-button>
          <el-button
            v-prevent-re-click
            @click="dialogFormVisible = false">
            {{ $t('test_track.cancel') }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>

import {getCurrentProjectID, getCurrentUserId} from "metersphere-frontend/src/utils/token";
import {listenGoBack, removeGoBackListener} from "metersphere-frontend/src/utils";
import MsFormDivider from "metersphere-frontend/src/components/MsFormDivider";
import {ASSERTION_REGEX_SUBJECT, ASSERTION_TYPE, Regex} from "metersphere-frontend/src/model/ApiTestModel";
import {getProject} from "../../../../../api/project";
import {modifyErrorReportLibrary, saveErrorReportLibrary} from "../../../../../api/error-report-library";

export default {
  name: "ErrorReportLibraryMaintainDialog",
  components: {MsFormDivider},
  data() {
    return {
      loading: false,
      libraryData: {},
      options: ASSERTION_TYPE,
      isReadOnly: false,
      subjects: ASSERTION_REGEX_SUBJECT,

      dialogFormVisible: false,
      isStepTableAlive: true,
      formLabelWidth: '80px',
      projectId: getCurrentProjectID(),
      defaultForm: {
        errorCode: "",
        createUser: getCurrentUserId(),
        description: '',
        status: true,
        matchType: "",
        content: {
          duration: {duration: 0},
          regexConfig: {
            subject: "",
            condition: "",
            value: "",
            assumeSuccess: "",
          },
          regex: [],
          jsonPath: [],
          xpath2: [],
          jsr223: [],
          document: {type: "JSON", data: {json: [], xml: []}},
        },
      },
      rules: {
        errorCode: [
          {required: true, message: this.$t('commons.input_content'), trigger: 'blur'},
          {max: 50, message: this.$t('test_track.length_less_than') + '50', trigger: 'blur'}
        ],
      },
    };
  },
  created() {
    this.clearForm();
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
    open(libraryReport) {
      if (!libraryReport) {
        this.clearForm();
      } else {
        this.libraryData = JSON.parse(JSON.stringify(libraryReport));
        if (libraryReport.content) {
          this.libraryData.content = JSON.parse(libraryReport.content);
        }
      }
      listenGoBack(this.close);
      this.dialogFormVisible = true;
      this.reload();
    },
    getProjectName() {
      getProject(this.projectId).then(response => {
        let project = response.data;
        if (project) {
          this.projectName = project.name;
        }
      })
    },
    save() {
      //检查是否符合邮件规范

      this.$refs['planFrom'].validate((valid) => {
        if (valid) {
          this.libraryData.content.regex = [];
          let regexItem = this.toRegex(this.libraryData.content.regexConfig);
          if (regexItem) {
            this.libraryData.content.regex.push(regexItem);
          }

          let param = JSON.parse(JSON.stringify(this.libraryData));
          param.projectId = getCurrentProjectID();
          if (this.libraryData.content) {
            param.content = JSON.stringify(this.libraryData.content);
          } else {
            param.content = "{}";
          }
          this.loading = true;
          let promise = undefined;
          if (this.libraryData.id) {
            promise = modifyErrorReportLibrary(param);
          } else {
            promise = saveErrorReportLibrary(param);
          }
          promise.then(() => {
            this.$success(this.$t('commons.save_success'));
            this.dialogFormVisible = false;
            this.$emit("refresh");
            this.loading = false;
          }).catch(() => {
            this.loading = false;
          })
        } else {
          return false;
        }
      });
    },
    validate(param) {
      if (param.errorCode === '') {
        this.$warning(this.$t('test_track.plan.input_plan_name'));
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
      this.libraryData = JSON.parse(JSON.stringify(this.defaultForm));
    },
    toRegex: function (option) {
      let expression = "";
      switch (option.condition) {
        case "CONTAINS":
          expression = ".*" + option.value + ".*";
          break;
        case "NOT_CONTAINS":
          expression = "(?s)^((?!" + option.value + ").)*$";
          break;
        case "EQUALS":
          expression = "^" + option.value + "$";
          break;
        case "START_WITH":
          expression = "^" + option.value;
          break;
        case "END_WITH":
          expression = option.value + "$";
          break;
      }
      return new Regex({
          subject: option.subject,
          expression: expression,
          description: null,
          assumeSuccess: option.assumeSuccess
        }
      );
    },
    after() {
    },
  }
}
</script>

<style scoped>
.email-footer {
  text-align: left;
  margin-left: 7px;
}

.select-user-group :deep(.el-checkbox__label) {
  font-size: 12px;
  font-weight: 0;
}

.assertion-item {
  width: 100%;
}

.assertion-select {
  width: 250px;
}
</style>
