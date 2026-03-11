# EasyFK-OSS 文件存储服务

> 基于 Spring Boot 和 x-file-storage 的统一文件存储服务，支持多种云存储平台、大文件分片上传、断点续传、秒传等功能。

---

## 目录

- [一、项目介绍](#一项目介绍)
- [二、功能列表](#二功能列表)
- [三、特点](#三特点)
- [四、优点](#四优点)
- [五、技术栈](#五技术栈)
- [六、快速开始](#六快速开始)
- [七、配置说明](#七配置说明)
- [八、API 接口文档](#八api-接口文档)
- [九、前端对接示例](#九前端对接示例)
- [十、常见问题](#十常见问题)

---

## 一、项目介绍

EasyFK-OSS 是 EasyFK 框架的文件存储服务模块，提供统一的文件上传、存储和管理能力。通过抽象存储层，支持无缝切换多种对象存储平台（MinIO、阿里云 OSS、腾讯云 COS、华为云 OBS、七牛云、百度云、Amazon S3 等），并提供大文件分片上传、断点续传、秒传等高级功能。

### 适用场景

- 企业级文件管理系统
- 图片/视频上传服务
- 大文件传输场景（视频、安装包、数据文件等）
- 多租户 SaaS 应用的文件存储
- 需要支持多种存储后端的应用

---

## 二、功能列表

### 基础上传功能

| 功能 | 说明 |
|------|------|
| 统一文件上传 | 支持所有类型文件的上传 |
| 图片上传 | 专用图片上传接口，支持压缩和缩略图 |
| 视频上传 | 专用视频上传接口 |
| 图片压缩 | 可配置压缩比例和质量 |
| 缩略图生成 | 自动生成指定尺寸的缩略图 |
| 业务分类存储 | 通过 bizName 参数实现文件分类存储 |

### 分片上传功能（大文件）

| 功能 | 说明 |
|------|------|
| 分片上传 | 将大文件切分为多个小块上传 |
| 断点续传 | 上传中断后可从断点继续 |
| 秒传 | 相同文件直接返回已存在的 URL |
| 上传状态查询 | 查询已上传的分片列表 |
| 取消上传 | 支持取消上传并清理已上传分片 |
| 分布式支持 | 基于 Redis 存储状态，支持多实例部署 |

### 存储平台支持

| 平台 | 配置文件 |
|------|----------|
| MinIO | application-minio.yml |
| 阿里云 OSS | application-aliyun.yml |
| 腾讯云 COS | application-tencent.yml |
| 华为云 OBS | application-huawei.yml |
| 七牛云 | application-qiniu.yml |
| 百度云 BOS | application-baidu.yml |
| Amazon S3 | application-amazon.yml |

---

## 三、特点

### 1. 统一抽象，多平台支持

通过配置文件切换存储平台，业务代码无需修改：

```yaml
dromara:
  x-file-storage:
    default-platform: minio  # 切换为 aliyun、tencent 等即可
```

### 2. 大文件分片上传

- 默认分片大小：5MB
- 最大支持文件：10GB
- 分片索引从 1 开始

### 3. 断点续传

- 基于 Redis 存储上传状态
- 每个分片独立记录，并发安全
- 支持跨服务实例续传

### 4. 秒传功能

- 基于文件 MD5 值判断
- 相同文件直接返回已存在的 URL
- 节省带宽和存储空间

### 5. 图片处理能力

- 自动生成缩略图
- 可配置图片压缩
- 支持指定格式压缩

---

## 四、优点

| 优点 | 说明 |
|------|------|
| **开箱即用** | 引入依赖，配置存储平台即可使用 |
| **平台无关** | 业务代码与存储平台解耦，切换平台只需改配置 |
| **高可用** | 支持分布式部署，状态存储在 Redis |
| **大文件友好** | 分片上传 + 断点续传，解决大文件上传难题 |
| **节省资源** | 秒传功能避免重复上传相同文件 |
| **安全可控** | 文件类型校验、大小限制、业务隔离 |
| **易于扩展** | 基于 x-file-storage，可扩展更多存储平台 |

---

## 五、技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 21 | JDK 版本 |
| Spring Boot | 3.2.x | 基础框架 |
| x-file-storage | 2.3.0 | 文件存储抽象层 |
| Redis | - | 分片上传状态存储 |
| Swagger/OpenAPI | 3.x | API 文档 |
| Gradle | 8.x | 构建工具 |

### 依赖的 EasyFK 组件

- `easyfk-core` - 核心工具类
- `cache-redis` - Redis 缓存服务
- `web-prd` - Web 生产环境配置

---

## 六、快速开始

### 1. 引入依赖

```groovy
dependencies {
    implementation("com.mcst:easyfk-oss")
}
```

### 2. 配置存储平台

以 MinIO 为例，在 `config/application-minio.yml` 中配置：

```yaml
dromara:
  x-file-storage:
    default-platform: minio
    thumbnail-suffix: ".min.jpg"
    minio:
      - platform: minio
        enable-storage: true
        access-key: your-access-key
        secret-key: your-secret-key
        end-point: http://your-minio-server:9000
        bucket-name: your-bucket
        domain: https://your-domain/bucket/
        base-path: upload/
```

### 3. 配置 Redis（分片上传需要）

```yaml
spring:
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      password: your-password
      database: 0

easyfk:
  config:
    oss:
      multipart:
        enabled: true  # 启用分片上传
```

### 4. 启动服务

```bash
./gradlew bootRun
```

服务默认端口：`9011`

---

## 七、配置说明

### 7.1 基础上传配置

配置前缀：`easyfk.config.oss.upload`

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `thumbnail` | Boolean | false | 是否生成缩略图 |
| `thumbnail-width` | Integer | 200 | 缩略图宽度 |
| `thumbnail-height` | Integer | 200 | 缩略图高度 |
| `compress` | Boolean | false | 是否压缩图片 |
| `compress-image-ext-name` | String | * | 压缩的图片格式，`*` 表示全部 |
| `scale` | Float | 1.0 | 压缩比例（0-1） |
| `quality` | Float | 0.5 | 压缩质量（0-1） |

### 7.2 分片上传配置

配置前缀：`easyfk.config.oss.multipart`

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `enabled` | Boolean | false | 是否启用分片上传 |
| `chunk-size` | Integer | 5242880 | 分片大小（字节），默认 5MB |
| `max-file-size` | Long | 10737418240 | 最大文件大小（字节），默认 10GB |
| `task-expire-hours` | Integer | 24 | 上传任务过期时间（小时） |
| `instant-upload-enabled` | Boolean | true | 是否启用秒传 |
| `instant-upload-cache-days` | Integer | 30 | 秒传缓存天数，0 表示永不过期 |

### 7.3 完整配置示例

```yaml
server:
  port: 9011

spring:
  servlet:
    multipart:
      max-file-size: 1000MB
      max-request-size: 1000MB
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      database: 0

easyfk:
  config:
    oss:
      upload:
        thumbnail: true
        thumbnail-width: 200
        thumbnail-height: 200
        compress: true
        scale: 0.8
        quality: 0.7
      multipart:
        enabled: true
        chunk-size: 5242880
        max-file-size: 10737418240
        task-expire-hours: 24
        instant-upload-enabled: true
        instant-upload-cache-days: 30
```

---

## 八、API 接口文档

### 8.1 基础上传接口

#### 统一上传

```
POST /oss/file/upload
Content-Type: multipart/form-data
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 上传的文件 |
| bizName | String | 否 | 业务名称，用于分类存储 |

**响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": "https://oss.example.com/bucket/IMAGE/abc123.jpg"
}
```

#### 图片上传

```
POST /oss/file/upload/image
Content-Type: multipart/form-data
```

仅接受图片格式（jpg、png、gif 等）。

#### 视频上传

```
POST /oss/file/upload/video
Content-Type: multipart/form-data
```

仅接受视频格式（mp4、avi、mov 等）。

---

### 8.2 分片上传接口

#### 初始化上传

```
POST /oss/file/multipart/init
Content-Type: application/json
```

**请求体**：

```json
{
  "fileName": "large-video.mp4",
  "fileSize": 104857600,
  "fileMd5": "d41d8cd98f00b204e9800998ecf8427e",
  "chunkSize": 5242880,
  "bizName": "video"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| fileName | String | 是 | 文件名 |
| fileSize | Long | 是 | 文件大小（字节） |
| fileMd5 | String | 是 | 文件 MD5 值（用于秒传） |
| chunkSize | Integer | 否 | 分片大小，默认 5MB |
| bizName | String | 否 | 业务名称 |

**响应示例（正常初始化）**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "uploadId": "abc123xyz",
    "chunkSize": 5242880,
    "totalChunks": 20,
    "uploadedChunks": [],
    "instantUpload": false
  }
}
```

**响应示例（秒传成功）**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "instantUpload": true,
    "fileUrl": "https://oss.example.com/bucket/VIDEO/existing-file.mp4"
  }
}
```

#### 查询上传状态

```
GET /oss/file/multipart/status/{uploadId}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "uploadId": "abc123xyz",
    "chunkSize": 5242880,
    "totalChunks": 20,
    "uploadedChunks": [1, 2, 3, 5, 6],
    "instantUpload": false
  }
}
```

#### 上传分片

```
POST /oss/file/multipart/chunk
Content-Type: multipart/form-data
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| uploadId | String | 是 | 上传 ID |
| chunkIndex | Integer | 是 | 分片索引（从 1 开始） |
| file | File | 是 | 分片文件 |

**响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

#### 完成上传

```
POST /oss/file/multipart/complete/{uploadId}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": "https://oss.example.com/bucket/VIDEO/abc123.mp4"
}
```

#### 取消上传

```
DELETE /oss/file/multipart/abort/{uploadId}
```

**响应示例**：

```json
{
  "code": 200,
  "message": "success"
}
```

---

## 九、前端对接示例

### 9.1 基础文件上传（JavaScript）

```javascript
async function uploadFile(file, bizName = '') {
  const formData = new FormData();
  formData.append('file', file);
  if (bizName) {
    formData.append('bizName', bizName);
  }

  const response = await fetch('/oss/file/upload', {
    method: 'POST',
    body: formData
  });

  const result = await response.json();
  if (result.code === 200) {
    console.log('上传成功:', result.data);
    return result.data;
  } else {
    throw new Error(result.message);
  }
}

// 使用示例
const fileInput = document.querySelector('input[type="file"]');
fileInput.addEventListener('change', async (e) => {
  const file = e.target.files[0];
  const url = await uploadFile(file, 'avatar');
  console.log('文件 URL:', url);
});
```

### 9.2 大文件分片上传（JavaScript）

```javascript
class ChunkUploader {
  constructor(options = {}) {
    this.chunkSize = options.chunkSize || 5 * 1024 * 1024; // 5MB
    this.baseUrl = options.baseUrl || '';
    this.onProgress = options.onProgress || (() => {});
  }

  // 计算文件 MD5
  async calculateMD5(file) {
    // 使用 spark-md5 库计算
    return new Promise((resolve) => {
      const reader = new FileReader();
      reader.onload = (e) => {
        const spark = new SparkMD5.ArrayBuffer();
        spark.append(e.target.result);
        resolve(spark.end());
      };
      reader.readAsArrayBuffer(file);
    });
  }

  // 初始化上传
  async initUpload(file, bizName = '') {
    const fileMd5 = await this.calculateMD5(file);
    
    const response = await fetch(`${this.baseUrl}/oss/file/multipart/init`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        fileName: file.name,
        fileSize: file.size,
        fileMd5: fileMd5,
        chunkSize: this.chunkSize,
        bizName: bizName
      })
    });

    const result = await response.json();
    if (result.code !== 200) {
      throw new Error(result.message);
    }

    return result.data;
  }

  // 上传单个分片
  async uploadChunk(uploadId, chunkIndex, chunk) {
    const formData = new FormData();
    formData.append('uploadId', uploadId);
    formData.append('chunkIndex', chunkIndex);
    formData.append('file', chunk);

    const response = await fetch(`${this.baseUrl}/oss/file/multipart/chunk`, {
      method: 'POST',
      body: formData
    });

    const result = await response.json();
    if (result.code !== 200) {
      throw new Error(result.message);
    }

    return result.data;
  }

  // 完成上传
  async completeUpload(uploadId) {
    const response = await fetch(`${this.baseUrl}/oss/file/multipart/complete/${uploadId}`, {
      method: 'POST'
    });

    const result = await response.json();
    if (result.code !== 200) {
      throw new Error(result.message);
    }

    return result.data;
  }

  // 查询上传状态（用于断点续传）
  async getUploadStatus(uploadId) {
    const response = await fetch(`${this.baseUrl}/oss/file/multipart/status/${uploadId}`);
    const result = await response.json();
    if (result.code !== 200) {
      throw new Error(result.message);
    }
    return result.data;
  }

  // 主上传方法
  async upload(file, bizName = '') {
    // 1. 初始化上传
    const initResult = await this.initUpload(file, bizName);

    // 2. 检查是否秒传成功
    if (initResult.instantUpload) {
      console.log('秒传成功');
      this.onProgress(100, file.size, file.size);
      return initResult.fileUrl;
    }

    const { uploadId, totalChunks, uploadedChunks } = initResult;
    const uploadedSet = new Set(uploadedChunks);

    // 3. 上传每个分片
    let uploadedCount = uploadedChunks.length;

    for (let i = 1; i <= totalChunks; i++) {
      // 跳过已上传的分片（断点续传）
      if (uploadedSet.has(i)) {
        continue;
      }

      const start = (i - 1) * this.chunkSize;
      const end = Math.min(start + this.chunkSize, file.size);
      const chunk = file.slice(start, end);

      await this.uploadChunk(uploadId, i, chunk);
      uploadedCount++;

      // 更新进度
      const progress = Math.round((uploadedCount / totalChunks) * 100);
      this.onProgress(progress, uploadedCount * this.chunkSize, file.size);
    }

    // 4. 完成上传
    const fileUrl = await this.completeUpload(uploadId);
    this.onProgress(100, file.size, file.size);

    return fileUrl;
  }
}

// 使用示例
const uploader = new ChunkUploader({
  chunkSize: 5 * 1024 * 1024,
  onProgress: (percent, uploaded, total) => {
    console.log(`上传进度: ${percent}% (${uploaded}/${total})`);
    document.querySelector('.progress-bar').style.width = `${percent}%`;
  }
});

const fileInput = document.querySelector('input[type="file"]');
fileInput.addEventListener('change', async (e) => {
  const file = e.target.files[0];
  
  try {
    const url = await uploader.upload(file, 'video');
    console.log('上传成功:', url);
  } catch (error) {
    console.error('上传失败:', error.message);
  }
});
```

### 9.3 Vue 3 组件示例

```vue
<template>
  <div class="upload-container">
    <input 
      type="file" 
      ref="fileInput" 
      @change="handleFileChange" 
      :accept="accept"
    />
    
    <div v-if="uploading" class="progress">
      <div class="progress-bar" :style="{ width: progress + '%' }"></div>
      <span>{{ progress }}%</span>
    </div>
    
    <div v-if="fileUrl" class="result">
      上传成功: <a :href="fileUrl" target="_blank">{{ fileUrl }}</a>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import SparkMD5 from 'spark-md5';

const props = defineProps({
  accept: { type: String, default: '*/*' },
  bizName: { type: String, default: '' },
  chunkSize: { type: Number, default: 5 * 1024 * 1024 }
});

const emit = defineEmits(['success', 'error']);

const fileInput = ref(null);
const uploading = ref(false);
const progress = ref(0);
const fileUrl = ref('');

// 计算 MD5
async function calculateMD5(file) {
  return new Promise((resolve) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      const spark = new SparkMD5.ArrayBuffer();
      spark.append(e.target.result);
      resolve(spark.end());
    };
    reader.readAsArrayBuffer(file);
  });
}

// 处理文件选择
async function handleFileChange(e) {
  const file = e.target.files[0];
  if (!file) return;

  uploading.value = true;
  progress.value = 0;
  fileUrl.value = '';

  try {
    // 小文件直接上传
    if (file.size <= props.chunkSize) {
      const url = await simpleUpload(file);
      fileUrl.value = url;
      emit('success', url);
    } else {
      // 大文件分片上传
      const url = await chunkUpload(file);
      fileUrl.value = url;
      emit('success', url);
    }
  } catch (error) {
    emit('error', error.message);
  } finally {
    uploading.value = false;
  }
}

// 简单上传
async function simpleUpload(file) {
  const formData = new FormData();
  formData.append('file', file);
  if (props.bizName) {
    formData.append('bizName', props.bizName);
  }

  const response = await fetch('/oss/file/upload', {
    method: 'POST',
    body: formData
  });

  const result = await response.json();
  if (result.code !== 200) throw new Error(result.message);
  
  progress.value = 100;
  return result.data;
}

// 分片上传
async function chunkUpload(file) {
  const fileMd5 = await calculateMD5(file);

  // 初始化
  const initRes = await fetch('/oss/file/multipart/init', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      fileName: file.name,
      fileSize: file.size,
      fileMd5,
      chunkSize: props.chunkSize,
      bizName: props.bizName
    })
  });

  const initResult = await initRes.json();
  if (initResult.code !== 200) throw new Error(initResult.message);

  // 秒传
  if (initResult.data.instantUpload) {
    progress.value = 100;
    return initResult.data.fileUrl;
  }

  const { uploadId, totalChunks, uploadedChunks } = initResult.data;
  const uploadedSet = new Set(uploadedChunks);
  let completed = uploadedChunks.length;

  // 上传分片
  for (let i = 1; i <= totalChunks; i++) {
    if (uploadedSet.has(i)) continue;

    const start = (i - 1) * props.chunkSize;
    const end = Math.min(start + props.chunkSize, file.size);
    const chunk = file.slice(start, end);

    const formData = new FormData();
    formData.append('uploadId', uploadId);
    formData.append('chunkIndex', i);
    formData.append('file', chunk);

    const res = await fetch('/oss/file/multipart/chunk', {
      method: 'POST',
      body: formData
    });

    const result = await res.json();
    if (result.code !== 200) throw new Error(result.message);

    completed++;
    progress.value = Math.round((completed / totalChunks) * 100);
  }

  // 完成上传
  const completeRes = await fetch(`/oss/file/multipart/complete/${uploadId}`, {
    method: 'POST'
  });

  const completeResult = await completeRes.json();
  if (completeResult.code !== 200) throw new Error(completeResult.message);

  return completeResult.data;
}
</script>

<style scoped>
.progress {
  margin-top: 10px;
  background: #eee;
  border-radius: 4px;
  height: 20px;
  position: relative;
}
.progress-bar {
  height: 100%;
  background: #4caf50;
  border-radius: 4px;
  transition: width 0.3s;
}
.progress span {
  position: absolute;
  right: 10px;
  top: 0;
  line-height: 20px;
}
</style>
```

---

## 十、常见问题

### Q1: 如何切换存储平台？

修改 `application.yml` 中的 `spring.config.import` 配置：

```yaml
spring:
  config:
    import:
      - "file:config/application-aliyun.yml"  # 切换为阿里云
```

### Q2: 分片上传失败后如何续传？

1. 调用 `GET /oss/file/multipart/status/{uploadId}` 获取已上传的分片列表
2. 前端跳过已上传的分片，继续上传剩余分片
3. 所有分片上传完成后调用完成接口

### Q3: 秒传不生效？

检查以下配置：

```yaml
easyfk.config.oss.multipart:
  instant-upload-enabled: true
```

确保前端在初始化时传递了正确的 `fileMd5` 参数。

### Q4: 上传大文件时超时？

1. 调整 Spring Boot 配置：

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 1000MB
      max-request-size: 1000MB
```

2. 如果使用 Nginx，调整超时配置：

```nginx
client_max_body_size 1000M;
proxy_read_timeout 600s;
proxy_send_timeout 600s;
```

### Q5: 分布式部署时断点续传不工作？

确保所有服务实例连接同一个 Redis 实例：

```yaml
spring:
  data:
    redis:
      host: your-redis-host
      port: 6379
```

### Q6: 如何清理过期的上传任务？

上传任务默认 24 小时后自动过期，由 Redis TTL 机制自动清理。可通过配置调整：

```yaml
easyfk.config.oss.multipart:
  task-expire-hours: 48  # 改为 48 小时
```

---

## 许可证

本项目为 EasyFK 框架的一部分。

---

> **文档版本**：1.0 | **最后更新**：2026-03-11
