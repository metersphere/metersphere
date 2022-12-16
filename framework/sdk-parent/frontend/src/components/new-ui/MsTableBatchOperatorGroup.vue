<template>
  <div class="batch-operator-layout">
    <div v-if="showOperators && showOperators.length > 0" class="showBtnGroup">
      <el-button size="small" v-for="(operator, index) in showOperators" :disabled="isDisable(operator)" v-if="isXPack(operator)" @click="click(operator)" :key="index">
        {{operator.name}}
      </el-button>
    </div>

    <el-dropdown size="small" v-if="moreOperators && moreOperators.length > 0" placement="bottom-start">
      <el-button type="primary" class="more-btn"><i class="el-icon-more"></i></el-button>
      <el-dropdown-menu slot="dropdown">
        <el-dropdown-item v-for="(operator,index) in moreOperators" :disabled="isDisable(operator)" :divided="isDivide(operator)" v-if="isXPack(operator)" @click.native.stop="click(operator)" :key="index">
          {{ operator.name }}
        </el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>

    <span class="select-text">{{$t('commons.table_select_row_count', [selectCounts])}}</span>

    <el-button type="text" class="clear-btn">{{$t('commons.clear')}}</el-button>
  </div>
</template>

<script>
import {hasLicense, hasPermissions} from "../../utils/permission";

export default {
  name: "MsTableBatchOperatorGroup.vue",
  props: {
    batchOperators: Array,
    selectCounts: Number
  },
  data() {
    return {
      showOperators: [],
      moreOperators: []
    };
  },
  created() {
    this.initLayout();
  },
  methods: {
    initLayout() {
      if (this.batchOperators && this.batchOperators.length > 3) {
        // 导航栏展示三个
        this.showOperators = this.batchOperators.slice(0, 3);
        this.moreOperators = this.batchOperators.slice(3, this.batchOperators.length);
      } else {
        this.showOperators = this.batchOperators;
      }
    },
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
    isDivide(item) {
      if (item.isDivide) {
        return item.isDivide;
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
.batch-operator-layout {
  display: inline-block;
  margin-top: 34px;
  height: 32px;
}

.showBtnGroup {
  display: inline-block;
}

:deep(.showBtnGroup button.el-button.el-button--default.el-button--small) {
  box-sizing: border-box;
  width: 80px;
  height: 32px;
  left: 0px;
  top: 0px;
  background: #FFFFFF;
  border: 1px solid #783887;
  border-radius: 4px;
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  text-align: center;
  color: #783887;
}

:deep(.showBtnGroup button.el-button.el-button--default.el-button--small:hover) {
  background: rgba(120, 56, 135, 0.15);
}

:deep(.showBtnGroup button.el-button.el-button--default.el-button--small span) {
  position: relative;
  top: -5px;
  left: 0px;
}

:deep(button.el-button.more-btn.el-button--primary.el-dropdown-selfdefine) {
  box-sizing: border-box;
  width: 32px;
  height: 32px;
  background: #FFFFFF;
  border: 1px solid #783887;
  border-radius: 4px;
  flex: none;
  order: 1;
  align-self: center;
  flex-grow: 0;
  position: relative;
  top: -6px;
  left: 11px;
  padding: 0px;
}

:deep(button.el-button.more-btn.el-button--primary.el-dropdown-selfdefine:hover) {
  background: rgba(120, 56, 135, 0.15);
}

:deep(button.el-button.more-btn.el-button--primary.el-dropdown-selfdefine i) {
  color: #783887;
}

.select-text {
  position: relative;
  left: 25px;
  top: -5px;
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  text-align: center;
  color: #646A73;
  flex: none;
  order: 1;
  flex-grow: 0;
}

:deep(button.el-button.clear-btn.el-button--text) {
  position: relative;
  left: 39px;
  top: -5px;
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  text-align: center;
  color: #783887;
  flex: none;
  order: 2;
  flex-grow: 0;
}
</style>
