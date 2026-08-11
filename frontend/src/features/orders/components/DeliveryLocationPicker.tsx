import { useEffect, useMemo, useState } from "react";
import { ExternalLink, LoaderCircle, LocateFixed, MapPin, Search } from "lucide-react";
import { MapContainer, Marker, TileLayer, useMap, useMapEvents } from "react-leaflet";
import type { LatLngExpression, LeafletMouseEvent } from "leaflet";

interface Props {
  address: string;
  latitude: number;
  longitude: number;
  onChange: (lat: number, lng: number) => void;
  onAddressChange: (address: string) => void;
  searchRequest?: number;
}

interface NominatimResult { lat: string; lon: string; display_name: string }

function ClickPicker({ onChange }: Pick<Props, "onChange">) {
  useMapEvents({ click(event: LeafletMouseEvent) { onChange(event.latlng.lat, event.latlng.lng); } });
  return null;
}

function RecenterMap({ position }: { position: LatLngExpression }) {
  const map = useMap();
  useEffect(() => { map.setView(position, 16); }, [map, position]);
  return null;
}

export default function DeliveryLocationPicker({ address, latitude, longitude, onChange, onAddressChange, searchRequest = 0 }: Props) {
  const position = useMemo<LatLngExpression>(() => [latitude, longitude], [latitude, longitude]);
  const [searching, setSearching] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const osmUrl = `https://www.openstreetmap.org/?mlat=${latitude}&mlon=${longitude}#map=17/${latitude}/${longitude}`;

  async function searchAddress() {
    const query = address.trim();
    if (!query) { setError("Escribe una dirección antes de buscarla."); return; }
    setSearching(true); setError(""); setMessage("");
    try {
      const params = new URLSearchParams({ q: `${query}, Perú`, format: "jsonv2", limit: "1", countrycodes: "pe", "accept-language": "es" });
      const response = await fetch(`https://nominatim.openstreetmap.org/search?${params.toString()}`, {
        headers: { Accept: "application/json" },
      });
      if (!response.ok) throw new Error("El servicio de ubicación no está disponible.");
      const results = await response.json() as NominatimResult[];
      if (!results.length) { setError("No encontramos esa dirección. Ajusta el texto o selecciona el punto en el mapa."); return; }
      const result = results[0];
      onChange(Number(result.lat), Number(result.lon));
      setMessage(`Ubicación encontrada: ${result.display_name}`);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "No se pudo buscar la dirección.");
    } finally { setSearching(false); }
  }

  useEffect(() => {
    if (searchRequest > 0) void searchAddress();
    // The parent increments this value only when Enter is pressed in the address field.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchRequest]);

  async function selectManually(lat: number, lng: number) {
    onChange(lat, lng);
    setError("");
    setSearching(true);
    setMessage("Buscando la dirección del punto seleccionado…");
    try {
      const params = new URLSearchParams({ lat: String(lat), lon: String(lng), format: "jsonv2", zoom: "18", "accept-language": "es" });
      const response = await fetch(`https://nominatim.openstreetmap.org/reverse?${params.toString()}`, {
        headers: { Accept: "application/json" },
      });
      if (!response.ok) throw new Error("No se pudo obtener la dirección del punto seleccionado.");
      const result = await response.json() as { display_name?: string };
      if (!result.display_name) throw new Error("El punto seleccionado no tiene una dirección identificable.");
      onAddressChange(result.display_name);
      setMessage(`Ubicación seleccionada: ${result.display_name}`);
    } catch (cause) {
      setMessage("Ubicación ajustada manualmente; conserva las coordenadas seleccionadas.");
      setError(cause instanceof Error ? cause.message : "No se pudo obtener la dirección.");
    } finally { setSearching(false); }
  }

  return <div className="space-y-2">
    <button type="button" onClick={() => void searchAddress()} disabled={searching}
      className="flex w-full items-center justify-center gap-2 rounded-lg border border-primary px-3 py-2 text-xs font-semibold text-primary hover:bg-primary/5 disabled:opacity-60">
      {searching ? <LoaderCircle className="h-4 w-4 animate-spin" /> : <Search className="h-4 w-4" />}
      {searching ? "Buscando ubicación…" : "Buscar dirección en el mapa"}
    </button>
    <div className="h-48 overflow-hidden rounded-xl border border-border">
      <MapContainer center={position} zoom={15} style={{ height: "100%", width: "100%" }}>
        <TileLayer attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://tile.openstreetmap.org/{z}/{x}/{y}.png" />
        <RecenterMap position={position} />
        <ClickPicker onChange={(lat, lng) => void selectManually(lat, lng)} />
        <Marker position={position} draggable eventHandlers={{
          dragend(event) { const point = event.target.getLatLng(); void selectManually(point.lat, point.lng); },
        }} />
      </MapContainer>
    </div>
    {error && <p className="text-xs text-red-600">{error}</p>}
    {message && <p className="line-clamp-2 text-xs text-emerald-700"><LocateFixed className="mr-1 inline h-3 w-3" />{message}</p>}
    <div className="flex items-center justify-between gap-3 text-[11px] text-muted-foreground">
      <span className="inline-flex items-center gap-1"><MapPin className="h-3 w-3" />Haz clic o arrastra el pin para corregir</span>
      <a href={osmUrl} target="_blank" rel="noreferrer" className="inline-flex items-center gap-1 text-primary hover:underline">
        Abrir mapa <ExternalLink className="h-3 w-3" />
      </a>
    </div>
  </div>;
}
