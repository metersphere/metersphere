<template>
  <el-card class="ms-cards">
    <div class="request-result">
      <div @click="active">
        <el-row :gutter="10" type="flex" align="middle" class="info">
          <el-col :span="10" v-if="indexNumber!=undefined">
            <el-tooltip :content="getName(request.name)" placement="top">
              <div class="method ms-req-name">
                <div class="el-step__icon is-text ms-api-col-create">
                  <div class="el-step__icon-inner"> {{ indexNumber }}</div>
                </div>
                <i class="icon el-icon-arrow-right" :class="{'is-active': isActive}" @click="active" @click.stop/>
                {{ getName(request.name) }}
              </div>
            </el-tooltip>
          </el-col>
          <el-col :span="9">
            <el-tooltip effect="dark" :content="request.responseResult.responseCode" style="overflow:hidden;text-overflow:ellipsis;white-space:nowrap;" placement="bottom" :open-delay="800">
              <div style="color: #5daf34" v-if="request.success">
                {{ request.responseResult.responseCode }}
              </div>
              <div style="color: #FE6F71" v-else>
                {{ request.responseResult.responseCode }}
              </div>
            </el-tooltip>
          </el-col>
          <el-col :span="3">
          <span v-if="request.success">
            {{ request.responseResult.responseTime }} ms
          </span>
            <span style="color: #FE6F71" v-else>
            {{ request.responseResult.responseTime }} ms
          </span>
          </el-col>
          <el-col :span="2">
            <div>
              <el-tag size="mini" v-if="request.unexecute">{{ $t('api_test.home_page.detail_card.unexecute') }}</el-tag>
              <el-tag size="mini" type="success" v-else-if="request.success"> {{ $t('api_report.success') }}</el-tag>
              <el-tag size="mini" type="danger" v-else> {{ $t('api_report.fail') }}</el-tag>
            </div>
          </el-col>
        </el-row>
      </div>

      <el-collapse-transition>
        <div v-show="isActive && !request.unexecute" style="width: 99%">
          <ms-request-result-tail :scenario-name="scenarioName"
                                  :request-type="requestType"
                                  :request="request"
                                  :console="console"
                                  v-if="isActive"/>
        </div>
      </el-collapse-transition>
    </div>
  </el-card>
</template>

<script>
import MsRequestMetric from "./RequestMetric";
import MsAssertionResults from "./AssertionResults";
import MsRequestText from "./RequestText";
import MsResponseText from "./ResponseText";
import MsRequestResultTail from "./RequestResultTail";

export default {
  name: "MsRequestResult",
  components: {
    MsResponseText,
    MsRequestText,
    MsAssertionResults,
    MsRequestMetric,
    MsRequestResultTail
  },
  props: {
    request: Object,
    scenarioName: String,
    indexNumber: Number,
    console: String,
  },
  data() {
    return {
      isActive: false,
      requestType: "",
      color: {
        type: String,
        default() {
          return "#B8741A";
        }
      },
      backgroundColor: {
        type: String,
        default() {
          return "#F9F1EA";
        }
      },
    }
  },
  methods: {
    active() {
      if (this.request.unexecute) {
        this.isActive = false;
      } else {
        this.isActive = !this.isActive;
      }
    },
    getName(name) {
      if (name && name.indexOf("<->") !== -1) {
        return name.split("<->")[0];
      }
      if (name && name.indexOf("^@~@^") !== -1) {
        let arr = name.split("^@~@^");
        let value = arr[arr.length - 1];
        if (value.indexOf("UUID=") !== -1) {
          return value.split("UUID=")[0];
        }
        if (value && value.startsWith("UUID=")) {
          return "";
        }
        if (value && value.indexOf("<->") !== -1) {
          return value.split("<->")[0];
        }
        return value;
      }
      if (name && name.startsWith("UUID=")) {
        return "";
      }
      return name;
    }
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

.icon.is-active {
  transform: rotate(90deg);
}

.ms-req-name {
  display: inline-block;
  margin: 0 5px;
  overflow-x: hidden;
  padding-bottom: 0;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
  width: 350px;
}
</style>
