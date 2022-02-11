<template>
  <el-dialog :close-on-click-modal="false" :visible.sync="dialogVisible" width="50%"
             :title="title"
             :destroy-on-close="true">
    <el-form ref="form" :model="form" label-width="auto" size="small" :rules="rules">
      <el-row>
        <el-col :span="11">
          <el-form-item :label="$t('commons.name')" prop="name">
            <el-input v-model="form.name"  class="form-input"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="11" >
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
      <el-form-item :label="$t('group.global_group')">
        <el-switch v-model="form.global" :disabled="dialogType === 'edit'"
                   @change="change(form.global)"></el-switch>
      </el-form-item>

    </el-form>

    <template v-slot:footer>
      <el-button @click="cancel" size="medium">{{ $t('commons.cancel') }}</el-button>
      <el-button type="primary" @click="onSubmit" size="medium">{{ $t('commons.confirm') }}</el-button>
    </template>
  </el-dialog>
</template>

<script>
import {GROUP_SYSTEM} from "@/common/js/constants";
import {getCurrentUserId, getCurrentWorkspaceId} from "@/common/js/utils";

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
      flag: false
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
        if (valid) {
          this.form.scopeId = getCurrentWorkspaceId();
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
      this.$refs['form'].validate(valid => {
        if (valid) {
          this.$post("/user/group/edit", this.form, () => {
            this.$success(this.$t('commons.modify_success'));
            this.$emit("refresh")
            this.dialogVisible = false;
          })
        } else {
          return false;
        }
      })
    },
    open(row, type, title) {
      this.title = title;
      this.isSystem = false;
      this.show = true;
      this.dialogVisible = true;
      this.dialogType = type;
      this.form = Object.assign({type: 'PROJECT' ,global: false , scopeId: getCurrentWorkspaceId()}, row);
    },
    cancel() {
      this.dialogVisible = false;
    },
    change(global) {
      this.$get("/user/group/list/ws/" + getCurrentWorkspaceId() + "/" + getCurrentUserId(), res => {
        let data = res.data;
        if (data) {
          data.forEach(row => {
            if (row.id === 'ws_admin') {
              this.flag = true;
            }
          })
        }
        if (this.flag === true) {
          this.show = this.isSystem ? false : !global;
        } else {
          this.form.global = false;
          this.$warning(this.$t('group.ws_admin_global'))
        }
      })
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

  }
}
</script>

<style scoped>
.form-input{
  width: 80%;
}
</style>
