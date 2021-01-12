<template>
  <el-card class="api-component">
    <div class="header" @click="active(data)">

      <div v-if="data.index" class="el-step__icon is-text" style="margin-right: 10px;" :style="{'color': color, 'background-color': backgroundColor}">
        <div class="el-step__icon-inner">{{data.index}}</div>
      </div>
      <el-button class="ms-left-buttion" size="small" :style="{'color': color, 'background-color': backgroundColor}">{{title}}</el-button>

      <span @click.stop>
        <slot name="headerLeft">
          <i class="icon el-icon-arrow-right" :class="{'is-active': data.active}"
             @click="active(data)"/>
          <el-input :draggable="draggable" v-if="!data.name || isShowInput" size="small" v-model="data.name" class="name-input"
                    @blur="isShowInput = false" :placeholder="$t('commons.input_name')"/>
          <span v-else>
            {{data.name}}
            <i class="el-icon-edit" style="cursor:pointer" @click="isShowInput = true" v-tester/>
          </span>
        </slot>
      </span>

      <div class="header-right" @click.stop>
        <el-switch v-model="data.enable" class="enable-switch"/>
        <slot name="button"></slot>
        <el-button size="mini" icon="el-icon-copy-document" circle @click="copyRow"/>
        <el-button size="mini" icon="el-icon-delete" type="danger" circle @click="remove"/>
      </div>
    </div>

    <el-collapse-transition>
      <div v-if="data.active && showCollapse" draggable>
        <el-divider></el-divider>
       <slot></slot>
      </div>
    </el-collapse-transition>
  </el-card>
</template>

<script>
    export default {
      name: "ApiBaseComponent",
      data() {
        return {
          isShowInput: false
        }
      },
      props: {
        draggable: Boolean,
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
        title: String
      },
      methods: {
        active() {
          this.$set(this.data, 'active', !this.data.active);
          this.$emit('active');
        },
        copyRow() {
          this.$emit('copy');
        },
        remove() {
          this.$emit('remove');
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
    margin-right: 20px;
    float: right;
  }

  .enable-switch {
    margin-right: 10px;
  }

</style>
