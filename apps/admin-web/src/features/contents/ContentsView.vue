<script setup lang="ts">
import { Plus, RefreshRight } from '@element-plus/icons-vue'
import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { useAuthStore } from '../auth/store'
import { listMedia } from '../media/api'
import { normalizeProblem, problemMessage } from '../../shared/api/problem'
import type { Content, ContentCategory, ContentRequest } from '../../shared/api/types'
import { formatDate } from '../../shared/utils/format'
import {
  categoryLabel,
  categoryLabels,
  statusLabel,
  statusLabels,
  statusTone,
} from '../../shared/utils/labels'
import {
  createContent,
  deleteContent,
  listContents,
  publishContent,
  unpublishContent,
  updateContent,
} from './api'

interface ContentFormModel {
  category: ContentCategory
  title: string
  summary: string
  body: string
  mediaId: string
}

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const queryClient = useQueryClient()
const drawerOpen = ref(false)
const editingId = ref<string | null>(null)
const formRef = ref<FormInstance>()
const formError = ref('')
const serverFieldErrors = reactive<Record<keyof ContentFormModel, string>>({
  category: '',
  title: '',
  summary: '',
  body: '',
  mediaId: '',
})
const form = reactive<ContentFormModel>({
  category: 'RECOMMENDED',
  title: '',
  summary: '',
  body: '',
  mediaId: '',
})
const rules: FormRules<ContentFormModel> = {
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { max: 160, message: '标题不能超过 160 个字符', trigger: 'blur' },
  ],
  summary: [
    { required: true, message: '请输入摘要', trigger: 'blur' },
    { max: 500, message: '摘要不能超过 500 个字符', trigger: 'blur' },
  ],
  body: [{ required: true, message: '请输入正文', trigger: 'blur' }],
}

function queryNumber(name: string, fallback: number) {
  const raw = Number(route.query[name])
  return Number.isInteger(raw) && raw > 0 ? raw : fallback
}

const page = computed(() => queryNumber('page', 1))
const size = computed(() => queryNumber('size', 20))
const status = computed(() => {
  const value = route.query.status
  return value === 'DRAFT' || value === 'PUBLISHED' || value === 'UNPUBLISHED' ? value : ''
})
const category = computed(() => {
  const value = route.query.category
  return value === 'RECOMMENDED' || value === 'LOCAL' || value === 'ACTIVITY' || value === 'STORE'
    ? value
    : ''
})
const contentQueryKey = computed(() => [
  'contents',
  page.value,
  size.value,
  status.value,
  category.value,
])
const contents = useQuery({
  queryKey: contentQueryKey,
  queryFn: () =>
    listContents({
      page: page.value - 1,
      size: size.value,
      status: status.value || undefined,
      category: category.value || undefined,
    }),
})
const media = useQuery({
  queryKey: ['media-options'],
  queryFn: () => listMedia(0, 100),
  staleTime: 60_000,
})
const mediaOptions = computed(() =>
  (media.data.value?.items ?? []).flatMap((item) =>
    item.id ? [{ id: item.id, label: item.originalFilename ?? item.id }] : [],
  ),
)

const saveMutation = useMutation({
  mutationFn: (body: ContentRequest) =>
    editingId.value ? updateContent(editingId.value, body) : createContent(body),
  onSuccess: async () => {
    ElMessage.success(editingId.value ? '内容已更新' : '内容已创建')
    drawerOpen.value = false
    await queryClient.invalidateQueries({ queryKey: ['contents'] })
    await queryClient.invalidateQueries({ queryKey: ['dashboard'] })
  },
})

function updateQuery(patch: Record<string, string | number | undefined>) {
  const next = { ...route.query }
  for (const [key, value] of Object.entries(patch)) {
    if (value === undefined || value === '') delete next[key]
    else next[key] = String(value)
  }
  void router.replace({ query: next })
}

function resetFilters() {
  void router.replace({ query: {} })
}

function setStatusFilter(value: string) {
  updateQuery({ status: value || undefined, page: 1 })
}

function setCategoryFilter(value: string) {
  updateQuery({ category: value || undefined, page: 1 })
}

function setPage(value: number) {
  updateQuery({ page: value })
}

function resetForm() {
  editingId.value = null
  Object.assign(form, { category: 'RECOMMENDED', title: '', summary: '', body: '', mediaId: '' })
  formError.value = ''
  clearServerFieldErrors()
  formRef.value?.clearValidate()
}

