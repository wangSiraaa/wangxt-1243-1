<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <div class="error-code">404</div>
      <div class="error-title">页面未找到</div>
      <div class="error-message">抱歉，您访问的页面不存在或已被移除</div>
      <el-button type="primary" size="large" @click="goHome">
        <el-icon><House /></el-icon>
        返回首页
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { House } from '@element-plus/icons-vue'

const router = useRouter()

const goHome = () => {
  router.push('/dashboard')
}
</script>

<style scoped>
.not-found-container {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7eb 100%);
}

.not-found-content {
  text-align: center;
  padding: 60px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
}

.error-code {
  font-size: 120px;
  font-weight: 700;
  line-height: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 20px;
}

.error-title {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.error-message {
  font-size: 16px;
  color: #909399;
  margin-bottom: 32px;
}

.el-button {
  padding: 12px 32px;
  font-size: 16px;
}

.el-button .el-icon {
  margin-right: 6px;
}
</style>
