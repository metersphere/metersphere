<template>
  <div>
    <el-card>
      <el-row>
        <div class="el-step__icon is-text ms-api-col" style="float: left">
          <div class="el-step__icon-inner">{{timer.$treeNodeId}}</div>
        </div>
        <div>
          <el-row :gutter="10" type="flex" align="middle">
            <el-col :span="2">{{ $t('api_test.request.wait') }}</el-col>
            <el-col :span="10">
              <el-input-number class="width-100" size="small" v-model="timer.delay" :min="0" :step="1000"/>
            </el-col>
            <el-col :span="2">ms</el-col>
            <el-col :span="8">
              <el-switch v-model="timer.enable" :inactive-text="$t('api_test.scenario.enable_disable')"/>
            </el-col>
            <el-col :span="2">
              <el-button size="mini" type="danger" icon="el-icon-delete" circle @click="remove"/>
            </el-col>
          </el-row>
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
        this.$emit('remove', this.timer,this.node);
      }
    }
  }
</script>

<style scoped>
  .width-100 {
    width: 100%
  }

  .ms-api-col {
    background-color: #F2F9EE;
    border-color: #67C23A;
    margin-right: 10px;
    color: #67C23A;
  }

  /deep/ .el-card__body {
    padding: 15px;
  }
</style>
