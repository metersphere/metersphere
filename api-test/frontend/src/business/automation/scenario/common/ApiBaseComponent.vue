<template>
  <el-card :style="{ 'border-color': colorStyle }" class="ms-base-card">
    <div class="ms-base-header" @click="active(data)">
      <slot name="beforeHeaderLeft">
        <div
          v-if="data.index"
          class="el-step__icon is-text"
          :style="{ color: color, 'background-color': backgroundColor }">
          <div class="el-step__icon-inner" :key="forceRerenderIndex">
            {{ data.index }}
          </div>
        </div>
        <el-tag class="ms-left-btn" size="small" :style="{ color: color, 'background-color': backgroundColor }"
          >{{ title }}
        </el-tag>
        <slot name="behindHeaderLeft" v-if="!isMax"></slot>
      </slot>

      <span v-show="!isMax">
        <slot name="headerLeft">
          <i
            class="icon el-icon-arrow-right"
            :class="{ 'is-active': data.active }"
            @click="active(data)"
            v-if="data.type != 'scenario' && !isMax"
            @click.stop />
          <span @click.stop v-if="isShowInput && isShowNameInput">
            <el-input
              :draggable="draggable"
              size="mini"
              v-model="data.name"
              class="name-input"
              @focus="active(data)"
              @blur="isShowInput = false"
              :placeholder="$t('commons.input_name')"
              ref="nameEdit"
              :disabled="data.disabled" />
          </span>

          <span
            :class="showVersion ? 'scenario-unscroll' : 'scenario-version'"
            id="moveout"
            @mouseenter="enter($event)"
            @mouseleave="leave($event)"
            v-else>
            <i
              class="el-icon-edit"
              style="cursor: pointer"
              @click="editName"
              v-show="data.referenced != 'REF' && !data.disabled" />
            <span>{{ data.name }}</span>
            <el-tag size="mini" v-if="data.method && !data.pluginId" style="margin-left: 1rem">{{
              getMethod()
            }}</el-tag>
            <slot name="afterTitle" />
          </span>
        </slot>
      </span>
      <span v-show="isMax">
        <slot name="headerLeft">
          <span class="ms-step-name-width">{{ data.name }}</span>
        </slot>
      </span>

      <div
        v-if="!ifFromVariableAdvance"
        class="header-right"
        @click.stop>
        <slot name="message" v-show="!isMax"></slot>
        <slot name="debugStepCode"></slot>

        <slot name="button" v-if="showVersion"></slot>

        <el-tooltip :content="$t('test_resource_pool.enable_disable')" placement="top" v-if="showBtn"
                    v-permission="[
          'PROJECT_API_SCENARIO:READ+EDIT',
          'PROJECT_API_SCENARIO:READ+CREATE',
          'PROJECT_API_SCENARIO:READ+COPY',
        ]">
          <el-switch
            v-model="data.enable"
            class="enable-switch"
            size="mini"
            :disabled="!showVersion || isDeleted || isEnabled()" />
        </el-tooltip>

        <el-button
          v-if="showVersion && showCopy"
          size="mini"
          icon="el-icon-copy-document"
          circle
          @click="copyRow"
          style="padding: 5px"
          v-permission="[
          'PROJECT_API_SCENARIO:READ+EDIT',
          'PROJECT_API_SCENARIO:READ+CREATE',
          'PROJECT_API_SCENARIO:READ+COPY',
        ]"
          :disabled="(data.disabled && !data.root && !data.isCopy) || !showVersion || isDeleted" />

        <el-button
          v-show="isSingleButton"
          size="mini"
          icon="el-icon-delete"
          type="danger"
          style="padding: 5px"
          circle
          @click="remove"
          :disabled="(data.disabled && !data.root && !data.isCopy) || !showVersion || isDeleted"
          v-permission="[
          'PROJECT_API_SCENARIO:READ+EDIT',
          'PROJECT_API_SCENARIO:READ+CREATE',
          'PROJECT_API_SCENARIO:READ+COPY',
          ]"/>

        <step-extend-btns
          style="display: contents"
          :data="data"
          :environmentType="environmentType"
          :environmentGroupId="environmentGroupId"
          :envMap="envMap"
          :is-scenario="true"
          @enable="enable"
          @copy="copyRow"
          @remove="remove"
          @openScenario="openScenario"
          v-permission="[
          'PROJECT_API_SCENARIO:READ+EDIT',
          'PROJECT_API_SCENARIO:READ+CREATE',
          'PROJECT_API_SCENARIO:READ+COPY',
        ]"
          v-show="isMoreButton" />
      </div>
    </div>
    <!--最大化不显示具体内容-->
    <div v-if="!isMax">
      <el-collapse-transition>
        <!-- 这里的组件默认不展开时不加载 -->
        <div v-if="data.active && showCollapse" :draggable="draggable">
          <el-divider></el-divider>
          <fieldset :disabled="data.disabled" class="ms-fieldset">
            <!--四种协议请求内容-->
            <slot name="request"></slot>
            <!--其他模版内容，比如断言，提取等-->
            <slot></slot>
          </fieldset>
          <!--四种协议执行结果内容-->
          <slot name="result"></slot>
        </div>
      </el-collapse-transition>
    </div>
  </el-card>
