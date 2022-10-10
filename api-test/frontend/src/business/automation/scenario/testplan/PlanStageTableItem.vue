<template>
    <div>
      <span v-for="item in option" :key="item.value">
        <span v-if="stage == item.value">
          {{$t(item.text)}}
        </span>
      </span>
    </div>
</template>

<script>
    export default {
      name: "PlanStageTableItem",
      components: {},
      props: {
        stage: {
          type: String
        },
        option: []
      }
    }
</script>

<style scoped>

</style>
