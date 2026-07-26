import { consume } from "@lit/context"
import { css, html, LitElement, nothing } from "lit"
import { customElement } from "lit/decorators.js"
import { repeat } from "lit/directives/repeat.js"
import { styleMap } from "lit/directives/style-map.js"
import type { OutputState } from "./data"
import { socketContext } from "./socketProvider"

import "./ograf/layer"

/**
 * Renders content to the DOM.
 */
@customElement("playout-canvas")
export class Canvas extends LitElement {
	static override styles = css`
        * {
            box-sizing: border-box;
        }

        :host {
            display: block;
            position: absolute;
            top: 0;
            left: 0;
            width: fit-content;
            height: fit-content;
        }
    `

	@consume({ context: socketContext, subscribe: true })
	private state: OutputState | undefined

	override render() {
		if (!this.state) return nothing

		const [width, height] = this.state.config.resolution
			.split("x")
			.map((it) => parseInt(it))

		return html`
            <div style=${styleMap({
							width: `${width}px`,
							height: `${height}px`,
						})}>
                ${repeat(
									Object.entries(this.state.layers),
									([, layer]) => layer.id,
									([, layer]) => {
										const [type, info] = layer.content.split(":")
										switch (type) {
											case "ograf":
												return html`
                                        <playout-layer-ograf 
                                                .layerId=${layer.id}
                                                .graphicId=${info}
                                                .templateData=${layer.templateData}
                                                .isPlaying=${layer.isPlaying}
                                        ></playout-layer-ograf>`
										}
									},
								)}
            </div>
        `
	}
}
