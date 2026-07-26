import { createContext, provide } from "@lit/context"
import { LitElement } from "lit"
import { customElement, property } from "lit/decorators.js"
import type { OutputState } from "./data"

export const socketContext = createContext<OutputState | undefined>("state")

/**
 * Manages a socket connection for a specific output.
 * Provides data on {@link socketContext}.
 */
@customElement("playout-socket")
export class OutputSocketProvider extends LitElement {
	@property({ type: String })
	public channel!: string

	@provide({ context: socketContext })
	private state: OutputState | undefined

	private socket: WebSocket | undefined

	private connect() {
		if (this.socket?.readyState === WebSocket.OPEN) return
		this.socket = new WebSocket(
			`ws://${window.location.host}/api/output/${this.channel}/watch`,
		)
		this.socket.onopen = () => {}
		this.socket.onmessage = (event) => {
			this.state = JSON.parse(event.data)
		}
		this.socket.onerror = () => {
			// TODO: log
		}
		this.socket.onclose = () => {
			this.socket = undefined
			setTimeout(() => this.connect(), 1000)
		}
	}

	// We don't render anything, just direct children, which all have their own shadow DOM.
	override readonly renderRoot = this

	override connectedCallback() {
		super.connectedCallback()
		this.connect()
	}

	override disconnectedCallback() {
		super.disconnectedCallback()
		this.socket?.close()
		this.socket = undefined
	}
}
