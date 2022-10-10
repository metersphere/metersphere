<template>
  <el-dialog :close-on-click-modal="false" :visible.sync="dialogVisible" width="50%"
             :title="title" v-loading="loading"
             :destroy-on-close="true">
    <el-form ref="form" :model="form" label-width="auto" size="small" :rules="rules">
      <el-row>
        <el-col :span="11">
          <el-form-item :label="$t('commons.name')" prop="name">
            <el-input v-model="form.name" class="form-input"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="11">
          <el-form-item :label="$t('group.type')" prop="type">
            <el-select v-model="form.type" :placeholder="$t('group.select_type')" class="form-input"
                       disabled>
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
      <el-form-item :label="form.scopeId === 'global' ? $t('group.global_group') : $t('group.ws_share')">
        <el-switch v-model="isShare" :disabled="dialogType === 'edit'" @change="change"/>
      </el-form-item>

    </el-form>

    <template v-slot:footer>
      <el-button @click="cancel" size="medium">{{ $t('commons.cancel') }}</el-button>
      <el-button type="primary" @click="onSubmit" size="medium">{{ $t('commons.confirm') }}</el-button>
    </template>
  </el-dialog>
</template>

<script>
import {getCurrentProjectID, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import {createUserGroup, modifyUserGroup} from "../../../api/user-group";

export default {
  name: "EditUserGroup",
  data() {
    return {
      dialogVisible: false,
      form: {
        global: false,
      },
      rules: {
        name: [
          {required: true, message: this.$t('commons.input_name'), trigger: 'blur'},
          {min: 2, max: 50, message: this.$t('commons.input_limit', [2, 50]), trigger: 'blur'},
        ],
        type: [
          {required: true, message: this.$t('group.select_type'), trigger: 'blur'},
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
      isShare: false,
      loading: false
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
        if (!this.form.scopeId) {
          this.form.scopeId = getCurrentProjectID();
        }
        this.loading = createUserGroup(this.form).then(() => {
          this.$success(this.$t('commons.save_success'));
          this.$emit("refresh")
          this.dialogVisible = false;
        });
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
        });
      })
    },
    open(row, type, title) {
      this.title = title;
      this.isSystem = false;
      this.show = true;
      this.dialogVisible = true;
      this.dialogType = type;
      if (row) {
        this.isShare = row.scopeId === getCurrentWorkspaceId();
      }
      this.form = Object.assign({type: 'PROJECT', global: false, scopeId: ''}, row);
    },
    cancel() {
      this.dialogVisible = false;
    },
    change(share) {
      this.form.scopeId = share ? getCurrentWorkspaceId() : getCurrentProjectID();
    },
  }
}
</script>

<style scoped>
.form-input {
  width: 80%;
}
</style>
