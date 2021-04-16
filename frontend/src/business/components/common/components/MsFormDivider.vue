<template>
  <p> {{title}} </p>
</template>

<script>
export default {
  name: "MsFormDivider",
  props: ['title']
};
</script>

<style scoped>
p {
  padding: 3px 5px;
  font-size: 16px;
  border-radius: 4px;
  border-left: 4px solid #783887;
  margin: 20px 0;
}

</style>
