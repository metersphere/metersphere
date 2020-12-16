<template>
  <el-card class="card-content" v-if="isShow">
    <el-button-group>
      <el-button plain size="small" icon="el-icon-tickets" :class="{active: activeButton == 'api'}" @click="click('api')"></el-button>
      <el-button plain  size="small" icon="el-icon-paperclip" :class="{active: activeButton == 'case'}" @click="click('case')"></el-button>
    </el-button-group>
    <slot></slot>
  </el-card>
</template>

<script>
    export default {
      name: "ApiListContainer",
      data() {
        return {
          activeButton: 'api',
          isShow: true
        }
      },
      methods: {
        click(type) {
          this.activeButton = type;
          // this.reload();
        },
        // reload() {
        //   this.isShow = false;
        //   this.$nextTick(() => {
        //     this.isShow = true;
        //   })
        // }
      }
    }
</script>

<style scoped>

  /*.active {*/
    /*background-color: #409EFF;*/
  /*}*/

</style>
