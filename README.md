# blr-free-ai
免费AI; 网页转API; deepseek-free

### 目前支持的模型
* deepseek-v3
* deepseek-r1

### 安装教程

```
docker run -d \
  -p 8210:8210 \
  -v /opt/blr-free-ai/cookies:/app/cookies:ro \
  --name blr-free-ai \
  bingliran/blr-free-ai:latest
```

### 使用教程

##### deepseek cookie获取
* 访问 https://yuanbao.tencent.com
* 使用QQ登录
* 使用Google的[Cookie Editor](https://chromewebstore.google.com/detail/cookie-editor/ookdjilphngeeeghgngjabigmpepanpl)插件导出cookie.json文件
* 将cookie文件放入到 docker启动命令 -v 中的 /opt/blr-free-ai/cookies 目录下

##### 豆包 cookie获取
* 暂不支持豆包(后续更新)

### 支持的API
* 文本生成模型(与OpenAI规范一致): `http://127.0.0.1:8210/v1/chat/completions`
  
  ```
    //纯文本
    {
      "model": "deepseek-r1",
      "messages": [{"role": "user", "content": "你好","name": "bingliran"}]
    }
    //文本图片组合
    {
      "model": "deepseek-r1",
      "messages": [{"role": "user", "content": "你好","name": "bingliran"},{"type":"image_url","image_url":{"url":"https://xxx.com/xxx.png"}]
    }
    //除了url地址之外还支持base64(image/png、image/jpeg、image/jpg)
    {
      "model": "deepseek-r1",
      "messages": [{"role": "user", "content": "你好","name": "bingliran"},{"type":"image_url","image_url":{"url":"data:image/png;base64,base64编码"}]
    }
  ```
* 图片生成模型(暂不支持)
