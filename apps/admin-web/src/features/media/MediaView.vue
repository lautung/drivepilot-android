<script setup lang="ts">
import { Delete, UploadFilled } from '@element-plus/icons-vue'
import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import { ElMessage, ElMessageBox, type UploadFile, type UploadInstance } from 'element-plus'
import { ref } from 'vue'

import { useAuthStore } from '../auth/store'
import { problemMessage } from '../../shared/api/problem'
import type { Media } from '../../shared/api/types'
import { formatBytes, formatDate } from '../../shared/utils/format'
import { deleteMedia, listMedia, uploadMedia } from './api'

const auth = useAuthStore()
const queryClient = useQueryClient()
const page = ref(1)
const size = 20
const progress = ref(0)
const uploadRef = ref<UploadInstance>()
const media = useQuery({
  queryKey: ['media', page],
  queryFn: () => listMedia(page.value - 1, size),
})
const uploadMutation = useMutation({
  mutationFn: (file: File) => uploadMedia(file, (value) => (progress.value = value)),
  onSuccess: async () => {
    ElMessage.success('媒体上传成功')
    progress.value = 0
    uploadRef.value?.clearFiles()
    await queryClient.invalidateQueries({ queryKey: ['media'] })
    await queryClient.invalidateQueries({ queryKey: ['media-options'] })
    await queryClient.invalidateQueries({ queryKey: ['dashboard'] })
  },
  onError: (error) => {
    ElMessage.error(problemMessage(error))
    progress.value = 0
    uploadRef.value?.clearFiles()
  },
})

function selectFile(uploadFile: UploadFile) {
  if (!uploadFile.raw || uploadMutation.isPending.value) return
  progress.value = 0
  uploadMutation.mutate(uploadFile.raw)
}

function setPage(value: number) {
  page.value = value
}

async function remove(item: Media) {
  if (!item.id) return
  try {
    await ElMessageBox.confirm(`确认删除“${item.originalFilename ?? '该媒体'}”？`, '删除媒体', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  try {
    await deleteMedia(item.id)
    ElMessage.success('媒体已删除')
    await queryClient.invalidateQueries({ queryKey: ['media'] })
    await queryClient.invalidateQueries({ queryKey: ['media-options'] })
    await queryClient.invalidateQueries({ queryKey: ['dashboard'] })
  } catch (error) {
    ElMessage.error(problemMessage(error))
  }
}
</script>

<template>
  <div>
    <header class="page-header">
      <div>
        <h1>媒体资源</h1>
        <p>上传和管理发现内容使用的图片资源。</p>
      </div>
    </header>

    <section v-if="auth.canWrite" class="upload-section">
      <ElUpload
        ref="uploadRef"
        drag
        action="#"
        accept="image/jpeg,image/png,image/webp"
        :auto-upload="false"
        :show-file-list="false"
        :disabled="uploadMutation.isPending.value"
        :on-change="selectFile"
      >
        <ElIcon class="upload-icon"><UploadFilled /></ElIcon>
        <div class="upload-title">拖放图片到此处，或点击选择</div>
        <div class="upload-hint">支持 JPEG、PNG、WebP，单个文件不超过 10 MiB</div>
        <ElProgress
          v-if="uploadMutation.isPending.value"
          class="upload-progress"
          :percentage="progress"
          :stroke-width="8"
        />
      </ElUpload>
    </section>

    <section class="content-panel">
      <div class="panel-heading">
        <div>
          <h2 class="section-heading">媒体列表</h2>
          <p class="section-description">媒体桶保持私有，此处仅展示安全元数据。</p>
        </div>
        <span class="result-count">共 {{ media.data.value?.totalElements ?? 0 }} 个文件</span>
      </div>
      <div v-if="media.isError.value" class="error-panel">
        <span>{{ problemMessage(media.error.value) }}</span
        ><ElButton @click="media.refetch()">重新加载</ElButton>
      </div>
      <ElTable
        v-else
        v-loading="media.isPending.value"
        :data="media.data.value?.items ?? []"
        table-layout="fixed"
        empty-text="暂无媒体资源"
      >
        <ElTableColumn
          prop="originalFilename"
          label="文件名"
          min-width="220"
          show-overflow-tooltip
        />
        <ElTableColumn prop="contentType" label="类型" width="130" />
        <ElTableColumn label="大小" width="110"
          ><template #default="{ row }">{{ formatBytes(row.sizeBytes) }}</template></ElTableColumn
        >
        <ElTableColumn label="SHA-256" min-width="190" show-overflow-tooltip
          ><template #default="{ row }"
            ><code class="hash">{{ row.sha256 ?? '—' }}</code></template
          ></ElTableColumn
        >
        <ElTableColumn prop="status" label="状态" width="100" />
        <ElTableColumn label="创建时间" width="170"
          ><template #default="{ row }">{{ formatDate(row.createdAt) }}</template></ElTableColumn
        >
        <ElTableColumn v-if="auth.canWrite" label="操作" width="100" fixed="right"
          ><template #default="{ row }"
            ><ElButton link type="danger" :icon="Delete" @click="remove(row)"
              >删除</ElButton
            ></template
          ></ElTableColumn
        >
      </ElTable>
      <div v-if="(media.data.value?.totalPages ?? 0) > 1" class="table-footer">
        <ElPagination
          background
          layout="prev, pager, next"
          :current-page="page"
          :page-size="size"
          :total="media.data.value?.totalElements ?? 0"
          @current-change="setPage"
        />
      </div>
    </section>
  </div>
</template>

<style scoped>
.upload-section {
  margin-top: 26px;
}
.upload-section :deep(.el-upload-dragger) {
  padding: 30px;
  border-color: var(--border);
  border-radius: 8px;
  background: #fafbfd;
}
.upload-section :deep(.el-upload-dragger:hover) {
  border-color: var(--accent);
  background: #f7f9ff;
}
.upload-icon {
  color: var(--accent);
  font-size: 34px;
}
.upload-title {
  margin-top: 10px;
  color: var(--text);
  font-size: 15px;
  font-weight: 650;
}
.upload-hint {
  margin-top: 7px;
  color: var(--muted);
  font-size: 12px;
}
.upload-progress {
  width: min(360px, 80%);
  margin: 18px auto 0;
}
.result-count {
  color: var(--muted);
  font-size: 13px;
}
.hash {
  color: #34445a;
  font-size: 12px;
}
</style>
