<template>

  <common-component :title="'测试结果'">

    <template>
      <el-table
        :data="testResults"
        stripe
        style="width: 100%">
        <el-table-column
          prop="module"
          :label="'模块'"
          width="180">
        </el-table-column>
        <el-table-column
          prop="caseCount"
          :label="'用例数'"
          width="180">
        </el-table-column>
        <el-table-column
          prop="passRate"
          :label="'通过率'">
        </el-table-column>
        <el-table-column
          prop="flawCount"
          :label="'缺陷数'">
        </el-table-column>
      </el-table>
    </template>

  </common-component>

</template>

<script>
    import CommonComponent from "./CommonComponent";
    export default {
      name: "TestResultComponent",
      components: {CommonComponent},
      data() {
        return {
        }
      },
      props: {
        testResults: {
          type: Array,
          default() {
            return [
              {
                module: '模块1',
                caseCount: '14',
                passRate: 10.8,
                flawCount: 3
              },
              {
                module: '模块2',
                caseCount: '24',
                passRate: 40,
                flawCount: 6
              },
              {
                module: '模块3',
                caseCount: '50',
                passRate: 76.9,
                flawCount: 8
              }
            ]
          }
        }
      }
    }
</script>

<style scoped>

</style>
