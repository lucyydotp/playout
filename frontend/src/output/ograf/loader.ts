interface OGrafManifest {
	id: string
	name: string
	main: string
}

function createElementName(ograf: OGrafManifest) {
	return `ograf-${ograf.id}`
}

/**
 * Fetches and loads an OGraf graphic into the browser's custom elements.
 * @param id the graphic's ID
 * @return the custom element the graphic was registered as
 */
async function loadOGraf(id: string): Promise<string> {
	const data = await fetch(`/api/content/ograf/${id}`)
	if (!data.ok)
		throw new Error(`Failed to load OGraf graphic ${id}: ${data.statusText}`)

	const json: OGrafManifest = await data.json()
	console.log(`Loading OGraf ${json.name} (${json.id})`)

	const { default: graphic } = await import(
		/* @vite-ignore */
		`/api/content/ograf/${id}/${json.main}`
	)
	if (graphic == null)
		throw new Error(`OGraf graphic ${id} has no default export`)
	if (!(graphic.prototype instanceof HTMLElement))
		throw new Error(`OGraf graphic ${id} does not extend HTMLElement`)

	const name = createElementName(json)
	customElements.define(name, graphic)
	return name
}

/**
 * A map of loaded graphics as graphic ID to promise of custom element name.
 */
const graphics = new Map<string, Promise<string>>()

/**
 * Loads an OGraf graphic by ID, returning its custom element name.
 */
export async function loadOGrafGraphic(id: string): Promise<string> {
	const existing = graphics.get(id)
	if (existing) return existing

	const graphic = loadOGraf(id)
	graphics.set(id, graphic)
	return graphic
}
