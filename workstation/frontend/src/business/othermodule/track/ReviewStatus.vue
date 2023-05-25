<template>
    <span>
      <ms-tag v-if="value === 'Prepare'" type="info" :content="$t('test_track.review.prepare')"/>
      <ms-tag v-if="value === 'Pass'" type="success" :content="$t('test_track.review.pass')"/>
      <ms-tag v-if="value === 'UnPass'" type="danger" :content="$t('test_track.review.un_pass')"/>
      <ms-tag v-if="value === 'Underway'" type="primary" :content="$t('test_track.review.underway')"/>
      <ms-tag v-if="value === 'Again'" type="warning" :content="$t('test_track.review.again')"/>
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

</style>
