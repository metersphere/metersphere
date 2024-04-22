<template>
  <div>
    <div v-if="parsedXml" class="container">
      <div v-for="(node, index) in flattenedXml" :key="index">
        <span style="white-space: pre" @click="copyXPath(node.xpath)" v-html="node.content"></span>
      </div>
    </div>
    <div v-if="!isValidXml">{{ t('ms.jsonpathPicker.xmlNotValid') }}</div>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';

  import { XpathNode } from './types';
  import HtmlBeautify from 'pretty';
  import XmlBeautify from 'xml-beautify';

  const props = defineProps<{
    xmlString: string;
  }>();
  const emit = defineEmits(['pick', 'init']);

  const { t } = useI18n();

  const parsedXml = ref<Document | null>(null);
  const flattenedXml = ref<XpathNode[]>([]);
  const tempXmls = ref<XpathNode[]>([]);
  const isValidXml = ref(true); // 是否是合法的xml

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
        `count(ancestor-or-self::*[local-name()="${node.localName}"]/preceding-sibling::*[local-name()="${node.localName}"]) + 1`,
        node,
        (prefix) => {
          // 获取命名空间的 URI
          const nsUri = node.lookupNamespaceURI(prefix);
          return nsUri || null;
        },
        XPathResult.NUMBER_TYPE,
        null
      ).numberValue; // 这里是执行 XPATH 表达式，获取当前节点在同级节点中的下标

      const xpath = `${currentPath}/*[local-name()="${node.localName}"][${sameNodesIndex}]`; // 拼接规则：当前路径/当前节点名[当前节点在同级同名节点中的下标]
      tempXmls.value.push({ content: node.nodeName, xpath });
      const children = Array.from(node.children);
      children.forEach((child) => {
        flattenXml(child, xpath); // 递归处理子节点
      });
    } else {
      // 同级的同名节点数量等于 1 时，不需要给当前节点名的 xpath 添加下标，因为这个标签是唯一的
      const xpath = `${currentPath}/*[local-name()="${node.localName}"]`;
      tempXmls.value.push({ content: node.nodeName, xpath });
      const children = Array.from(node.children);
      children.forEach((child) => {
        flattenXml(child, xpath);
      });
    }
  }

  /**
   * 将html扁平化
   * @param node html节点
   * @param currentPath 当前路径
   */
  function flattenHtml(node: HTMLElement | Element, currentPath: string) {
    const sameNameSiblings = getSameNameSiblings(node);
    if (sameNameSiblings.length > 1) {
      const sameNodesIndex = document.evaluate(
        `count(ancestor-or-self::${node.nodeName.toLowerCase()}/preceding-sibling::${node.nodeName.toLowerCase()}) + 1`,
        node,
        null,
        XPathResult.NUMBER_TYPE,
        null
      ).numberValue;
      const xpath = `${currentPath}/${node.nodeName.toLowerCase()}[${sameNodesIndex}]`;
      tempXmls.value.push({ content: node.nodeName.toLowerCase(), xpath });
      const children = Array.from(node.children);
      children.forEach((child) => {
        flattenHtml(child, xpath);
      });
    } else {
      const xpath = `${currentPath}/${node.nodeName.toLowerCase()}`;
      tempXmls.value.push({ content: node.nodeName.toLowerCase(), xpath });
      const children = Array.from(node.children);
      children.forEach((child) => {
        flattenHtml(child, xpath);
      });
    }
  }

  function copyXPath(xpath: string) {
    if (xpath) {
      emit('pick', xpath);
    }
  }

  /**
   * 替换文档
   * @param beautifyDoc 格式化后的文档
   * @param isHtml 是否是html
   */
  function replaceDoc(beautifyDoc: string, isHtml = false) {
    // 先将 XML/HTML 字符串格式化，然后解析转换并给每个开始标签加上复制 icon
    let resultArr: XpathNode[] = [];
    const tempStr = beautifyDoc
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/(&lt;([^/][^&]*?)&gt;)/g, '<span style="color: rgb(var(--primary-5));cursor: pointer">$1📋</span>');
    if (isHtml) {
      resultArr = HtmlBeautify(
        tempStr.replace(/(\S)(?=<)/gs, '$1\n'), // html 标签换行
        { ocd: true }
      )
        .split(/\r?\n/)
        .map((e) => ({ content: e, xpath: '' }));
    } else {
      resultArr = tempStr.split(/\r?\n/).map((e) => ({ content: e, xpath: '' }));
    }
    flattenedXml.value = resultArr;
  }

  /**
   * 解析html
   */
  function parseHtml() {
    try {
      const parser = new DOMParser();
      const xmlDoc = parser.parseFromString(props.xmlString, 'text/html');
      // 如果存在 parsererror 元素，说明 HTML 不合法
      const htmlErrors = xmlDoc.getElementsByTagName('parsererror');
      if (htmlErrors.length > 0) {
        isValidXml.value = false;
        return;
      }
      isValidXml.value = true;
      parsedXml.value = xmlDoc;
      const beautifyDoc = HtmlBeautify(props.xmlString, { ocd: true });
      replaceDoc(beautifyDoc, true);
      // 解析真实 HTML 并将其扁平化，得到每个节点的 xpath
      tempXmls.value = [];
      flattenHtml(xmlDoc.documentElement, '');
      // 将扁平化后的 XML/HTML 字符串中的每个节点的 xpath 替换为真实的 xpath
      flattenedXml.value = flattenedXml.value
        .map((e) => {
          const targetNodeIndex = tempXmls.value.findIndex((txt) => e.content.includes(`&lt;${txt.content}`));
          if (targetNodeIndex >= 0) {
            const { xpath } = tempXmls.value[targetNodeIndex];
            tempXmls.value.splice(targetNodeIndex, 1); // 匹配成功后，将匹配到的节点从 tempXmls 中删除，避免重复匹配
            return {
              ...e,
              xpath,
            };
          }
          return e.content.includes('&lt;/') ? e : false;
        })
        .filter(Boolean) as any[];
      emit('init', 'html');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error('Error parsing XML:', error);
    }
  }

  /**
   * 解析xml
   */
  function parseXml() {
    try {
      const parser = new DOMParser();
      const xmlDoc = parser.parseFromString(props.xmlString, 'application/xml');
      // 如果存在 parsererror 元素，说明 XML 不合法
      const xmlErrors = xmlDoc.getElementsByTagName('parsererror');
      if (xmlErrors.length > 0) {
        parseHtml();
        return;
      }
      isValidXml.value = true;
      parsedXml.value = xmlDoc;
      const beautifyDoc = new XmlBeautify().beautify(props.xmlString);
      replaceDoc(beautifyDoc);
      // 解析真实 XML 并将其扁平化，得到每个节点的 xpath
      tempXmls.value = [];
      flattenXml(xmlDoc.documentElement, '');
      // 将扁平化后的 XML 字符串中的每个节点的 xpath 替换为真实的 xpath
      flattenedXml.value = flattenedXml.value
        .map((e) => {
          const targetNodeIndex = tempXmls.value.findIndex((txt) => e.content.includes(`&lt;${txt.content}`));
          if (targetNodeIndex >= 0) {
            const { xpath } = tempXmls.value[targetNodeIndex];
            tempXmls.value.splice(targetNodeIndex, 1); // 匹配成功后，将匹配到的节点从 tempXmls 中删除，避免重复匹配
            return {
              ...e,
              xpath,
            };
          }
          return false;
        })
        .filter(Boolean) as any[];
      emit('init', 'xml');
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

<style lang="less" scoped>
  .container {
    @apply h-full overflow-y-auto;
    .ms-scroll-bar();

    padding: 12px 1.85em;
    border-radius: var(--border-radius-small);
  }
</style>
