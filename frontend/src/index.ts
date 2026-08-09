import { Router } from "@lit-labs/router"
import { html, LitElement } from "lit"
import { customElement } from "lit/decorators.js"

import "./styles.css"

import "./output/socketProvider"
import "./output/canvas"
import "./output/debug"
import "./output/multiview"

import { ROUTES } from "./routes"

@customElement("playout-root")
class Root extends LitElement {
	private readonly router = new Router(this, ROUTES, {
		fallback: {
			render: () => html`<h1>Not Found</h1>`,
		},
	})

	render() {
		return this.router.outlet()
	}
}
