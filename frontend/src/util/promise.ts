/**
 * Creates a promise with explicit handles to resolve and reject.
 */
export function promiseWithResolvers() {
	let resolve!: () => void
	let reject!: () => void
	const promise = new Promise<void>((res, rej) => {
		resolve = res
		reject = rej
	})
	return { promise, resolve, reject }
}
