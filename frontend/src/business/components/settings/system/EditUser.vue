<template>
  <el-dialog :close-on-click-modal="false" :title="title"
             :visible.sync="createVisible" width="40%"
             @closed="handleClose" class="edit-user-dialog"
             :destroy-on-close="true">
    <div v-loading="result.loading">
      <el-form :model="form" label-position="right" label-width="120px" size="small" :rules="rule" ref="createUserForm">
        <el-form-item label="ID" prop="id">
          <el-input v-model="form.id" autocomplete="off" :placeholder="$t('user.input_id_placeholder')" :disabled="type === 'Edit'"/>
        </el-form-item>
        <el-form-item :label="$t('commons.username')" prop="name">
          <el-input v-model="form.name" autocomplete="off" :placeholder="$t('user.input_name')"/>
        </el-form-item>
        <el-form-item :label="$t('commons.email')" prop="email">
          <el-input v-model="form.email" autocomplete="off" :placeholder="$t('user.input_email')"/>
        </el-form-item>
        <el-form-item :label="$t('commons.phone')" prop="phone">
          <el-input v-model="form.phone" autocomplete="off" :placeholder="$t('user.input_phone')"/>
        </el-form-item>
        <el-form-item :label="$t('commons.password')" prop="password" v-if="type === 'Add'">
          <el-input v-model="form.password" autocomplete="new-password" show-password
                    :placeholder="$t('user.input_password')"/>
        </el-form-item>
        <div v-for="(group, index) in form.groups" :key="index">
          <el-form-item :label="'用户组'+index"
                        :prop="'groups.' + index + '.type'"
                        :rules="{required: true, message: '请选择用户组', trigger: 'change'}"
          >
            <el-select filterable v-model="group.type" placeholder="请选择用户组" :disabled="!!group.type"
                       class="edit-user-select" @change="getResource(group.type, index)">
              <el-option
                v-for="item in activeGroup(group)"
                :key="item.id"
                :label="item.name"
                :value="item.id + '+' +item.type"
              >
              </el-option>
            </el-select>
            <el-button @click.prevent="removeGroup(group)" style="margin-left: 20px;" v-if="form.groups.length > 1">
              {{ $t('commons.delete') }}
            </el-button>
          </el-form-item>
          <div v-if="groupType(group.type) === org">
            <el-form-item :label="$t('organization.select_organization')"
                          :prop="'groups.' + index + '.ids'"
                          :rules="{required: true, message: $t('organization.select_organization'), trigger: 'change'}"
            >
              <el-select filterable v-model="group.ids" :placeholder="$t('organization.select_organization')" multiple
                         class="edit-user-select">
                <el-option
                  v-for="item in group.organizations"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
          </div>
          <div v-if="groupType(group.type) === ws">
            <el-form-item :label="$t('workspace.select')"
                          :prop="'groups.' + index + '.ids'"
                          :rules="{required: true, message: $t('workspace.select'), trigger: 'change'}"
            >
              <el-select filterable v-model="group.ids" :placeholder="$t('workspace.select')" multiple
                         class="edit-user-select">
                <el-option
                  v-for="item in group.workspaces"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
          </div>
          <div v-if="groupType(group.type) === project">
            <el-form-item label="选择项目"
                          :prop="'groups.' + index + '.ids'"
                          :rules="{required: true, message: '请选择项目', trigger: 'change'}"
            >
              <el-select filterable v-model="group.ids" placeholder="请选择项目" multiple
                         class="edit-user-select">
                <el-option
                  v-for="item in group.projects"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
          </div>
        </div>

        <el-form-item>
          <template>
            <el-button type="success" style="width: 100%;" @click="addGroup('createUserForm')" :disabled="btnAddRole">
              {{ $t('group.add') }}
            </el-button>
          </template>
        </el-form-item>
      </el-form>
    </div>
    <template v-slot:footer>
      <div class="dialog-footer">
        <el-button @click="createVisible = false" size="medium">{{ $t('commons.cancel') }}</el-button>
        <el-button type="primary" @click="createUser('createUserForm')" @keydown.enter.native.prevent size="medium">
          {{ $t('commons.confirm') }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script>
import {EMAIL_REGEX, PHONE_REGEX} from "@/common/js/regex";
import {GROUP_ORGANIZATION, GROUP_PROJECT, GROUP_SYSTEM, GROUP_WORKSPACE} from "@/common/js/constants";

export default {
  name: "EditUser",
  components: {},
  data() {
    return {
      result: {},
      createVisible: false,
      updateVisible: false,
      btnAddRole: false,
      form: {
        groups: [{
          type: ''
        }]
      },
      rule: {
        id: [
          {required: true, message: this.$t('user.input_id'), trigger: 'blur'},
          {min: 1, max: 50, message: this.$t('commons.input_limit', [1, 50]), trigger: 'blur'},
          {
            required: true,
            pattern: '^[^\u4e00-\u9fa5]+$',
            message: this.$t('user.special_characters_are_not_supported'),
            trigger: 'blur'
          }
        ],
        name: [
          {required: true, message: this.$t('user.input_name'), trigger: 'blur'},
          {min: 2, max: 50, message: this.$t('commons.input_limit', [2, 50]), trigger: 'blur'},
          {
            required: true,
            message: this.$t('user.special_characters_are_not_supported'),
            trigger: 'blur'
          }
        ],
        phone: [
          {
            pattern: PHONE_REGEX,
            message: this.$t('user.mobile_number_format_is_incorrect'),
            trigger: 'blur'
          }
        ],
        email: [
          {required: true, message: this.$t('user.input_email'), trigger: 'blur'},
          {
            required: true,
            pattern: EMAIL_REGEX,
            message: this.$t('user.email_format_is_incorrect'),
            trigger: 'blur'
          }
        ],
        password: [
          {required: true, message: this.$t('user.input_password'), trigger: 'blur'},
          {
            required: true,
            pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[^]{8,30}$/,
            message: this.$t('member.password_format_is_incorrect'),
            trigger: 'blur'
          }
        ],
      },
      userGroup: [],
      organizations: [],
      workspaces: [],
      projects: [],
      type: "Add",
      title: "创建用户",
    }
  },
  computed: {
    org() {
      return GROUP_ORGANIZATION;
    },
    ws() {
      return GROUP_WORKSPACE;
    },
    project() {
      return GROUP_PROJECT;
    }
  },
  methods: {
    open(type, title, row) {
      this.type = type ? type : this.type;
      this.title = title ? title : this.title;

      if (type === 'Edit') {
        this.result = this.$get('/user/group/all/' + encodeURIComponent(row.id), response => {
          let data = response.data;
          this.$set(this.form, "groups", data);
        });
        this.form = Object.assign({}, row);
      }
      this.createVisible = true;
      this.getAllUserGroup();
    },
    handleClose() {
      this.createVisible = false;
      this.form = {groups: [{type: ''}]};
      this.btnAddRole = false;
    },
    activeGroup(roleInfo) {
      return this.userGroup.filter(function (group) {
        let value = true;
        if (!roleInfo.selects) {
          return true;
        }
        if (roleInfo.selects.length === 0) {
          value = true;
        }
        for (let i = 0; i < roleInfo.selects.length; i++) {
          let idType = group.id + "+" + group.type;
          if (idType === roleInfo.selects[i]) {
            value = false;
          }
        }
        return value;
      })
    },
    removeGroup(item) {
      let index = this.form.groups.indexOf(item);
      if (index !== -1) {
        this.form.groups.splice(index, 1)
      }
      if (this.form.groups.length < this.userGroup.length) {
        this.btnAddRole = false;
      }
    },
    addGroup(validForm) {
      this.$refs[validForm].validate(valid => {
        if (valid) {
          let roleInfo = {};
          roleInfo.selects = [];
          let ids = this.form.groups.map(r => r.type);
          ids.forEach(id => {
            roleInfo.selects.push(id);
          })
          let groups = this.form.groups;
          groups.push(roleInfo);
          if (this.form.groups.length > this.userGroup.length - 1) {
            this.btnAddRole = true;
          }
        } else {
          return false;
        }
      })
    },
    createUser(createUserForm) {
      this.$refs[createUserForm].validate(valid => {
        if (valid) {
          let url = this.type === 'Add' ? '/user/special/add' : '/user/special/update';
          this.result = this.$post(url, this.form, () => {
            this.$success(this.$t('commons.save_success'));
            this.$emit("refresh");
            this.createVisible = false;
          });
        } else {
          return false;
        }
      })
    },
    getAllUserGroup() {
      this.$post('/user/group/get', {type: GROUP_SYSTEM}, res => {
        let data = res.data;
        if (data) {
          this.userGroup = data;
        }
      })
    },
    groupType(idType) {
      if (!idType) {
        return;
      }
      return idType.split("+")[1];
    },
    getResource(idType, index) {
      if (!idType) {
        return;
      }
      let id = idType.split("+")[0];
      let type = idType.split("+")[1];
      this.result = this.$get('/organization/list/resource/' + id + "/" + type, res => {
        let data = res.data;
        if (data) {
          this._setResource(data, index, type);
        }
      })
    },
    _setResource(data, index, type) {
      switch (type) {
        case GROUP_ORGANIZATION:
          this.form.groups[index].organizations = data.organizations;
          break;
        case GROUP_WORKSPACE:
          this.form.groups[index].workspaces = data.workspaces;
          break;
        case GROUP_PROJECT:
          this.form.groups[index].projects = data.projects;
          break;
        default:
      }
    }
  }
}
</script>

<style scoped>
.edit-user-dialog >>> .el-dialog__body {
  padding-bottom: 0;
  padding-left: 0;
}

.edit-user-dialog >>> .el-dialog__footer {
  padding-top: 0;
}

.edit-user-select {
  width: 80%;
}
</style>
