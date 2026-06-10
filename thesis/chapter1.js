const fs = require('fs');
const {
  Document, Packer, Paragraph, TextRun, Header, Footer,
  AlignmentType, HeadingLevel, PageNumber, TableOfContents,
} = require('docx');

// ============================================================
// 论文信息
// ============================================================
const TITLE = '心理咨询管理系统';
const AUTHOR = '陈豪威';
const STUDENT_ID = '22200135120';
const ADVISOR = '华泽';
const ADVISOR_TITLE = '副教授';
const DEPARTMENT = '电子与信息工程学院';
const MAJOR = '计算机科学与技术';
const YEAR = '2022级';

// ============================================================
// 格式化工具
// ============================================================
const FONT_SONG = '宋体';
const FONT_HEI = '黑体';
const FONT_EN = 'Times New Roman';
const BODY_SIZE = 24; // 小4号 = 12pt
const HEADING1_SIZE = 32;
const HEADING2_SIZE = 28;
const HEADING3_SIZE = 24;
const LINE_SPACING = 360; // 1.5倍行距

function bodyPara(text, options = {}) {
  const { indent = true, bold = false, alignment = AlignmentType.JUSTIFIED } = options;
  const runs = [];
  const parts = splitMixed(text);
  for (const part of parts) {
    const isChinese = /[一-鿿　-〿＀-￯]/.test(part);
    runs.push(new TextRun({
      text: part,
      font: isChinese ? FONT_SONG : FONT_EN,
      size: BODY_SIZE,
      bold,
    }));
  }
  return new Paragraph({
    alignment,
    spacing: { line: LINE_SPACING },
    indent: indent ? { firstLine: 480 } : undefined,
    children: runs,
  });
}

function heading1(text) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_1,
    spacing: { before: 240, after: 120, line: LINE_SPACING },
    alignment: AlignmentType.CENTER,
    children: [new TextRun({ text, font: FONT_HEI, size: HEADING1_SIZE, bold: true })],
  });
}

function heading2(text) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_2,
    spacing: { before: 180, after: 120, line: LINE_SPACING },
    children: [new TextRun({ text, font: FONT_HEI, size: HEADING2_SIZE, bold: true })],
  });
}

function heading3(text) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_3,
    spacing: { before: 120, after: 60, line: LINE_SPACING },
    children: [new TextRun({ text, font: FONT_HEI, size: HEADING3_SIZE, bold: true })],
  });
}

function splitMixed(text) {
  const result = [];
  let current = '';
  let currentIsChinese = null;
  for (const ch of text) {
    const isChinese = /[一-鿿　-〿＀-￯]/.test(ch);
    if (currentIsChinese === null) {
      currentIsChinese = isChinese;
      current = ch;
    } else if (isChinese === currentIsChinese) {
      current += ch;
    } else {
      result.push(current);
      current = ch;
      currentIsChinese = isChinese;
    }
  }
  if (current) result.push(current);
  return result;
}

function emptyLine(count = 1) {
  const paras = [];
  for (let i = 0; i < count; i++) {
    paras.push(new Paragraph({ spacing: { line: LINE_SPACING }, children: [new TextRun({ text: '', size: BODY_SIZE })] }));
  }
  return paras;
}

// ============================================================
// 页面设置 (A4)
// ============================================================
const sectionProps = {
  page: {
    size: { width: 11906, height: 16838 },
    margin: { top: 1440, right: 1440, bottom: 1440, left: 1800 },
  },
};

// ============================================================
// 封面
// ============================================================
function buildCover() {
  const children = [];
  children.push(new Paragraph({
    alignment: AlignmentType.LEFT,
    spacing: { line: 360 },
    children: [new TextRun({ text: `学号：${STUDENT_ID}`, font: FONT_SONG, size: BODY_SIZE })],
  }));
  children.push(new Paragraph({
    alignment: AlignmentType.LEFT,
    spacing: { line: 360 },
    children: [new TextRun({ text: '成绩：', font: FONT_SONG, size: BODY_SIZE })],
  }));
  children.push(...emptyLine(3));
  children.push(new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { line: 480 },
    children: [new TextRun({ text: '苏州科技大学', font: FONT_HEI, size: 52, bold: true })],
  }));
  children.push(...emptyLine(2));
  children.push(new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { line: 480 },
    children: [new TextRun({ text: '毕业设计（论文）', font: FONT_HEI, size: 44, bold: true })],
  }));
  children.push(...emptyLine(2));
  children.push(new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { line: 480 },
    children: [new TextRun({ text: `题  目　${TITLE}`, font: FONT_HEI, size: 36, bold: true })],
  }));
  children.push(...emptyLine(1));
  children.push(new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { line: 360 },
    children: [new TextRun({ text: '性  质：毕业设计□       毕业论文□', font: FONT_SONG, size: BODY_SIZE })],
  }));
  children.push(...emptyLine(3));
  const infoLines = [
    `院    系：${DEPARTMENT}`,
    `专    业：${MAJOR}`,
    `年    级：${YEAR}`,
    `姓    名：${AUTHOR}`,
    `指导教师：${ADVISOR}（${ADVISOR_TITLE}）`,
    `二〇二六年五月`,
  ];
  for (const line of infoLines) {
    children.push(new Paragraph({
      alignment: AlignmentType.CENTER,
      spacing: { line: 400 },
      children: [new TextRun({ text: line, font: FONT_SONG, size: BODY_SIZE })],
    }));
  }
  return { children };
}

