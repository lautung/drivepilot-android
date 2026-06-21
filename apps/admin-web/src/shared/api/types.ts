import type { components, operations } from './schema'

export type AdminSessionResponse = components['schemas']['AdminSessionResponse']
export type Content = components['schemas']['ContentView']
export type ContentRequest = components['schemas']['ContentRequest']
export type ContentPage = components['schemas']['PageResponseContentView']
export type Media = components['schemas']['MediaResponse']
export type MediaPage = components['schemas']['PageResponseMediaResponse']
export type UserView = components['schemas']['UserView']
export type ContentFilters = NonNullable<operations['list_1']['parameters']['query']>
export type ContentCategory = NonNullable<Content['category']>
export type ContentStatus = NonNullable<Content['status']>
export type UserRole = NonNullable<UserView['role']>

export interface FieldError {
  field: string
  message: string
}

export interface ApiProblemShape {
  status?: number
  code?: string
  detail?: string
  title?: string
  fieldErrors?: readonly FieldError[]
}
