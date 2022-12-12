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
      <div class="opt-add-row">
        <el-button
          size="small"
          icon="el-icon-plus"
          v-permission="['PROJECT_TRACK_CASE:READ+EDIT']"
          :disabled="readOnly || !resourceId"
          type="primary"
          @click="openRelevance"
          >{{ $t($t("commons.add")) }}</el-button
        >
      </div>
    </div>
    <div class="dep-list-wrap" v-if="expand">
      <test-case-relationship-list
        v-if="resourceType === 'TEST_CASE'"
        :case-id="resourceId"
        :read-only="readOnly"
        :relationship-type="relationshipType"
        :version-enable="versionEnable"
        @setCount="setCount"
        @deleteRelationship="handleDelete"
        ref="testCaseRelationshipList"
      />
    </div>
    <div class="split" v-else></div>
  </div>
</template>

<script>
import TestCaseRelationshipList from "./CaseRelationshipTableList";
import { deleteRelationshipEdge } from "@/business/utils/sdk-utils";

export default {
  name: "RelationshipList",
  components: { TestCaseRelationshipList },
  data() {
    return {
      expand: true,
      result: {},
      data: [],
      condition: {},
      options: [],
      value: "",
    };
  },
  props: {
    resourceId: String,
    readOnly: Boolean,
    relationshipType: String,
    title: String,
    resourceType: String,
    versionEnable: Boolean,
  },
  methods: {
    getTableData() {
      this.$refs.testCaseRelationshipList.getTableData();
    },
    openRelevance() {
      this.$refs.testCaseRelationshipList.openRelevance();
    },
    handleDelete(sourceId, targetId) {
      deleteRelationshipEdge(sourceId, targetId).then(() => {
        this.getTableData();
        this.$success(this.$t("commons.delete_success"), false);
      });
    },
    setCount(count) {
      this.$emit("setCount", count);
    },
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
