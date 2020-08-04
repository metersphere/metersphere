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
          <el-select v-model="form.type" style="width: 80%">
            <el-option label="用例等级" value="priority"/>
            <el-option label="类型" value="type"/>
            <el-option label="测试方式" value="method"/>
            <el-option label="维护人" value="maintainer"/>
          </el-select>
        </el-form-item>
        <el-form-item label="更新后属性值为" prop="value">
          <el-select v-model="form.value" style="width: 80%">
            <el-option label="值1" value="value1"/>
            <el-option label="值2" value="value2"/>
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

  export default {
    name: "BatchEdit",
    components: {
      MsDialogFooter
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
      }
    },
    methods: {
      submit(form) {
        this.$refs[form].validate((valid) => {
          if (valid) {
            this.$emit("submit", this.form);
          } else {
            return false;
          }
        });
      },
      open() {
        this.dialogVisible = true;
        this.size = this.$parent.selectRows.size;
      },
      handleClose() {
        this.form = {};
      }
    }
  }
</script>

<style scoped>

</style>
