<template>
  <el-dialog
    :close-on-click-modal="false"
    width="60%"
    class="schedule-edit"
    :visible.sync="dialogVisible"
    append-to-body
    @close="close"
  >
    <template>
      <div>
        <el-tabs v-model="activeName">
          <el-tab-pane :label="$t('schedule.edit_timer_task')" name="first">
            <el-form :model="form" :rules="rules" ref="from">
              <el-form-item prop="cronValue">
                <el-input
                  :disabled="isReadOnly"
                  v-model="form.cronValue"
                  class="inp"
                  :placeholder="$t('schedule.please_input_cron_expression')"
                />
                <el-button
                  :disabled="isReadOnly"
                  type="primary"
                  @click="saveCron"
                  >{{ $t('commons.save') }}
                </el-button>
              </el-form-item>
              <el-form-item>
                <el-link
                  :disabled="isReadOnly"
                  type="primary"
                  @click="showCronDialog"
                >
                  {{ $t('schedule.generate_expression') }}
                </el-link>
              </el-form-item>
              <crontab-result :ex="form.cronValue" ref="crontabResult" />
            </el-form>
            <el-dialog
              width="60%"
              :title="$t('schedule.generate_expression')"
              :visible.sync="showCron"
              :modal="false"
            >
              <crontab
                @hide="showCron = false"
                @fill="crontabFill"
                :expression="schedule.value"
                ref="crontab"
              />
            </el-dialog>
          </el-tab-pane>
          <el-tab-pane
            :label="$t('api_test.home_page.running_task_list.title')"
            name="second"
          >
            <swagger-task-list></swagger-task-list>
          </el-tab-pane>
        </el-tabs>
      </div>
    </template>
  </el-dialog>
</template>

<script>
import {
  createDefinitionSchedule,
  getDefinitionByResourceId,
  updateDefinitionSchedule,
} from '@/api/definition';
import { getScheduleByIdAndType } from '@/api/schedule';
import {
  getCurrentProjectID,
  getCurrentUser,
  getCurrentWorkspaceId,
} from 'metersphere-frontend/src/utils/token';
import {
  listenGoBack,
  removeGoBackListener,
} from 'metersphere-frontend/src/utils';
import Crontab from 'metersphere-frontend/src/components/cron/Crontab';
import CrontabResult from 'metersphere-frontend/src/components/cron/CrontabResult';
import { cronValidate } from 'metersphere-frontend/src/utils/cron';
import SwaggerTaskList from '@/business/definition/components/import/SwaggerTaskList';

function defaultCustomValidate() {
  return { pass: true };
}

