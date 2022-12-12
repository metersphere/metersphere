<template>
    <span>
      <el-tag v-if="value === 'Prepare'" class="status-label prepare">
        {{ $t('test_track.review.prepare') }}
      </el-tag>
      <el-tag v-else-if="value === 'Pass'" class="status-label pass">
        {{ $t('test_track.review.pass') }}
      </el-tag>
      <el-tag v-else-if="value === 'UnPass'" class="status-label unPass">
        {{ $t('test_track.review.un_pass') }}
      </el-tag>
      <el-tag v-else-if="value === 'Underway'" class="status-label underway">
        {{ $t('test_track.review.underway') }}
      </el-tag>
      <el-tag v-else-if="value === 'Again'" class="status-label again">
        {{ $t('test_track.review.again') }}
      </el-tag>
      <el-tag v-else class="status-label prepare">
        {{ value }}
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
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  padding: 0 6px;
  border-radius: 2px;
  border-color: transparent;
  align-items: center;
}

.pass {
  color: #2EA121;
  background-color: rgba(52, 199, 36, 0.2);
}

.unPass {
  color: #D83931;
  background-color: rgba(245, 74, 69, 0.2);
}

.again {
  color: #DE7802;
  background-color: rgba(255, 136, 0, 0.2);
}

.underway {
  color: #783887;
  background-color:  rgba(120, 56, 135, 0.2);
}

.prepare {
  color: #646A73;
  background-color: rgba(31, 35, 41, 0.1);;
}
</style>
