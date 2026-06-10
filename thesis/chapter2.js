const fs = require('fs');
const {
  Document, Packer, Paragraph, TextRun, Header, Footer,
  AlignmentType, HeadingLevel, PageNumber,
} = require('docx');

const FONT_SONG = '宋体';
const FONT_HEI = '黑体';
const FONT_EN = 'Times New Roman';
const BODY_SIZE = 24;
const HEADING1_SIZE = 32;
const HEADING2_SIZE = 28;
const HEADING3_SIZE = 24;
const LINE_SPACING = 360;

function bodyPara(text, options = {}) {
  const { indent = true, bold = false, alignment = AlignmentType.JUSTIFIED } = options;
  const runs = [];
  const parts = splitMixed(text);
  for (const part of parts) {
    const isChinese = /[一-鿿　-〿＀-￯]/.test(part);
    runs.push(new TextRun({ text: part, font: isChinese ? FONT_SONG : FONT_EN, size: BODY_SIZE, bold }));
  }
  return new Paragraph({ alignment, spacing: { line: LINE_SPACING }, indent: indent ? { firstLine: 480 } : undefined, children: runs });
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
    if (currentIsChinese === null) { currentIsChinese = isChinese; current = ch; }
    else if (isChinese === currentIsChinese) { current += ch; }
    else { result.push(current); current = ch; currentIsChinese = isChinese; }
  }
  if (current) result.push(current);
  return result;
}

const sectionProps = {
  page: { size: { width: 11906, height: 16838 }, margin: { top: 1440, right: 1440, bottom: 1440, left: 1800 } },
};

