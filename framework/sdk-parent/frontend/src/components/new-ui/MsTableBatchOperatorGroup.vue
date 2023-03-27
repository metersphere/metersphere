<template>
  <div class="batch-operator-layout">
    <div v-if="showOperators && showOperators.length > 0" class="showBtnGroup">
      <el-button size="small" v-for="(operator, index) in showOperators" :disabled="isDisable(operator)" v-if="isXPack(operator)" v-prevent-re-click @click.native.stop="click(operator)" :key="index" :class="{'is-delete': operator.isDelete}">
        {{operator.name}}
      </el-button>
    </div>

    <el-dropdown size="small" v-if="moreOperators && moreOperators.length > 0" placement="top-start" ref="parentMenu">
      <el-button type="primary" class="more-btn"><i class="el-icon-more"></i></el-button>
      <el-dropdown-menu slot="dropdown" class="more-operate-menu" @mouseenter.native="() => $refs.parentMenu.show()">
        <template v-for="(operator, index) in moreOperators">
          <el-dropdown-item v-if="operator.children && operator.children.length > 0" :key="index">
            <!--  多级: 暂时支持2级 -->
            <el-dropdown placement="right-end" ref="childrenMenu">
              <span class="more-operate-menu-children-content" :class="{active: operator.isActive}">{{operator.name}}<i class="el-icon-arrow-right el-icon--right"/></span>
              <el-dropdown-menu slot="dropdown" class="more-operate-menu-children" @mouseenter.native="() => $refs.parentMenu.show()" @mouseleave.native="() => $refs.parentMenu.hide()">
                <template v-for='childOperator in operator.children'>
                  <el-dropdown-item v-if="isXPack(childOperator)" :disabled="isDisable(childOperator)" :divided="isDivide(childOperator)"
                                    style="height: 50px" @click.native.stop="click(childOperator)" @mouseenter="childMove">
                    <span class="operator" :class="{active: operator.isActive}" :style="isDisable(operator) ? 'color: #bbb!important' : ''">{{childOperator.name}}</span>
                    <span class="tips">{{childOperator.tips}}</span>
                  </el-dropdown-item>
                </template>
              </el-dropdown-menu>
            </el-dropdown>
          </el-dropdown-item>
          <!-- 一级 -->
          <el-dropdown-item v-else :disabled="isDisable(operator)" :style="isDisable(operator) ? 'color: #bbb!important' : ''" :divided="isDivide(operator)" @click.native.stop="click(operator)" :key="index" :class="{active: operator.isActive}">
            {{ operator.name }}
          </el-dropdown-item>
        </template>
      </el-dropdown-menu>
    </el-dropdown>

    <span class="select-text">{{$t('commons.table_select_row_count', [selectCounts])}}</span>

    <el-button type="text" class="clear-btn" @click="clear">{{$t('commons.clear')}}</el-button>
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
    clear() {
      this.$emit("clear")
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
    },
    childMove() {
    },
  }
}
</script>

<style scoped>
.batch-operator-layout {
  display: inline-block;
  margin-top: 28px;
  height: 32px;
}

.showBtnGroup {
  display: inline-block;
}

:deep(.showBtnGroup button.el-button.el-button--default.el-button--small) {
  box-sizing: border-box;
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
  top: -5.5px;
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

.el-dropdown-menu__item, .more-operate-menu-children-content {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #1F2329!important;
  height: 32px;
  padding-top: 6px;
}

.el-dropdown-menu__item:hover {
  background-color: rgba(31, 35, 41, 0.1)!important;
}

.more-operate-menu {
  width: 164px!important;
  margin-left: 10px;
}

.more-operate-menu-children {
  margin-left: 16px;
  margin-bottom: -12px;
  width: 135px!important;
}

:deep(.more-operate-menu-children-content li.el-dropdown-menu__item) {
  height: 50px!important;
}

.operator {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  display: flex;
  align-items: center;
  color: #1F2329;
  flex: none;
  order: 0;
  align-self: stretch;
  flex-grow: 0;
}

.tips {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 12px;
  line-height: 20px;
  display: block;
  align-items: center;
  color: #8F959E;
  flex: none;
  order: 1;
  align-self: stretch;
  flex-grow: 0;
}

.el-dropdown-menu--small .el-dropdown-menu__item.el-dropdown-menu__item--divided:before {
  height: 0px;
}

.active {
  color: #F54A45!important;
}

.is-delete {
  color: #F54A45!important;
  border: 1px solid #F54A45!important;
}

.is-delete:hover {
  background: rgba(245, 74, 69, 0.2)!important;
}

:deep(.el-icon-arrow-right) {
  margin-left: 30px;
}

:deep(.el-icon-arrow-right:before) {
  font-size: 15px;
  color: #8F959E;
}

.disabled-class {
  cursor: not-allowed;
}
</style>

<style>
/* 消除小三角 */
.el-popper[x-placement^=top] .popper__arrow{
  border: none;
}

.el-popper[x-placement^=top] .popper__arrow::after {
  border: none;
}

.el-popper[x-placement^=bottom] .popper__arrow{
  border: none;
}

.el-popper[x-placement^=bottom] .popper__arrow::after {
  border: none;
}

.el-popper[x-placement^=right] .popper__arrow{
  border: none;
}

.el-popper[x-placement^=right] .popper__arrow::after {
  border: none;
}

.el-popper[x-placement^=left] .popper__arrow{
  border: none;
}

.el-popper[x-placement^=left] .popper__arrow::after {
  border: none;
}
</style>
