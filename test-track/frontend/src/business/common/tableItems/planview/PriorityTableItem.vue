<template>
  <div>
    <el-tag v-if="value === 'P0'" class="status-label P0">
      {{ value }}
    </el-tag>
    <el-tag v-else-if="value === 'P1'" class="status-label P1">
      {{ value }}
    </el-tag>
    <el-tag v-else-if="value === 'P2'" class="status-label P2">
      {{ value }}
    </el-tag>
    <el-tag v-else-if="value === 'P3'" class="status-label P3">
      {{ value }}
    </el-tag>
    <el-tag v-else class="status-label unknown">
      {{ value }}
    </el-tag>
  </div>

</template>

<script>
    import MsTag from "metersphere-frontend/src/components/MsTag";
    export default {
      name: "PriorityTableItem",
      components: {MsTag},
      props: {
        value: {
          type: String
        }
      }
    }
</script>

<style scoped>
.status-label {
  height: 24px;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  padding: 0px 6px;
  border-radius: 2px;
  border-color: transparent;
}

.P0 {
  background-color: rgba(245, 74, 69, 0.2);;
  color: #F54A45;
}

.P1 {
  background-color: rgba(255, 136, 0, 0.2);
  color: #FF8800;
}

.P2 {
  background-color: rgba(120, 56, 135, 0.2);
  color: #783887;
}

.P3 {
  background-color: rgba(0, 214, 185, 0.2);
  color: #00D6B9;
}

.unknown {
  background-color: rgba(31, 35, 41, 0.1);
  color: #1F2329;
}
</style>
