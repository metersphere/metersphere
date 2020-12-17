<template>
  <el-card class="card-content" v-if="isShow">
    <el-button-group>
      <el-button plain size="small" icon="el-icon-tickets" :class="{active: isApiListEnable}" @click="apiChange('api')"></el-button>
      <el-button plain class="case-button" size="small" icon="el-icon-paperclip" :class="{active: !isApiListEnable}" @click="caseChange('case')"></el-button>
    </el-button-group>
    <slot></slot>
  </el-card>
</template>

<script>
    export default {
      name: "ApiListContainer",
      data() {
        return {
          isShow: true
        }
      },
      props: {
        isApiListEnable: Boolean
      },
      methods: {
        apiChange() {
          this.$emit('isApiListEnableChange', true);
        },
        caseChange() {
          this.$emit('isApiListEnableChange', false);
        }
      }
    }
</script>

<style scoped>

  .active {
    border: solid 1px #6d317c;
  }

  .case-button {
    border-left: solid 1px #6d317c;
  }


</style>
