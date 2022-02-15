<template>
  <div class="scenario-result">
    <div v-if="(node.children && node.children.length >0) || node.unsolicited
    || (node.type && this.stepFilter.get('AllSamplerProxy').indexOf(node.type) === -1)">
      <el-card class="ms-card">
        <div class="el-step__icon is-text ms-api-col">
          <div class="el-step__icon-inner">
            {{ node.index }}
          </div>
        </div>
        <el-tooltip effect="dark" :content="node.label" placement="top">
          <span>{{ node.label }}</span>
        </el-tooltip>
      </el-card>
    </div>
    <div v-else>
      <ms-request-result
        :request="node.value"
        :indexNumber="node.index"
        :error-code="node.errorCode"
        :scenarioName="node.label"
        :console="console"
        :isActive="isActive"
        v-on:requestResult="requestResult"
      />
    </div>
  </div>
</template>

<script>
import MsRequestResult from "./RequestResult";
import {STEP} from "@/business/components/api/automation/scenario/Setting";

export default {
  name: "MsScenarioResult",
  components: {MsRequestResult},
  props: {
    scenario: Object,
    node: Object,
    console: String,
    isActive: Boolean,
  },

  data() {
    return {
      stepFilter: new STEP,
    }
  },
  methods: {
    active() {
      this.isActive = !this.isActive;
    },
    requestResult(requestResult) {
      this.$emit("requestResult", requestResult);
    }
  },

  computed: {
    assertion() {
      return this.scenario.passAssertions + " / " + this.scenario.totalAssertions;
    },
    success() {
      return this.scenario.error === 0;
    }
  }
}
</script>

<style scoped>
.scenario-result {
  width: 100%;
  padding: 2px 0;
}

.scenario-result + .scenario-result {
  border-top: 1px solid #DCDFE6;
}

.ms-card >>> .el-card__body {
  padding: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.scenario-result .info {
  height: 40px;
  cursor: pointer;
}

.scenario-result .icon {
  padding: 5px;
}

.scenario-result .icon.is-active {
  transform: rotate(90deg);
}

.ms-api-col {
  background-color: #EFF0F0;
  border-color: #EFF0F0;
  margin-right: 10px;
  font-size: 12px;
  color: #64666A;
}

.ms-card .ms-api-col-create {
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
</style>
