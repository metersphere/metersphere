<template>
  <form-create v-model:api="fApi" :rule="rule" :option="options"></form-create>
</template>

<script>
  import { defineComponent, ref } from 'vue';
  import formCreate from '@form-create/arco-design';
  import PassWord from './formcreate-password.vue';

  formCreate.component('PassWord', PassWord);
  const FormCreate = formCreate.$form();
  export default defineComponent({
    name: 'MyFormCreate',
    props: {
      rule: {
        type: Array,
        required: true,
      },
      options: {
        type: Object,
        default: () => ({}),
      },
    },
    setup(props) {
      const fApi = ref({});

      // 注册事件监听
      props.rule.forEach((item) => {
        if (item.emit && item.emit.length > 0) {
          item.emit.forEach((eventName) => {
            let fullEventName = '';
            if (item.emitPrefix) {
              fullEventName = `${item.emitPrefix}-${eventName}`;
              FormCreate.$on(fullEventName, (...args) => {
                if (eventName === 'change' && props[`change${item.field}`]) {
                  props[`change${item.field}`](...args);
                } else if (eventName === 'blur' && props[`blur${item.field}`]) {
                  props[`blur${item.field}`](...args);
                }
              });
            }
          });
        }
      });

      return {
        fApi,
      };
    },
  });
</script>

<style scoped></style>
