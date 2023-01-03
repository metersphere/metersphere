<template>
    <span>
      <el-tag v-if="value === 'Prepare'" class="status-label prepare">
        {{ $t('test_track.review.prepare') }}
      </el-tag>
       <el-tag v-if="value === 'Again'" class="status-label again">
        {{ $t('test_track.review.again') }}
      </el-tag>
      <el-tag v-else-if="value === 'Pass'" class="status-label pass">
        {{ $t('test_track.review.pass') }}
      </el-tag>
      <el-tag v-else-if="value === 'UnPass'" class="status-label unPass">
        {{ $t('test_track.review.un_pass') }}
      </el-tag>
      <el-tag v-else class="status-label prepare">
        {{value}}
      </el-tag>
    </span>
</template>

<script>
import MsTag from "metersphere-frontend/src/components/MsTag";

export default {
  name: "ReviewStatus",
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
  padding: 1px 6px;
  border-radius: 2px;
  border-color: transparent;
}

.prepare {
  background-color: rgba(31, 35, 41, 0.1);
  color: #646A73;
}

.again {
  background-color: rgba(116, 72, 146, 0.1);
  color: #744892FF;
}

.pass {
  background-color: rgba(52, 199, 36, 0.2);
  color: #2EA121;
}

.unPass {
  background-color: rgba(245, 74, 69, 0.2);
  color: #D83931;
}
</style>
