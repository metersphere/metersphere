<template>
  <div>
    <el-drawer
      :close-on-click-modal="false"
      :visible.sync="visible"
      :size="widthCalc"
      @close="close"
      destroy-on-close
      :full-screen="isFullScreen"
      ref="relevanceDialog"
      custom-class="file-drawer"
      append-to-body
    >
      <template slot="title">
        <div style="color: #1f2329; font-size: 16px; font-weight: 500">
          {{ dialogTitle }}
        </div>
      </template>
      <case-diff-viewer
        :left-version="leftVersion"
        :right-version="rightVersion"
        :caseId="caseId"
      ></case-diff-viewer>
    </el-drawer>
  </div>
</template>
<script>
import CaseDiffViewer from "@/business/case/components/case/diff/CaseDiffViewer";
export default {
  name: "CaseDiffSideViewer",
  components: { CaseDiffViewer },
  data() {
    return {
      visible: false,
      isFullScreen: false,
      // props 数据
      leftVersion: {},
      rightVersion: {},
      caseId: "",
    };
  },
  props: {
    width: {
      type: Number,
      default: 1152,
    },
    dialogTitle: {
      type: String,
      default() {
        return this.$t("case.version_comparison");
      },
    },
  },
  computed: {
    widthCalc() {
      if (!isNaN(this.width)) {
        //计算rem
        let remW = (this.width / 1440) * 100;
        let standW = (1152 / 1440) * 100;
        return remW > standW ? remW : standW + "%";
      }
      return this.width;
    },
  },
  methods: {
    open(leftVersion, rightVersion, caseId) {
      this.leftVersion = leftVersion;
      this.rightVersion = rightVersion;
      this.caseId = caseId;
      this.visible = true;
    },
    close() {
      this.visible = false;
    },
  },
};
</script>
<style scoped lang="scss">
@import "@/business/style/index.scss";
.content-box {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.body-wrap {
  display: flex;
  /* height: px2rem(763); */
  /* min-height: px2rem(763); */
  flex: 9;
  .aside-wrap {
    width: px2rem(268);
    border-right: 1px solid rgba(31, 35, 41, 0.15);
    padding: px2rem(24) px2rem(24) 0 px2rem(24);
  }
  .content-wrap {
    width: px2rem(930);
  }
}
.footer-wrap {
  flex: 1;
  width: 100%;
  height: px2rem(80);
  background: #ffffff;
  box-shadow: 0px -1px 4px rgba(31, 35, 41, 0.1);
}

.footer-wrap .options {
  height: 80px;
  background: #ffffff;
  box-shadow: 0px -1px 4px rgba(31, 35, 41, 0.1);
  overflow: hidden;
}
.footer-wrap .options-btn {
  display: flex;
  margin-top: 24px;
  height: 32px;
  margin-right: 24px;
  float: right;
}
</style>