// ============================================================
// 第1章 绪论（精简版，4-5页）
// ============================================================
function buildChapter1() {
  const children = [];

  children.push(heading1('第1章　绪论'));

  // ===== 1.1 研究背景与意义 =====
  children.push(heading2('1.1　研究背景与意义'));

  children.push(bodyPara(
    '随着我国经济社会的快速发展和生活节奏的持续加快，心理健康问题日益成为影响公众生活质量和社会和谐的重要因素。国务院发布的《"健康中国2030"规划纲要》明确提出要加强心理健康服务体系建设和规范化管理，教育部等部门联合印发的《全面加强和改进新时代学生心理健康工作专项行动计划（2023—2025年）》也要求高校构建完善的心理健康教育与咨询服务体系。在这一政策背景下，利用信息技术手段提升心理健康服务的可及性和便捷性，已成为心理健康领域的重要发展方向。'
  ));

  children.push(bodyPara(
    '传统心理咨询服务以线下面对面咨询为主要形式，受限于咨询师资源分布不均、服务时间固定、场地依赖性强等因素，大量具有心理困扰的人群未能获得及时有效的专业帮助。尤其在高校场景中，学生群体面临学业压力、就业焦虑、人际关系等多重挑战，心理服务需求旺盛，但高校心理咨询中心的服务接待能力有限，供需矛盾突出。移动互联网的普及为突破这一困境提供了新的思路：微信作为国内普及率最高的移动应用，其小程序生态具备无需下载安装、触手可及的技术特性，能够有效降低用户获取服务的门槛。将心理咨询服务与微信小程序相结合，可以使心理健康服务更加便捷地触达有需求的人群。'
  ));

  children.push(bodyPara(
    '与此同时，心理咨询行业的运营管理同样面临挑战。咨询师需要高效管理预约信息、撰写咨询记录和与用户保持沟通；机构管理者需要对咨询师排班、用户数据和运营状况进行统一的调度和分析。然而，目前多数小型心理咨询机构仍依赖人工方式进行日常管理，效率较低且容易出错。因此，构建一套覆盖用户、咨询师和管理者三端的在线心理咨询管理系统，对于提升心理健康服务的整体运营效率具有重要的现实意义。'
  ));

  children.push(bodyPara(
    '综上所述，本课题旨在利用当前成熟的软件开发技术，设计并实现一套基于微信小程序的心理咨询管理系统，为用户提供在线预约、实时沟通等便捷服务，为咨询师提供高效的日常工作支持工具，为管理者提供数据化的运营管理平台，从而推动心理咨询服务的数字化和规范化。'
  ));

  // ===== 1.2 国内外研究现状 =====
  children.push(heading2('1.2　国内外研究现状'));

  // 1.2.1 心理咨询系统
  children.push(heading3('1.2.1　心理咨询系统研究现状'));

  children.push(bodyPara(
    '心理健康领域的数字化服务始于在线心理测评和健康信息网站，随着互联网技术的演进逐步发展为集预约、咨询、管理于一体的综合性在线平台。在学术研究层面，牛浩然设计并实现了一款面向心理领域的测评咨询系统，采用Java EE框架整合了在线测评和预约咨询功能，验证了在线心理咨询系统的可行性和实用性[1]。罗楷文在心理测评系统中引入了表情识别和语音识别功能，实现了多模态的心理状态辅助评估，拓展了心理测评系统的功能边界[2]。张伟明开发了一套大学生心理健康测试及分析系统，实现了在线测评、数据分析和报告生成等功能，证明了信息化手段在心理健康评估中的实际应用价值[3]。这些工作从功能架构和技术方案等维度探索了心理咨询系统的建设方向，为本文的系统设计提供了有价值的参考。'
  ));

  // 1.2.2 微信小程序在医疗健康领域
  children.push(heading3('1.2.2　微信小程序在医疗健康领域的应用'));

  children.push(bodyPara(
    '微信小程序自推出以来，凭借轻量化、易传播、无需安装等技术特点，在医疗健康领域得到了广泛关注和应用。张铭栖以老年肺癌手术患者为研究对象，构建了一款出院准备辅助微信小程序，通过知识推送、症状监测和在线咨询等功能模块，显著提升了患者的出院准备度和自我管理能力[4]。吴衍娴基于"护联体"模式，为结直肠癌造口患者开发了自我管理微信小程序，整合了健康档案管理、护理指导和在线随访功能，为延续性护理提供了便捷的技术手段[5]。这些研究充分验证了微信小程序作为医疗服务载体的技术可行性和用户接受度，为本系统选择微信小程序作为用户端和咨询师端的载体提供了实践依据。'
  ));

  children.push(bodyPara(
    '从技术层面来看，微信小程序提供了成熟的组件体系和API接口，支持网络通信、本地存储、实时通信等功能，能够满足心理咨询系统对多媒体交互的需求。同时，小程序依托微信生态的用户体系，可以简化用户注册登录流程，降低使用门槛。在整体技术架构方面，小程序前端通常与RESTful API后端配合使用，通过HTTP请求进行数据交互，这种架构模式已经在多个医疗健康项目中得到成功验证[4][5]，是当前较为成熟且广泛采用的技术方案。'
  ));

  // 1.2.3 现状总结
  children.push(heading3('1.2.3　现有研究的总结'));

  children.push(bodyPara(
    '综合来看，当前国内外在心理咨询系统和医疗健康类微信小程序方面的研究已取得显著进展，但仍存在以下不足：其一，多数研究专注于单一用户角色（如患者端）的功能实现，缺乏面向心理咨询完整业务流程的多角色协同系统设计；其二，现有心理咨询平台多聚焦于用户端功能，对咨询师的工作支持和管理端的运营管理关注不够；其三，部分系统的功能模块之间缺乏有效的数据联动，影响了整体服务效率。本课题正是在上述研究基础上，针对现有不足，设计并实现一套功能完善、三端协同的在线心理咨询管理系统。'
  ));

  // ===== 1.3 本文研究内容 =====
  children.push(heading2('1.3　本文研究内容'));

  children.push(bodyPara(
    '本文围绕心理咨询管理系统的设计与实现，面向用户、咨询师和管理者三类角色，构建涵盖在线预约、实时沟通、咨询记录管理等核心业务的全流程服务平台。具体研究内容包括以下四个方面：'
  ));

  children.push(bodyPara(
    '（1）三端协同的系统架构设计。针对心理咨询服务的完整业务流程，分析用户端、咨询师端和管理端的功能需求，设计覆盖预约前、咨询中和咨询后各环节的全流程系统架构。用户端提供咨询师浏览、在线预约、实时聊天和心理测评等功能；咨询师端支持预约管理、咨询记录撰写和消息沟通；管理端负责用户管理、排班调度和数据统计分析。通过清晰的角色分离与接口设计，确保三端之间数据一致、流程顺畅。'
  ));

  children.push(bodyPara(
    '（2）基于病情标签的咨询师匹配机制。设计病情标签体系，在用户预约阶段引导其选择心理困扰类型，以结构化标签结合补充描述的方式记录病情信息，并基于标签匹配度、咨询师评分和经验值三个维度为用户推荐适合的咨询师，提升匹配效率和咨询效果。'
  ));

  children.push(bodyPara(
    '（3）实时通信与消息管理。利用WebSocket技术实现咨询师与用户之间的实时消息通信，针对咨询师端设计消息列表页面，按用户维度进行会话分组，支持按病情标签筛选和已读/未读状态过滤，提升咨询师的工作效率。'
  ));

  children.push(bodyPara(
    '（4）数据驱动的运营管理功能。为管理端提供用户管理、排班调度、统计分析等运营支撑功能，其中统计分析模块涵盖预约趋势、病情分布、用户增长等维度，以图表形式呈现系统运营状况，为管理决策提供数据支撑。'
  ));

  // ===== 1.4 本文章节结构 =====
  children.push(heading2('1.4　本文章节结构'));

  children.push(bodyPara('本文共分为七章，各章节内容安排如下：'));

  children.push(bodyPara(
    '第1章绪论。阐述课题的研究背景与社会意义，综述国内外在心理咨询系统、微信小程序医疗应用等方向的研究现状，分析现有研究的不足，明确本文的研究内容和目标，并介绍论文的整体章节安排。'
  ));

  children.push(bodyPara(
    '第2章相关基础知识。介绍系统开发所涉及的核心技术与框架，包括Spring Boot后端框架、MyBatis-Plus数据访问框架、微信小程序开发框架、MySQL数据库、WebSocket实时通信协议以及JWT身份认证机制，为后续章节的系统设计与实现奠定技术基础。'
  ));

  children.push(bodyPara(
    '第3章需求分析。从三类用户角色出发进行需求概览，通过用例图对系统功能进行宏观描述，梳理用户端、咨询师端和管理端的具体功能需求，并对核心功能编写用例分析表进行详细阐述。'
  ));

  children.push(bodyPara(
    '第4章系统设计。围绕系统功能模块划分展开概要设计，绘制系统模块结构图，并对各核心模块通过UML活动图、时序图等工具进行详细设计分析，涵盖数据库设计和接口设计等内容。'
  ));

  children.push(bodyPara(
    '第5章系统实现。对应第4章的模块划分，逐一阐述各核心功能模块的具体实现过程，结合系统运行截图和核心代码展示实现成果，并对关键算法和代码逻辑进行解析。'
  ));

  children.push(bodyPara(
    '第6章系统测试。介绍系统测试环境，选取核心功能模块设计测试用例，记录测试输入、预期输出和实际结果，并对测试结果进行分析与总结。'
  ));

  children.push(bodyPara(
    '第7章总结。对本文工作进行系统性总结，归纳主要完成的工作内容，分析系统当前存在的不足与局限，并对今后的优化方向进行展望。'
  ));

  return children;
}

