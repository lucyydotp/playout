/**
 * Returns a new promise that resolves after the next animation frame.
 */
export function rAF(): Promise<void> {
	return new Promise((r) => requestAnimationFrame(() => r()))
}

/**
 * Waits for two animation frames.
 */
export async function doubleRAF() {
	await rAF()
	await rAF()
}
