<template>
  <el-card class="api-component">
    <div class="header" @click="active(data)">
      <slot name="beforeHeaderLeft">
        <div v-if="data.index" class="el-step__icon is-text" style="margin-right: 10px;" :style="{'color': color, 'background-color': backgroundColor}">
          <div class="el-step__icon-inner">{{data.index}}</div>
        </div>
        <el-button class="ms-left-buttion" size="mini" :style="{'color': color, 'background-color': backgroundColor}">{{title}}</el-button>
        <el-tag size="mini" v-if="data.method">{{data.method}}</el-tag>
      </slot>

      <span @click.stop>
        <slot name="headerLeft">
          <i class="icon el-icon-arrow-right" :class="{'is-active': data.active}"
             @click="active(data)" v-if="data.type!='scenario'  && !isMax "/>
          <el-input :draggable="draggable" v-if="isShowInput && isShowNameInput" size="mini" v-model="data.name" class="name-input"
                    @blur="isShowInput = false" :placeholder="$t('commons.input_name')" ref="nameEdit" :disabled="data.disabled"/>
          <span v-else-if="isMax">
             <el-tooltip :content="data.name" placement="top">
              <span>{{data.name}}</span>
            </el-tooltip>
          </span>
          <span v-else>
            {{data.name}}
            <i class="el-icon-edit" style="cursor:pointer" @click="editName" v-tester v-if="data.referenced!='REF' && !data.disabled"/>
          </span>
        </slot>
        <slot name="behindHeaderLeft" v-if="!isMax"></slot>
      </span>

      <div class="header-right" @click.stop>
        <slot name="message"></slot>
        <el-tooltip :content="$t('test_resource_pool.enable_disable')" placement="top" v-if="showBtn">
          <el-switch v-model="data.enable" class="enable-switch"/>
        </el-tooltip>
        <slot name="button"></slot>
        <step-extend-btns style="display: contents" @copy="copyRow" @remove="remove" v-if="showBtn"/>
      </div>

    </div>
    <div class="header" v-if="!isMax">
      <fieldset :disabled="data.disabled" class="ms-fieldset">
        <el-collapse-transition>6.
          <div v-if="data.active && showCollapse" :draggable="draggable">
            <el-divider></el-divider>
            <slot></slot>
          </div>
        </el-collapse-transition>
      </fieldset>
    </div>

  </el-card>
</template>

<script>
  import StepExtendBtns from "../component/StepExtendBtns";

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
      if (this.data && this.data.type === "JmeterElement") {
        this.data.active = false;
      }
    },
    methods: {
      active() {
        // 这种写法性能极差，不要再放开了
        //this.$set(this.data, 'active', !this.data.active);
        this.$emit('active');
      },
      copyRow() {
        this.$emit('copy');
      },
      remove() {
        this.$emit('remove');
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

  .ms-left-buttion {
    margin-right: 15px;
  }

  .header-right {
    margin-top: 5px;
    float: right;
    z-index: 1;
  }

  .enable-switch {
    margin-right: 10px;
  }

  .node-title {
    display: inline-block;
    margin: 0px;
    overflow-x: hidden;
    padding-bottom: 0;
    text-overflow: ellipsis;
    vertical-align: middle;
    white-space: nowrap;
    width: 100px;
  }

  fieldset {
    padding: 0px;
    margin: 0px;
    min-width: 100%;
    min-inline-size: 0px;
    border: 0px;
  }

</style>
