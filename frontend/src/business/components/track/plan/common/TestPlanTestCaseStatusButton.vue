<template>
  <el-row type="flex" justify="start" :gutter="20">
    <el-col>
      <el-button type="success" round
                 :icon="status == 'Pass' ? 'el-icon-check' : ''"
                 @click="setStatus('Pass')"> {{$t('test_track.pass')}}</el-button>
    </el-col>
    <el-col >
      <el-button type="danger" round
                 :icon="status == 'Failure' ? 'el-icon-check' : ''"
                 @click="setStatus('Failure')"> {{$t('test_track.failure')}}</el-button>
    </el-col>
    <el-col >
      <el-button type="warning" round
                 :icon="status == 'Blocking' ? 'el-icon-check' : ''"
                 @click="setStatus('Blocking')"> {{$t('test_track.blocking')}}</el-button>
    </el-col>
    <el-col >
      <el-button type="info" round
                 :icon="status == 'Skip' ? 'el-icon-check' : ''"
                 @click="setStatus('Skip')"> {{$t('test_track.skip')}}</el-button>
    </el-col>
  </el-row>
</template>

<script>
    export default {
      name: "TestPlanTestCaseStatusButton",
      data() {
        return {
        }
      },
      props: {
        status: {
          type: String
        }
      },
      methods: {
        setStatus(status) {
          this.$emit('statusChange', status);
        }
      }
    }
</script>

<style scoped>
  .el-row {
    width: 50%;
  }

</style>
