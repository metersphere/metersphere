<template>
  <div class="create-box">
    <el-tooltip class="item" effect="dark" :content="tips" placement="left">
      <el-button @click="exec()" type="primary" size="mini" circle>
        <font-awesome-icon :icon="['fas', 'plus']"/>
      </el-button>
    </el-tooltip>
  </div>
</template>

<script>
  export default {
    name: "MsCreateBox",
    props: {
      tips: String,
      exec: Function
    }
  }
</script>

<style scoped>
  .create-box {
    width: 100%;
    background: #FCFCFC;
    border-bottom-left-radius: 3px;
    border-bottom-right-radius: 3px;
    box-shadow: 0 5px 20px 0 rgba(0, 0, 0, .1);
    height: 60px;
    line-height: 60px;
    text-align: center;
  }

  .create-box .el-button--mini.is-circle {
    width: 28.5px;
  }
</style>
