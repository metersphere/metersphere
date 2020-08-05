<template>
  <div>
    <el-dialog
      title="批量编辑用例"
      :visible.sync="dialogVisible"
      width="25%"
      class="batch-edit-dialog"
      :destroy-on-close="true"
      @close="handleClose"
    >
      <el-form :model="form" label-position="right" label-width="150px" size="medium" ref="form" :rules="rules">
        <el-form-item :label="$t('test_track.case.batch_update', [size])" prop="type">
          <el-select v-model="form.type" style="width: 80%" @change="changeType">
            <el-option label="用例等级" value="priority"/>
            <el-option label="类型" value="type"/>
            <el-option label="测试方式" value="method"/>
            <el-option label="维护人" value="maintainer"/>
          </el-select>
        </el-form-item>
        <el-form-item label="更新后属性值为" prop="value">
          <el-select v-model="form.value" style="width: 80%" :filterable="filterable">
            <el-option v-for="(option, index) in options" :key="index" :value="option.id" :label="option.name">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="dialogVisible = false"
          @confirm="submit('form')"/>
      </template>
    </el-dialog>
  </div>
</template>

<script>
  import MsDialogFooter from "../../../common/components/MsDialogFooter";
  import {WORKSPACE_ID} from "../../../../../common/js/constants";
  import {listenGoBack, removeGoBackListener} from "../../../../../common/js/utils";

  export default {
    name: "BatchEdit",
    components: {
      MsDialogFooter
    },
    created() {
      this.getMaintainerOptions();
    },
    data() {
      return {
        dialogVisible: false,
        form: {},
        size: 0,
        rules: {
          type: {required: true, message: "请选择属性", trigger: ['blur','change']},
          value: {required: true, message: "请选择属性对应的值", trigger: ['blur','change']}
        },
        options: [],
        priorities: [
          {name: 'P0', id: 'P0'},
          {name: 'P1', id: 'P1'},
          {name: 'P2', id: 'P2'},
          {name: 'P3', id: 'P3'}
        ],
        types: [
          {name: this.$t('commons.functional'), id: 'functional'},
          {name: this.$t('commons.performance'), id: 'performance'},
          {name: this.$t('commons.api'), id: 'api'}
        ],
        methods: [
          {name: this.$t('test_track.case.manual'), id: 'manual'},
          {name: this.$t('test_track.case.auto'), id: 'auto'}
        ],
        maintainers: [],
        filterable: false,
      }
    },
    methods: {
      submit(form) {
        this.$refs[form].validate((valid) => {
          if (valid) {
            this.$emit("batchEdit", this.form);
            this.dialogVisible = false;
          } else {
            return false;
          }
        });
      },
      open() {
        this.dialogVisible = true;
        this.size = this.$parent.selectRows.size;
        listenGoBack(this.handleClose);
      },
      handleClose() {
        this.form = {};
        removeGoBackListener(this.handleClose);
      },
      changeType(val) {
        this.$set(this.form, "value", "");
        this.filterable = val === "maintainer";
        switch (val) {
          case "priority":
            this.options = this.priorities;
            break;
          case "type":
            this.options = this.types;
            break;
          case "method":
            this.options = this.methods;
            break;
          case "maintainer":
            this.options = this.maintainers;
            break;
          default:
            this.options = [];
        }
      },
      getMaintainerOptions() {
        let workspaceId = localStorage.getItem(WORKSPACE_ID);
        this.$post('/user/ws/member/tester/list', {workspaceId: workspaceId}, response => {
          this.maintainers = response.data;
        });
      }
    }
  }
</script>

<style scoped>

</style>