// ============================================================
// 构建文档
// ============================================================
async function main() {
  const sections = [];

  // 封面
  const cover = buildCover();
  sections.push({
    properties: { ...sectionProps },
    children: cover.children,
  });

  // 第1章 绪论
  sections.push({
    properties: { ...sectionProps },
    headers: {
      default: new Header({
        children: [new Paragraph({
          alignment: AlignmentType.CENTER,
          children: [new TextRun({ text: '苏州科技大学毕业设计（论文）', font: FONT_SONG, size: 20 })],
        })],
      }),
    },
    footers: {
      default: new Footer({
        children: [new Paragraph({
          alignment: AlignmentType.CENTER,
          children: [new TextRun({ text: '第　', font: FONT_SONG, size: 20 }),
                     new TextRun({ children: [PageNumber.CURRENT], font: FONT_EN, size: 20 }),
                     new TextRun({ text: '　页', font: FONT_SONG, size: 20 })],
        })],
      }),
    },
    children: buildChapter1(),
  });

  const doc = new Document({
    styles: {
      default: {
        document: { run: { font: FONT_SONG, size: BODY_SIZE } },
      },
      paragraphStyles: [
        {
          id: 'Heading1', name: 'Heading 1', basedOn: 'Normal', next: 'Normal', quickFormat: true,
          run: { size: HEADING1_SIZE, bold: true, font: FONT_HEI },
          paragraph: { spacing: { before: 240, after: 120, line: LINE_SPACING }, outlineLevel: 0, alignment: AlignmentType.CENTER },
        },
        {
          id: 'Heading2', name: 'Heading 2', basedOn: 'Normal', next: 'Normal', quickFormat: true,
          run: { size: HEADING2_SIZE, bold: true, font: FONT_HEI },
          paragraph: { spacing: { before: 180, after: 120, line: LINE_SPACING }, outlineLevel: 1 },
        },
        {
          id: 'Heading3', name: 'Heading 3', basedOn: 'Normal', next: 'Normal', quickFormat: true,
          run: { size: HEADING3_SIZE, bold: true, font: FONT_HEI },
          paragraph: { spacing: { before: 120, after: 60, line: LINE_SPACING }, outlineLevel: 2 },
        },
      ],
    },
    sections,
  });

  const buffer = await Packer.toBuffer(doc);
  const outDir = 'D:/claude毕业论文';
  if (!fs.existsSync(outDir)) {
    fs.mkdirSync(outDir, { recursive: true });
  }
  fs.writeFileSync(`${outDir}/第1章_绪论.docx`, buffer);
  console.log('Done: 第1章_绪论.docx');
}

main().catch(err => { console.error(err); process.exit(1); });
