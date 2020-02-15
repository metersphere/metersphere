<template>
  <el-row>
    <el-col :span="8">
      <div>
        <el-row>
          <el-col :span="12">
            <div>
              并发用户：
              <el-input
                type="number"
                placeholder="请输入线程数"
                v-model="threadNumber"
                show-word-limit
              >
              </el-input>
            </div>
          </el-col>
          <el-col :span="12">
            <div>
              压测时长：
              <el-input
                type="number"
                placeholder="请输入时长（min）"
                v-model="duration"
                show-word-limit
              >
              </el-input>
            </div>
          </el-col>
        </el-row>
        <el-row>
          <div>
            在
            <div style="width: 80px;">
              <el-input
                type="number"
                v-model="rampUpTime"
                show-word-limit
              >
              </el-input>
            </div>
            分钟内，分
            <div style="width: 80px;">
              <el-input
                type="number"
                v-model="rampUpCount"
                show-word-limit
              >
              </el-input>
            </div>
            次增加并发用户
          </div>
        </el-row>
      </div>
    </el-col>
    <el-col :span="16">
      压力预估图

    </el-col>
  </el-row>
</template>

<script>
  export default {
    name: "TestPlanPressureConfig",
    data() {
      return {
        threadNumber: 10,
        duration: 10,
        rampUpTime: 5,
        rampUpCount: 2,
      }
    },
    methods: {
      testChange() {
        this._changeTestPlan(function (testPlan) {
          testPlan.pressureConifg = "1"
        })
      },
      _changeTestPlan(updateTestPlanFunc) {
        this.$emit('change-test-plan', updateTestPlanFunc);
      },
    }
  }
</script>
