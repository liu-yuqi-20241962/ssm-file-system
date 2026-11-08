# 基于 SSM 的资料上传下载系统

《Java 企业级开发（SSM 框架整合）》课堂实训作业 —— 实现「学员登录 → 提交作业文件 → 查看提交记录 → 下载本人文件」的完整闭环。

## 一、技术栈

| 项目 | 版本 | 说明 |
|------|------|------|
| JDK | 17 | 编译与运行 |
| Spring / Spring MVC | 6.1.14 | Jakarta 体系（Servlet 6.0） |
| MyBatis | 3.5.16 | 持久层，XML 映射 |
| mybatis-spring | 3.0.3 | SSM 整合 |
| 连接池 | Druid 1.2.20 | |
| MySQL 驱动 | mysql-connector-j 8.0.33 | 兼容 MySQL 5.7 / 8 / 9 |
| 服务器 | Tomcat 10.1 / 11 | WAR 部署 |
| 视图层 | JSP + JSTL 3.0 | |
| 构建 | Maven 3.6+ | |

> 说明：本课程全程使用 Jakarta 体系（Tomcat 10/11 + Servlet 6.0 + Java 17），
> 故采用 Spring 6 而非旧版 Spring 5/javax。任务书中的 "Tomcat 9.x" 为模板遗留表述，
> 本工程在 Tomcat 11 上开箱即用。

## 二、目录结构

```
ssm-file-system/
├── pom.xml
├── sql/
│   ├── schema.sql          # 建库建表
│   └── data.sql            # 测试账号（SHA-256 加盐）
├── src/main/java/com/example/ssm/
│   ├── controller/         # LoginController、FileController、全局异常处理器
│   ├── interceptor/        # LoginInterceptor（登录拦截器）
│   ├── config/             # WebMvcConfig（addInterceptors 注册拦截器）
│   ├── entity/             # SysUser、FileInfo
│   ├── mapper/             # SysUserMapper、FileMapper（接口）
│   ├── service/            # UserService、FileService + impl
│   ├── aop/                # DownloadLogAspect（下载日志切面，F5）
│   ├── exception/          # FileAccessException
│   └── util/               # PasswordUtil（SHA-256 加盐）
├── src/main/resources/
│   ├── spring-datasource.xml   # 根容器：数据源/MyBatis/事务/AOP
│   ├── spring-mvc.xml          # MVC 容器：DelegatingWebMvcConfiguration/控制器/视图/静态资源/上传
│   ├── mybatis-config.xml      # MyBatis 全局配置
│   ├── jdbc.properties         # 数据库连接（改账号密码在这）
│   ├── config.properties       # 上传根目录（改存储目录在这）
│   ├── log4j.properties
│   └── mapper/                 # SysUserMapper.xml、FileMapper.xml
└── src/main/webapp/
    ├── WEB-INF/web.xml
    ├── WEB-INF/views/          # login/list/upload/error.jsp
    └── static/css/style.css
```

## 三、运行步骤

### 1. 初始化数据库

用 MySQL 客户端执行 `sql/schema.sql` 与 `sql/data.sql`
（或直接 `mysql -uroot -p < sql/schema.sql` 再 `... < sql/data.sql`）。

脚本会创建数据库 `ssm_file_db` 及三张表，并写入两个测试账号。

### 2. 修改连接配置（如与本机不符）

编辑 `src/main/resources/jdbc.properties`：

```properties
jdbc.url=jdbc:mysql://localhost:3306/ssm_file_db?...
jdbc.username=root
jdbc.password=你的密码
```

上传存储目录在 `src/main/resources/config.properties`（默认 `C:/Users/18526/upload`，可改；**不要放在磁盘根目录**，Windows 根目录建文件夹需管理员权限）。

### 3. 用 IDEA 打开工程

1. `File → Open`，选择本工程目录（会自动识别为 Maven 工程并加载依赖）。
2. `Settings → Build → Compiler → Java Compiler`，确认 Target bytecode 为 17；
   `Project Structure → SDK` 选 JDK 17。
3. 等待 Maven 依赖下载完成（右下角进度条）。

### 4. 配置 Tomcat 运行（两种方式任选其一）

#### 方式 A：Smart Tomcat 插件（社区版/无许可证均可）

1. `Settings → Plugins → Marketplace` 搜索安装 `Smart Tomcat`（或从
   https://plugins.jetbrains.com/plugin/9492-smart-tomcat 下载 zip 手动安装）。
2. `Run → Edit Configurations → + → Smart Tomcat`：
   - `Tomcat Server → Configure` 指向本机 Tomcat 目录（如 `D:\tomcat\apache-tomcat-11.0.18`）；
   - `Deployment Directory` 选工程的 `src/main/webapp`；
   - `Context Path` 填 `/`；`Server Port` 8080；JRE 选 17。
3. 点 `Run`，访问 `http://localhost:8080/`。

#### 方式 B：Tomcat Server（旗舰版带许可证）