</template>

<script>
import StepExtendBtns from '../component/StepExtendBtns';
import { STEP } from '../Setting';
import { useApiStore } from '@/store';

let store = useApiStore();

export default {
  name: 'ApiBaseComponent',
  components: { StepExtendBtns },
  data() {
    return {
      isShowInput: false,
      colorStyle: '',
      stepFilter: new STEP(),
    };
  },
  props: {
    draggable: Boolean,
    innerStep: {
      type: Boolean,
      default: false,
    },
    isMax: {
      type: Boolean,
      default: false,
    },
    showBtn: {
      type: Boolean,
      default: true,
    },
    showVersion: {
      type: Boolean,
      default: true,
    },
    data: {
      type: Object,
      default() {
        return {};
      },
    },
    color: {
      type: String,
      default() {
        return '#B8741A';
      },
    },
    backgroundColor: {
      type: String,
      default() {
        return '#F9F1EA';
      },
    },
    showCollapse: {
      type: Boolean,
      default() {
        return true;
      },
    },
    isShowNameInput: {
      type: Boolean,
      default() {
        return true;
      },
    },
    title: String,
    ifFromVariableAdvance: {
      type: Boolean,
      default: false,
    },
    environmentType: String,
    environmentGroupId: String,
    envMap: Map,
    showEnable: {
      type: Boolean,
      default: true,
    },
    showCopy: {
      type: Boolean,
      default: true,
    },
    isDeleted: {
      type: Boolean,
      default: false,
    },
  },
  watch: {
    selectStep() {
      if (store.selectStep && store.selectStep.resourceId === this.data.resourceId) {
        this.colorStyle = this.color;
      } else {
        this.colorStyle = '';
      }
    },
  },
  created() {
    let typeArray = ["LoopController", "IfController","TransactionController"];
    if (typeArray.includes(this.data.type) && !this.data.disabled) {
      this.data.hashTree.forEach(item => {
        item.isCopy = true;
      })
    }
    if (!this.data.name) {
      this.isShowInput = true;
    }
    if (this.$refs.nameEdit) {
      this.$nextTick(() => {
        this.$refs.nameEdit.focus();
      });
    }
    if (this.data && this.stepFilter.get('AllSamplerProxy').indexOf(this.data.type) != -1) {
      if (!this.data.method) {
        this.data.method = this.data.protocol;
      }
    }
  },
  computed: {
    selectStep() {
      return store.selectStep;
    },
    forceRerenderIndex() {
      return store.forceRerenderIndex;
    },
    isSingleButton() {
      if (this.data.type === 'ConstantTimer' || this.data.type === 'Assertions') {
        return (
          this.innerStep && this.showVersion && this.stepFilter.get('ALlSamplerStep').indexOf(this.data.type) !== -1
        );
      }
      return this.showVersion && this.stepFilter.get('ALlSamplerStep').indexOf(this.data.type) !== -1;
    },
    isMoreButton() {
      if (this.data.type === 'ConstantTimer' || this.data.type === 'Assertions') {
        return (
          !this.innerStep ||
          (this.showBtn &&
            (!this.data.disabled || this.data.root || this.data.isCopy || this.data.showExtend) &&
            this.showVersion &&
            this.stepFilter.get('ALlSamplerStep').indexOf(this.data.type) === -1)
        );
      }
      return (
        this.showBtn &&
        (!this.data.disabled || this.data.root || this.isDeleted || this.data.isCopy || this.data.showExtend) &&
        this.showVersion &&
        this.stepFilter.get('ALlSamplerStep').indexOf(this.data.type) === -1
      );
    },
  },
  methods: {
    isEnabled() {
      return this.stepFilter.get("ALlSamplerStep").indexOf(this.data.type) !== -1 && this.data.caseEnable;
    },
    active() {
      this.$emit('active');
    },
    getMethod() {
      if (this.data.protocol === 'HTTP') {
        return this.data.method;
      } else if (this.data.protocol === 'dubbo://') {
        return 'DUBBO';
      } else {
        return this.data.protocol;
      }
    },
    copyRow() {
      this.$emit('copy');
    },
    remove() {
      this.$emit('remove');
    },
    openScenario(data) {
      this.$emit('openScenario', data);
    },
    editName() {
      this.isShowInput = true;
      this.$nextTick(() => {
        this.$refs.nameEdit.focus();
      });
    },
    enter($event) {
      if (this.showVersion) {
        $event.currentTarget.className = 'scenario-name';
      } else {
        $event.currentTarget.className = 'scenario-version';
      }
    },
    leave($event) {
      if (this.showVersion) {
        $event.currentTarget.className = 'scenario-unscroll';
      } else {
        $event.currentTarget.className = 'scenario-version';
      }
    },
    enable() {
      this.data.enable = !this.data.enable;
    },
  },
};
</script>

