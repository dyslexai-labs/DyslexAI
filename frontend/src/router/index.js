import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'home', component: { template: '<div />' } },
  { path: '/imagem/origem', name: 'select-image-source', component: { template: '<div />' } },
  { path: '/imagem/confirmar', name: 'confirm-image', component: { template: '<div />' } },
  { path: '/fala/preparar', name: 'confirm-audio', component: { template: '<div />' } },
  { path: '/processamento', name: 'processing', component: { template: '<div />' } },
  { path: '/leitor', name: 'reader', component: { template: '<div />' } },
]

export const routeScreenNames = routes.map(route => route.name)

export const router = createRouter({
  history: createWebHashHistory(),
  routes,
})
