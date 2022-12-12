<template>
    <span>
      <span v-if="value === 'Pass' || value === 'ForcePass'" class="status-label pass">
        {{ $t('test_track.plan_view.pass') }}
      </span>
      <span v-else-if="value === 'UnPass' || value === 'ForceUnPass'" class="status-label unPass">
        {{ $t('test_track.plan_view.not_pass') }}
      </span>
      <span v-else-if="value === 'Again'" class="status-label again">
        {{ $t('test_track.review.again') }}
      </span>
      <span v-else class="status-label">
        {{ value }}
      </span>
    </span>
</template>

<script>

export default {
  name: "CommentStatusText",
  components: {},
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
  padding: 0 6px;
  border-radius: 2px;
  border-color: transparent;
}

.pass {
  color: #34C724;
}

.unPass{
  color: #F54A45;
}

.again {
  color: #DE7802;
}
</style>