function clearServerFieldErrors() {
  for (const field of Object.keys(serverFieldErrors) as (keyof ContentFormModel)[]) {
    serverFieldErrors[field] = ''
  }
}

function openCreate() {
  resetForm()
  drawerOpen.value = true
}

function openEdit(row: Content) {
  editingId.value = row.id ?? null
  Object.assign(form, {
    category: row.category ?? 'RECOMMENDED',
    title: row.title ?? '',
    summary: row.summary ?? '',
    body: row.body ?? '',
    mediaId: row.media?.id ?? '',
  })
  formError.value = ''
  drawerOpen.value = true
}

async function save() {
  formError.value = ''
  clearServerFieldErrors()
  if (!(await formRef.value?.validate().catch(() => false))) return
  try {
    await saveMutation.mutateAsync({
      category: form.category,
      title: form.title.trim(),
      summary: form.summary.trim(),
      body: form.body.trim(),
      ...(form.mediaId ? { mediaId: form.mediaId } : {}),
    })
  } catch (error) {
    const problem = normalizeProblem(error)
    for (const fieldError of problem.fieldErrors) {
      if (fieldError.field in serverFieldErrors) {
        serverFieldErrors[fieldError.field as keyof ContentFormModel] = fieldError.message
      }
    }
    formError.value = problemMessage(problem)
  }
}

async function runAction(label: string, action: () => Promise<unknown>) {
  try {
    await action()
    ElMessage.success(`${label}成功`)
    await queryClient.invalidateQueries({ queryKey: ['contents'] })
    await queryClient.invalidateQueries({ queryKey: ['dashboard'] })
  } catch (error) {
    ElMessage.error(problemMessage(error))
  }
}

