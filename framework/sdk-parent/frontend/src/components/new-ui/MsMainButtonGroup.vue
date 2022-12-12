<template>
  <div class="top-btn-group-layout">
    <el-button size="small" v-for="(btn, index) in buttonGroup" :disabled="isDisable(btn)" v-if="isXPack(btn)" @click="click(btn)" :class="{'iconBtn' : btn.icon}"
               :key="index" :icon="btn.icon" :type="btn.isPrimary ? 'primary': ''" :size="btn.size">
      {{btn.name}}
    </el-button>
  </div>
</template>

<script>
import {hasLicense, hasPermissions} from "../../utils/permission";

export default {
  name: "MsTopButtonGroup",
  data() {
    return {
    }
  },
  props: {
    buttonGroup: {
      type: Array
    },
  },
  methods: {
    click(btn) {
      if (btn.handleClick instanceof Function) {
        btn.handleClick();
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
    },
    isXPack(item) {
      if (item.isXPack) {
        return hasLicense();
      } else {
        return true;
      }
    }
  }
}
</script>

<style scoped>
.top-btn-group-layout {
  width: 100%;
  height: 32px;
}

:deep(.el-button + .el-button) {
  margin-left: 12px;
}

/* common style:  elementui small btn 样式可提取为公共*/
:deep(.el-button--small span) {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  position: relative;
  top: -5px;
}

:deep(.el-button) {
  width: 80px;
  height: 32px;
  border-radius: 4px;
}

.iconBtn {
  width: 98px;
}

:deep(.iconBtn i){
  position: relative;
  top: -5px;
  width: 12px;
  left: -4px;
  height: 12px;
}

:deep(.iconBtn span) {
  position: relative;
  left: -7px;
}

:deep(.el-button--default) {
  background: #FFFFFF;
  border: 1px solid #BBBFC4;
}

:deep(.el-button--default:hover) {
  border: 1px solid #783887;
  background: rgba(120, 56, 135, 0.15);
}
</style>
