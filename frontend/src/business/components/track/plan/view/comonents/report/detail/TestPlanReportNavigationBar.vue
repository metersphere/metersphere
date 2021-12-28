<template>
  <div>
    <ms-drawer class="drawer-content" :visible="true" :size="10" direction="left" :show-full-screen="false" :is-show-close="false">
      <div class="title-item" >
         <span class="title-name">{{$t('test_track.report.content')}}</span>
        <el-tabs tab-position="right" v-model="activeName">
          <el-tab-pane v-for="item in data" :key="item.title" :label="item.title" :name="item.link"/>
        </el-tabs>
      </div>
        <div class="hiddenBottom">
          <span :class="navBtnClass">{{$t('test_track.report.content')}}</span>
        </div>
    </ms-drawer>
  </div>
</template>

<script>
import MsDrawer from "@/business/components/common/components/MsDrawer";
import {DEFAULT_LANGUAGE, EN_US} from "@/common/js/constants";
import {getCurrentUser} from "@/common/js/utils";
export default {
  name: "TestPlanReportNavigationBar",
  components: {MsDrawer},
  props: {
    isTemplate: Boolean,
    overviewEnable: Boolean,
    summaryEnable: Boolean,
    functionalEnable: Boolean,
    apiEnable: Boolean,
    loadEnable: Boolean,
  },
  data() {
    return {
      activeName: "1",
      data: [],
      contents: [
        {
          link: 'overview',
          title: this.$t('test_track.report.overview'),
        },
        {
          link: 'summary',
          title: this.$t('test_track.report.report_summary'),
        },
        {
          link: 'functional',
          title: this.$t('test_track.report.analysis_functional'),
        },
        {
          link: 'api',
          title: this.$t('test_track.report.analysis_api'),
        },
        {
          link: 'load',
          title: this.$t('test_track.report.analysis_load'),
        }
      ]
    }
  },
  watch: {
    activeName() {
      let url = new URL(window.location.href);
      if (this.isTemplate) {
        window.location.href = window.location.href.split('#')[0] + '#' + this.activeName;
      } else {
        window.location.href = url.origin + '#' + this.activeName;
      }
    },
    overviewEnable() {
      this.setData();
    },
    summaryEnable() {
      this.setData();
    },
    functionalEnable() {
      this.setData();
    },
    apiEnable() {
      this.setData();
    },
    loadEnable() {
      this.setData();
    },
  },
  computed: {
    navBtnClass() {
      let lang = getCurrentUser().language;
      if (!lang) {
        lang = localStorage.getItem(DEFAULT_LANGUAGE);
      }
      if (lang === EN_US) {
        return 'en-button-span';
      } else {
        return 'zh-button-span';
      }
    }
  },
  mounted() {
    this.setData();
  },
  methods: {
    setData() {
      let keyMap = new Map([
        ['overview', this.overviewEnable],
        ['summary', this.summaryEnable],
        ['functional', this.functionalEnable],
        ['api', this.apiEnable],
        ['load', this.loadEnable],
      ]);
      this.data = [];
      this.contents.forEach(item => {
        if (keyMap.get(item.link)) {
          this.data.push(item);
        }
      });
    }
  }
}
</script>

<style scoped>

.hiddenBottom {
  width: 20px;
  height: 60px;
  /*top: calc((100vh - 80px)/3);*/
  right: -27px;
  padding: 3px;
  /*top: 0;*/
  top: 40%;
  /*line-height: 30px;*/
  border-radius: 0 15px 15px 0;
  /*background-color: #acb7c1;*/
  background-color: #783887;
  color: white;
  display: inline-block;
  position: absolute;
  cursor: pointer;
  opacity: 0.5;
  font-size: 10px;
  font-weight: bold;
  margin-left: 1px;
}

.en-button-span {
  writing-mode: vertical-rl;
  letter-spacing: 0.1em;
}

.zh-button-span {
  writing-mode: vertical-rl;
  letter-spacing: 0.8em;
  margin-top: 13px;
}

.hiddenBottom i {
  margin-left: -2px;
}

.hiddenBottom:hover {
  /*background-color: #777979;*/
  opacity: 0.8;
  /*width: 12px;*/
}

.hiddenBottom:hover i {
  margin-left: 0;
  color: white;
}


.ms-drawer {
  padding: 15px;
  height: 300px !important;
  width: 230px !important;
  top: calc((100vh - 200px)/3) !important;
  border: 1px solid #E6E6E6;
  border-radius: 10px;
  box-sizing: border-box;
  background-color: #FFF;
  overflow: visible !important;
}

.drawer-content {
    width: 0px !important;
    padding: 0px !important;
}

.drawer-content >>> .el-tabs__nav{
    width: 140px !important;
}

.drawer-content:hover{
  padding: 15px !important;
  height: 300px !important;
  width: 230px !important;
}

.title-item {
  padding: 5px
}

.el-icon-paperclip {
  margin-right: 5px;
}

.title-name {
  font-size: 18px;
  margin-left: 15px;
}
</style>
