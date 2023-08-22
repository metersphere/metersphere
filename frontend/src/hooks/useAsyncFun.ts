import { ref } from 'vue';

export default function useAsyncHandler() {
  const loading = ref<boolean>(false);

  async function handleAsyncProcess<T>(reqAsyncFun: T): Promise<any> {
    loading.value = true;
    try {
      await reqAsyncFun;
    } catch (error) {
      console.log(error);
      loading.value = false;
      return Promise.reject();
    } finally {
      loading.value = false;
    }
  }

  return {
    loading,
    handleAsyncProcess,
  };
}
