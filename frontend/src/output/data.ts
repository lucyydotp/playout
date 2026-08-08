export interface OutputState {
	config: {
		resolution: `${number}x${number}`
	}
	layers: Record<number, LayerState>
}

export interface LayerState {
	id: string
	currentStep: number
	content: string
	templateData: object
}
