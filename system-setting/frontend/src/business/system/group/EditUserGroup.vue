<template>
  <el-dialog :close-on-click-modal="false" :visible.sync="dialogVisible" width="50%"
             :title="title" v-loading="loading"
             :destroy-on-close="true">
    <el-form ref="form" :model="form" label-width="110px" size="small" :rules="rules">
      <el-row>
        <el-col :span="11">
          <el-form-item :label="$t('commons.name')" prop="name">
            <el-input v-model="form.name" class="form-input"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="11" >
          <el-form-item :label="$t('group.type')" prop="type">
            <el-select v-model="form.type" :placeholder="$t('group.select_type')"  @change="changeGroup" :disabled="dialogType === 'edit'" class="form-input">
              <el-option :label="$t('group.system')" value="SYSTEM"></el-option>
              <el-option :label="$t('group.workspace')" value="WORKSPACE"></el-option>
              <el-option :label="$t('group.project')" value="PROJECT"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item :label="$t('group.description')" prop="description">
        <el-input type="textarea" v-model="form.description" style="width: 83%"></el-input>
      </el-form-item>
      <el-form-item :label="$t('group.global_group')">
        <el-switch v-model="form.global" :disabled="dialogType === 'edit' || form.type === 'SYSTEM'" @change="change(form.global)"></el-switch>
      </el-form-item>

      <el-form-item :label="showLabel" v-if="show" prop="scopeId">
        <el-select v-model="form.scopeId" :placeholder="$t('project.please_choose_workspace')" :disabled="dialogType === 'edit'" clearable style="width: 83%">
          <el-option v-for="item in workspaces" :key="item.id" :label="item.name" :value="item.id"/>
        </el-select>
      </el-form-item>
    </el-form>

    <template v-slot:footer>
      <el-button @click="cancel" size="medium">{{ $t('commons.cancel') }}</el-button>
      <el-button type="primary" @click="onSubmit" size="medium">{{ $t('commons.confirm') }}</el-button>
    </template>
  </el-dialog>
</template>

<script>
import {GROUP_SYSTEM} from "metersphere-frontend/src/utils/constants";
import {createUserGroup, modifyUserGroup} from "../../../api/user-group";
import {getWorkspaces} from "../../../api/workspace";
import {getproject} from "../../../api/project";

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
          {required: true, message: this.$t('commons.input_name'), trigger: 'blur'},
          {min: 2, max: 50, message: this.$t('commons.input_limit', [2, 50]), trigger: 'blur'},
        ],
        type: [
          {required: true, message:  this.$t('group.select_type'), trigger: 'blur'},
        ],
        description: [
          {min: 2, max: 90, message: this.$t('commons.input_limit', [2, 90]), trigger: 'blur'},
        ],
        scopeId: [
          {required: true, message: this.$t('group.select_belong_organization'), trigger: 'blur'},
        ]
      },
      dialogType: '',
      isSystem: false,
      show: true,
      workspaces: [],
      title: this.$t('group.create'),
      loading: false,
      showLabel: '',
    }
  },
  props: {
    dialogTitle: {
      type: String,
      default() {
        return this.$t('group.create')
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
    },
    create() {
      this.$refs['form'].validate(valid => {
        if (!valid) {
          return false;
        }
        this.loading = createUserGroup(this.form).then(() => {
          this.$success(this.$t('commons.save_success'));
          this.$emit("refresh")
          this.dialogVisible = false;
        })
      })
    },
    edit() {
      this.$refs['form'].validate(valid => {
        if (!valid) {
          return false;
        }
        this.loading = modifyUserGroup(this.form).then(() => {
          this.$success(this.$t('commons.modify_success'));
          this.$emit("refresh")
          this.dialogVisible = false;
        })
      })
    },
    open(row, type, title) {
      this.workspaces = [];
      this.showLabel = '';
      this.title = title;
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
      this.getWorkspace();
      if(type === 'edit'){
        this.getproject();
      }
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
    getWorkspace() {
      getWorkspaces().then(res => {
        let data = res.data;
        if (data) {
          this.workspaces = data;
          let name = this.workspaces.find(item => item.id === this.form.scopeId)
          if (name) {
            this.showLabel = this.$t('project.owning_workspace');
          } else {
            this.showLabel = this.$t('project.owning_project')
          }
        }
      })
    },
    getproject() {
      getproject().then(res => {
        let data = res.data;
        if (data) {
          this.workspaces = this.workspaces.concat(data);
        }
      })
    }
  }
}
</script>

<style scoped>
.form-input{
  width: 80%;
}
</style>
