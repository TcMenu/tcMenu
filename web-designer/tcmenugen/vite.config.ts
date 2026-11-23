import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';

export default defineConfig({
    plugins: [react()],
    server: {
        port: 3000,
        open: true,
        proxy: {
            '/api': 'http://localhost:8080',
        },
    },
    build: {
        outDir: 'build', // Keeps CRA's output folder name so deployment pipelines don't break
    },
    test: {
        globals: true,
        environment: 'jsdom',
        setupFiles: './src/setupTests.ts', // Only include if you have a setupTests file
    },
});
