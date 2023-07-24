<template>
  <el-dialog :close-on-click-modal="false" :title="title"
             :visible.sync="createVisible" width="40%"
             @closed="handleClose" class="edit-user-dialog"
             :destroy-on-close="true" v-loading="loading">
    <div>
      <el-form :model="form" label-width="120px" size="small" :rules="rule" ref="createUserForm">
        <el-form-item label="ID" prop="id">
          <el-input v-model="form.id" autocomplete="off" :placeholder="$t('user.input_id_placeholder')"
                    :disabled="type === 'Edit'" class="form-input"/>
        </el-form-item>
        <el-form-item :label="$t('commons.username')" prop="name">
          <el-input v-model="form.name" autocomplete="off" :placeholder="$t('user.input_name')" class="form-input"/>
        </el-form-item>
        <el-form-item :label="$t('commons.email')" prop="email">
          <el-input v-model="form.email" autocomplete="off" :placeholder="$t('user.input_email')" class="form-input"/>
        </el-form-item>
        <el-form-item :label="$t('commons.phone')" prop="phone">
          <el-input v-model="form.phone" autocomplete="off" :placeholder="$t('user.input_phone')" class="form-input"/>
        </el-form-item>
        <el-form-item :label="$t('commons.password')" prop="password" v-if="type === 'Add'">
          <el-input v-model="form.password" autocomplete="new-password" show-password
                    :placeholder="$t('user.input_password')" class="form-input"/>
        </el-form-item>
        <div v-for="(group, index) in form.groups" :key="index">
          <el-form-item :label="getLabel(index)"
                        :prop="'groups.' + index + '.type'"
                        :rules="{required: true, message: $t('user.select_group'), trigger: 'change'}"
          >
            <el-select filterable v-model="group.type" :placeholder="$t('user.select_group')"
                       class="edit-user-select"
                       :disabled="form.groups[index].type != null && form.groups[index].type !== '' "
                       @change="getResource(group.type, index)">
              <el-option
                  v-for="item in activeGroup(group)"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id + '+' +item.type"
              >
              </el-option>
            </el-select>
            <el-button @click.prevent="removeGroup(group)" style="margin-left: 20px;">
              {{ $t('commons.delete') }}
            </el-button>
          </el-form-item>
          <div v-if="groupType(group.type) === GROUP_TYPE.WORKSPACE">
            <el-form-item :label="$t('commons.workspace')"
                          :prop="'groups.' + index + '.ids'"
                          :rules="{required: true, message: $t('workspace.select'), trigger: 'change'}"
            >
              <el-select filterable v-model="group.ids" :placeholder="$t('system_user.search_get_more_tip')" multiple
                         :filter-method="(value) => filterWorkspaceOption(value, group)"
                         @visible-change="(value) => resetWorkspaceOption(value, group)"
                         :popper-append-to-body="false"
                         class="edit-user-select" @change="updateWorkSpace(group.index,group.type)" @focus="focusWorkspace" ref="workspaceContent">
                <el-option :style="{width: workspaceOptionWidth}"
                    v-for="item in group.workspaceOptions"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                    :title="item.name">
                </el-option>
              </el-select>
            </el-form-item>
          </div>
          <div v-if="groupType(group.type) === GROUP_TYPE.PROJECT">
            <el-form-item :label="$t('commons.project')"
                          :prop="'groups.' + index + '.ids'"
                          :rules="{required: true, message: $t('user.select_project'), trigger: 'change'}"
            >
              <el-select filterable v-model="group.ids" :placeholder="$t('system_user.search_get_more_tip')" multiple
                         :filter-method="(value) => filterProjectOption(value, group)"
                         @visible-change="(value) => resetProjectOption(value, group)"
                         :popper-append-to-body="false"
                         class="edit-user-select" @change="setWorkSpaceIds(group.ids,group.projects)" @focus="focusProject" ref="projectContent">
                <el-option :style="{width: projectOptionWidth}"
                    v-for="item in group.projectOptions"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                    :title="item.name">
                </el-option>
              </el-select>
            </el-form-item>
          </div>
        </div>

        <el-form-item>
          <template>
            <el-button type="success" class="form-input" @click="addGroup('createUserForm')" :disabled="btnAddGroup">
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
import {EMAIL_REGEX, PHONE_REGEX} from "metersphere-frontend/src/utils/regex";
import {GROUP_TYPE} from "metersphere-frontend/src/utils/constants";
import {getAllUserGroupByType, getUserAllGroups} from "@/api/user-group";
import {specialCreateUser, specialModifyUser} from "@/api/user";
import {getGroupResource} from "@/api/workspace";

