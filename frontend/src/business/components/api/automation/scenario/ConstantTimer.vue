<template>
  <div>
    <el-card>
      <el-row>
        <div class="el-step__icon is-text ms-api-col">
          <div class="el-step__icon-inner">{{timer.index}}</div>
        </div>
        <el-button class="ms-title-buttion" size="small">{{$t('api_test.automation.wait_controller')}}</el-button>
        <el-input-number class="width-100" size="small" v-model="timer.delay" :min="0" :step="1000"/>
        ms
        <div style="margin-right: 20px; float: right">
          <el-switch v-model="timer.enable" style="margin-left: 10px"/>
          <el-button size="mini" icon="el-icon-copy-document" circle @click="copyRow" style="margin-left: 10px"/>
          <el-button size="mini" icon="el-icon-delete" type="danger" circle @click="remove" style="margin-left: 10px"/>
        </div>
      </el-row>
    </el-card>
  </div>
</template>

<script>
  export default {
    name: "MsConstantTimer",
    props: {
      timer: {},
      node: {},
    },
    data() {
      return {}
    },
    methods: {
      remove() {
        this.$emit('remove', this.timer, this.node);
      },
      copyRow() {
        this.$emit('copyRow', this.timer, this.node);
      },
    }
  }
</script>

<style scoped>
  .width-100 {
    width: 40%
  }

  .ms-api-col {
    background-color: #F2F9EE;
    border-color: #67C23A;
    margin-right: 10px;
    color: #67C23A;
  }

  .ms-title-buttion {
    background-color: #F2F9EE;
    color: #67C23A;
    margin-right: 20px;
  }

  /deep/ .el-card__body {
    padding: 15px;
  }
</style>
