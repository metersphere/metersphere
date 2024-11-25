<template>
  <a-select
    v-model="project"
    :class="props.class || 'w-[260px]'"
    allow-search
    :allow-create="false"
    @change="selectProject"
  >
    <template v-if="!props.useDefaultArrowIcon" #arrow-icon>
      <icon-caret-down />
    </template>
    <template v-if="$slots.prefix" #prefix>
      <slot name="prefix"></slot>
    </template>
    <a-tooltip v-for="item of projectList" :key="item.id" :mouse-enter-delay="500" :content="item.name">
      <a-option :value="item.id" :class="item.id === project ? 'arco-select-option-selected' : ''">
        {{ item.name }}
      </a-option>
    </a-tooltip>
  </a-select>
</template>

<script setup lang="ts">
  import { getProjectList } from '@/api/modules/project-management/project';
  import useAppStore from '@/store/modules/app';

  import type { ProjectListItem } from '@/models/setting/project';

  const props = defineProps<{
    class?: string;
    useDefaultArrowIcon?: boolean;
  }>();
  const emit = defineEmits<{
    (e: 'change', val: string, project?: ProjectListItem): void;
  }>();

  const appStore = useAppStore();
  const projectList = ref<ProjectListItem[]>([]);
  const project = defineModel<string>('project', {
    default: () => '',
  });

  function selectProject(
    value: string | number | boolean | Record<string, any> | (string | number | boolean | Record<string, any>)[]
  ) {
    let _project = projectList.value.find((item) => item.id === value);
    // 项目移除成员后，再次进去待办无获取内容
    if (!_project) {
      const [_pro] = projectList.value;
      _project = _pro;
      project.value = projectList.value[0].id;
    } else {
      project.value = value as string;
    }
    // 确保值更新后再触发
    nextTick(() => {
      emit('change', project.value, _project);
    });
  }

  async function init() {
    if (!project.value) {
      project.value = appStore.currentProjectId;
    }
    try {
      if (appStore.currentOrgId) {
        const res = await getProjectList(appStore.getCurrentOrgId);
        projectList.value = res;
        selectProject(project.value);
      } else {
        projectList.value = [];
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  onBeforeMount(() => {
    init();
  });

  defineExpose({
    init,
  });
</script>

<style lang="less" scoped></style>
