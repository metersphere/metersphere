<template>


  <common-component :title="'基础信息'">

    <el-row type="flex" justify="space-between">
      <el-col :span="12">
        <span>所属项目：</span>
        <span class="item-value">{{reportInfo.project}}</span>
      </el-col>
      <el-col :span="12">
        <span>测试负责人：</span>
        <span class="item-value">{{reportInfo.principal}}</span>
      </el-col>
    </el-row>

    <el-row type="flex" justify="space-between">
      <el-col :span="12">
        <span>开始时间：</span>
        <span class="item-value">{{reportInfo.startTime}}</span>
      </el-col>
      <el-col :span="12">
        <span>结束时间：</span>
        <span class="item-value">{{reportInfo.endTime}}</span>
      </el-col>
    </el-row>

    <el-row type="flex" justify="space-between">
      <el-col :span="12">
        <span>测试执行人：</span>
        <span v-for="item in reportInfo.executors" :key="item">{{item}}</span>
      </el-col>
    </el-row>

  </common-component>

</template>

<script>
    import CommonComponent from "./CommonComponent";
    export default {
      name: "BaseInfoComponent",
      components: {CommonComponent},
      data() {
        return {
        }
      },
      props: {
        reportInfo: {
          type: Object,
          default() {
            return {
              project: '项目名称',
              principal: '由丽媛',
              executors: ['由丽媛','王振','陈建星'],
              startTime: '2020-6-18',
              endTime: '2020-6-18'
            }
          }
        }
      }
    }
</script>

<style scoped>

  span {
    margin-right: 5px;
    display: inline-block;
  }

  .el-col span:first-child {
    font-weight: bold;
    width: 100px;
  }

  .el-row {
    height: 60px;
  }

</style>
