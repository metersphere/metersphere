// import MSR from '@/api/http/index';

export const getCaseList = () => {
  // return MSR.post<CommonList<FileItem>>({ url: FilePageUrl, data });
  return Promise.resolve({
    list: [
      {
        id: 'ded3d43',
        name: '测试评审1',
        creator: '张三',
        reviewer: '李四',
        module: '模块1',
        status: 0, // 未开始、进行中、已完成、已归档
        result: 0, // 通过、不通过、评审中
        caseCount: 100,
        passCount: 0,
        failCount: 10,
        reviewCount: 20,
        reviewingCount: 25,
        tags: ['标签1', '标签2'],
        type: 'single',
        desc: 'douifd9304',
        cycle: [1700200794229, 1700200994229],
      },
      {
        id: 'g545hj4',
        name: '测试评审2',
        creator: '张三',
        reviewer: '李四',
        module: '模块1',
        status: 1, // 未开始、进行中、已完成、已归档
        result: 1, // 通过、不通过、评审中
        caseCount: 105,
        passCount: 50,
        failCount: 10,
        reviewCount: 20,
        reviewingCount: 25,
        tags: ['标签1', '标签2'],
        type: 'single',
        desc: 'douifd9304',
        cycle: [1700200794229, 1700200994229],
      },
      {
        id: 'hj65b54',
        name: '测试评审3',
        creator: '张三',
        reviewer: '李四',
        module: '模块1',
        status: 2, // 未开始、进行中、已完成、已归档
        result: 2, // 通过、不通过、评审中
        caseCount: 125,
        passCount: 70,
        failCount: 10,
        reviewCount: 20,
        reviewingCount: 25,
        passRate: '80%',
        tags: ['标签1', '标签2'],
        type: 'single',
        desc: 'douifd9304',
        cycle: [1700200794229, 1700200994229],
      },
      {
        id: 'wefwefw',
        name: '测试评审4',
        creator: '张三',
        reviewer: '李四',
        module: '模块1',
        status: 3, // 未开始、进行中、已完成、已归档
        result: 3, // 通过、不通过、评审中
        caseCount: 130,
        passCount: 70,
        failCount: 10,
        reviewCount: 0,
        reviewingCount: 50,
        passRate: '80%',
        tags: ['标签1', '标签2'],
        type: 'single',
        desc: 'douifd9304',
        cycle: [1700200794229, 1700200994229],
      },
      {
        id: 'g4ggtrgrtg',
        name: '测试评审5',
        creator: '张三',
        reviewer: '李四',
        module: '模块1',
        status: 3, // 未开始、进行中、已完成、已归档
        result: 4, // 通过、不通过、评审中
        caseCount: 130,
        passCount: 70,
        failCount: 10,
        reviewCount: 0,
        reviewingCount: 50,
        passRate: '80%',
        tags: ['标签1', '标签2'],
        type: 'single',
        desc: 'douifd9304',
        cycle: [1700200794229, 1700200994229],
      },
    ],
    current: 1,
    pageSize: 10,
    total: 2,
  });
};

export const getCaseDetail = () => {
  // return MSR.post<CommonList<FileItem>>({ url: FilePageUrl, data });
  return new Promise((resolve) => {});
};
