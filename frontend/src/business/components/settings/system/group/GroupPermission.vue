<template>
  <div>
    <span v-for="(permission, index) in permissions" :key="index">
      <el-checkbox v-if="permission.license" v-xpack class="permission-checkbox"
                   v-model="permission['checked']" @change="change($event, permission)" :disabled="readOnly">
        {{ $t(permission.name) }}
      </el-checkbox>
      <el-checkbox v-else class="permission-checkbox"
                   v-model="permission['checked']" @change="change($event, permission)" :disabled="isReadOnly(permission)">
        {{ $t(permission.name) }}
      </el-checkbox>
    </span>
  </div>
</template>

<script>
export default {
  name: "GroupPermission",
  props: {
    permissions: {
      type: Array,
      default() {
        return {}
      }
    },
    selected: {
      type: Array,
      default() {
        return []
      }
    },
    readOnly: {
      type: Boolean,
      default() {
        return false;
      }
    },
    group: {
      type: Object,
      default() {
        return {};
      }
    }
  },
  computed: {
    isReadOnly() {
      return function (permission) {
        // 禁止取消系统管理员用户组的读取和设置权限
        const isSystemGroupPermission = permission.id === 'SYSTEM_GROUP:READ' || permission.id === 'SYSTEM_GROUP:READ+SETTING_PERMISSION';
        const isDefaultSystemGroup = this.group.id === 'admin' && isSystemGroupPermission;
        return this.readOnly || isDefaultSystemGroup;
      }
    }
  },
  methods: {
    change(val, permission) {
      // 取消读权限则取消其它所有操作权限
      let id = permission.id.split(":")[1];
      if (id === "READ" && !val) {
        this.permissions.map(p => p.checked = val);
      } else {
        if (val) {
          let p = this.permissions.filter(p => p.id.split(":")[1] === "READ");
          if (p.length > 0) {
            p[0].checked = val;
          }
        }
        permission.checked = val;
      }
    }
  }
}
</script>

<style scoped>
.permission-checkbox {
  margin-right: 25px;
}
</style>
