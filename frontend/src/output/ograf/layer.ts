import { customElement } from "lit/decorators.js"
import type { Graphic } from "ograf/dist/apis/graphicsAPI"
import { loadOGrafGraphic } from "./loader"
import { promiseWithResolvers } from "../../util/promise"
import { doubleRAF } from "../../util/raf"

type GraphicElement = Graphic & HTMLElement

/**
 * Renders an OGraf graphic as a layer.
 */
@customElement("playout-layer-ograf")
class OGrafLayer extends HTMLElement {
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

	/** Whether the layer is currently playing. */
	#isPlaying: boolean = false

	get isPlaying() {
		return this.#isPlaying ?? false
	}

	set isPlaying(value) {
		this.queue = this.queue.then(async () => {
			if (this.#isPlaying === value) return
			this.#isPlaying = value

			if (value) {
				await this.element?.playAction({})
			} else {
				await this.element?.stopAction({})
			}
		})
	}

	private element: GraphicElement | undefined
	private setupPromise = promiseWithResolvers()
	private queue: Promise<void> = this.setupPromise.promise

	async connectedCallback() {
		const elementId = await loadOGrafGraphic(this.graphicId)
		this.element = document.createElement(elementId) as GraphicElement
		this.appendChild(this.element)

		await this.element.load({
			data: this.templateData,
			renderType: "realtime",
			renderCharacteristics: {},
		})

		// FIXME: something weird is going on here timing-wise,
		//  possibly an interaction between this element and the graphic itself?
		await doubleRAF()
		await doubleRAF()
		this.setupPromise.resolve()
	}

	disconnectedCallback() {
		this.element?.remove()
		this.element = undefined
	}
}
