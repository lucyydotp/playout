import { consume } from "@lit/context"
import { css, html, LitElement } from "lit"
import { customElement, property } from "lit/decorators.js"
import type { OutputState } from "./data"
import { socketContext } from "./socketProvider"

@customElement("playout-debug-controls")
export class OutputDebugControls extends LitElement {
	static override styles = css`
        * { 
            font-family: sans-serif;
            box-sizing: border-box;
        }
        :host {
            box-sizing: border-box;
            display: block;
            height: fit-content;
            width: 100%;
            min-height: 100vh;
            background: #767676;
            color: #eee;
            padding: 0.05px;
        }

        playout-canvas {
            background: white;
            border: 2px solid black;
            position: initial;
            margin: 1em auto;
            color: initial;
            overflow: clip;

            /* Force a new fixed-positioning containing block, so fixed-position graphics are placed correctly */
            transform: scale(1); 
        }
    `

	@consume({ context: socketContext, subscribe: true })
	@property({ attribute: false })
	private state: OutputState | undefined

	override render() {
		return html`
            <playout-canvas></playout-canvas>
            <pre><code>${JSON.stringify(this.state, null, 4)}</code></pre>
        `
	}
}
