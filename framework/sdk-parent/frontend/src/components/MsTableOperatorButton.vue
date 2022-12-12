<template>
  <el-tooltip :content="tip" v-if=isDivButton
              placement="bottom"
              :enterable="false"
              effect="dark">
    <el-button @click="exec"
               @keydown.enter.native.prevent
               type="primary"
               :disabled="isReadOnly"
               circle
               style="color:white;padding: 0px 0.1px;width: 28px;height: 28px;"
               size="mini">
      <div style="transform: scale(0.8)">
        <span style="margin-left: -4px;line-height: 27px;">{{ tip }}</span>
      </div>
    </el-button>
  </el-tooltip>
  <el-button @click="exec" v-else-if="isTextButton"
             @keydown.enter.native.prevent
             type="text" class="text-btn"
             :disabled="isReadOnly"
             size="mini"> {{ tip }}
  </el-button>
  <el-dropdown size="small" v-else-if="isMoreOperate" placement="bottom-end">
    <el-button type="primary" class="more-btn" ref="moreBtn" :class="{'more-btn-hover': isHover}"><i class="el-icon-more"></i></el-button>
    <el-dropdown-menu slot="dropdown" class="more-operate-menu" @mouseenter.native="isHover = true" @mouseleave.native="isHover = false">
      <template v-for="(operator, index) in childOperate">
        <!-- 列表dropdown只支持一级 -->
        <el-dropdown-item v-permission="operator.permissions" :disabled="operator.isDisable" :divided="operator.isDivide"
                          @click.native.stop="click(operator)" :key="index" :class="{'active': operator.isActive}">
          {{ operator.tip }}
        </el-dropdown-item>
      </template>
    </el-dropdown-menu>
  </el-dropdown>
  <ms-tip-button v-else
                 :disabled="disabled || isReadOnly"
                 @click="exec"
                 @clickStop="clickStop"
                 :type="type"
                 :tip="tip"
                 :icon="icon" size="mini" circle/>
</template>

<script>
import MsTableButton from "./MsTableButton";
import MsTipButton from "./MsTipButton";

export default {
  name: "MsTableOperatorButton",
  components: {MsTipButton, MsTableButton},
  data() {
    return {
      isReadOnly: false,
      isHover: false
    }
  },
  props: {
    isMoreOperate: {
      type: Boolean,
      default: false,
    },
    childOperate: {
      type: Array
    },
    isTextButton: {
      type: Boolean,
      default: false,
    },
    isDivButton: {
      type: Boolean,
      default: false,
    },
    icon: {
      type: String,
      default: 'el-icon-question'
    },
    type: {
      type: String,
      default: 'primary'
    },
    tip: {
      type: String
    },
    disabled: {
      type: Boolean,
      default: false
    },
    isTesterPermission: {
      type: Boolean,
      default: false
    },
    rowData: Object
  },
  methods: {
    exec() {
      this.$emit('exec');
    },
    clickStop() {
      this.$emit('clickStop');
    },
    click(btn) {
      if (btn.exec instanceof Function) {
        btn.exec(this.rowData);
      }
    }
  }
}
</script>

<style scoped>
.text-btn {
  width: 28px;
  height: 22px;
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  text-align: center;
  color: #783887;
  flex: none;
  order: 1;
  flex-grow: 0;
  padding: 0;
  margin: 0 16px 0 0;
}

.active {
  color: #F54A45!important;
}

.more-btn {
  box-sizing: border-box;
  width: 32px;
  height: 32px;
  background: #FFFFFF;
  border: none;
  border-radius: 4px;
  flex: none;
  order: 1;
  align-self: center;
  flex-grow: 0;
  padding: 0px;
  background-color: transparent;
  color: #783887;;
}

.more-btn:hover {
  background-color: rgba(120, 56, 135, 0.1)!important;
}

.more-btn-hover {
  background-color: rgba(120, 56, 135, 0.1)!important;
}

:deep(button.el-button.more-btn.el-button--primary.el-dropdown-selfdefine i) {
  color: #783887;
}

.el-dropdown-menu__item:hover {
  background-color: rgba(31, 35, 41, 0.1)!important;
}

.more-operate-menu {
  width: 120px!important;
  margin-top: 0px;
  margin-right: 20px;
}

.el-dropdown-menu__item {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #1F2329!important;
  height: 32px;
  padding-top: 6px;
}

li.el-dropdown-menu__item.el-dropdown-menu__item--divided.active {
  color: #F54A45!important;
}

li.el-dropdown-menu__item.el-dropdown-menu__item--divided.active:before {
  height: 0;
}
</style>
