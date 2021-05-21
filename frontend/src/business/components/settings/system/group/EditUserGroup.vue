<template>
  <el-dialog :close-on-click-modal="false" :visible.sync="dialogVisible" width="50%"
             :title="dialogTitle"
             :destroy-on-close="true">
    <el-form ref="form" :model="form" label-width="auto" size="small" :rules="rules">
      <el-row>
        <el-col :span="11">
          <el-form-item label="名称" prop="name">
            <el-input v-model="form.name"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="11" :offset="2">
          <el-form-item label="所属类型" prop="type">
            <el-select v-model="form.type" placeholder="请选择所属类型" style="width: 100%" @change="changeGroup">
              <el-option label="系统" value="SYSTEM"></el-option>
              <el-option label="组织" value="ORGANIZATION"></el-option>
              <el-option label="工作空间" value="WORKSPACE"></el-option>
              <el-option label="项目" value="PROJECT"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="描述" prop="description">
        <el-input type="textarea" v-model="form.description"></el-input>
      </el-form-item>
      <el-form-item label="全局用户组">
        <el-switch v-model="form.global" :disabled="dialogType === 'edit'" @change="change(form.global)"></el-switch>
      </el-form-item>

      <el-form-item label="所属组织" v-if="show" prop="scopeId">
        <el-select v-model="form.scopeId" placeholder="请选择所属组织" style="width: 100%;" :disabled="dialogType === 'edit'" clearable>
          <el-option v-for="item in organizations" :key="item.id" :label="item.name" :value="item.id"/>
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
import {GROUP_SYSTEM} from "@/common/js/constants";
import {getCurrentUserId} from "@/common/js/utils";

export default {
  name: "EditUserGroup",
  data() {
    return {
      dialogVisible: false,
      form: {
        global: false
      },
      rules: {
        name: [
          {required: true, message: '请输入名称', trigger: 'blur'},
          {min: 2, max: 50, message: this.$t('commons.input_limit', [2, 50]), trigger: 'blur'},
        ],
        type: [
          {required: true, message: '请选择所属类型', trigger: 'blur'},
        ],
        description: [
          {min: 2, max: 90, message: this.$t('commons.input_limit', [2, 90]), trigger: 'blur'},
        ],
        scopeId: [
          {required: true, message: '请选择所属组织', trigger: 'blur'},
        ]
      },
      dialogType: '',
      isSystem: false,
      show: true,
      organizations: []
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
      this.$refs['form'].validate(valid => {
        if (valid) {
          this.$post("/user/group/add", this.form, () => {
            this.$success(this.$t('commons.save_success'));
            this.$emit("refresh")
            this.dialogVisible = false;
          })
        } else {
          return false;
        }
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
      this.isSystem = false;
      this.show = true;
      this.dialogVisible = true;
      this.dialogType = type;
      this.form = Object.assign({}, row);
      if (type !== 'create') {
        if (this.form.type === GROUP_SYSTEM) {
          this.form.global = true;
          this.show = false;
        } else {
          this.form.global = this.form.scopeId === "global";
          this.show = !this.form.global;
        }
      }
      this.getOrganization();
    },
    cancel() {
      this.dialogVisible = false;
    },
    change(global) {
      this.show = this.isSystem ? false : !global;
    },
    changeGroup(val) {
      if (val === GROUP_SYSTEM) {
        this.isSystem = true;
        this.$set(this.form, "global", true);
        this.change(true);
      } else {
        this.isSystem = false;
      }
    },
    getOrganization() {
      this.$get("/user/group/org/" + getCurrentUserId(), res => {
        let data = res.data;
        if (data) {
          this.organizations = data;
        }
      })
    }
  }
}
</script>

<style scoped>

</style>
