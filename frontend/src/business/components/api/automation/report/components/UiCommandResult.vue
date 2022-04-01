<template>
  <el-card class="ms-cards">
    <div class="request-result">
      <div>
        <el-row :gutter="16" type="flex" align="middle" class="info">
          <el-col class="ms-req-name-col" :span="18" v-if="indexNumber != undefined">
            <div class="method ms-req-name">
              <div class="el-step__icon is-text ms-api-col-create">
                <div class="el-step__icon-inner"> {{ indexNumber }}</div>
              </div>
              <span>{{ label }}</span>
            </div>
          </el-col>

          <el-col :span="3">
            <span v-if="!isUnexecute" :style="!result.success ? 'color: #FE6F71' : ''">
              {{ result.endTime - result.startTime }} ms
            </span>
          </el-col>

          <el-col :span="3">
            <el-popover
              placement="right"
              trigger="hover"
              popper-class="issues-popover"
              v-if="!isUnexecute">
              <el-image
                style="width: 100px; height: 100px"
                :src="'/resource/ui/get?fileName=' + result.url"
                :preview-src-list="['/resource/ui/get?fileName=' + result.url]">
              </el-image>
              <el-button slot="reference" type="text">{{ $t('截图') }}</el-button>
            </el-popover>
          </el-col>

          <el-col :span="2">
            <div>
              <el-tag size="mini" v-if="isUnexecute">
                {{ $t('api_test.home_page.detail_card.unexecute') }}
              </el-tag>
              <el-tag size="mini" type="success" v-else-if="result.success">
                {{ $t('api_report.success') }}
              </el-tag>
              <el-tooltip v-else :content="result.body" placement="top">
                <el-tag size="mini" type="danger" >
                  {{ $t('api_report.fail') }}
                </el-tag>
              </el-tooltip>
            </div>
          </el-col>
        </el-row>
      </div>

    </div>
  </el-card>
</template>

<script>
import commandDefinition from "@/business/components/xpack/ui/definition/command-definition";

export default {
  name: "UiCommandResult",
  props: {
    indexNumber: Number,
    result: Object,
    command: Object
  },
  computed: {
    label() {
      return this.command.label && commandDefinition[this.command.label] ? commandDefinition[this.command.label].cnName : '';
    },
    isUnexecute() {
      return this.result && this.result.status === 'unexecute';
    }
  },
  data() {
    return {
    }
  },
  methods: {
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

.ms-cards >>> .el-card__body {
  padding: 1px;
}

.sub-result:last-child {
  border-bottom: 1px solid #EBEEF5;
}

.ms-test-running {
  color: #6D317C;
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

/deep/ .el-step__icon {
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
</style>
