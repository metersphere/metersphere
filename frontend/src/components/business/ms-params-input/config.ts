/* eslint-disable no-template-curly-in-string */
import { MockParamItem } from './types';

// mock基础分组
export const mockBaseGroup: MockParamItem[] = [
  {
    label: 'ms.paramsInput.bool',
    value: '@bool',
    desc: 'ms.paramsInput.boolDesc',
  },
];
// mock数字分组
export const mockNumberGroup: MockParamItem[] = [
  {
    label: 'ms.paramsInput.natural',
    value: '@natural',
    desc: 'ms.paramsInput.naturalDesc',
  },
  {
    label: 'ms.paramsInput.naturalRange',
    value: '@natural(1,100)',
    desc: 'ms.paramsInput.naturalRangeDesc',
    inputGroup: [
      {
        type: 'number',
        value: 1,
        label: 'ms.paramsInput.min',
        placeholder: 'ms.paramsInput.minNaturalPlaceholder',
      },
      {
        type: 'number',
        value: 100,
        label: 'ms.paramsInput.max',
        placeholder: 'ms.paramsInput.maxNaturalPlaceholder',
      },
    ],
  },
  {
    label: 'ms.paramsInput.integer',
    value: '@integer',
    desc: 'ms.paramsInput.integerDesc',
  },
  {
    label: 'ms.paramsInput.integerRange',
    value: '@integer(1,100)',
    desc: 'ms.paramsInput.integerRangeDesc',
    inputGroup: [
      {
        type: 'number',
        value: 1,
        label: 'ms.paramsInput.min',
        placeholder: 'ms.paramsInput.minIntegerPlaceholder',
      },
      {
        type: 'number',
        value: 100,
        label: 'ms.paramsInput.max',
        placeholder: 'ms.paramsInput.maxIntegerPlaceholder',
      },
    ],
  },
  {
    label: 'ms.paramsInput.float',
    value: '@floatNumber(1,10,2,5)',
    desc: 'ms.paramsInput.floatDesc',
    inputGroup: [
      {
        type: 'number',
        value: 1,
        label: 'ms.paramsInput.floatIntegerMin',
        placeholder: 'ms.paramsInput.commonPlaceholder',
      },
      {
        type: 'number',
        value: 10,
        label: 'ms.paramsInput.floatIntegerMax',
        placeholder: 'ms.paramsInput.commonPlaceholder',
      },
      {
        type: 'number',
        value: 2,
        label: 'ms.paramsInput.floatMin',
        placeholder: 'ms.paramsInput.commonPlaceholder',
      },
      {
        type: 'number',
        value: 5,
        label: 'ms.paramsInput.floatMax',
        placeholder: 'ms.paramsInput.commonPlaceholder',
      },
    ],
  },
  {
    label: 'ms.paramsInput.integerArray',
    value: '@range(1,100,1)',
    desc: 'ms.paramsInput.integerArrayDesc',
    inputGroup: [
      {
        type: 'number',
        value: 1,
        label: 'start',
        placeholder: 'ms.paramsInput.integerArrayStartPlaceholder',
      },
      {
        type: 'number',
        value: 100,
        label: 'end',
        placeholder: 'ms.paramsInput.integerArrayEndPlaceholder',
      },
      {
        type: 'number',
        value: 1,
        label: 'step',
        placeholder: 'ms.paramsInput.integerArrayStepPlaceholder',
      },
    ],
  },
];
// mock字符串分组
export const mockStringGroup: MockParamItem[] = [
  {
    label: 'ms.paramsInput.character',
    value: '@character(pool)',
    desc: 'ms.paramsInput.characterDesc',
    inputGroup: [
      {
        type: 'input',
        value: '',
        label: 'ms.paramsInput.character',
        placeholder: 'ms.paramsInput.commonPlaceholder',
      },
    ],
  },
  {
    label: 'ms.paramsInput.characterLower',
    value: "@character('lower')",
    desc: 'ms.paramsInput.characterLowerDesc',
  },
  {
    label: 'ms.paramsInput.characterUpper',
    value: "@character('upper')",
    desc: 'ms.paramsInput.characterUpperDesc',
  },
  {
    label: 'ms.paramsInput.characterSymbol',
    value: "@character('symbol')",
    desc: 'ms.paramsInput.characterSymbolDesc',
  },
  {
    label: 'ms.paramsInput.string',
    value: '@string(1,10)',
    desc: 'ms.paramsInput.stringDesc',
    inputGroup: [
      {
        type: 'number',
        value: 1,
        label: 'ms.paramsInput.stringMin',
        placeholder: 'ms.paramsInput.commonPlaceholder',
      },
      {
        type: 'number',
        value: 10,
        label: 'ms.paramsInput.stringMax',
        placeholder: 'ms.paramsInput.commonPlaceholder',
      },
    ],
  },
];
// mock日期分组
export const mockDateGroup: MockParamItem[] = [
  {
    label: 'ms.paramsInput.date',
    value: "@date('yyyy-MM-dd')",
    desc: 'ms.paramsInput.dateDesc',
  },
  {
    label: 'ms.paramsInput.time',
    value: "@time('HH:mm:ss')",
    desc: 'ms.paramsInput.timeDesc',
  },
  {
    label: 'ms.paramsInput.dateTime',
    value: "@dateTime('yyyy-MM-dd HH:mm:ss')",
    desc: 'ms.paramsInput.dateTimeDesc',
  },
  {
    label: 'ms.paramsInput.nowDateTime',
    value: "@now('yyyy-MM-dd HH:mm:ss')",
    desc: 'ms.paramsInput.nowDateTimeDesc',
  },
];
// mockWeb变量分组
export const mockWebMap: MockParamItem[] = [
  {
    label: 'ms.paramsInput.url',
    value: "@url('http')",
    desc: 'ms.paramsInput.urlDesc',
    inputGroup: [
      {
        type: 'inputAppendSelect',
        value: '',
        label: 'ms.paramsInput.url',
        inputValue: '',
        selectValue: 'http',
        placeholder: 'ms.paramsInput.urlPlaceholder',
        options: [
          {
            label: 'http',
            value: 'http',
          },
          {
            label: 'https',
            value: 'https',
          },
        ],
      },
    ],
  },
  {
    label: 'ms.paramsInput.protocol',
    value: '@protocol',
    desc: 'ms.paramsInput.protocolDesc',
  },
  {
    label: 'ms.paramsInput.domain',
    value: '@domain',
    desc: 'ms.paramsInput.domainDesc',
  },
  {
    label: 'ms.paramsInput.topDomain',
    value: '@tld',
    desc: 'ms.paramsInput.topDomainDesc',
  },
  {
    label: 'ms.paramsInput.email',
    value: '@email',
    desc: 'ms.paramsInput.emailDesc',
  },
  {
    label: 'ms.paramsInput.ip',
    value: '@ip',
    desc: 'ms.paramsInput.ipDesc',
  },
];
// mock地区分组
export const mockLocationMap: MockParamItem[] = [
  {
    label: 'ms.paramsInput.location',
    value: '@region',
    desc: 'ms.paramsInput.locationDesc',
  },
  {
    label: 'ms.paramsInput.province',
    value: '@province',
    desc: 'ms.paramsInput.provinceDesc',
  },
  {
    label: 'ms.paramsInput.city',
    value: '@city',
    desc: 'ms.paramsInput.cityDesc',
  },
  {
    label: 'ms.paramsInput.county',
    value: '@county',
    desc: 'ms.paramsInput.countyDesc',
  },
  {
    label: 'ms.paramsInput.provinceCityCounty',
    value: '@county(true)',
    desc: 'ms.paramsInput.provinceCityCountyDesc',
  },
  {
    label: 'ms.paramsInput.zip',
    value: '@zip',
    desc: 'ms.paramsInput.zipDesc',
  },
];
// mock个人信息分组
export const mockPersonalMap: MockParamItem[] = [
  {
    label: 'ms.paramsInput.idCard',
    value: '@idCard',
    desc: 'ms.paramsInput.idCardDesc',
  },
  {
    label: 'ms.paramsInput.specifyIdCard',
    value: '@idCard(birth)',
    desc: 'ms.paramsInput.specifyIdCardDesc',
    inputGroup: [
      {
        type: 'date',
        value: '',
        label: 'ms.paramsInput.birthday',
        placeholder: 'ms.paramsInput.birthdayPlaceholder',
      },
    ],
  },
  {
    label: 'ms.paramsInput.phone',
    value: '@phoneNumber',
    desc: 'ms.paramsInput.phoneDesc',
  },
];
// mock英文名分组
export const mockEnglishNameGroupMap: MockParamItem[] = [
  {
    label: 'ms.paramsInput.englishName',
    value: '@first',
    desc: 'ms.paramsInput.englishNameDesc',
  },
  {
    label: 'ms.paramsInput.englishSurname',
    value: '@last',
    desc: 'ms.paramsInput.englishSurnameDesc',
  },
  {
    label: 'ms.paramsInput.englishFullName',
    value: '@name',
    desc: 'ms.paramsInput.englishFullNameDesc',
  },
];
// mock中文名分组
export const mockChineseNameGroupMap: MockParamItem[] = [
  {
    label: 'ms.paramsInput.chineseName',
    value: '@cfirst',
    desc: 'ms.paramsInput.chineseNameDesc',
  },
  {
    label: 'ms.paramsInput.chineseSurname',
    value: '@clast',
    desc: 'ms.paramsInput.chineseSurnameDesc',
  },
  {
    label: 'ms.paramsInput.chineseFullName',
    value: '@cname',
    desc: 'ms.paramsInput.chineseFullNameDesc',
  },
];
// mock颜色分组
export const mockColorGroupMap: MockParamItem[] = [
  {
    label: 'ms.paramsInput.color',
    value: '@color',
    desc: 'ms.paramsInput.colorDesc',
  },
  {
    label: 'RGB',
    value: '@rgb',
    desc: 'ms.paramsInput.RGBDesc',
  },
  {
    label: 'RGBA',
    value: '@rgba',
    desc: 'ms.paramsInput.RGBADesc',
  },
  {
    label: 'HSL',
    value: '@hsl',
    desc: 'ms.paramsInput.hslDesc',
  },
];
// mock英文文本分组
export const mockEnglishTextGroupMap: MockParamItem[] = [
  {
    label: 'ms.paramsInput.englishText',
    value: '@paragraph',
    desc: 'ms.paramsInput.englishTextDesc',
  },
  {
    label: 'ms.paramsInput.englishSentence',
    value: '@sentence',
    desc: 'ms.paramsInput.englishSentenceDesc',
  },
  {
    label: 'ms.paramsInput.englishWord',
    value: '@word',
    desc: 'ms.paramsInput.englishWordDesc',
  },
  {
    label: 'ms.paramsInput.englishTitle',
    value: '@title',
    desc: 'ms.paramsInput.englishTitleDesc',
  },
];
// mock中文文本分组
export const mockChineseTextGroupMap: MockParamItem[] = [
  {
    label: 'ms.paramsInput.chineseText',
    value: '@cparagraph',
    desc: 'ms.paramsInput.chineseTextDesc',
  },
  {
    label: 'ms.paramsInput.chineseSentence',
    value: '@csentence',
    desc: 'ms.paramsInput.chineseSentenceDesc',
  },
  {
    label: 'ms.paramsInput.chineseWord',
    value: '@cword',
    desc: 'ms.paramsInput.chineseWordDesc',
  },
  {
    label: 'ms.paramsInput.chineseTitle',
    value: '@ctitle',
    desc: 'ms.paramsInput.chineseTitleDesc',
  },
];
// mock正则表达式分组
export const mockRegExpMap: MockParamItem[] = [
  {
    label: 'ms.paramsInput.regexp',
    value: '@regexp',
    desc: 'ms.paramsInput.regexpDesc',
    inputGroup: [
      {
        type: 'input',
        value: '',
        label: 'ms.paramsInput.regexp',
        placeholder: 'ms.paramsInput.commonPlaceholder',
      },
    ],
  },
];
// mock函数
export const mockFunctions: MockParamItem[] = [
  {
    label: 'md5',
    value: 'md5',
    desc: 'ms.paramsInput.md5Desc',
  },
  {
    label: 'base64',
    value: 'base64',
    desc: 'ms.paramsInput.base64Desc',
  },
  {
    label: 'unbase64',
    value: 'unbase64',
    desc: 'ms.paramsInput.unbase64Desc',
  },
  {
    label: 'substr',
    value: 'substr',
    desc: 'ms.paramsInput.substrDesc',
    inputGroup: [
      {
        type: 'number',
        value: undefined,
        label: 'start',
        placeholder: 'ms.paramsInput.substrStartPlaceholder',
      },
      {
        type: 'number',
        value: undefined,
        label: 'end',
        placeholder: 'ms.paramsInput.substrEndPlaceholder',
      },
    ],
  },
  {
    label: 'concat',
    value: 'concat',
    desc: 'ms.paramsInput.concatDesc',
    inputGroup: [
      {
        type: 'input',
        value: '',
        label: 'ms.paramsInput.concat',
        placeholder: 'ms.paramsInput.commonPlaceholder',
      },
    ],
  },
  {
    label: 'lconcat',
    value: 'lconcat',
    desc: 'ms.paramsInput.lconcatDesc',
    inputGroup: [
      {
        type: 'input',
        value: '',
        label: 'ms.paramsInput.lconcat',
        placeholder: 'ms.paramsInput.commonPlaceholder',
      },
    ],
  },
  {
    label: 'sha1',
    value: 'sha1',
    desc: 'ms.paramsInput.sha1Desc',
  },
  {
    label: 'sha224',
    value: 'sha224',
    desc: 'ms.paramsInput.sha224Desc',
  },
  {
    label: 'sha256',
    value: 'sha256',
    desc: 'ms.paramsInput.sha256Desc',
  },
  {
    label: 'sha384',
    value: 'sha384',
    desc: 'ms.paramsInput.sha384Desc',
  },
  {
    label: 'sha512',
    value: 'sha512',
    desc: 'ms.paramsInput.sha512Desc',
  },
  {
    label: 'lower',
    value: 'lower',
    desc: 'ms.paramsInput.lowerDesc',
  },
  {
    label: 'upper',
    value: 'upper',
    desc: 'ms.paramsInput.upperDesc',
  },
  {
    label: 'length',
    value: 'length',
    desc: 'ms.paramsInput.lengthDesc',
  },
  {
    label: 'number',
    value: 'number',
    desc: 'ms.paramsInput.numberDesc',
  },
];
// mock所有变量
export const mockAllParams: MockParamItem[] = [
  ...mockBaseGroup,
  ...mockNumberGroup,
  ...mockStringGroup,
  ...mockDateGroup,
  ...mockWebMap,
  ...mockLocationMap,
  ...mockPersonalMap,
  ...mockEnglishNameGroupMap,
  ...mockChineseNameGroupMap,
  ...mockColorGroupMap,
  ...mockEnglishTextGroupMap,
  ...mockChineseTextGroupMap,
  ...mockRegExpMap,
];
// mock所有分组
export const mockAllGroup = [
  {
    value: 'base',
    label: 'ms.paramsInput.base',
    children: mockBaseGroup,
  },
  {
    value: 'string',
    label: 'ms.paramsInput.string',
    children: mockStringGroup,
  },
  {
    value: 'personal',
    label: 'ms.paramsInput.personal',
    children: mockPersonalMap,
  },
  {
    value: 'dateTime',
    label: 'ms.paramsInput.dateOrTime',
    children: mockDateGroup,
  },
  {
    value: 'cname',
    label: 'ms.paramsInput.chineseFullName',
    children: mockChineseNameGroupMap,
  },
  {
    value: 'ename',
    label: 'ms.paramsInput.englishFullName',
    children: mockEnglishNameGroupMap,
  },
  {
    value: 'ctext',
    label: 'ms.paramsInput.cText',
    children: mockChineseTextGroupMap,
  },
  {
    value: 'etext',
    label: 'ms.paramsInput.eText',
    children: mockEnglishTextGroupMap,
  },
  {
    value: 'web',
    label: 'ms.paramsInput.web',
    children: mockWebMap,
  },
  {
    value: 'number',
    label: 'ms.paramsInput.number',
    children: mockNumberGroup,
  },
  {
    value: 'location',
    label: 'ms.paramsInput.county',
    children: mockLocationMap,
  },
  {
    value: 'color',
    label: 'ms.paramsInput.color',
    children: mockColorGroupMap,
  },
  {
    value: 'regexp',
    label: 'ms.paramsInput.regexp',
    children: mockRegExpMap,
  },
];