<style scoped>
.icon.is-active {
  transform: rotate(90deg);
}

.name-input {
  width: 30%;
}

.el-icon-arrow-right {
  margin-right: 3px;
}

.ms-left-btn {
  margin-right: 5px;
  margin-left: 0px;
}

.ms-base-header {
  min-height: 26px;
}

.header-right {
  margin-top: 1px;
  float: right;
  z-index: 1;
}

.enable-switch {
  margin: 0px 5px 0px;
  padding-bottom: 2px;
  width: 30px;
}

.scenario-version {
  display: inline-block;
  font-size: 13px;
  margin: 0 5px;
  /*overflow-x: hidden;*/
  overflow-x: scroll;
  overflow-y: hidden;
  padding-bottom: 0;
  /*text-overflow: ellipsis;*/
  vertical-align: middle;
  white-space: nowrap;
  width: calc(100% - 23rem);
  height: auto;
}

.scenario-version::-webkit-scrollbar {
  background-color: #fff;
}

/*定义滚动条轨道 内阴影+圆角*/
.scenario-version::-webkit-scrollbar-track {
  -webkit-box-shadow: inset 0 0 6px #fff;
  border-radius: 1px;
  background-color: #ffffff;
}

/*定义滑块 内阴影+圆角*/
.scenario-version::-webkit-scrollbar-thumb {
  border-radius: 1px;
  -webkit-box-shadow: inset 0 0 6px #fff;
  background-color: #783887;
}

.scenario-version::-webkit-scrollbar {
  /* width: 0px; */
  height: 3px;
  position: fixed;
}

.scenario-name {
  display: inline-block;
  font-size: 13px;
  margin: 0 0px;
  /*overflow-x: hidden;*/
  overflow-x: auto;
  overflow-y: hidden;
  padding-bottom: 0;
  /*text-overflow: ellipsis;*/
  vertical-align: middle;
  white-space: nowrap;
  width: calc(100% - 30rem);
  height: auto;
  scrollbar-width: thin;
  scrollbar-color: transparent transparent;
  scrollbar-track-color: transparent;
  -ms-scrollbar-track-color: transparent;
}

.scenario-name::-webkit-scrollbar {
  background-color: #fff;
}

/*定义滚动条轨道 内阴影+圆角*/
.scenario-name::-webkit-scrollbar-track {
  -webkit-box-shadow: inset 0 0 6px #fff;
  border-radius: 1px;
  background-color: #ffffff;
}

/*定义滑块 内阴影+圆角*/
.scenario-name::-webkit-scrollbar-thumb {
  border-radius: 1px;
  -webkit-box-shadow: inset 0 0 6px #fff;
  background-color: #783887;
}

.scenario-name::-webkit-scrollbar {
  /* width: 0px; */
  height: 3px;
  position: fixed;
}

.scenario-unscroll {
  display: inline-block;
  font-size: 13px;
  margin: 0 0px;
  overflow-x: hidden;
  /*overflow-x: auto;*/
  overflow-y: hidden;
  padding-bottom: 0;
  /*text-overflow: ellipsis;*/
  vertical-align: middle;
  white-space: nowrap;
  width: calc(100% - 30rem);
  height: auto;
  scrollbar-width: thin;
  scrollbar-color: transparent transparent;
  scrollbar-track-color: transparent;
  -ms-scrollbar-track-color: transparent;
}

:deep(.el-step__icon) {
  width: 20px;
  height: 20px;
  font-size: 12px;
}

fieldset {
  padding: 0px;
  margin: 0px;
  min-width: 100%;
  min-inline-size: 0px;
  border: 0px;
}

.ms-step-name-width {
  font-size: 12px;
  display: inline-block;
  margin: 0 0px;
  overflow-x: hidden;
  padding-bottom: 0;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
  width: 130px;
}

.ms-base-card {
  min-height: 36px;
}

.is-text {
  margin-right: 5px;
}
</style>
