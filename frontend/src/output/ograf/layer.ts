import { consume } from "@lit/context"
import { LitElement, nothing } from "lit"
import { customElement, state } from "lit/decorators.js"
import type { Graphic } from "ograf/dist/apis/graphicsAPI"
import type { OutputState } from "../data"
import { socketContext } from "../socketProvider"
import { loadOGrafGraphic } from "./loader"

type GraphicElement = Graphic & HTMLElement

/**
 * Renders an OGraf graphic as a layer.
 */
@customElement("playout-layer-ograf")
class OGrafLayer extends LitElement {
	/** The layer's ID. */
	layerId!: string
	/** The OGraf graphic's ID. */
	graphicId!: string

	#templateData: object | undefined

	get templateData() {
		return this.#templateData
	}

	set templateData(value) {
		this.#templateData = value
		this.element?.updateAction({ data: value })
	}

	// OGraf elements have their own shadow DOM, no need to create a second one
	override readonly renderRoot = this

	@consume({ context: socketContext })
	private state: OutputState | undefined

	@state()
	private element?: GraphicElement
	private queue: Promise<void> = Promise.resolve()

	override connectedCallback() {
		super.connectedCallback()
		this.queue = this.queue.then(async () => {
			console.log("connecting ograf layer", this.layerId, this.graphicId)
			const elementId = await loadOGrafGraphic(this.graphicId)
			this.element = document.createElement(elementId) as GraphicElement

			await this.element.load({
				data: this.templateData,
				renderType: "realtime",
				renderCharacteristics: {},
			})
			// TODO: for testing only, remove this
			setTimeout(() => this.element?.playAction?.({}), 100)
		})
	}

	override disconnectedCallback() {
		super.disconnectedCallback()
		this.element?.remove()
	}

	override render() {
		return this.element ?? nothing
	}
}