export default {
  name: 'ImportScheduleEdit',
  components: { SwaggerTaskList, CrontabResult, Crontab },

  props: {
    customValidate: {
      type: Function,
      default: defaultCustomValidate,
    },
    isReadOnly: {
      type: Boolean,
      default: false,
    },
  },

  watch: {
    'schedule.value'() {
      this.form.cronValue = this.schedule.value;
    },
  },
  data() {
    const validateCron = (rule, cronValue, callback) => {
      let customValidate = this.customValidate(this.getIntervalTime());
      if (!cronValue) {
        callback(new Error(this.$t('commons.input_content')));
      } else if (!cronValidate(cronValue)) {
        callback(new Error(this.$t('schedule.cron_expression_format_error')));
      } else if (!this.intervalValidate()) {
        callback(new Error(this.$t('schedule.cron_expression_interval_error')));
      } else if (!customValidate.pass) {
        callback(new Error(customValidate.info));
      } else {
        callback();
      }
    };
    return {
      operation: true,
      dialogVisible: false,
      schedule: {
        value: '',
      },
      showCron: false,
      form: {
        cronValue: '',
      },
      activeName: 'first',
      swaggerUrl: String,
      projectId: String,
      moduleId: String,
      paramSwaggerUrlId: String,
      modulePath: String,
      modeId: String,
      rules: {
        cronValue: [
          { required: true, validator: validateCron, trigger: 'blur' },
        ],
      },
    };
  },
  methods: {
    currentUser: () => {
      return getCurrentUser();
    },
    intervalValidate() {
      if (this.getIntervalTime() < 1 * 60 * 1000) {
        return false;
      }
      return true;
    },
    open(param) {
      getDefinitionByResourceId(param).then((response) => {
        this.paramSwaggerUrlId = response.data;
        if (this.paramSwaggerUrlId) {
          this.findSchedule(this.paramSwaggerUrlId);
        }
        this.project = param.projectId;
        this.swaggerUrl = param.swaggerUrl;
        this.dialogVisible = true;
        this.form.cronValue = this.schedule.value;
        this.moduleId = param.moduleId;
        this.modulePath = param.modulePath;
        this.modeId = param.modeId;
        listenGoBack(this.close);
      });

      this.activeName = 'first';
    },
    findSchedule(paramSwaggerUrlId) {
      let scheduleResourceID = paramSwaggerUrlId;
      let taskType = 'SWAGGER_IMPORT';
      this.result = getScheduleByIdAndType(scheduleResourceID, taskType).then(
        (response) => {
          if (response.data != null) {
            this.schedule = response.data;
          } else {
            this.schedule = {};
          }
        }
      );
    },
    crontabFill(value, resultList) {
      //确定后回传的值
      this.form.cronValue = value;
      this.$refs.crontabResult.resultList = resultList;
      this.$refs['from'].validate();
    },
    showCronDialog() {
      let tmp = this.schedule.value;
      this.schedule.value = '';
      this.$nextTick(() => {
        this.schedule.value = tmp;
        this.showCron = true;
      });
    },
    saveCron() {
      this.$refs['from'].validate((valid) => {
        if (valid) {
          this.intervalShortValidate();
          let formCronValue = this.form.cronValue;
          this.schedule.enable = true;
          this.schedule.value = formCronValue;
          this.saveSchedule();
          this.dialogVisible = false;
        } else {
          return false;
        }
      });
    },
    saveSchedule() {
      this.checkScheduleEdit();
      let param = {};
      param = this.schedule;
      param.resourceId = this.swaggerUrl;
      param.projectId = getCurrentProjectID();
      param.workspaceId = getCurrentWorkspaceId();
      param.moduleId = this.moduleId;
      param.modulePath = this.modulePath;
      param.modeId = this.modeId;
      if (this.paramSwaggerUrlId) {
        updateDefinitionSchedule(param).then(() => {
          this.$success(this.$t('commons.save_success'));
        });
      } else {
        createDefinitionSchedule(param).then(() => {
          this.$success(this.$t('commons.save_success'));
        });
      }
    },
    checkScheduleEdit() {
      if (this.create) {
        this.$message(this.$t('api_test.environment.please_save_test'));
        return false;
      }
      return true;
    },
    close() {
      this.dialogVisible = false;
      this.form.cronValue = '';
      this.$refs['from'].resetFields();
      if (!this.schedule.value) {
        this.$refs.crontabResult.resultList = [];
      }
      removeGoBackListener(this.close);
    },
    intervalShortValidate() {
      if (this.getIntervalTime() < 3 * 60 * 1000) {
        // return false;
        this.$info(this.$t('schedule.cron_expression_interval_short_error'));
      }
      return true;
    },
    resultListChange() {
      this.$refs['from'].validate();
    },
    getIntervalTime() {
      let resultList = this.$refs.crontabResult.resultList;
      let time1 = new Date(resultList[0]);
      let time2 = new Date(resultList[1]);
      return time2 - time1;
    },
  },
  computed: {
    isTesterPermission() {
      return true;
    },
  },
};
</script>

<style scoped>
.inp {
  width: 50%;
  margin-right: 20px;
}

.el-form-item {
  margin-bottom: 10px;
}
</style>
