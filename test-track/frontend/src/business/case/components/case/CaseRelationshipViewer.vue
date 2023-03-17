<!--
    依赖关系 容器
 -->
<template>
  <div class="dependencies-container">
    <!-- 图标展示 -->
    <div class="dep-header-wrap">
      <div class="header-row" @click="openGraph">
        <div class="dep-icon">
          <img
            src="/assets/module/figma/icon_organization_outlined.svg"
            alt=""
          />
        </div>
        <div class="dep-label">{{ $t("case.dependencies") }}</div>
      </div>
    </div>
    <!-- 前置用例 -->
    <div class="dep-pre-wrap">
      <case-relationship-list
        :read-only="readOnly"
        :title="
          resourceType === 'TEST_CASE'
            ? $t('commons.relationship.pre_case')
            : $t('commons.relationship.pre_api')
        "
        relationship-type="PRE"
        :resource-id="resourceId"
        :version-enable="versionEnable"
        @setCount="setPreCount"
        :resource-type="resourceType"
        ref="preRelationshipList"
      ></case-relationship-list>
    </div>
    <!-- 后置用例 -->
    <div class="dep-post-wrap">
      <case-relationship-list
        :read-only="readOnly"
        :version-enable="versionEnable"
        :title="
          resourceType === 'TEST_CASE'
            ? $t('commons.relationship.post_case')
            : $t('commons.relationship.post_api')
        "
        relationship-type="POST"
        :resource-id="resourceId"
        @setCount="setPostCount"
        :resource-type="resourceType"
        ref="postRelationshipList"
      ></case-relationship-list>
    </div>

    <relationship-graph-drawer
      v-permission
      :graph-data="graphData"
      @closeRelationGraph="closeRelationGraph"
      ref="relationshipGraph"
    />
  </div>
</template>

<script>
import RelationshipGraphDrawer from "metersphere-frontend/src/components/graph/RelationshipGraphDrawer";
import RelationshipList from "@/business/common/RelationshipList";
import { getRelationshipGraph } from "@/api/graph";
import CaseRelationshipList from "../common/CaseRelationshipList";

export default {
  name: "CaseRelationshipViewer",
  components: {
    RelationshipGraphDrawer,
    RelationshipList,
    CaseRelationshipList,
  },
  props: ["resourceId", "resourceType", "readOnly", "versionEnable"],
  data() {
    return {
      graphData: {},
      preCount: 0,
      postCount: 0,
    };
  },
  methods: {
    open() {
      this.$refs.preRelationshipList.getTableData();
      this.$refs.postRelationshipList.getTableData();
    },
    openGraph() {
      if (!this.resourceId) {
        this.$warning(this.$t("api_test.automation.save_case_info"));
        return;
      }
      getRelationshipGraph(this.resourceId, this.resourceType).then((r) => {
        this.graphData = r.data;
        this.$refs.relationshipGraph.open();
        this.$emit("openDependGraphDrawer", true);
      });
    },
    closeRelationGraph() {
      this.$emit("openDependGraphDrawer", false);
    },
    setPreCount(count) {
      this.preCount = count;
      this.$emit("setCount", this.preCount + this.postCount);
    },
    setPostCount(count) {
      this.postCount = count;
      this.$emit("setCount", this.preCount + this.postCount);
    },
  },
};
</script>

<style scoped>
.left-icon {
  width: 4%;
  display: inline-block;
  position: absolute;
  top: 25px;
}
</style>
<style scoped lang="scss">
@import "@/business/style/index.scss";
.dependencies-container {
  .dep-header-wrap {
    margin-top: 24px;
    width: 98px;
    min-width: 98px;
    height: 32px;
    background: #ffffff;
    border: 1px solid #DCDFE6;
    border-radius: 4px;
    gap: 4px;
    .header-row {
      display: flex;
      color: #1f2329;
      border-radius: 4px;
      padding-left: 12.58px;
      align-items: center;
      height: 100%;
      cursor: pointer;
      .dep-icon {
        margin-right: 4.58px;
        width: 14px;
        height: 14px;
        img {
          width: 100%;
          height: 100%;
        }
      }

      .dep-label {
      }
    }

    .dep-pre-wrap {
      max-height: px2rem(269);
    }
    .dep-post-wrap {
      max-height: px2rem(269);
    }
  }
}

.dependencies-container .dep-header-wrap .header-row:hover {
  background-color: whitesmoke;
}
</style>
