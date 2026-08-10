export const APP_NAME = "FleetBite";

/** Local de operación (demo) — Av. Manuel Olguín 561, Santiago de Surco. */
export const RESTAURANT = {
  name: "FleetBite — Local Surco",
  address: "Av. Manuel Olguín 561, Santiago de Surco 15023",
  lat: -12.0875,
  lng: -76.9734,
  mapsUrl: "https://maps.app.goo.gl/SvkBek3TqQeyicpx6",
} as const;

/** Coordenadas aproximadas por dirección de entrega (demo Lima). */
export const DELIVERY_COORDS: Record<string, [number, number]> = {
  "Av. Larco 1150, Miraflores": [-12.1215, -77.0298],
  "Jr. Moquegua 340, Cercado": [-12.0464, -77.0428],
  "Calle Las Flores 220, San Isidro": [-12.0965, -77.0352],
  "Av. Benavides 5200, Surco": [-12.1328, -76.9945],
  "Calle Roma 348, Miraflores": [-12.1188, -77.0315],
  "Av. Universitaria 1900, San Miguel": [-12.0772, -77.0901],
  "Jr. Callao 1200, Breña": [-12.0589, -77.0502],
  "Av. Grau 780, Barranco": [-12.1456, -77.0212],
};

export function coordsForAddress(address: string): [number, number] {
  return DELIVERY_COORDS[address] ?? [RESTAURANT.lat - 0.02, RESTAURANT.lng - 0.01];
}

/** Distancia en km (Haversine). */
export function haversineKm(
  a: { lat: number; lng: number },
  b: { lat: number; lng: number },
): number {
  const R = 6371;
  const dLat = ((b.lat - a.lat) * Math.PI) / 180;
  const dLng = ((b.lng - a.lng) * Math.PI) / 180;
  const lat1 = (a.lat * Math.PI) / 180;
  const lat2 = (b.lat * Math.PI) / 180;
  const h =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLng / 2) ** 2;
  return R * 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
}

export function googleDirectionsUrl(destination: string): string {
  const origin = encodeURIComponent(RESTAURANT.address);
  const dest = encodeURIComponent(destination);
  return `https://www.google.com/maps/dir/?api=1&origin=${origin}&destination=${dest}&travelmode=driving`;
}

/** Abrir un destino en Google Maps (modo directions desde ubicación actual). */
export function googleMapsNavigateUrl(destination: string): string {
  return `https://www.google.com/maps/dir/?api=1&destination=${encodeURIComponent(destination)}&travelmode=driving`;
}
