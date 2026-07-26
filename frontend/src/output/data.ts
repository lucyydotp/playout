export interface OutputState {
	config: {
		resolution: `${number}x${number}`
	}
	layers: Record<number, LayerState>
}

export interface LayerState {
	id: string
	isPlaying: boolean
	content: string
	templateData: object
}
