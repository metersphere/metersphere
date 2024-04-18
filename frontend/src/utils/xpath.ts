import { DOMParser as XmlDOMParser } from '@xmldom/xmldom';
import * as xpath from 'xpath';

/**
 * xpath 匹配 xml 文本
 * @param xmlText xml文本
 * @param xpathQuery xpath
 * @returns  匹配节点
 */
export function matchXMLWithXPath(xmlText: string, xpathQuery: string): xpath.SelectReturnType {
  try {
    // 解析 XML 文本
    const xmlDoc = new XmlDOMParser().parseFromString(xmlText, 'text/xml');

    // 创建一个命名空间解析器
    const resolver = (prefix: string) => {
      // 获取命名空间的 URI
      const nsUri = xmlDoc.lookupNamespaceURI(prefix);
      return nsUri || null;
    };

    // 使用 XPath 查询匹配的节点
    const nodes = xpath.selectWithResolver(xpathQuery, xmlDoc, { lookupNamespaceURI: resolver });

    // 返回匹配结果
    return nodes;
  } catch (error) {
    // eslint-disable-next-line no-console
    console.error('Error parsing XML or executing XPath query:', error);
    return null;
  }
}

/**
 * 通过 xpath 提取 html 文档对应节点内容
 * @param htmlText html文本
 * @param xpathQuery xpath
 * @returns  匹配节点的文本内容
 */
export function extractTextFromHtmlWithXPath(htmlText: string, xpathQuery: string): Node[] {
  try {
    // 解析 HTML 文本
    const parser = new DOMParser();
    const htmlDoc = parser.parseFromString(htmlText, 'text/html');

    // 使用 XPath 查询匹配的节点
    const iterator = document.evaluate(xpathQuery, htmlDoc.documentElement, null, XPathResult.ANY_TYPE, null);
    // 提取匹配节点的文本内容
    let node = iterator.iterateNext();
    const nodes: Node[] = [];
    while (node) {
      nodes.push(node);
      node = iterator.iterateNext();
    }

    // 返回匹配节点
    return nodes;
  } catch (error) {
    // eslint-disable-next-line no-console
    console.error('Error parsing HTML or executing XPath query:', error);
    return [];
  }
}
