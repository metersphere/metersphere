<template>
  <el-dialog :close-on-click-modal="false" :visible.sync="dialogVisible" width="50%"
             :title="dialogTitle"
             :destroy-on-close="true">
    <el-form ref="form" :model="form" label-width="auto" size="small" :rules="rules">
      <el-row>
        <el-col :span="11">
          <el-form-item label="名称">
            <el-input v-model="form.name"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="11" :offset="2">
          <el-form-item label="所属类型">
            <el-select v-model="form.type" placeholder="请选择所属类型" style="width: 100%">
              <el-option label="系统" value="system"></el-option>
              <el-option label="组织" value="organization"></el-option>
              <el-option label="工作空间" value="workspace"></el-option>
              <el-option label="项目" value="project"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>


      <el-form-item label="描述">
        <el-input type="textarea" v-model="form.description"></el-input>
      </el-form-item>
      <el-form-item label="全局用户组">
        <el-switch v-model="form.global" :disabled="dialogType === 'edit'"></el-switch>
      </el-form-item>

      <el-form-item label="所属组织" v-if="!form.global">
        <el-select v-model="form.scopeId" placeholder="请选择所属组织" style="width: 100%;" :disabled="dialogType === 'edit'">
          <el-option label="区域一" value="shanghai"></el-option>
          <el-option label="区域二" value="beijing"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSubmit">确定</el-button>
        <el-button @click="cancel">取消</el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<script>
export default {
  name: "EditUserGroup",
  data() {
    return {
      dialogVisible: false,
      form: {
        global: false
      },
      rules: {

      },
      dialogType: '',
    }
  },
  props: {
    dialogTitle: {
      type: String,
      default() {
        return "创建用户组"
      }
    }
  },
  methods: {
    onSubmit() {
      if (this.dialogType === 'create') {
        this.create();
        return;
      }

      if (this.dialogType === 'edit') {
        this.edit();
      }

      if (this.dialogType === 'copy') {
        return;
      }

    },
    create() {
      this.$post("/user/group/add", this.form, () => {
        this.$success(this.$t('commons.save_success'));
        this.$emit("refresh")
        this.dialogVisible = false;
      })
    },
    edit() {
      this.$post("/user/group/edit", this.form, () => {
        this.$success(this.$t('commons.modify_success'));
        this.$emit("refresh")
        this.dialogVisible = false;
      })
    },
    open(row, type) {
      this.dialogVisible = true;
      this.dialogType = type;
      this.form = Object.assign({}, row);
      this.form.global = this.form.scopeId === "global";
    },
    cancel() {
      this.dialogVisible = false;
    }
  }
}
</script>

<style scoped>

</style>