// ============================================================
// 第2章 相关基础知识
// ============================================================
function buildChapter2() {
  const children = [];

  children.push(heading1('第2章　相关基础知识'));

  // ===== 2.1 Spring Boot =====
  children.push(heading2('2.1　Spring Boot后端框架'));

  children.push(bodyPara(
    'Spring Boot是基于Spring Framework的Java应用开发框架，通过对Spring生态的深度封装，简化了企业级Java应用的构建与部署过程。本系统选择Spring Boot作为后端框架，主要基于以下两方面考虑。其一，Spring Boot的自动配置机制能够根据项目引入的依赖自动完成框架配置，开发者无须编写繁琐的XML文件即可快速搭建可运行的项目骨架，降低了项目初期的搭建成本。其二，Spring Boot内置了Tomcat、Jetty等Servlet容器，应用可打包为可执行JAR文件直接运行，无需额外部署外部应用服务器，在开发、测试和生产环境中均能保持一致的部署方式。'
  ));

  children.push(bodyPara(
    '在工程实践中，Spring Boot的起步依赖机制简化了组件集成流程。引入spring-boot-starter-web依赖即可获得Spring MVC、嵌入式Tomcat和Jackson序列化等一整套Web开发组件。引入spring-boot-starter-websocket依赖即可启用WebSocket支持，无需手动配置消息代理。MyBatis-Plus的集成同样便捷，添加mybatis-plus-boot-starter依赖并配置数据源后即可使用。这种高度可集成性使开发工作能够聚焦于业务逻辑，而非框架间的配置协调。此外，Spring Boot生态成熟、社区活跃，开发中遇到问题时可快速获得解决方案，降低了项目的技术风险。综合以上因素，Spring Boot在快速开发、易于部署和高可集成性方面较好地满足了本系统的需求。'
  ));

  // ===== 2.2 MyBatis-Plus =====
  children.push(heading2('2.2　MyBatis-Plus数据访问框架'));

  children.push(bodyPara(
    'MyBatis-Plus是MyBatis的增强工具，在保留MyBatis全部功能的基础上，提供了通用CRUD操作、分页查询、条件构造器等便捷特性。本系统选择MyBatis-Plus作为数据访问层框架，主要因为它在保持原生SQL灵活性的同时，大幅减少了重复的数据访问代码。传统MyBatis开发中，开发者需要为每张数据表手动编写Mapper接口和XML映射文件，即使基本的增删改查操作也需逐一配置。MyBatis-Plus通过继承BaseMapper接口，使开发者仅需定义实体类和Mapper接口即可获得二十余种通用数据操作方法，涵盖按条件查询、分页查询和批量插入等常用场景，显著提升了数据访问层的开发效率。'
  ));

  children.push(bodyPara(
    '在实际开发中，MyBatis-Plus的分页插件为管理端各列表页面提供了高效的分页能力。只需在Controller层构造Page对象并调用Mapper的selectPage方法，框架即可自动生成分页查询并返回封装好的结果，包含总记录数、当前页和每页数据等完整分页信息。MyBatis-Plus的LambdaQueryWrapper条件构造器则以Java实体类的getter方法引用替代字符串形式的字段名，在编译期即可发现字段名拼写错误，避免了传统方式中字符串硬编码可能导致的运行时异常。对于复杂查询场景，MyBatis-Plus完全兼容原生MyBatis的XML映射方式，开发者可在BaseMapper之外自行定义Mapper方法和XML语句，兼顾了开发便捷性与灵活性。'
  ));

  // ===== 2.3 MySQL =====
  children.push(heading2('2.3　MySQL关系型数据库'));

  children.push(bodyPara(
    'MySQL是一款开源的关系型数据库管理系统，以性能稳定、部署简便和生态成熟在Web应用开发中得到了广泛应用。本系统选择MySQL作为数据存储方案有多方面的考量。心理咨询管理系统涉及用户、咨询师、预约记录、咨询记录、评价数据等多种结构化数据，这些数据之间存在明确的关系约束，如预约关联用户和咨询师、评价关联预约等。关系型数据库通过外键约束和关联查询能够自然地表达和维护这些数据关系，这是非关系型数据库难以替代的优势。此外，MySQL对ACID事务特性的支持能够保障预约创建、状态变更等关键业务操作的数据一致性。'
  ));

  children.push(bodyPara(
    '在性能方面，MySQL对本系统预期的数据规模（数千用户、数万条预约和聊天记录）完全能够胜任，其索引机制和查询优化器可以有效支撑多条件检索和统计聚合等查询需求。MySQL与Spring Boot和MyBatis-Plus的集成已有成熟实践：Spring Boot提供了自动配置的数据源和连接池管理，MyBatis-Plus封装了高效的SQL生成和结果映射，三者配合形成了稳定可靠的数据持久化方案。本系统的数据库操作以CRUD为主，不涉及海量数据的实时分析，MySQL在功能适配性、开发效率和运维成本之间取得了合适的平衡。'
  ));

  // ===== 2.4 微信小程序 =====
  children.push(heading2('2.4　微信小程序开发框架'));

  children.push(bodyPara(
    '微信小程序是运行于微信客户端内的轻型应用，用户无须通过应用商店下载安装即可直接使用。本系统选择微信小程序作为用户端和咨询师端的前端载体，主要从用户覆盖度和使用场景两方面考虑。微信作为国内覆盖最广的移动应用，选择小程序可以最大程度降低用户获取心理服务的技术门槛，避免了下载独立APP所带来的获客成本。同时，小程序即用即走的产品形态与心理咨询服务场景较为契合——用户在有困扰时能够快速找到入口并获得服务，而不需在手机中长期保留一个低频使用的独立应用。'
  ));

  children.push(bodyPara(
    '从技术角度看，微信小程序提供了原生框架，包含WXML、WXSS和JavaScript逻辑层，同时提供丰富的API接口用于网络请求、本地数据缓存、图片选择和语音录制等功能[4][5]。本系统开发中，利用小程序的双向数据绑定机制进行页面状态管理，通过wx.request接口与后端RESTful API交互，使用wx.setStorageSync实现用户Token的本地持久化。小程序的事件驱动模型使页面交互逻辑的编写较为直观。此外，小程序提供的WebSocket API（wx.connectSocket）使系统可直接在客户端内建立长连接，实现消息的即时收发，无需引入额外的第三方通信库。'
  ));

  // ===== 2.5 WebSocket =====
  children.push(heading2('2.5　WebSocket实时通信协议'));

  children.push(bodyPara(
    'WebSocket是HTML5中定义的一种网络通信协议，用于在客户端与服务器之间建立持久化的双向连接。与HTTP协议的"请求—响应"模式不同，WebSocket连接一旦建立，双方均可随时向对方发送数据，无需等待对方先发起请求。协议在初始握手阶段通过HTTP完成升级，之后数据便可在已建立的TCP连接上进行全双工传输，连接的维持开销远低于反复发起HTTP请求的方式。'
  ));

  children.push(bodyPara(
    '本系统选择WebSocket作为消息推送方案，主要原因是聊天功能需要消息送达的即时性。传统的HTTP轮询方式下，客户端需要每隔数秒主动查询服务端是否有新消息，消息存在固定的轮询间隔延迟，且高频次的轮询请求会消耗不必要的服务器资源和移动端流量。WebSocket连接建立后，服务端在新消息到达时可立即推送至客户端，消除了轮询带来的延迟和资源消耗问题，更适应即时消息场景的需求。'
  ));

  children.push(bodyPara(
    '在本系统的实现中，Spring Boot通过spring-boot-starter-websocket提供了WebSocket的内建支持。后端定义了ChatWebSocketHandler负责处理连接的建立、消息接收转发和连接关闭等生命周期事件。消息发送方面，系统采用了混合策略：消息发送通过HTTP POST接口完成以保证可靠的持久化存储，消息通知通过WebSocket推送以实现即时到达。考虑到心理咨询场景的对话频率通常以分钟为间隔，WebSocket的连接数量完全在服务器承载范围内，当前数据规模下无需引入独立的消息中间件。'
  ));

  // ===== 2.6 JWT =====
  children.push(heading2('2.6　JWT身份认证机制'));

  children.push(bodyPara(
    'JWT（JSON Web Token）是一种基于JSON的轻量级身份认证令牌规范，常用于前后端分离架构中的用户身份验证。本系统选择JWT作为认证方案，主要因为它具备无状态特性：服务器不需要在内存或数据库中保存用户的会话信息，用户在登录成功后获得一个加密签名的Token，后续每次请求在HTTP头中携带该Token即可完成身份验证。这种无状态机制对分布式部署较为友好——任何服务器节点仅凭Token本身即可验证用户身份，无需查询集中的会话存储。'
  ));

  children.push(bodyPara(
    'JWT令牌由三部分组成：头部（Header）、载荷（Payload）和签名（Signature）。头部声明令牌类型和签名算法，载荷携带用户标识、角色类型和过期时间等信息，签名由服务器使用密钥对前两部分进行哈希计算生成，用于防止令牌被篡改。在本系统中，用户登录成功后后端生成JWT令牌返回给客户端，小程序端将Token存储于本地缓存。之后每次发起请求时，前端的请求封装模块自动从缓存中取出Token并附加到HTTP请求的Authorization头中。服务端通过AuthInterceptor拦截器统一校验每个请求携带的Token的有效性和过期状态。这一机制避免了Cookie-Session模式在小程序场景中的不便，在保证认证安全性的同时保持了实现的简洁性。'
  ));

  // ===== 2.7 咨询师推荐算法 =====
  children.push(heading2('2.7　咨询师推荐算法'));

  children.push(bodyPara(
    '当用户在预约页面选择病情标签后，系统需要在多位咨询师中为其推荐最适合的人选。为了实现这一目标，本系统设计了一种基于多因子加权排序的推荐算法，综合考虑病情标签匹配度、咨询师评分和经验值三个维度，计算每位咨询师的综合得分并进行排序。'
  ));

  children.push(bodyPara(
    '算法选择多因子加权而非单一排序指标的理由如下：若仅按评分排序，新入驻的咨询师因评价数量少、评分不稳定而难以获得推荐机会；若仅按匹配度排序，则忽略了咨询师的实际服务质量和经验积累。通过引入三个维度并设置合理的权重配比，能够在匹配精度、服务口碑和经验可信度之间取得平衡。'
  ));

  children.push(bodyPara(
    '具体而言，算法由以下三个因子组成：因子一为病情标签匹配度得分，权重占50%。系统将用户在预约时选择的病情标签与每位咨询师的擅长领域标签进行比对，统计匹配的标签数量占用户所选标签总数的比例，此比例乘以满分为50的系数作为匹配度得分。若用户选择了焦虑、失眠、职场压力三个标签而某位咨询师擅长领域覆盖了其中两个，则其匹配度为2/3×50≈33.3分。这一因子是推荐的核心依据，确保用户的病情与咨询师的专长领域尽可能一致。'
  ));

  children.push(bodyPara(
    '因子二为归一化评分得分，权重占30%。系统从数据库中获取每位咨询师的历史评价平均分，将所有咨询师的评分映射到0到30的区间内：得分等于（咨询师评分/所有咨询师最高评分）×30。归一化处理的目的是消除评分的量纲差异，使得评分因子与匹配度因子在数值上处于同一数量级，避免某一因子因数值过大而在加权求和时不合理地占据主导地位。'
  ));

  children.push(bodyPara(
    '因子三为对数经验值得分，权重占20%。系统统计每位咨询师已完成咨询的总次数作为其经验值，对经验值取自然对数后乘以系数k进行缩放，使其得分范围大致落在0到20的区间。采用对数变换的目的在于：经验值的边际收益是递减的，一位完成了200次咨询的咨询师与一位完成了100次咨询的咨询师在经验可信度上的实际差异并不如数字看起来那么大，对数函数有效地压缩了高经验值区间的差距，避免经验权重过大导致推荐结果过于向资深咨询师倾斜。'
  ));

  children.push(bodyPara(
    '最终，每位咨询师的综合得分为上述三个因子的加权和：综合得分 = 匹配度得分 + 评分得分 + 经验值得分。系统按综合得分降序排列，将排名靠前的咨询师列表返回给用户端展示。该算法的优势在于实现逻辑清晰、计算结果可解释——用户可以看到推荐的咨询师是因为在哪些维度上具有优势，而非一个无法理解的数值。同时，各因子的计算不依赖大规模训练数据和复杂模型，适合在当前数据规模下稳定运行，后续可根据实际运营效果对各因子的权重进行调整优化。'
  ));

  // ===== 2.8 本章小结 =====
  children.push(heading2('2.8　本章小结'));

  children.push(bodyPara(
    '本章围绕心理咨询管理系统开发过程中所涉及的核心技术与理论基础进行了概述。首先介绍了Spring Boot后端框架的自动配置机制和高度可集成性，其次阐述了MyBatis-Plus数据访问框架在简化数据库操作方面的优势，接着说明了MySQL关系型数据库在保障数据一致性和满足关联查询需求方面的适用性。在前端方面，分析了微信小程序作为用户端和咨询师端载体在用户覆盖度和使用便捷性方面的突出优势。随后讨论了WebSocket协议在实现实时消息通信中的作用，以及JWT无状态认证机制在前后端分离架构中的适用性。最后，详细介绍了本系统设计的咨询师推荐算法的三因子加权原理及其设计理由。本章内容为后续章节的需求分析、系统设计与实现提供了必要的技术背景。'
  ));

  return children;
}