export default {
  name: "EditUser",
  components: {},
  data() {
    return {
      limitOptionCount: 400,
      result: {},
      loading: false,
      createVisible: false,
      updateVisible: false,
      btnAddGroup: false,
      workspaceOptionWidth: "100%",
      projectOptionWidth: "100%",
      form: {
        groups: [{
          type: '',
          showSearchGetMore: false,
        }]
      },
      rule: {
        id: [
          {required: true, message: this.$t('user.input_id'), trigger: 'blur'},
          {min: 1, max: 50, message: this.$t('commons.input_limit', [1, 50]), trigger: 'blur'},
          {
            required: true,
            pattern: '^[^\u4e00-\u9fa5]+$',
            message: this.$t('user.chinese_characters_are_not_supported'),
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
            pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[\s\S]{8,30}$/,
            message: this.$t('member.password_format_is_incorrect'),
            trigger: 'blur'
          }
        ],
      },
      userGroup: [],
      workspaces: [],
      projects: [],
      type: "Add",
      title: "创建用户",
      currentWSGroupIndex: -1,
      currentGroupWSIds: new Set,
      GROUP_TYPE,
    }
  },
  methods: {
    focusWorkspace() {
      this.$nextTick(() => {
        let el = this.$refs.workspaceContent;
        this.workspaceOptionWidth = el[0].$el.offsetWidth + 'px'
      });
    },
    focusProject() {
      this.$nextTick(() => {
        let el = this.$refs.projectContent;
        this.projectOptionWidth = el[0].$el.offsetWidth + 'px'
      });
    },
    open(type, title, row) {
      this.type = type ? type : this.type;
      this.title = title ? title : this.title;

      if (type === 'Edit') {
        this.loading = getUserAllGroups(encodeURIComponent(row.id)).then(res => {
          let data = res.data;
          this.$set(this.form, "groups", data);
          for (let group of this.form.groups) {
            this.handleWorkspaceOption(group, group.workspaces);
            this.handleProjectOption(group, group.projects);
          }
        })
        this.form = Object.assign({}, row);
      }

      if (this.$refs.createUserForm) {
        this.$refs.createUserForm.clearValidate();
      }

      this.createVisible = true;
      this.getAllUserGroup();
    },
    handleClose() {
      this.createVisible = false;
      this.form = {groups: [{type: ''}]};
      this.btnAddGroup = false;
      this.currentWSGroupIndex = -1;
      this.currentGroupWSIds = new Set;
    },
    activeGroup(group) {
      return this.userGroup.filter(ug => {
        if (!group.selects) {
          return true;
        }
        let sign = true;
        for (let groupSelect of group.selects) {
          if ((ug.id + "+" + ug.type) === groupSelect) {
            sign = false;
            break;
          }
        }
        return sign;
      })
    },
    removeGroup(item) {
      if (this.form.groups.length === 1) {
        this.$info(this.$t('system_user.remove_group_tip'));
        return;
      }
      let index = this.form.groups.indexOf(item);
      let isRemove = this.checkRemove(item, index);
      if (!isRemove) {
        return;
      }
      if (item.type) {
        let _type = item.type.split("+")[1];
        if (_type === GROUP_TYPE.WORKSPACE) {
          this.currentWSGroupIndex = -1;
        } else {
          if (this.currentWSGroupIndex > index) {
            this.currentWSGroupIndex = this.currentWSGroupIndex - 1
          }
        }
      }
      if (index !== -1) {
        this.form.groups.splice(index, 1)
      }
      if (this.form.groups.length < this.userGroup.length) {
        this.btnAddGroup = false;
      }
    },
    checkRemove(item, index) {
      if (!item.type) {
        return true;
      }
      let type = item.type.split("+")[1];
      if (type === GROUP_TYPE.WORKSPACE) {
        let isHaveWorkspace = 0;
        let isHaveProject = 0;
        for (let i = 0; i < this.form.groups.length; i++) {
          if (index === i) {
            continue;
          }
          let group = this.form.groups[i];
          if (!group.type) {
            continue;
          }
          let _type = group.type.split("+")[1];
          if (_type === GROUP_TYPE.WORKSPACE) {
            isHaveWorkspace += 1;
          }
          if (_type === GROUP_TYPE.PROJECT) {
            isHaveProject += 1;
          }
        }
        if (isHaveWorkspace === 0 && isHaveProject > 0) {
          this.$warning(this.$t('commons.not_eligible_for_deletion'))
          return false;
        } else {
          this.currentGroupWSIds = new Set;
        }
      }
      return true;
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
            this.btnAddGroup = true;
          }
        } else {
          return false;
        }
      })
    },
    createUser(createUserForm) {
      this.$refs[createUserForm].validate(valid => {
        if (!valid) {
          return false;
        }
        let promise = this.type === 'Add' ? specialCreateUser(this.form) : specialModifyUser(this.form);
        this.loading = promise.then(() => {
          this.$success(this.$t('commons.save_success'));
          this.$emit("refresh");
          this.createVisible = false;
        })
      })
    },
    getAllUserGroup() {
      getAllUserGroupByType({type: GROUP_TYPE.SYSTEM}).then(res => {
        if (res.data) {
          this.userGroup = res.data;
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
      if (index > 0 && this.form.groups[index].ids && this.form.groups[index].ids.length > 0) {
        return;
      }
      let isHaveWorkspace = false;
      if (type === GROUP_TYPE.PROJECT) {
        for (let i = 0; i < this.form.groups.length; i++) {
          let group = this.form.groups[i];
          let _type = group.type.split("+")[1];
          if (_type === GROUP_TYPE.WORKSPACE) {
            isHaveWorkspace = true;
            break;
          }
        }
      } else if (type === GROUP_TYPE.WORKSPACE) {
        isHaveWorkspace = true;
      }
      this.loading = getGroupResource(id, type).then(res => {
        let data = res.data;
        if (data) {
          this._setResource(data, index, type);
          if (id === 'super_group') {
            return;
          }
          if (isHaveWorkspace === false) {
            this.addWorkspaceGroup(id, index);
          }
        }
      });
    },
    _setResource(data, index, type) {
      switch (type) {
        case GROUP_TYPE.WORKSPACE: {
          this.form.groups[index].workspaces = data.workspaces;
          this.handleWorkspaceOption(this.form.groups[index], data.workspaces);
          break;
        }
        case GROUP_TYPE.PROJECT:
          this.form.groups[index].projects = data.projects;
          this.handleProjectOption(this.form.groups[index], data.projects);
          break;
        default:
      }
    },
    addWorkspaceGroup(id, index) {
      let isHaveWorkSpace;
      this.form.groups.forEach(item => {
        if (item.type === "ws_member+WORKSPACE") {
          isHaveWorkSpace = true;
        }
      })
      if (isHaveWorkSpace) {
        return;
      }
      this.loading = getGroupResource(id, GROUP_TYPE.WORKSPACE).then(res => {
        let data = res.data;
        if (data) {
          let roleInfo = {};
          roleInfo.selects = [];
          roleInfo.type = "ws_member+WORKSPACE";
          let ids = this.form.groups.map(r => r.type);
          ids.forEach(id => {
            roleInfo.selects.push(id);
          })
          if (this.currentGroupWSIds.size > 0) {
            roleInfo.ids = [];
            this.currentGroupWSIds.forEach(item => {
              roleInfo.ids.push(item);
            })
          } else {
            roleInfo.ids = [];
          }
          let groups = this.form.groups;
          groups.push(roleInfo);
          this.currentWSGroupIndex = index + 1;
          this._setResource(data, index + 1, GROUP_TYPE.WORKSPACE);
        }
      })
    },
    getLabel(index) {
      let a = index + 1;
      return this.$t('commons.group') + a;
    },
    setWorkSpaceIds(ids, projects) {
      projects.forEach(project => {
        ids.forEach(item => {
          if (item === project.id) {
            this.currentGroupWSIds.add(project.workspaceId);
            if (this.form.groups[this.currentWSGroupIndex] && this.form.groups[this.currentWSGroupIndex].ids.indexOf(project.workspaceId) === -1) {
              this.form.groups[this.currentWSGroupIndex].ids.push(project.workspaceId);
            } else if (this.form.groups.filter(g => g.type === "ws_member+WORKSPACE").length > 0
              && this.form.groups.filter(g => g.type === "ws_member+WORKSPACE")[0].ids
              && this.form.groups.filter(g => g.type === "ws_member+WORKSPACE")[0].ids.indexOf(project.workspaceId) === -1) {
              this.form.groups.filter(g => g.type === "ws_member+WORKSPACE")[0].ids.push(project.workspaceId);
            }
          }
        })
      });
    },
    updateWorkSpace(index, type) {
      let _type = type.split("+")[1];
      if (_type === GROUP_TYPE.WORKSPACE) {
        this.currentGroupWSIds.forEach(item => {
          if (this.form.groups[index] && this.form.groups[index].ids) {
            this.form.groups[index].ids.push(item);
          }
        })
      } else {
        this.form.groups[index].ids = [];
      }
    },
    handleWorkspaceOption(group, workspaces) {
      if (!workspaces) {
        return;
      }
      this.$set(group, 'showSearchGetMore', workspaces.length > this.limitOptionCount);
      const options = workspaces.slice(0, this.limitOptionCount);
      this.$set(group, 'workspaceOptions', options);
      if (!group.ids || group.ids.length === 0) {
        return;
      }
      for (let id of group.ids) {
        let index = options.findIndex(o => o.id === id);
        if (index <= -1) {
          let obj = workspaces.find(d => d.id === id);
          if (obj) {
            group.workspaceOptions.unshift(obj);
          }
        }
      }
    },
    handleProjectOption(group, projects) {
      if (!projects) {
        return;
      }
      this.$set(group, 'showSearchGetMore', projects.length > this.limitOptionCount);
      const options = projects.slice(0, this.limitOptionCount);
      this.$set(group, 'projectOptions', options);
      if (!group.ids || group.ids.length === 0) {
        return;
      }
      for (let id of group.ids) {
        let index = options.findIndex(o => o.id === id);
        if (index <= -1) {
          let obj = projects.find(d => d.id === id);
          if (obj) {
            group.projectOptions.unshift(obj);
          }
        }
      }
    },
    filterWorkspaceOption(queryString, group) {
      let workspaces = group.workspaces;
      let copy = JSON.parse(JSON.stringify(workspaces));
      this.handleWorkspaceOption(group, queryString ? copy.filter(this.createFilter(queryString)) : copy);
    },
    filterProjectOption(queryString, group) {
      let projects = group.projects;
      let copy = JSON.parse(JSON.stringify(projects));
      this.handleProjectOption(group, queryString ? copy.filter(this.createFilter(queryString)) : copy);
    },
    createFilter(queryString) {
      return item => (item.name.toLowerCase().indexOf(queryString.toLowerCase()) !== -1);
    },
    resetWorkspaceOption(val, group) {
      if (val) {
        this.handleWorkspaceOption(group, group.workspaces);
      }
    },
    resetProjectOption(val, group) {
      if (val) {
        this.handleProjectOption(group, group.projects);
      }
    }
  }
}
</script>

<style scoped>

.form-input {
  width: 80%;
}

.edit-user-select {
  width: 80%;
}

:deep(.el-select__tags-text) {
  display: inline-block;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.el-tag.el-tag--info .el-tag__close) {
  top: -5px;
}
</style>