// JMeter变量分组
export const JMeterVariableGroup = [
  {
    label: 'ms.paramsInput.randomFromMultipleVars',
    value: '${__RandomFromMultipleVars}',
  },
  {
    label: 'ms.paramsInput.split',
    value: '${__split}',
  },
  {
    label: 'ms.paramsInput.eval',
    value: '${__eval}',
  },
  {
    label: 'ms.paramsInput.evalVar',
    value: '${__evalVar}',
  },
  {
    label: 'ms.paramsInput.V',
    value: '${__V}',
  },
];
// JMeter编码分组
export const JMeterCodeGroup = [
  {
    label: 'ms.paramsInput.escapeHtml',
    value: '${__escapeHtml}',
  },
  {
    label: 'ms.paramsInput.escapeXml',
    value: '${__escapeXml}',
  },
  {
    label: 'ms.paramsInput.unescape',
    value: '${__unescape}',
  },
  {
    label: 'ms.paramsInput.unescapeHtml',
    value: '${__unescapeHtml}',
  },
  {
    label: 'ms.paramsInput.urldecode',
    value: '${__urldecode}',
  },
  {
    label: 'ms.paramsInput.urlencode',
    value: '${__urlencode}',
  },
];
// JMeter脚本分组
export const JMeterScriptGroup = [
  {
    label: 'ms.paramsInput.groovy',
    value: '${__groovy}',
  },
  {
    label: 'ms.paramsInput.BeanShell',
    value: '${__BeanShell}',
  },
  {
    label: 'ms.paramsInput.javaScript',
    value: '${__javaScript}',
  },
  {
    label: 'ms.paramsInput.jexl2',
    value: '${__jexl2}',
  },
  {
    label: 'ms.paramsInput.jexl3',
    value: '${__jexl3}',
  },
];
// JMeter时间分组
export const JMeterTimeGroup = [
  {
    label: 'ms.paramsInput.jmeterTime',
    value: '${__time}',
  },
  {
    label: 'ms.paramsInput.timeShift',
    value: '${__timeShift}',
  },
  {
    label: 'ms.paramsInput.dateTimeConvert',
    value: '${__dateTimeConvert}',
  },
  {
    label: 'ms.paramsInput.RandomDate',
    value: '${__RandomDate}',
  },
];
// JMeter属性分组
export const JMeterPropertyGroup = [
  {
    label: 'ms.paramsInput.isPropDefined',
    value: '${__isPropDefined}',
  },
  {
    label: 'ms.paramsInput.readProperty',
    value: '${__property}',
  },
  {
    label: 'ms.paramsInput.P',
    value: '${__P}',
  },
  {
    label: 'ms.paramsInput.setProperty',
    value: '${__setProperty}',
  },
  {
    label: 'ms.paramsInput.isVarDefined',
    value: '${__isVarDefined}',
  },
];
// JMeter数字分组
export const JMeterNumberGroup = [
  {
    label: 'ms.paramsInput.counter',
    value: '${__counter}',
  },
  {
    label: 'ms.paramsInput.intSum',
    value: '${__intSum}',
  },
  {
    label: 'ms.paramsInput.longSum',
    value: '${__longSum}',
  },
  {
    label: 'ms.paramsInput.Random',
    value: '${__Random}',
  },
];
// JMeter文件分组
export const JMeterFileGroup = [
  {
    label: 'ms.paramsInput.StringFromFile',
    value: '${__StringFromFile}',
  },
  {
    label: 'ms.paramsInput.FileToString',
    value: '${__FileToString}',
  },
  {
    label: 'ms.paramsInput.CSVRead',
    value: '${__CSVRead}',
  },
  {
    label: 'ms.paramsInput.XPath',
    value: '${__XPath}',
  },
  {
    label: 'ms.paramsInput.StringToFile',
    value: '${__StringToFile}',
  },
];
// JMeter信息分组
export const JMeterInfoGroup = [
  {
    label: 'ms.paramsInput.digest',
    value: '${__digest}',
  },
  {
    label: 'ms.paramsInput.threadNum',
    value: '${__threadNum}',
  },
  {
    label: 'ms.paramsInput.threadGroupName',
    value: '${__threadGroupName}',
  },
  {
    label: 'ms.paramsInput.samplerName',
    value: '${__samplerName}',
  },
  {
    label: 'ms.paramsInput.machineIP',
    value: '${__machineIP}',
  },
  {
    label: 'ms.paramsInput.machineName',
    value: '${__machineName}',
  },
  {
    label: 'ms.paramsInput.TestPlanName',
    value: '${__TestPlanName}',
  },
];
// JMeter字符串分组
export const JMeterStringGroup = [
  {
    label: 'ms.paramsInput.log',
    value: '${__log}',
  },
  {
    label: 'ms.paramsInput.logn',
    value: '${__logn}',
  },
  {
    label: 'ms.paramsInput.RandomString',
    value: '${__RandomString}',
  },
  {
    label: 'ms.paramsInput.UUID',
    value: '${__UUID}',
  },
  {
    label: 'ms.paramsInput.char',
    value: '${__char}',
  },
  {
    label: 'ms.paramsInput.changeCase',
    value: '${__changeCase}',
  },
  {
    label: 'ms.paramsInput.regexFunction',
    value: '${__regexFunction}',
  },
];
// JMeter所有分组
export const JMeterAllGroup = [
  {
    value: 'variable',
    label: 'ms.paramsInput.variable',
    children: JMeterVariableGroup,
  },
  {
    value: 'code',
    label: 'ms.paramsInput.code',
    children: JMeterCodeGroup,
  },
  {
    value: 'script',
    label: 'ms.paramsInput.script',
    children: JMeterScriptGroup,
  },
  {
    value: 'time',
    label: 'ms.paramsInput.time',
    children: JMeterTimeGroup,
  },
  {
    value: 'property',
    label: 'ms.paramsInput.property',
    children: JMeterPropertyGroup,
  },
  {
    value: 'number',
    label: 'ms.paramsInput.number',
    children: JMeterNumberGroup,
  },
  // {
  //   value: 'file',
  //   label: 'ms.paramsInput.file',
  //   children: JMeterFileGroup,
  // },
  {
    value: 'info',
    label: 'ms.paramsInput.info',
    children: JMeterInfoGroup,
  },
  {
    value: 'string',
    label: 'ms.paramsInput.string',
    children: JMeterStringGroup,
  },
];
// JMeter所有变量
export const JMeterAllVars = [
  ...JMeterVariableGroup,
  ...JMeterCodeGroup,
  ...JMeterScriptGroup,
  ...JMeterTimeGroup,
  ...JMeterPropertyGroup,
  ...JMeterNumberGroup,
  // ...JMeterFileGroup,
  ...JMeterInfoGroup,
  ...JMeterStringGroup,
];
// 同名函数但参数不同，需要特殊处理
export const sameFuncNameVars = [
  '@county(true)',
  '@character(pool)',
  "@character('lower')",
  "@character('upper')",
  "@character('symbol')",
  '@idCard(birth)',
  '@natural(1,100)',
  '@integer(1,100)',
];
// 带形参的函数集合，指的是函数入参为形参，如果用户未填写实参则不需要填充到入参框中
export const formalParameterVars = ['@character(pool)', '@idCard(birth)', '@natural(1,100)', '@integer(1,100)'];
