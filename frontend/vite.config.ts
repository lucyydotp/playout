import { defineConfig } from "vite"

export default defineConfig({
	root: "src",
	build: {
		rolldownOptions: {
			output: {
				dir: "build/dist",
                cleanDir: true,
			},
		},
	},
	server: {
		proxy: {
            "/api": {
                target: "http://localhost:8080",
                ws: true,
                changeOrigin: true,
            },
		},
	},
})
