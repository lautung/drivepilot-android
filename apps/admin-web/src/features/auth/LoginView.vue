<script setup lang="ts">
import { Lock, User } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { problemMessage } from '../../shared/api/problem'
import { useAuthStore } from './store'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const errorMessage = ref('')
const form = reactive({ username: '', password: '' })
const rules: FormRules<typeof form> = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 64, message: '用户名长度应为 3 到 64 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 72, message: '密码长度应为 8 到 72 个字符', trigger: 'blur' },
  ],
}

async function submit() {
  errorMessage.value = ''
  if (!(await formRef.value?.validate().catch(() => false))) return
  submitting.value = true
  try {
    await auth.login(form.username, form.password)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    await router.replace(redirect)
  } catch (error) {
    errorMessage.value = problemMessage(error)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <section class="login-brand-panel" aria-label="DrivePilot 项目信息">
      <div class="login-brand">DrivePilot</div>
      <div class="login-brand-copy">
        <h1>让内容运营与车辆体验保持同步</h1>
        <p>安全、清晰地管理发现内容与媒体资源。</p>
      </div>
      <div class="login-footnote">Vue 3 · Spring Boot · OpenAPI</div>
    </section>

    <section class="login-form-panel">
      <div class="login-form-wrap">
        <h2>登录管理中心</h2>
        <p>使用管理员或只读演示账号继续。</p>

        <ElAlert
          v-if="errorMessage"
          class="login-alert"
          :title="errorMessage"
          type="error"
          show-icon
          :closable="false"
        />
        <ElForm
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          @submit.prevent="submit"
        >
          <ElFormItem label="用户名" prop="username">
            <ElInput
              v-model="form.username"
              size="large"
              autocomplete="username"
              placeholder="请输入用户名"
              :prefix-icon="User"
            />
          </ElFormItem>
          <ElFormItem label="密码" prop="password">
            <ElInput
              v-model="form.password"
              size="large"
              type="password"
              show-password
              autocomplete="current-password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              @keyup.enter="submit"
            />
          </ElFormItem>
          <ElButton
            class="login-submit"
            type="primary"
            size="large"
            native-type="submit"
            :loading="submitting"
            >登录</ElButton
          >
        </ElForm>
        <p class="security-note">刷新凭证由 HttpOnly Cookie 保存，访问令牌仅驻留于当前页面内存。</p>
      </div>
    </section>
  </main>
</template>