async function changeStatus(row: Content, action: 'publish' | 'unpublish') {
  if (!row.id) return
  const label = action === 'publish' ? '发布' : '下架'
  try {
    await ElMessageBox.confirm(`确认${label}“${row.title ?? '该内容'}”？`, `${label}内容`, {
      type: 'warning',
      confirmButtonText: label,
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  await runAction(label, () =>
    action === 'publish' ? publishContent(row.id!) : unpublishContent(row.id!),
  )
}

async function remove(row: Content) {
  if (!row.id) return
  try {
    await ElMessageBox.confirm(`删除后无法恢复，确认删除“${row.title ?? '该内容'}”？`, '删除内容', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  await runAction('删除', () => deleteContent(row.id!))
  if ((contents.data.value?.items?.length ?? 0) === 1 && page.value > 1)
    updateQuery({ page: page.value - 1 })
}
</script>

<template>
  <div>
    <header class="page-header">
      <div>
        <h1>内容管理</h1>
        <p>管理 Android 发现页中的内容、发布状态和关联媒体。</p>
      </div>
      <ElButton v-if="auth.canWrite" type="primary" :icon="Plus" @click="openCreate"
        >新建内容</ElButton
      >
    </header>

    <section class="filter-rail" aria-label="内容筛选">
      <label>状态</label>
      <ElSelect
        :model-value="status"
        placeholder="全部状态"
        clearable
        @update:model-value="setStatusFilter"
      >
        <ElOption
          v-for="(label, value) in statusLabels"
          :key="value"
          :label="label"
          :value="value"
        />
      </ElSelect>
      <label>分类</label>
      <ElSelect
        :model-value="category"
        placeholder="全部分类"
        clearable
        @update:model-value="setCategoryFilter"
      >
        <ElOption
          v-for="(label, value) in categoryLabels"
          :key="value"
          :label="label"
          :value="value"
        />
      </ElSelect>
      <ElButton :icon="RefreshRight" text @click="resetFilters">重置</ElButton>
    </section>

    <section class="content-panel">
      <div class="panel-heading">
        <div>
          <h2 class="section-heading">内容列表</h2>
          <p class="section-description">筛选和分页均由服务端执行。</p>
        </div>
        <span class="result-count">共 {{ contents.data.value?.totalElements ?? 0 }} 条</span>
      </div>
      <div v-if="contents.isError.value" class="error-panel">
        <span>{{ problemMessage(contents.error.value) }}</span
        ><ElButton @click="contents.refetch()">重新加载</ElButton>
      </div>
      <ElTable
        v-else
        v-loading="contents.isPending.value"
        :data="contents.data.value?.items ?? []"
        table-layout="fixed"
        empty-text="暂无符合条件的内容"
      >
        <ElTableColumn prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <ElTableColumn label="分类" width="100"
          ><template #default="{ row }">{{ categoryLabel(row.category) }}</template></ElTableColumn
        >
        <ElTableColumn label="状态" width="118"
          ><template #default="{ row }"
            ><i class="status-dot" :class="statusTone(row.status)" />{{
              statusLabel(row.status)
            }}</template
          ></ElTableColumn
        >
        <ElTableColumn label="关联媒体" min-width="170" show-overflow-tooltip
          ><template #default="{ row }">{{ row.media?.id ?? '未关联' }}</template></ElTableColumn
        >
        <ElTableColumn label="更新时间" width="170"
          ><template #default="{ row }">{{ formatDate(row.updatedAt) }}</template></ElTableColumn
        >
        <ElTableColumn v-if="auth.canWrite" label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <ElButton link type="primary" @click="openEdit(row)">编辑</ElButton>
            <ElButton
              v-if="row.status !== 'PUBLISHED'"
              link
              type="success"
              @click="changeStatus(row, 'publish')"
              >发布</ElButton
            >
            <ElButton v-else link type="warning" @click="changeStatus(row, 'unpublish')"
              >下架</ElButton
            >
            <ElButton link type="danger" @click="remove(row)">删除</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
      <div v-if="(contents.data.value?.totalPages ?? 0) > 1" class="table-footer">
        <ElPagination
          background
          layout="prev, pager, next"
          :current-page="page"
          :page-size="size"
          :total="contents.data.value?.totalElements ?? 0"
          @current-change="setPage"
        />
      </div>
    </section>

    <ElDrawer
      v-model="drawerOpen"
      :title="editingId ? '编辑内容' : '新建内容'"
      size="min(540px, 92vw)"
      destroy-on-close
    >
      <ElAlert
        v-if="formError"
        :title="formError"
        type="error"
        :closable="false"
        show-icon
        class="form-alert"
      />
      <ElForm ref="formRef" :model="form" :rules="rules" label-position="top">
        <ElFormItem label="分类" prop="category" :error="serverFieldErrors.category"
          ><ElSelect v-model="form.category" class="full-width"
            ><ElOption
              v-for="(label, value) in categoryLabels"
              :key="value"
              :label="label"
              :value="value" /></ElSelect
        ></ElFormItem>
        <ElFormItem label="标题" prop="title" :error="serverFieldErrors.title"
          ><ElInput v-model="form.title" maxlength="160" show-word-limit
        /></ElFormItem>
        <ElFormItem label="摘要" prop="summary" :error="serverFieldErrors.summary"
          ><ElInput
            v-model="form.summary"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
        /></ElFormItem>
        <ElFormItem label="正文" prop="body" :error="serverFieldErrors.body"
          ><ElInput
            v-model="form.body"
            type="textarea"
            :rows="10"
            placeholder="正文使用普通多行文本，不支持 HTML 或 Markdown"
        /></ElFormItem>
        <ElFormItem label="关联媒体" :error="serverFieldErrors.mediaId"
          ><ElSelect
            v-model="form.mediaId"
            class="full-width"
            clearable
            filterable
            placeholder="可选"
            ><ElOption
              v-for="item in mediaOptions"
              :key="item.id"
              :label="item.label"
              :value="item.id" /></ElSelect
        ></ElFormItem>
      </ElForm>
      <template #footer
        ><ElButton @click="drawerOpen = false">取消</ElButton
        ><ElButton type="primary" :loading="saveMutation.isPending.value" @click="save">{{
          editingId ? '保存修改' : '创建内容'
        }}</ElButton></template
      >
    </ElDrawer>
  </div>
</template>

<style scoped>
.filter-rail {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 24px;
  padding: 16px 18px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: #fafbfd;
}
.filter-rail label {
  color: var(--muted);
  font-size: 13px;
  font-weight: 620;
}
.filter-rail .el-select {
  width: 170px;
  margin-right: 8px;
}
.result-count {
  color: var(--muted);
  font-size: 13px;
}
.form-alert {
  margin-bottom: 18px;
}
.full-width {
  width: 100%;
}
@media (max-width: 680px) {
  .filter-rail {
    align-items: stretch;
    flex-direction: column;
  }
  .filter-rail .el-select {
    width: 100%;
  }
}
</style>
