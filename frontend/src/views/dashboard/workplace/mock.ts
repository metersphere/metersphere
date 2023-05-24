import Mock from 'mockjs';
import qs from 'query-string';
import dayjs from 'dayjs';
import { GetParams } from '#/global';
import setupMock, { successResponseWrap } from '@/utils/setup-mock';

const textList = [
  {
    key: 1,
    clickNumber: '346.3w+',
    title: '经济日报：财政政策要精准提升…',
    increases: 35,
  },
  {
    key: 2,
    clickNumber: '324.2w+',
    title: '双12遇冷，消费者厌倦了电商平…',
    increases: 22,
  },
  {
    key: 3,
    clickNumber: '318.9w+',
    title: '致敬坚守战“疫”一线的社区工作…',
    increases: 9,
  },
  {
    key: 4,
    clickNumber: '257.9w+',
    title: '普高还是职高？家长们陷入选择…',
    increases: 17,
  },
  {
    key: 5,
    clickNumber: '124.2w+',
    title: '人民快评：没想到“浓眉大眼”的…',
    increases: 37,
  },
];
const imageList = [
  {
    key: 1,
    clickNumber: '15.3w+',
    title: '杨涛接替陆慷出任外交部美大司…',
    increases: 15,
  },
  {
    key: 2,
    clickNumber: '12.2w+',
    title: '图集：龙卷风袭击美国多州房屋…',
    increases: 26,
  },
  {
    key: 3,
    clickNumber: '18.9w+',
    title: '52岁大姐贴钱照顾自闭症儿童八…',
    increases: 9,
  },
  {
    key: 4,
    clickNumber: '7.9w+',
    title: '杭州一家三口公园宿营取暖中毒',
    increases: 0,
  },
  {
    key: 5,
    clickNumber: '5.2w+',
    title: '派出所副所长威胁市民？警方调…',
    increases: 4,
  },
];
const videoList = [
  {
    key: 1,
    clickNumber: '367.6w+',
    title: '这是今日10点的南京',
    increases: 5,
  },
  {
    key: 2,
    clickNumber: '352.2w+',
    title: '立陶宛不断挑衅致经济受损民众…',
    increases: 17,
  },
  {
    key: 3,
    clickNumber: '348.9w+',
    title: '韩国艺人刘在石确诊新冠',
    increases: 30,
  },
  {
    key: 4,
    clickNumber: '346.3w+',
    title: '关于北京冬奥会，文在寅表态',
    increases: 12,
  },
  {
    key: 5,
    clickNumber: '271.2w+',
    title: '95后现役军人荣立一等功',
    increases: 2,
  },
];
setupMock({
  setup() {
    Mock.mock(new RegExp('/api/content-data'), () => {
      const presetData = [58, 81, 53, 90, 64, 88, 49, 79];
      const getLineData = () => {
        const count = 8;
        return new Array(count).fill(0).map((el, idx) => ({
          x: dayjs()
            .day(idx - 2)
            .format('YYYY-MM-DD'),
          y: presetData[idx],
        }));
      };
      return successResponseWrap([...getLineData()]);
    });
    Mock.mock(new RegExp('/api/popular/list'), (params: GetParams) => {
      const { type = 'text' } = qs.parseUrl(params.url).query;
      if (type === 'image') {
        return successResponseWrap([...videoList]);
      }
      if (type === 'video') {
        return successResponseWrap([...imageList]);
      }
      return successResponseWrap([...textList]);
    });
  },
});
