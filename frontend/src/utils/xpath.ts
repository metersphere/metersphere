import { DOMParser } from '@xmldom/xmldom';
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
    const xmlDoc = new DOMParser().parseFromString(xmlText, 'text/xml');

    // 使用 XPath 查询匹配的节点
    const nodes = xpath.select(xpathQuery, xmlDoc);

    // 返回匹配结果
    return nodes;
  } catch (error) {
    // eslint-disable-next-line no-console
    console.error('Error parsing XML or executing XPath query:', error);
    return null;
  }
}

export default { matchXMLWithXPath };
