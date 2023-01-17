<template>
  <el-dropdown class="scenario-ext-btn">
    <el-link type="primary" :underline="false">
      <el-icon class="el-icon-more"></el-icon>
    </el-link>
    <el-dropdown-menu slot="dropdown">
      <el-dropdown-item v-for="(item,index) in dropdownItems" :key="index" @click.native.stop="click(item)"
                        :disabled="isDisable(item)"
                        :command="item.value"
                        v-modules="item.modules"

      >{{ item.name }}
      </el-dropdown-item>
    </el-dropdown-menu>
  </el-dropdown>
</template>

<script>
import {hasPermissions} from 'metersphere-frontend/src/utils/permission';

export default {
  name: "TableExtendBtns",
  props: {
    dropdownItems: Array,
    row: Object,
  },
  methods: {
    click(btn) {
      if (btn.exec instanceof Function) {
        btn.exec(this.row);
      }
    },
    isDisable(item) {
      if (item.isDisable) {
        if (item.isDisable instanceof Function) {
          return item.isDisable();
        } else {
          return item.isDisable;
        }
      }
      if (item.permissions && item.permissions.length > 0) {
        return !hasPermissions(...item.permissions);
      }
      return false;
    }
  }
}
</script>

<style scoped>
.scenario-ext-btn {
  margin-left: 10px;
}
</style>