1. `Run → Edit Configurations → + → Tomcat Server → Local`。
2. `Application server → Configure`，指向本机 Tomcat 目录。
3. `Deployment` 标签 → `+` → `Artifact...` → 选择 `ssm-file-system:war exploded`。
4. 点 `Run`，访问 `http://localhost:8080/`（Application context 填 `/` 时）。

### 5. 访问

浏览器打开：`http://localhost:8080/`（自动跳转列表，未登录会重定向到登录页）

测试账号：

| 账号 | 密码 | 姓名 |
|------|------|------|
| zhangsan | 123456 | 张三 |
| lisi | 123456 | 李四 |

## 四、功能清单

| 功能 | 说明 |
|------|------|
| 登录认证 | 查 `sys_user` 校验，SHA-256 加盐存储，成功写入 session |
| 登录拦截器 | 未登录访问受保护页重定向登录页，放行登录页/登录接口/静态资源；按任务书要求用 WebMvcConfigurer#addInterceptors 注册（spring-mvc.xml 显式声明 DelegatingWebMvcConfiguration 支撑） |
| 文件上传 | 支持一次多文件；服务端校验（≤10MB、扩展名白名单）；UUID 安全重命名；按 日期+分类 分目录落盘；元信息入库 |
| 文件分类 | 按扩展名归类为 文档/图片/压缩包，分类存储 + 列表分类筛选 |
| 提交列表 | 只显示本人记录；展示文件名/分类/大小/上传时间/下载次数/状态；空数据友好提示 |
| 文件下载 | 按 fileId 查库取路径（不拼接用户输入）；归属校验防越权；中文名 RFC 5987 编码；异常友好提示 |
| 冻结下载 | 上传者可冻结/解冻自己的文件，冻结后下载被拒绝 |
| 下载次数统计 | 每次下载写入 download_log，列表实时显示下载次数 |
| 下载日志 AOP | F5：DownloadLogAspect 切面记录 文件ID/下载人/时间 |

## 五、验收现象对照

| # | 验收要求 | 对应实现 |
|---|----------|----------|
| 1 | 登录成功，上传 PDF，磁盘 `upload/日期/` 出现文件，`file_info` 新增记录，列表显示 | F2 上传 + 元信息入库 |
| 2 | 未登录直接访问上传页被重定向到登录页 | LoginInterceptor + WebMvcConfig |
| 3 | 下载文件正常，中文文件名不乱码 | `filename*=UTF-8''` 编码 |
| 4 | 换账号登录，看不到也下载不了上一个账号的文件 | 列表按 `uploader_id` 过滤 + 下载归属校验 |

## 六、关键设计说明（答辩参考）

1. **为什么原始文件名要存数据库**：落盘名是 `UUID.扩展名`（避免中文/特殊字符引发路径问题、覆盖与路径穿越），
   用户下载时必须还原成原始中文名，所以原始文件名只能作为元数据存入 `file_info`，磁盘上不放原始名。
2. **为什么不能把用户输入拼进磁盘路径**：下载时按 `fileId` 查库取 `file_path + store_name`，
   再拼接上传根目录；若拼接用户提交的文件名，可能被 `../` 构造路径穿越读取任意文件。
3. **为什么密码要加盐**：相同密码加不同盐后哈希不同，可抵抗彩虹表；盐与哈希分开存储，登录时用该用户的盐重新计算比对。
4. **为什么服务端必须校验**：前端校验可被绕过，大小与扩展名白名单都必须在服务端再验一遍。
5. **file_info(uploader_id) 索引作用**：列表/归属校验都按 `uploader_id` 查询，索引避免全表扫描。
6. **为什么 spring-mvc.xml 要显式声明 DelegatingWebMvcConfiguration**：Spring 6.1 起 `<mvc:annotation-driven/>` 改为直接注册 HandlerMapping/Adapter 等基础设施，不再注册 DelegatingWebMvcConfiguration。若不显式声明，容器里没有收集 WebMvcConfigurer 的组件，`WebMvcConfig#addInterceptors` 永远不会被调用，拦截器会静默失效（任务书要求的注册方式在纯 XML 工程中依赖此委托类）。

## 七、选做功能（已实现）

- **F5 下载日志**：`DownloadLogAspect` 切面在下载成功后把「文件 ID、下载人、下载时间」写入 `download_log` 表，
  形成下载审计记录。
- **多文件上传**：一次可选择多个文件，逐个校验落盘，全部/部分成功均有明确提示。
- **文件分类**：按扩展名归类（文档/图片/压缩包），分类分目录存储 + 列表筛选。
- **冻结下载**：上传者可将自己的文件冻结（status=2）/解冻，冻结后拒绝下载。
- **下载次数统计**：列表展示每个文件的下载次数（来自 `download_log` 计数）。

## 八、常见问题

- **启动报数据库连接失败**：确认 MySQL 服务已启动（`net start MySQL90`），`jdbc.properties` 账号密码正确，且已执行建库脚本。
- **中文乱码**：确认数据库/表为 `utf8mb4`，JSP 与 `CharacterEncodingFilter` 均为 UTF-8。
- **上传大文件报错**：容器上限 20MB 兜底，业务层精确拦截 10MB 并给出友好提示。
