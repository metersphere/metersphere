<template>
  <el-breadcrumb separator-class="el-icon-arrow-right">
    <el-breadcrumb-item>
      <a @click="showAll">
        <i class="el-icon-s-home"></i>&nbsp;
        {{$t('test_track.plan_view.all_case')}}
      </a>
    </el-breadcrumb-item>
    <el-breadcrumb-item v-for="nodeName in data" :key="nodeName">{{nodeName}}</el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script>
    export default {
      name: "NodeBreadcrumb",
      data() {
        return {
          data: []
        }
      },
      props: {
        nodeNames: {
          type: Array
        }
      },
      watch: {
        nodeNames() {
          this.filterData();
        }
      },
      methods: {
        showAll() {
          this.$emit('refresh');
        },
        filterData() {
          this.data = this.nodeNames;
          if (this.data.length > 4) {
            let lastData = this.data[this.data.length - 1];
            this.data.splice(1, this.data.length);
            this.data.push('...');
            this.data.push(lastData);
          }
        }
      }
    }
</script>

<style scoped>

</style>
