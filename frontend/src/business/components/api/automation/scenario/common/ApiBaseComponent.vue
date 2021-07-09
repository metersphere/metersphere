<template>
  <el-card>
    <div class="header" @click="active(data)">
      <slot name="beforeHeaderLeft">
        <div v-if="data.index" class="el-step__icon is-text" style="margin-right: 10px;" :style="{'color': color, 'background-color': backgroundColor}">
          <div class="el-step__icon-inner">{{data.index}}</div>
        </div>
        <el-tag class="ms-left-btn" size="small" :style="{'color': color, 'background-color': backgroundColor}">{{title}}</el-tag>
        <el-tag size="mini" v-if="data.method">{{getMethod()}}</el-tag>
      </slot>
      <slot name="behindHeaderLeft" v-if="!isMax"></slot>

      <span>
        <slot name="headerLeft">
          <i class="icon el-icon-arrow-right" :class="{'is-active': data.active}"
             @click="active(data)" v-if="data.type!='scenario' && !isMax " @click.stop/>
          <span @click.stop v-if="isShowInput && isShowNameInput">
            <el-input :draggable="draggable" size="mini" v-model="data.name" class="name-input" @focus="active(data)"
                      @blur="isShowInput = false" :placeholder="$t('commons.input_name')" ref="nameEdit" :disabled="data.disabled"/>
          </span>
          <span :class="isMax?'ms-step-name':'scenario-name'" v-else>
            <i class="el-icon-edit" style="cursor:pointer;" @click="editName"
               v-if="data.referenced!='REF' && !data.disabled"/>
            <el-tooltip placement="top" :content="data.name">
              <span>{{data.name}}</span>
            </el-tooltip>
          </span>
        </slot>
      </span>

      <div class="header-right" @click.stop>
        <slot name="message"></slot>
        <slot name="debugStepCode"></slot>
        <el-tooltip :content="$t('test_resource_pool.enable_disable')" placement="top" v-if="showBtn">
          <el-switch v-model="data.enable" class="enable-switch" size="mini" :disabled="data.disabled && !data.root" style="width: 30px"/>
        </el-tooltip>
        <slot name="button"></slot>
        <el-tooltip content="Copy" placement="top">
          <el-button size="mini" icon="el-icon-copy-document" circle @click="copyRow" style="padding: 5px" :disabled="data.disabled && !data.root"/>
        </el-tooltip>
        <step-extend-btns style="display: contents" :data="data" @copy="copyRow" @remove="remove" @openScenario="openScenario" v-if="showBtn && (!data.disabled || data.root)"/>
      </div>

    </div>
    <!--最大化不显示具体内容-->
    <div class="header" v-if="!isMax">
      <el-collapse-transition>
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
  import StepExtendBtns from "../component/StepExtendBtns";
  import {ELEMENTS} from "../Setting";

  export default {
    name: "ApiBaseComponent",
    components: {StepExtendBtns},
    data() {
      return {
        isShowInput: false
      }
    },
    props: {
      draggable: Boolean,
      isMax: {
        type: Boolean,
        default: false,
      },
      showBtn: {
        type: Boolean,
        default: true,
      },
      data: {
        type: Object,
        default() {
          return {}
        },
      },
      color: {
        type: String,
        default() {
          return "#B8741A"
        }
      },
      backgroundColor: {
        type: String,
        default() {
          return "#F9F1EA"
        }
      },
      showCollapse: {
        type: Boolean,
        default() {
          return true
        }
      },
      isShowNameInput: {
        type: Boolean,
        default() {
          return true
        }
      },
      title: String
    },
    created() {
      if (!this.data.name) {
        this.isShowInput = true;
      }
      if (this.$refs.nameEdit) {
        this.$nextTick(() => {
          this.$refs.nameEdit.focus();
        });
      }
      if (this.data && ELEMENTS.get("AllSamplerProxy").indexOf(this.data.type) != -1) {
        if (!this.data.method) {
          this.data.method = this.data.protocol;
        }
      }
    },
    methods: {
      active() {
        this.$emit('active');
      },
      getMethod() {
        if (this.data.protocol === "HTTP") {
          return this.data.method;
        }
        else if (this.data.protocol === "dubbo://") {
          return "DUBBO";
        }
        else {
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
      }
    }
  }
</script>

<style scoped>

  .icon.is-active {
    transform: rotate(90deg);
  }

  .name-input {
    width: 30%;
  }

  .el-icon-arrow-right {
    margin-right: 5px;
  }

  .ms-left-btn {
    font-size: 13px;
    margin-right: 15px;
  }

  .header-right {
    margin-top: 0px;
    float: right;
    z-index: 1;
  }

  .enable-switch {
    margin-right: 10px;
  }

  .ms-step-name {
    display: inline-block;
    font-size: 13px;
    margin: 0 5px;
    overflow-x: hidden;
    padding-bottom: 0;
    text-overflow: ellipsis;
    vertical-align: middle;
    white-space: nowrap;
    width: 140px;
  }

  .scenario-name {
    display: inline-block;
    font-size: 13px;
    margin: 0 5px;
    overflow-x: hidden;
    padding-bottom: 0;
    text-overflow: ellipsis;
    vertical-align: middle;
    white-space: nowrap;
    width: calc(100% - 35rem);
  }

  /deep/ .el-step__icon {
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
    display: inline-block;
    margin: 0 5px;
    overflow-x: hidden;
    padding-bottom: 0;
    text-overflow: ellipsis;
    vertical-align: middle;
    white-space: nowrap;
    width: 400px;
  }

</style>
