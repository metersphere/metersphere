<template>
  <el-card class="ms-cards">
    <div class="request-result">
      <div @click="active">
        <el-row :gutter="16" type="flex" align="middle" class="info">
          <el-col class="ms-req-name-col" :span="18" v-if="indexNumber != undefined">
            <div class="method ms-req-name">
              <div class="el-step__icon is-text ms-api-col-create">
                <div class="el-step__icon-inner"> {{ indexNumber }}</div>
              </div>
              <i class="icon el-icon-arrow-right" :class="{'is-active': showActive}" @click="active" @click.stop/>
              <span>{{ label }}</span>
            </div>
          </el-col>

          <el-col :span="3">
            <span v-if="!isUnexecute" :style="result && !result.success ? 'color: #FE6F71' : ''">
              {{ result.time }} ms
            </span>
          </el-col>

          <el-col :span="3">
            <!-- 兼容旧数据 报告截图 -->
            <el-popover
              placement="right"
              trigger="hover"
              popper-class="issues-popover"
              v-if="!isUnexecute && result.uiImg">
              <el-image
                style="width: 100px; height: 100px"
                :src="'/resource/ui/get?fileName=' + result.uiImg + '&reportId=' + result.reportId"
                :preview-src-list="['/resource/ui/get?fileName=' + result.uiImg + '&reportId=' + result.reportId]">
              </el-image>
              <el-button slot="reference" type="text">{{ $t('ui.screenshot') }}</el-button>
            </el-popover>

            <div @click.stop="triggerViewer" v-if="!isUnexecute && uiScreenshots && result.combinationImg"
                 style="color: #783887;"> {{ $t('ui.screenshot') }}
            </div>
          </el-col>

          <el-col :span="3">
            <div>
              <el-tag size="mini" v-if="isUnexecute">
                {{ $t('api_test.home_page.detail_card.unexecute') }}
              </el-tag>
              <el-tag size="mini" type="success" v-else-if="result && result.success">
                {{ $t('api_report.success') }}
              </el-tag>
              <el-tag v-else size="mini" type="danger">
                {{ $t('api_report.fail') }}
              </el-tag>
            </div>
          </el-col>
        </el-row>
      </div>

      <el-collapse-transition>
        <div v-show="showActive" style="width: 99%">
          <ui-command-result-detail
            v-loading="detail.loading"
            :result="detail"
          />
        </div>
      </el-collapse-transition>
      <ui-screenshot-viewer ref="screenshotViewer" v-if="!isUnexecute && uiScreenshots && result.combinationImg"
                            :src="'/resource/ui/get?fileName=' + result.combinationImg + '&reportId=' + result.reportId"/>
    </div>
  </el-card>
</template>

<script>
import UiCommandResultDetail from "./UiCommandResultDetail";
import UiScreenshotViewer from "./UiScreenshotViewer";

export default {
  name: "UiCommandResult",
  components: {UiCommandResultDetail, UiScreenshotViewer},
  props: {
    indexNumber: Number,
    result: Object,
    command: Object,
    stepId: String,
    isActive: {
      type: Boolean,
      default: false
    },
    treeNode: Object,
  },
  data() {
    return {
      showActive: false,
      detail: {
        loading: false
      },
      uiScreenshots: []
    }
  },
  mounted() {
    if (this.result.uiScreenshots) {
      this.uiScreenshots = this.result.uiScreenshots || [];
    }
  },
  computed: {
    label() {
      if (!this.$t("ui." + this.command.label).startsWith("ui")) {
        return this.$t("ui." + this.command.label);
      }
      return this.command.label;
    },
    isUnexecute() {
      return !this.result || this.result.status === 'unexecute';
    }
  },
  watch: {
    isActive() {
      this.showActive = this.isActive;
    },
    errorCode() {
      this.baseErrorCode = this.errorCode;
    },
    'treeNode.expanded'() {
      if (this.treeNode.expanded) {
        this.loadRequestInfoExpand();
      }
    }
  },
  methods: {
    triggerViewer() {
      this.$refs.screenshotViewer.open();
    },
    active() {
      if (this.isUnexecute) {
        this.showActive = false;
      } else {
        this.showActive = !this.showActive;
      }
      if (this.showActive) {
        this.loadRequestInfoExpand();
      }
    },
    loadRequestInfoExpand() {
      if (this.command && this.command.value) {
        if (!this.command.value.time && this.command.value.startTime && this.command.value.endTime) {
          this.command.value.time = this.command.value.endTime - this.command.value.startTime;
        }
        this.result = this.command.value;
        this.detail = this.command.value;
      } else {
        if (!this.detail.hasData) {
          this.detail.loading = true;
          this.$get("/ui/automation/selectReportContent/" + this.stepId).then(response => {
            let requestResult = response.data;
            if (requestResult) {
              this.detail = requestResult;
            }
            this.$nextTick(() => {
              this.detail.loading = false;
              this.detail.hasData = true;
            });
          });
        }
      }
    },
  },
}
</script>

<style scoped>
.request-result {
  min-height: 30px;
  padding: 2px 0;
}

.request-result .info {
  margin-left: 20px;
  cursor: pointer;
}

.request-result .method {
  color: #1E90FF;
  font-size: 14px;
  font-weight: 500;
  line-height: 35px;
  padding-left: 5px;
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.request-result .url {
  color: #7f7f7f;
  font-size: 12px;
  font-weight: 400;
  margin-top: 4px;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  word-break: break-all;
}

.request-result .tab .el-tabs__header {
  margin: 0;
}

.request-result .text {
  height: 300px;
  overflow-y: auto;
}

.sub-result .info {
  background-color: #FFF;
}

.sub-result .method {
  border-left: 5px solid #1E90FF;
  padding-left: 20px;
}

.ms-cards :deep(.el-card__body) {
  padding: 1px;
}

.sub-result:last-child {
  border-bottom: 1px solid #EBEEF5;
}

.ms-test-running {
  color: #783887;
}

.ms-test-error_code {
  color: #F6972A;
  background-color: #FDF5EA;
  border-color: #FDF5EA;
}

.ms-api-col {
  background-color: #EFF0F0;
  border-color: #EFF0F0;
  margin-right: 10px;
  font-size: 12px;
  color: #64666A;
}

.ms-api-col-create {
  background-color: #EBF2F2;
  border-color: #008080;
  margin-right: 10px;
  font-size: 12px;
  color: #008080;
}

:deep(.el-step__icon) {
  width: 20px;
  height: 20px;
  font-size: 12px;
}

.el-divider--horizontal {
  margin: 2px 0;
  background: 0 0;
  border-top: 1px solid #e8eaec;
}

.ms-req-name {
  display: inline-block;
  margin: 0 5px;
  padding-bottom: 0;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
  width: 350px;
}

.ms-req-name-col {
  overflow-x: hidden;
}

.icon.is-active {
  transform: rotate(90deg);
}
</style>
