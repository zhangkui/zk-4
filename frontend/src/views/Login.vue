<template>
  <div class="login-container">
    <div class="login-bg"></div>
    <div class="login-content">
      <div class="login-left">
        <div class="title-section">
          <h1 class="main-title">园区级微电网运行监测平台</h1>
          <p class="sub-title">Microgrid Monitoring Platform</p>
        </div>
        <div class="features">
          <div class="feature-item">
            <el-icon :size="28"><Lightning /></el-icon>
            <span>实时监测</span>
          </div>
          <div class="feature-item">
            <el-icon :size="28"><DataAnalysis /></el-icon>
            <span>数据分析</span>
          </div>
          <div class="feature-item">
            <el-icon :size="28"><Setting /></el-icon>
            <span>智能调度</span>
          </div>
        </div>
      </div>
      <div class="login-right">
        <div class="login-box">
          <h2 class="login-title">用户登录</h2>
          <el-form
            ref="formRef"
            :model="loginForm"
            :rules="loginRules"
            size="large"
            class="login-form"
          >
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                :prefix-icon="User"
                clearable
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                :prefix-icon="Lock"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            <el-button
              type="primary"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form>
          <div class="login-tips">
            <p>默认账号：admin / admin123</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import type { LoginParams } from '@/types/api'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive<LoginParams>({
  username: '',
  password: ''
})

const loginRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    loading.value = true
    await userStore.loginAction({ ...loginForm })
    ElMessage.success('登录成功')
    router.push('/admin-home')
  } catch (err: any) {
    if (err?.message) {
      ElMessage.error(err.message)
    }
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  z-index: 0;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -50%;
    width: 100%;
    height: 100%;
    background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
    animation: float 20s ease-in-out infinite;
  }

  &::after {
    content: '';
    position: absolute;
    bottom: -50%;
    left: -50%;
    width: 100%;
    height: 100%;
    background: radial-gradient(circle, rgba(255,255,255,0.08) 0%, transparent 70%);
    animation: float 25s ease-in-out infinite reverse;
  }
}

@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(30%, 30%); }
}

.login-content {
  position: relative;
  z-index: 1;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  gap: 80px;
}

.login-left {
  flex: 1;
  max-width: 600px;
  color: #fff;

  .title-section {
    margin-bottom: 60px;

    .main-title {
      font-size: 42px;
      font-weight: 700;
      margin-bottom: 16px;
      text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
    }

    .sub-title {
      font-size: 20px;
      opacity: 0.9;
      letter-spacing: 4px;
    }
  }

  .features {
    display: flex;
    gap: 40px;

    .feature-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12px;
      opacity: 0.95;

      span {
        font-size: 16px;
      }
    }
  }
}

.login-right {
  width: 420px;
  flex-shrink: 0;
}

.login-box {
  background: #fff;
  border-radius: 12px;
  padding: 40px 36px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);

  .login-title {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 32px;
    text-align: center;
  }

  .login-form {
    .el-form-item {
      margin-bottom: 24px;
    }
  }

  .login-btn {
    width: 100%;
    height: 44px;
    font-size: 16px;
    font-weight: 500;
    letter-spacing: 4px;
  }

  .login-tips {
    margin-top: 20px;
    text-align: center;
    color: #909399;
    font-size: 13px;
  }
}

@media (max-width: 900px) {
  .login-content {
    flex-direction: column;
    gap: 40px;
    padding: 20px;
  }

  .login-left {
    text-align: center;

    .title-section {
      margin-bottom: 32px;

      .main-title {
        font-size: 28px;
      }

      .sub-title {
        font-size: 16px;
      }
    }

    .features {
      justify-content: center;
    }
  }

  .login-right {
    width: 100%;
    max-width: 420px;
  }
}
</style>
