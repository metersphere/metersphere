<template>
  <div class="dep-container">
    <div class="dep-header-wrap">
      <div class="label-row">
        <div class="control-open-opt" @click="expand = !expand">
          <!-- 展开状态 -->
          <i class="el-icon-caret-bottom" v-if="expand"></i>
          <!-- 折叠状态 -->
          <i class="el-icon-caret-right" v-else></i>
        </div>
        <div class="dep-title">{{ title }}</div>
      </div>
    </div>
    <div class="dep-list-wrap" v-if="expand">
      <test-case-relationship-list
        :tableData="tableData"
        :relationshipType="relationshipType"
        ref="testCaseRelationshipList"
      />
    </div>
    <div class="split" v-else></div>
  </div>
</template>

<script>
import TestCaseRelationshipList from "./CaseDiffRelationshipTableList";
import { deleteRelationshipEdge } from "@/business/utils/sdk-utils";

export default {
  name: "CaseDiffRelationshipList",
  components: { TestCaseRelationshipList },
  data() {
    return {
      expand: true,
      result: {},
      data: [],
      options: [],
      value: "",
    };
  },
  props: {
    tableData: [],
    title: String,
    relationshipType: String,
  },
};
</script>

<style scoped lang="scss">
@import "@/business/style/index.scss";
.dep-container {
  margin-top: px2rem(24);
  .dep-header-wrap {
    display: flex;
    justify-content: space-between;
    margin-top: px2rem(13);
    .label-row {
      display: flex;
      align-items: center;
      .control-open-opt {
        margin-right: px2rem(9.25);
        cursor: pointer;
        color: #783887;
        i {
          width: px2rem(9.5);
          height: px2rem(6.25);
        }
      }

      .dep-title {
        font-weight: 500;
        font-size: 16px;
        color: #1f2329;
      }
    }

    .opt-add-row {
      .el-button--small {
        background: #ffffff;
        border: 1px solid #783887;
        border-radius: 4px;
        color: #783887;
        height: 32px;
        line-height: 32px;
        padding: 0px 18.17px 0px 18.17px !important;
        font-size: 14px !important;
      }
      :deep(.el-icon-plus:before) {
        width: 11.67px;
        height: 11.67px;
        color: #783887;
      }
    }
  }
  .dep-list-wrap {
    margin-top: px2rem(12);
  }
  .split {
    height: 1px;
    background-color: rgba(31, 35, 41, 0.15);
    margin-top: px2rem(24);
  }
}
</style>
