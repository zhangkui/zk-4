<template>
  <div class="page-container profile-page">
    <div class="page-header">
      <h2 class="page-title">个人中心</h2>
    </div>

    <el-row :gutter="20">
      <el-col :md="8" :span="24">
        <div class="card profile-card">
          <div class="avatar-wrap">
            <el-avatar :size="80">
              {{ userStore.userInfo?.realName?.charAt(0) || userStore.userInfo?.username?.charAt(0) || 'U' }}
            </el-avatar>
          </div>
          <div class="user-name">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</div>
          <div class="user-username">@{{ userStore.userInfo?.username }}</div>
          <div class="user-roles">
            <el-tag v-for="role in userStore.userInfo?.roles" :key="role.id" style="margin-right: 4px">
              {{ role.roleName }}
            </el-tag>
          </div>
        </div>
      </el-col>

      <el-col :md="16" :span="24">
        <div class="card">
          <div class="card-header">
            <span class="card-title">基本信息</span>
          </div>
          <div class="card-body">
            <el-form
              ref="formRef"
              :model="formData"
              :rules="formRules"
              label-width="100px"
              style="max-width: 500px"
            >
              <el-form-item label="用户名">
                <el-input v-model="formData.username" disabled />
              </el-form-item>
              <el-form-item label="真实姓名" prop="realName">
                <el-input v-model="formData.realName" placeholder="请输入真实姓名" />
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="formData.email" placeholder="请输入邮箱" />
              </el-form-item>
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="formData.phone" placeholder="请输入手机号" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存修改</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>

        <div class="card" style="margin-top: 16px">
          <div class="card-header">
            <span class="card-title">修改密码</span>
          </div>
          <div class="card-body">
            <el-form
              ref="pwdFormRef"
              :model="pwdForm"
              :rules="pwdRules"
              label-width="100px"
              style="max-width: 500px"
            >
              <el-form-item label="原密码" prop="oldPassword">
                <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="pwdSubmitLoading" @click="handlePwdSubmit">修改密码</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useUserStore } from '@/store/user'
import { updateUser, updateUserPassword } from '@/api/user'

const userStore = useUserStore()
const formRef = ref<FormInstance>()
const pwdFormRef = ref<FormInstance>()
const submitLoading = ref(false)
const pwdSubmitLoading = ref(false)

const formData = reactive({
  username: '',
  realName: '',
  email: '',
  phone: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const formRules: FormRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }]
}

const pwdRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

function loadUserInfo() {
  if (userStore.userInfo) {
    formData.username = userStore.userInfo.username
    formData.realName = userStore.userInfo.realName
    formData.email = userStore.userInfo.email
    formData.phone = userStore.userInfo.phone
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (userStore.userInfo) {
          await updateUser(userStore.userInfo.id, {
            realName: formData.realName,
            email: formData.email,
            phone: formData.phone,
            status: userStore.userInfo.status,
            roleIds: (userStore.userInfo.roles || []).map((r: any) => r.id)
          })
          await userStore.fetchUserInfo()
          ElMessage.success('修改成功')
        }
      } catch (err: any) {
        ElMessage.error(err?.message || '修改失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

async function handlePwdSubmit() {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate(async (valid) => {
    if (valid) {
      pwdSubmitLoading.value = true
      try {
        if (userStore.userInfo) {
          const result = await updateUserPassword(
            userStore.userInfo.id,
            pwdForm.oldPassword,
            pwdForm.newPassword
          )
          if (result) {
            ElMessage.success('密码修改成功')
            pwdForm.oldPassword = ''
            pwdForm.newPassword = ''
            pwdForm.confirmPassword = ''
          } else {
            ElMessage.error('原密码错误')
          }
        }
      } catch (err: any) {
        ElMessage.error(err?.message || '密码修改失败')
      } finally {
        pwdSubmitLoading.value = false
      }
    }
  })
}

onMounted(loadUserInfo)
</script>

<style lang="scss" scoped>
.profile-page {
  .profile-card {
    padding: 24px;
    text-align: center;

    .avatar-wrap {
      margin-bottom: 16px;
    }

    .user-name {
      font-size: 20px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 4px;
    }

    .user-username {
      color: #909399;
      margin-bottom: 12px;
    }

    .user-roles {
      margin-top: 12px;
    }
  }

  .card {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.06);
    margin-bottom: 16px;

    .card-header {
      padding: 16px 20px;
      border-bottom: 1px solid #ebeef5;

      .card-title {
        font-size: 15px;
        font-weight: 600;
        color: #303133;
      }
    }

    .card-body {
      padding: 20px;
    }
  }
}
</style>
