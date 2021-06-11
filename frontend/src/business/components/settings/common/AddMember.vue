<template>
  <el-dialog :close-on-click-modal="false" :title="$t('member.create')" :visible.sync="dialogVisible" width="40%"
             :destroy-on-close="true"
             @close="close" v-loading="result.loading">
    <el-form :model="form" ref="form" :rules="rules" label-position="right" label-width="100px" size="small">
      <el-form-item :label="$t('commons.member')" prop="userIds"
                    :rules="{required: true, message: $t('member.please_choose_member'), trigger: 'blur'}">
        <el-select
          v-model="form.userIds"
          multiple
          filterable
          :popper-append-to-body="false"
          class="select-width"
          :placeholder="$t('member.please_choose_member')">
          <el-option
            v-for="item in userList"
            :key="item.id"
            :label="item.id"
            :value="item.id">
            <template>
              <span class="user-select-left">{{ item.name }} ({{ item.id }})</span>
              <span class="user-select-right">{{ item.email }}</span>
            </template>
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item :label="$t('commons.group')" prop="groupIds">
        <el-select v-model="form.groupIds" multiple :placeholder="$t('group.please_select_group')" class="select-width">
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
        <el-button type="primary" @click="submitForm('form')" @keydown.enter.native.prevent size="medium">
          {{ $t('commons.confirm') }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script>

export default {
  name: "AddMember",
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
      result: {}
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
    }
  },
  methods: {
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
      this.result = this.$get('/user/list/', response => {
        this.userList = response.data;
      })
      this.result = this.$post('/user/group/list', {type: this.groupType, resourceId: this.groupScopeId}, response => {
        this.$set(this.form, "groups", response.data);
      })
    },
    close() {
      this.dialogVisible = false;
      this.form = {};
    }
  }
}
</script>

<style scoped>
.user-select-left {
  float: left;
}

.user-select-right {
  float: right;
  margin-right: 18px;
  color: #8492a6;
  font-size: 13px;
}

.select-width {
  width: 100%;
}
</style>
