<template>
  <div>
    <span v-for="(permission, index) in permissions" :key="index" style="margin-right: 25px;">
      <el-checkbox v-model="permission['checked']" @change="change($event, permission)">
        {{ permission.name }}
      </el-checkbox>
    </span>
  </div>
</template>

<script>
export default {
  name: "GroupPermission",
  props: {
    permissions: {
      type: Array,
      default() {
        return {}
      }
    },
    selected: {
      type: Array,
      default() {
        return []
      }
    }
  },
  data() {
    return {

    }
  },
  methods: {
    change(val, permission) {
      permission.checked = val;
    }
  }
}
</script>

<style scoped>

</style>
