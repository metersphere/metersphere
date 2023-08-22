import { ref } from 'vue';

export default function useAsyncHandler() {
  const confirmLoading = ref<boolean>(false);

  async function handleAsyncProcess<T>(reqFun: T): Promise<any> {
    confirmLoading.value = true;
    try {
      await reqFun;
    } catch (error) {
      console.log(error);
      confirmLoading.value = false;
      return new Promise(() => {});
    } finally {
      confirmLoading.value = false;
    }
  }

  return {
    confirmLoading,
    handleAsyncProcess,
  };
}
