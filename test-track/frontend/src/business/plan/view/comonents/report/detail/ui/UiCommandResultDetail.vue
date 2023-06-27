<template>
  <div class="request-result">
    <div>
      <el-row type="flex" justify="end" class="info">
        <el-col :span="24">
          <div class="time">
            {{ $t('api_report.start_time') }}：{{ startTime }}
            {{ $t('report.test_end_time') }}：{{ endTime }}
          </div>
        </el-col>
      </el-row>
    </div>
    <el-collapse-transition>
      <div v-show="isActive">
        <div class="text-container">
          <el-collapse-transition>
            <el-tabs value="body" v-show="isActive">
              <el-tab-pane :label="$t('ui.log')" name="body" class="pane">
                <div class="ms-div">
                  <pre>{{ getBody() }}</pre>
                </div>
              </el-tab-pane>

              <el-tab-pane :label="$t('api_report.assertions')" name="assertions" class="pane assertions">
                <ms-assertion-results :show-content="false" :assertions="result.assertions"/>
              </el-tab-pane>
              <el-tab-pane :label="$t('api_test.request.extract.label')" name="label" class="pane">
                <pre>{{ result.vars }}</pre>
              </el-tab-pane>
            </el-tabs>
          </el-collapse-transition>
        </div>
      </div>
    </el-collapse-transition>
  </div>
</template>

<script>
import MsAssertionResults from "@/business/plan/view/comonents/report/detail/ui/AssertionResults.vue";

export default {
  name: "UiCommandResultDetail",
  components: {MsAssertionResults},
  props: {
    request: Object,
    scenarioName: String,
    requestType: String,
    console: String,
    result: {
      type: Object,
      default() {
        return {}
      }
    },
  },

  data() {
    return {
      isActive: true,
      isCodeEditAlive: true,
      modes: ['text', 'json', 'xml', 'html'],
      sqlModes: ['text', 'table'],
      mode: 'text'
    }
  },

  methods: {
    formatDate(time, cFormat) {
      if (arguments.length === 0) {
        return null;
      }
      const format = cFormat || '{y}-{m}-{d} {h}:{i}:{s}:{t}';
      let date;
      if (typeof time === 'object') {
        date = time;
      } else {
        if (typeof time === 'string' && /^[0-9]+$/.test(time)) {
          time = parseInt(time);
        }
        if (typeof time === 'number' && time.toString().length === 10) {
          time = time * 1000;
        }
        date = new Date(time);
      }
      const formatObj = {
        y: date.getFullYear(),
        m: date.getMonth() + 1,
        d: date.getDate(),
        h: date.getHours(),
        i: date.getMinutes(),
        s: date.getSeconds(),
        a: date.getDay(),
        t: date.getMilliseconds()
      };
      const timeStr = format.replace(/{(y|m|d|h|i|s|a|t)+}/g, (result, key) => {
        let value = formatObj[key];
        // Note: getDay() returns 0 on Sunday
        if (key === 'a') {
          return ['日', '一', '二', '三', '四', '五', '六'][value];
        }
        if (result.length > 0 && value < 10) {
          value = '0' + value;
        }
        return value || 0;
      });
      return timeStr;
    },
    active() {
      this.isActive = !this.isActive;
    },
    reload() {
      this.isCodeEditAlive = false;
      this.$nextTick(() => (this.isCodeEditAlive = true));
    },
    modeChange(mode) {
      this.mode = mode;
    },
    sqlModeChange(mode) {
      this.mode = mode;
    },
    getBody(){
      if (!this.result.success) {
        return this.result.body ? this.result.body : ""
      } else {
        return "OK";
      }
    }
  },

  watch: {
    'request.responseResult'() {
      this.reload();
    }
  },

  computed: {
    assertion() {
      return this.request.passAssertions + " / " + this.request.totalAssertions;
    },
    hasSub() {
      return this.request.subRequestResults.length > 0;
    },
    startTime() {
      if (this.result.startTime) {
        return this.formatDate(this.result.startTime);
      }
      return "--";
    },
    endTime() {
      if (this.result.endTime) {
        return this.formatDate(this.result.endTime);
      }
      return "--";
    }
  },
}
</script>

<style scoped>

.request-result .info {
  background-color: #F9F9F9;
  margin-left: 20px;
  cursor: pointer;
}

.request-result {
  width: 100%;
  min-height: 40px;
  padding: 2px 0;
}

.request-result .time {
  color: #7f7f7f;
  font-size: 12px;
  font-weight: 400;
  margin-top: 4px;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  word-break: break-all;
}

.text-container .icon {
  padding: 5px;
}

.text-container .collapse {
  cursor: pointer;
}

.text-container .collapse:hover {
  opacity: 0.8;
}

.text-container .icon.is-active {
  transform: rotate(90deg);
}

.text-container .pane {
  background-color: #F5F5F5;
  padding: 1px 0;
  height: 250px;
  overflow-y: auto;
}

.text-container .pane.cookie {
  padding: 0;
}

:deep(.el-tabs__nav-wrap::after) {
  height: 0px;
}

.ms-div {
  margin-top: 20px;
}

pre {
  margin: 0;
}

.time {
  text-align: end;
  margin-right: 20px;
}

</style>
