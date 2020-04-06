<template>

  <el-drawer
    :title="testCase.name"
    :before-close="handleClose"
    :visible.sync="dialog"
    direction="ttb"
    size="100%"
    ref="drawer">

    <div>

      <el-row >
        <el-col :span="3" :offset="1">
          <span class="cast_label">优先级：</span>
          <span class="cast_item">{{testCase.priority}}</span>
        </el-col>
        <el-col :span="3">
          <span class="cast_label">用例类型：</span>
          <span class="cast_item" v-if="testCase.type == 'functional'">{{$t('test_track.functional_test')}}</span>
          <span class="cast_item" v-if="testCase.type == 'performance'">{{$t('commons.performance')}}</span>
          <span class="cast_item" v-if="testCase.type == 'api'">{{$t('commons.api')}}</span>
        </el-col>
      </el-row>

      <el-row>
        <el-col :span="3" :offset="1">
          <span class="cast_label">测试方式：</span>
          <span v-if="testCase.method == 'manual'">{{$t('test_track.manual')}}</span>
          <span v-if="testCase.method == 'auto'">{{$t('test_track.auto')}}</span>
        </el-col>
        <el-col :span="3">
          <span class="cast_label">所属模块：</span>
          <span class="cast_item">{{testCase.nodePath}}</span>
        </el-col>
      </el-row>

      <el-row>
        <el-col :offset="1" :span="2">
          <el-button type="success" round
                     :icon="testCase.status == 'Pass' ? 'el-icon-check' : ''"
                     @click="setTestCaseStatus('Pass')"> 成功</el-button>
        </el-col>
        <el-col :span="2">
          <el-button type="danger" round
                     :icon="testCase.status == 'Failure' ? 'el-icon-check' : ''"
                     @click="setTestCaseStatus('Failure')"> 失败</el-button>
        </el-col>
        <el-col :span="2">
          <el-button type="warning" round
                     :icon="testCase.status == 'Blocking' ? 'el-icon-check' : ''"
                     @click="setTestCaseStatus('Blocking')"> 阻塞</el-button>
        </el-col>
        <el-col :span="2">
          <el-button type="info" round
                     :icon="testCase.status == 'Skip' ? 'el-icon-check' : ''"
                     @click="setTestCaseStatus('Skip')"> 跳过</el-button>
        </el-col>
      </el-row>


      <el-row>
        <el-col :span="20" :offset="1">
          <el-table
            :data="testCase.results"
            class="tb-edit"
            :default-sort = "{prop: 'num', order: 'ascending'}"
            highlight-current-row>
            <el-table-column :label="$t('test_track.number')" prop="num" min-width="8%"></el-table-column>
            <el-table-column :label="$t('test_track.step_desc')" prop="desc" min-width="30%">
              <template v-slot:default="scope">
                <span>{{scope.row.desc}}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('test_track.expected_results')" prop="result" min-width="30%">
              <template v-slot:default="scope">
                <span>{{scope.row.result}}</span>
              </template>
            </el-table-column>
            <el-table-column label="实际结果" min-width="30%">
              <template v-slot:default="scope">
                <el-input
                  size="small"
                  v-model="scope.row.actualResult"
                  :placeholder="$t('commons.input_content')"
                  clearable></el-input>
                <span>{{scope.row.actualResult}}</span>
              </template>
            </el-table-column>
            <el-table-column label="步骤执行结果" min-width="15%">
              <template v-slot:default="scope">
                  <el-select v-model="scope.row.stepResult" placeholder="选择执行结果">
                    <el-option label="通过" value="Pass"></el-option>
                    <el-option label="失败" value="Failure"></el-option>
                    <el-option label="阻塞" value="Blocking"></el-option>
                    <el-option label="跳过" value="Skip"></el-option>
                  </el-select>
              </template>
            </el-table-column>
          </el-table>
        </el-col>
      </el-row>

      <el-row >
        <el-col :span="40" :offset="1">
          <span>备注：</span>
          <span>{{testCase.remark}}</span>
          <span v-if="testCase.remark == null" style="color: gainsboro">未填写</span>
        </el-col>
      </el-row>

      <el-row type="flex" justify="end">
        <el-col :span="5">
          <div>
            <el-button @click="cancel">取 消</el-button>
            <el-button type="primary" @click="saveCase">{{ '保 存' }}</el-button>
          </div>
        </el-col>
      </el-row>

    </div>
  </el-drawer>

</template>

<script>
    export default {
      name: "TestPlanTestCaseEdit",
      data() {
        return {
          dialog: false,
          testCase: {}
        };
      },
      methods: {
        handleClose(done) {
          this.dialog = false;
        },
        cancel() {
          this.dialog = false;
        },
        setTestCaseStatus(status) {
          this.testCase.status = status;
        },
        saveCase() {
          let param = {};
          param.id = this.testCase.id;
          param.status = this.testCase.status;
          param.results = JSON.stringify(this.testCase.results);
          this.$post('/test/plan/case/edit', param, () => {
            this.$refs.drawer.closeDrawer();
            this.$message.success("保存成功！");
            this.$emit('refresh');
          });
        }
      }
    }
</script>

<style scoped>


  .tb-edit .el-input {
    display: none;
    color: black;
  }
  .tb-edit .current-row .el-input {
    display: block;
  }
  .tb-edit .current-row .el-input+span {
    display: none;
  }

  .el-row {
    margin-bottom: 3%;
  }


</style>
