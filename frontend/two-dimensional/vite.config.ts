import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

import { createHtmlPlugin } from 'vite-plugin-html'
const path = require("path");
export default defineConfig(({ command, mode }) => {
    const env = loadEnv(mode, process.cwd(), '')
    return {
        // vite config
        define: {
            __APP_ENV__: env.APP_ENV,
           
        },
     
        plugins: [vue(),
        createHtmlPlugin({
            minify: true,
            pages: [
                {
                    entry: '/src/main.ts',
                    filename: 'index.html',
                    template: 'index.html',
                    injectOptions: {
                        data: {
                            title: env.VITE_APP_TITLE,
                            baseURL: env.VITE_APP_BASE_URL,
                            bootstrapIcons: env.VITE_APP_BOOTSTRAP_ICONS_CSS,
                            bootstrapBundle: env.VITE_APP_BOOTSTRAP_BUNDLE,
                            bootstrapCss: env.VITE_APP_BOOTSTRAP_CSS,
                            injectScript: `<script src="./inject.js"></script>`,
                        },
                    },
                },
            ]
        })],
        test: {
            globals: true,
            setupFiles: "src/setupTests.ts",
            includeSource: ["src/**/*.{js,ts,vue}"]
        },
        resolve: {
            alias: {
                "@": path.resolve(__dirname, "./src"),
            },
        },
    }
})