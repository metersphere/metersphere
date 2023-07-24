<template>
  <el-drawer
    :visible.sync="visible"
    append-to-body
    :size="calcWidth"
    :close-on-click-modal="false"
    class="case-desc-drawer-layout"
  >
    <template v-slot:title>
      {{ $t("test_track.case.case_desc") }}
    </template>

    <div style="height: calc(100vh - 60px); overflow: scroll">
      <el-form :model="data" class="case-desc-form">
        <div class="prerequisite-item-layout">
          <h3>{{ $t("test_track.case.prerequisite") }}</h3>
          <div class="case-desc-form-div">
            <span v-if="!data['prerequisite']">{{ $t("case.none") }}</span>
            <ms-mark-down-text
              v-else
              :disabled="true"
              :data="data"
              prop="prerequisite"
            />
          </div>
        </div>

        <step-change-item :label-width="formLabelWidth" :form="data" />

        <div v-if="data.stepModel === 'TEXT'" class="prerequisite-item-layout">
          <h3>{{ $t("test_track.case.step_desc") }}</h3>
          <div class="case-desc-form-div">
            <span v-if="!data['stepDescription']">{{ $t("case.none") }}</span>
            <ms-mark-down-text
              v-else
              :disabled="true"
              :data="data"
              prop="stepDescription"
            />
          </div>
        </div>

        <div v-if="data.stepModel === 'TEXT'" class="prerequisite-item-layout">
          <h3>{{ $t("test_track.case.expected_results") }}</h3>
          <div class="case-desc-form-div">
            <span v-if="!data['expectedResult']">{{ $t("case.none") }}</span>
            <ms-mark-down-text
              v-else
              :disabled="true"
              :data="data"
              prop="expectedResult"
            />
          </div>
        </div>

        <div
          v-if="data.stepModel === 'STEP' || !data.stepModel"
          class="prerequisite-item-layout"
        >
          <div class="case-desc-form-div case-desc-form-table-div">
            <ms-case-desc-text-item :data="data" />
          </div>
        </div>
      </el-form>
    </div>
  </el-drawer>
</template>

<script>
import MsFormDivider from "metersphere-frontend/src/components/MsFormDivider";
import MsCaseDescTextItem from "metersphere-frontend/src/components/new-ui/MsCaseDescTextItem";
import StepChangeItem from "@/business/case/components/StepChangeItem";
import TestCaseStepItem from "@/business/case/components/TestCaseStepItem";
import MsMarkDownText from "metersphere-frontend/src/components/MsMarkDownText";

export default {
  name: "TestCasePreview",
  components: {
    MsFormDivider,
    MsCaseDescTextItem,
    StepChangeItem,
    TestCaseStepItem,
    MsMarkDownText,
  },
  props: {
    width: {
      type: Number,
      default: 1280,
    },
  },
  computed: {
    calcWidth() {
      if (!isNaN(this.width)) {
        //计算rem
        let remW = (this.width / 1440) * 100;
        let standW = (1280 / 1440) * 100;
        return remW > standW ? standW : remW + "%";
      }
      return this.width;
    },
  },
  data() {
    return {
      result: {},
      formLabelWidth: "100px",
      stepForLabelWidth: "150px",
      visible: false,
      data: {},
    };
  },
  methods: {
    open() {
      this.visible = true;
    },
    close() {
      this.visible = false;
    },
    setData(data) {
      this.data = data;
    },
  },
};
</script>

<style scoped>
:deep(.el-drawer__header) {
  font-family: "PingFang SC";
  font-style: normal;
  font-weight: bold;
  font-size: 16px;
  line-height: 24px;
  color: #1f2329;
  flex: none;
  flex-grow: 0;
  padding: 16px 24px;
  border-bottom: 1px solid rgba(31, 35, 41, 0.15);
  margin: 0;
}

:deep(i.el-dialog__close.el-icon.el-icon-close) {
  float: right;
}

.prerequisite-item-layout {
  padding: 0px 24px 0 24px;
  font-family: "PingFang SC";
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #1f2329;
  flex: none;
  order: 0;
  flex-grow: 0;
  margin-top: 24px;
  margin-bottom: 20px;
}

.prerequisite-item-layout h3 {
  font-weight: bold;
  margin: 0 0 8px;
  font-size: 14px;
}

.case-desc-form .el-form-item {
  margin: 0;
}

.case-desc-form :deep(.el-form-item .el-form-item__label) {
  font-family: "PingFang SC";
  font-style: normal;
  font-weight: bold;
  font-size: 14px;
  line-height: 22px;
  color: #1f2329;
  flex: none;
  order: 0;
  flex-grow: 0;
  margin-top: 8px;
  padding-right: 20px;
}

:deep(.ms-table-header-cell) {
  height: 46px;
  background-color: #f5f6f7;
  font-family: "PingFang SC";
  font-size: 14px;
  font-weight: 500;
  border: 1px solid rgba(31, 35, 41, 0.15);
  border-right-width: 0;
  border-left-width: 0;
  color: #646a73;
  line-height: 22px;
  padding: 0px;
  align-items: center;
  flex: none;
  order: 0;
  flex-grow: 0;
}

.case-desc-form-div {
  height: 150px;
  overflow-y: scroll;
}
.case-desc-form-table-div {
  height: auto;
}
</style>
