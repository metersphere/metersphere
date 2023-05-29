<template>
  <el-dialog :close-on-click-modal="false" :title="$t('member.create')" :visible.sync="dialogVisible" width="45%"
             :destroy-on-close="true"
             @close="close" v-loading="loading">
    <el-form :model="form" ref="form" :rules="rules" label-position="right" label-width="80px" size="small">
      <el-form-item :label="$t('commons.member')" prop="userIds"
                    :rules="{required: true, message: $t('member.please_choose_member'), trigger: 'blur'}">
        <el-select
          v-model="form.userIds"
          multiple
          filterable
          :filter-method="filterUserOption"
          @visible-change="resetUserOption"
          :popper-append-to-body="false"
          class="member_select"
          :placeholder="$t('member.please_choose_member')">
          <el-option
            v-for="item in userList"
            :key="item.id"
            :label="item.id"
            :value="item.id">
            <user-option-item :user="item"/>
          </el-option>
          <div style="text-align: center; color: #8a8b8d;" v-if="showUserSearchGetMore">
            {{ $t('user.search_get_more_tip') }}
          </div>
        </el-select>
      </el-form-item>
      <el-form-item :label="$t('commons.group')" prop="groupIds">
        <el-select v-model="form.groupIds" multiple :placeholder="$t('group.please_select_group')" class="group_select">
          <el-option
            v-for="item in form.groups"
            :key="item.id"
            :label="item.name"
            :value="item.id">
          </el-option>
        </el-select>
      </el-form-item>
    </el-form>
    <template v-slot:footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false" size="medium">{{ $t('commons.cancel') }}</el-button>
        <el-button type="primary" @click="submitForm('form')" size="medium" @keydown.enter.native.prevent>
          {{ $t('commons.confirm') }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script>

import UserOptionItem from "./UserOptionItem";
import {GROUP_PROJECT} from 'metersphere-frontend/src/utils/constants'
import {getUserListByResourceUrl} from "../../api/user";
import {getUserGroupList} from "../../api/user-group";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";

export default {
  name: "AddMember",
  components: {UserOptionItem},
  data() {
    return {
      dialogVisible: false,
      form: {},
      rules: {
        userIds: [
          {required: true, message: this.$t('member.please_choose_member'), trigger: ['blur']}
        ],
        groupIds: [
          {required: true, message: this.$t('group.please_select_group'), trigger: ['blur']}
        ]
      },
      userList: [],
      userListCopy: [],
      loading: false,
      limitOptionCount: 400,
      showUserSearchGetMore: false,
    }
  },
  props: {
    groupType: {
      type: String,
      default() {
        return '';
      }
    },
    groupScopeId: {
      type: String,
      default() {
        return '';
      }
    },
    projectId: {
      type: String,
      default() {
        return '';
      }
    },
    userResourceUrl: {
      type: String,
      default() {
        return '/user/list';
      }
    }
  },
  methods: {
    visibleChange(val) {
      if (!val) {
        this.userFilter(null);
      }
    },
    submitForm() {
      this.$refs['form'].validate((valid) => {
        if (valid) {
          let param = {
            userIds: this.form.userIds,
            groupIds: this.form.groupIds,
          };
          this.$emit("submit", param);
        }
      });
    },
    open() {
      this.dialogVisible = true;
      this.loading = getUserListByResourceUrl(this.userResourceUrl).then(res => {
        this.handleUserOption(res.data);
        this.userListCopy = res.data;
      })
      let param = {type: this.groupType, resourceId: this.groupScopeId};
      if (this.groupType === GROUP_PROJECT) {
        param.projectId = this.projectId || getCurrentProjectID();
      }
      this.loading = getUserGroupList(param).then(res => {
        this.$set(this.form, "groups", res.data);
      })
    },
    close() {
      this.dialogVisible = false;
      this.form = {};
    },
    userFilter(val) {
      if (val) {
        this.userList = this.userListCopy.filter((item) => {
          if (!!~item.id.indexOf(val) || (item.name && !!~item.name.indexOf(val))) {
            return true;
          }
        })
      } else {
        this.userList = this.userListCopy;
      }
    },
    handleUserOption(users) {
      if (!users) {
        return;
      }
      this.showUserSearchGetMore = users.length > this.limitOptionCount;
      this.userList = users.slice(0, this.limitOptionCount);
      if (!this.form.userIds || this.form.userIds.length === 0) {
        return;
      }
      this._handleSelectOption(this.form.userIds, this.userList);
    },
    _handleSelectOption(ids, data) {
      for (let id of ids) {
        let index = data.findIndex(o => o.id === id);
        if (index <= -1) {
          let obj = this.userListCopy.find(d => d.id === id);
          if (obj) {
            data.unshift(obj);
          }
        }
      }
    },
    filterUserOption(queryString) {
      this.handleUserOption(queryString ? this.userListCopy.filter(this.createFilter(queryString)) : this.userListCopy);
    },
    createFilter(queryString) {
      return item => {
        return (item.name && item.name.toLowerCase().indexOf(queryString.toLowerCase()) !== -1);
      };
    },
    resetUserOption(val) {
      if (val) {
        this.handleUserOption(this.userListCopy);
      }
    },
  }
}
</script>

<style scoped>
.member_select, .group_select {
  display: block;
}
</style>
