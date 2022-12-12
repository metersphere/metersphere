<template>
  <el-drawer
    :visible.sync="visible"
    append-to-body
    size="70%"
    :close-on-click-modal="false"
    class="case-desc-drawer-layout"
  >
    <template v-slot:title>
      {{$t('test_track.case.case_desc')}}
    </template>
    <div v-loading="loading" style="height: 100%; overflow: auto">
      <el-form :model="data" class="case-desc-form">
        <div class="prerequisite-item-layout">
          <h3>{{$t('test_track.case.prerequisite')}}</h3>
          <span>{{data.prerequisite}}</span>
        </div>
        <step-change-item :label-width="formLabelWidth" :form="data"/>
        <ms-case-desc-text-item v-if="data.stepModel === 'TEXT'" :title="$t('test_track.case.step_desc')" :data="data" :content="data.stepDescription"/>
        <ms-case-desc-text-item v-if="data.stepModel === 'TEXT'" :title="$t('test_track.case.expected_results')" :data="data" :content="data.expectedResult"/>
        <ms-case-desc-text-item v-if="data.stepModel === 'STEP' || !data.stepModel" :data="data"/>
      </el-form>
    </div>
  </el-drawer>
</template>

<script>
import MsFormDivider from "metersphere-frontend/src/components/MsFormDivider";
import MsCaseDescTextItem from "metersphere-frontend/src/components/new-ui/MsCaseDescTextItem";
import StepChangeItem from "@/business/case/components/StepChangeItem";
import TestCaseStepItem from "@/business/case/components/TestCaseStepItem";

export default {
  name: "TestCasePreview",
  components: {
    MsFormDivider,
    MsCaseDescTextItem,
    StepChangeItem,
    TestCaseStepItem,
  },
  props: ['loading'],
  data() {
    return {
      result: {},
      formLabelWidth: "100px",
      visible: false,
      data: {}
    }
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
    }
  }
}
</script>

<style scoped>
:deep(.el-drawer__header) {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: bold;
  font-size: 16px;
  line-height: 24px;
  color: #1F2329;
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
  padding: 24px;
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #1F2329;
  flex: none;
  order: 0;
  flex-grow: 0;
}

.prerequisite-item-layout h3{
  font-weight: bold;
  margin: 0 0 10px;
  font-size: 14px;
}

.case-desc-form .el-form-item {
  margin: 0;
}

.case-desc-form :deep(.el-form-item .el-form-item__label) {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: bold;
  font-size: 14px;
  line-height: 22px;
  color: #1F2329;
  flex: none;
  order: 0;
  flex-grow: 0;
  margin-top: 8px;
  padding-right: 20px;
}

:deep(.ms-table-header-cell) {
  height: 46px;
  background-color: #F5F6F7;
  font-family: 'PingFang SC';
  font-size: 14px;
  font-weight: 500;
  border: 1px solid rgba(31, 35, 41, 0.15);
  border-right-width: 0;
  border-left-width: 0;
  color: #646A73;
  line-height: 22px;
  padding: 0px;
  align-items: center;
  flex: none;
  order: 0;
  flex-grow: 0;
}
</style>
