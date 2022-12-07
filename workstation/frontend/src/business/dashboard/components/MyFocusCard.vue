<template>
  <el-card class="table-card" >
    <el-row :gutter="10" style="margin-bottom: 20px">
      <el-col :span="24">
        <span class="top-css">{{ $t('workstation.focus')  }}</span>
      </el-col>
    </el-row>
  </el-card>
</template>

<script>
export default {
  name: "MyFocusCard",
  data() {
    return {
      contentArray:[
        {
          name:"功能用例",
          value:123

        },
        {
          name:"测试计划",
          value:123
        },
        {
          name:"用例评审",
          value:123
        },
        {
          name:"缺陷管理",
          value:123
        },
        {
          name:"接口定义",
          value:123
        },
        {
          name:"接口用例",
          value:123
        },
        {
          name:"接口自动化",
          value:123
        },
        {
          name:"性能测试",
          value:123
        },
      ]
    };
  }
}
</script>

<style scoped>
.top-css {
  left: 24px;
  font-weight: 650;
  font-style: normal;
  font-size: 18px;
  position: absolute;
  align-self: flex-start;
  padding: 0px 0px 0px 0px;
  box-sizing: border-box;
  width: 100%;
}
:deep(.el-card__body ){
  margin-top: 24px;
}
</style>
