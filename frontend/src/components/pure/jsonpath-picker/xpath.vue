<template>
  <div v-if="parsedXml">
    <div v-for="(node, index) in flattenedXml" :key="index">
      <span style="white-space: pre" @click="copyXPath(node.xpath)" v-html="node.content"></span>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { XpathNode } from './types';

  const props = defineProps<{
    xmlString: string;
  }>();

  const emit = defineEmits(['pick']);

  const parsedXml = ref<Document | null>(null);
  const flattenedXml = ref<XpathNode[]>([]);
  const tempXmls = ref<XpathNode[]>([]);

  /**
   * 获取同名兄弟节点
   * @param node 节点
   */
  function getSameNameSiblings(node: HTMLElement | Element) {
    const siblings = node.parentNode ? Array.from(node.parentNode.children) : [];
    return siblings.filter((sibling) => sibling.tagName === node.tagName);
  }

  /**
   * 将xml扁平化
   * @param node xml节点
   * @param currentPath 当前路径
   */
  function flattenXml(node: HTMLElement | Element, currentPath: string) {
    const sameNameSiblings = getSameNameSiblings(node);
    if (sameNameSiblings.length > 1) {
      // 同级的同名节点数量大于 1 时，需要给当前节点名的 xpath 添加下标
      const sameNodesIndex = document.evaluate(
        `count(ancestor-or-self::*/preceding-sibling::${node.nodeName}) + 1`,
        node,
        null,
        XPathResult.NUMBER_TYPE,
        null
      ).numberValue; // 这里是执行 XPATH 表达式，获取当前节点在同级节点中的下标

      const xpath = `${currentPath}/${node.tagName}[${sameNodesIndex}]`; // 拼接规则：当前路径/当前节点名[当前节点在同级同名节点中的下标]
      tempXmls.value.push({ content: node.tagName, xpath });
      const children = Array.from(node.children);
      children.forEach((child) => {
        flattenXml(child, xpath); // 递归处理子节点
      });
    } else {
      // 同级的同名节点数量等于 1 时，不需要给当前节点名的 xpath 添加下标，因为这个标签是唯一的
      const xpath = `${currentPath}/${node.tagName}`;
      tempXmls.value.push({ content: node.tagName, xpath });
      const children = Array.from(node.children);
      children.forEach((child) => {
        flattenXml(child, xpath);
      });
    }
  }

  function copyXPath(xpath: string) {
    if (xpath) {
      emit('pick', xpath);
    }
  }

  /**
   * 解析xml
   */
  function parseXml() {
    try {
      const parser = new DOMParser();
      const xmlDoc = parser.parseFromString(props.xmlString, 'application/xml');
      parsedXml.value = xmlDoc;
      // 先将 XML 字符串解析转换并给每个开始标签加上复制 icon
      flattenedXml.value = props.xmlString
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/(&lt;\w+\s*[^&gt;]*&gt;)/g, '<span style="color: rgb(var(--primary-5));cursor: pointer">$1📋</span>')
        .replace(/(&lt;\/\w+\s*[^&gt;]*&gt;)/g, '<span style="color: rgb(var(--primary-5));">$1</span>')
        .split(/\r?\n/)
        .map((e) => ({ content: e, xpath: '' }));
      // 解析真实 XML 并将其扁平化，得到每个节点的 xpath
      flattenXml(xmlDoc.documentElement, '');
      // 将扁平化后的 XML 字符串中的每个节点的 xpath 替换为真实的 xpath
      flattenedXml.value = flattenedXml.value.map((e) => {
        const targetNodeIndex = tempXmls.value.findIndex((t) => e.content.includes(`&lt;${t.content}`));
        if (targetNodeIndex >= 0) {
          const { xpath } = tempXmls.value[targetNodeIndex];
          tempXmls.value.splice(targetNodeIndex, 1); // 匹配成功后，将匹配到的节点从 tempXmls 中删除，避免重复匹配
          return {
            ...e,
            xpath,
          };
        }
        return e;
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error('Error parsing XML:', error);
    }
  }

  watch(
    () => props.xmlString,
    () => {
      parseXml();
    },
    {
      immediate: true,
    }
  );
</script>
