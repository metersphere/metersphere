<template>
  <el-drawer :visible="visible" append-to-body>
    <ms-drawer
      :size="85"
      @close="close"
      direction="right"
      :show-full-screen="false"
      :is-show-close="false"
      style="height: auto"
      ref="msDrawer"
    >
      <template v-slot:header>
        <drawer-header
          :title="$t('commons.relationship.graph')"
          @close="close"
          @export="exportCharts"
        />
      </template>
      <div>
        <el-scrollbar class="graph-scrollbar">
          <relationship-graph
            :height="height"
            :width="width"
            :data="graphData.data"
            :links="graphData.links"
            @finished="handleFinished"
            ref="relationshipGraph"
          />
        </el-scrollbar>
      </div>
    </ms-drawer>
  </el-drawer>
</template>

<script>
import MsDrawer from "../MsDrawer";
import DrawerHeader from "../head/DrawerHeader";
import RelationshipGraph from "./RelationshipGraph";

export default {
  name: "RelationshipGraphDrawer",
  components: { RelationshipGraph, DrawerHeader, MsDrawer },
  props: ["graphData"],
  data() {
    return {
      visible: false,
      height: "calc(100vh - 48px)",
      width: "calc(100vh - 240px)",
    };
  },
  methods: {
    exportCharts(type) {
      this.$refs.relationshipGraph.exportCharts(
        this.$t("commons.relationship.graph"),
        type
      );
    },
    open() {
      this.visible = true;

      this.$nextTick(() => {
        this.graphData.links.forEach((item) => {
          item.lineStyle = {
            curveness: item.curveness * 2,
          };
          item.emphasis = {
            lineStyle: {
              color: "#783887", //高亮颜色
              width: 5,
            },
          };
        });

        this.height = "calc(100vh - 48px)";
        this.width = null;

        let clientWidth = document.body.clientWidth;
        let clientHeight = document.body.clientHeight;
        let widthCoefficient = 86; // 85.33333
        let heightCoefficient = 86; // 53.1578947368421

        let width = this.graphData.xunitCount * widthCoefficient;
        if (width > clientWidth) {
          this.width = width;
        }
        let height = this.graphData.yunitCount * heightCoefficient;
        if (height > clientHeight) {
          this.height = height;
        }

        this.$nextTick(() => {
          this.$refs.relationshipGraph.reload();
        });
      });
      if (this.$refs.msDrawer) {
        this.$refs.msDrawer.init();
      }
    },
    handleFinished() {
      // 滚动条设置居中
      let graphScrollbar = document.querySelector(
        ".graph-scrollbar .el-scrollbar__wrap"
      );
      let graphScrollbarView = document.querySelector(
        ".graph-scrollbar .el-scrollbar__wrap .ms-chart"
      );
      graphScrollbar.scrollLeft =
        (graphScrollbarView?.clientWidth - graphScrollbar?.clientWidth) / 2;
    },
    close() {
      this.visible = false;
    },
  },
};
</script>

<style scoped></style>