async function main() {
  const sections = [
    {
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
      children: buildChapter2(),
    },
  ];

  const doc = new Document({
    styles: {
      default: { document: { run: { font: FONT_SONG, size: BODY_SIZE } } },
      paragraphStyles: [
        { id: 'Heading1', name: 'Heading 1', basedOn: 'Normal', next: 'Normal', quickFormat: true,
          run: { size: HEADING1_SIZE, bold: true, font: FONT_HEI },
          paragraph: { spacing: { before: 240, after: 120, line: LINE_SPACING }, outlineLevel: 0, alignment: AlignmentType.CENTER } },
        { id: 'Heading2', name: 'Heading 2', basedOn: 'Normal', next: 'Normal', quickFormat: true,
          run: { size: HEADING2_SIZE, bold: true, font: FONT_HEI },
          paragraph: { spacing: { before: 180, after: 120, line: LINE_SPACING }, outlineLevel: 1 } },
      ],
    },
    sections,
  });

  const buffer = await Packer.toBuffer(doc);
  const outDir = 'D:/claude毕业论文';
  if (!fs.existsSync(outDir)) { fs.mkdirSync(outDir, { recursive: true }); }
  fs.writeFileSync(`${outDir}/第2章_相关基础知识.docx`, buffer);
  console.log('Done: 第2章_相关基础知识.docx');
}

main().catch(err => { console.error(err); process.exit(1); });
