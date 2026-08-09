import type { RouteConfig } from "@lit-labs/router"
import { html } from "lit"

export const ROUTES: RouteConfig[] = [
	{
		path: "/render/debug/:channel",
		render: ({ channel }) => html`
            <playout-socket channel=${channel}>
                <playout-debug-controls></playout-debug-controls>
            </playout-socket>`,
	},
	{
		path: "/render/:channel",
		render: ({ channel }) =>
			html`
                <playout-socket channel=${channel}>
                    <playout-canvas></playout-canvas>
                </playout-socket>`,
	},
]
