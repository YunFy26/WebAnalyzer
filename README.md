# 项目介绍

基于 [Tai-e](https://github.com/pascal-lab/Tai-e) 插件系统开发的 Web 应用分析程序，可以直接用来分析 Web 项目。目前只对 Spring 项目的动态特性做了处理。

- URL 映射：提取 Spring Web 应用中的路由信息，生成URL与其对应的入口方法映射关系
- Bean 提取：自动识别和提取 Spring 应用中的 Bean
- 调用图构造：为每个入口方法生成独立的调用图
    - 处理依赖注入的对象，能够正确处理依赖注入对象的方法调用
    - 尚未处理面向切面编程
- 漏洞检测：基于已识别的调用流，检测潜在的漏洞点，识别漏洞类型
    - 相关代码在 `llm` 分支，在使用大模型进行检测前，请在 `DefaultLLMConnector.java` 中填写 api key，目前仅实现了 GPT 与讯飞星火大模型客户端
    - 使用前请加入运行参数：`-v`
    - 这部分会消耗大量 Token
    - **效果待优化**...更好的处理方式正在开发中...

> 项目尚处于开发阶段

针对 web 多入口生成独立调用图功能目前可用

漏洞检测需要填写自己的 api key（GPT 或讯飞星火大模型）或者根据 `ModelClient.java` 接口实现任意大模型客户端

😀后续会更新文档，解释处理流程

😀[前端](https://github.com/YunFy26/AnalyzerView)界面正在实现...

# 使用说明

1. `options.yml`
   
    ```yml
    optionsFile: null
    printHelp: false
    # 第三方依赖路径，支持输入jar包
    classPath :
        - WebGoat/BOOT-INF/lib
    # 应用程序类路径
    appClassPath: [WebGoat/BOOT-INF/classes]
    mainClass:
    inputClasses: []
    javaVersion: 17
    prependJVM: true
    allowPhantom: true
    worldBuilderClass: pascal.taie.frontend.soot.SootWorldBuilder
    outputDir: output
    preBuildIR: false
    worldCacheMode: false
    # 分析范围：APP，REACHABLE，ALL
    scope: APP
    nativeModel: true
    planFile: null
    analyses:
    #  ir-dumper: ;
      routerAnalysis: ""
      beanAnalysis: ""
      injectPointsAnalysis: ""
      cg: ""
      #  cfg: ""
      #  icfg: ""
      pta: "plugins:[org.example.spring.plugin.ProcessDIPlugin]"
    onlyGenPlan: false
    keepResult:
      - $KEEP-ALL
    ```
    
2. 运行参数
   
    ```bash
    -o="configs/options.yml"
    ```
    
    或者
   
    ```bash
    --options-file="configs/options.yml"
    ```
    
    关于 `options.yml` 中选项的详细信息请阅读：[Tai-e 配置](https://tai-e.pascal-lab.net/docs/current/reference/en/command-line-options.html)

# 输出

- `output/urls.txt`: 路由映射关系
- `output/callFlows/*.dot`: 不同入口的调用图，以入口方法命名

# TODO

- [ ] 处理面向切面编程特性，完善调用图
- [ ] 抽象出入口方法参数类型的对象，完善调用图
- [ ] 优化路由信息提取流程，输出结果
- [ ] 优化 Bean 输出结果
- [ ] 自动添加污点流 Source 点
- [ ] 更改大模型检测漏洞的方案
- [ ] 更改输出效果，考虑增加前端界面

# Reference

参考了

- [https://mp.weixin.qq.com/s/ywoT5J5wSXdCZ-jcfzj7ng](https://mp.weixin.qq.com/s/ywoT5J5wSXdCZ-jcfzj7ng)
- [https://xz.aliyun.com/t/14058?time__1311=GqAxuDRD07G%3DKGNPeeqBKqPWTTrW%3De%3DFa4D](https://xz.aliyun.com/t/14058?time__1311=GqAxuDRD07G%3DKGNPeeqBKqPWTTrW%3De%3DFa4D)

处理了三种依赖注入情况与 `this` 变量，而非仅处理 Field 注入
