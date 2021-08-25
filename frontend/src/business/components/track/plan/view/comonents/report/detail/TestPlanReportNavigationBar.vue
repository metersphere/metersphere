<template>
  <div>
    <ms-drawer :class="{'hidden': asideHidden}" :visible="true" :size="10" direction="left" :show-full-screen="false" :is-show-close="false">
      <div class="title-item" v-for="item in data" :key="item.title">
        <i class="el-icon-paperclip"></i>
        <a :href="'#' + item.link">{{ item.title }}</a>
      </div>

      <div class="hiddenBottom" @click.stop="asideHidden = !asideHidden">
        <i v-if="!asideHidden" class="el-icon-arrow-left"/>
        <i v-if="asideHidden" class="el-icon-arrow-right"/>
      </div>
    </ms-drawer>
  </div>
</template>

<script>
import MsDrawer from "@/business/components/common/components/MsDrawer";
export default {
  name: "TestPlanReportNavigationBar",
  components: {MsDrawer},
  data() {
    return {
      asideHidden: true,
      data: [
        {
          link: 'overview',
          title: '概览'
        },
        {
          link: 'summary',
          title: '报告总结'
        },
        {
          link: 'functional',
          title: '功能用例统计分析'
        },
        {
          link: 'api',
          title: '接口用例统计分析'
        },
        {
          link: 'load',
          title: '性能用例统计分析'
        }
      ]
    }
  }
}
</script>

<style scoped>

.hiddenBottom {
  width: 8px;
  height: 50px;
  /*top: calc((100vh - 80px)/3);*/
  right: -10px;
  /*top: 0;*/
  top: 40%;
  line-height: 50px;
  border-radius: 0 15px 15px 0;
  background-color: #acb7c1;
  display: inline-block;
  position: absolute;
  cursor: pointer;
  opacity: 0.6;
  font-size: 2px;
  margin-left: 1px;
}

.hiddenBottom i {
  margin-left: -2px;
}

.hiddenBottom:hover {
  background-color: #783887;
  opacity: 0.8;
  width: 12px;
}

.hiddenBottom:hover i {
  margin-left: 0;
  color: white;
}


.ms-drawer {
  padding: 15px;
  height: 230px !important;
  width: 200px !important;
  top: calc((100vh - 200px)/3) !important;
  border: 1px solid #E6E6E6;
  padding: 10px;
  border-radius: 10px;
  box-sizing: border-box;
  background-color: #FFF;
  overflow: visible !important;
}

.hidden {
  width: 0px !important;
  padding: 0px !important;
}

.title-item {
  margin: 15px;
}

.el-icon-paperclip {
  margin-right: 5px;
}

</style>
