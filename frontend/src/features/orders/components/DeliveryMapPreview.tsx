import { useEffect, useMemo } from "react";
import { MapContainer, TileLayer, Marker, Popup, Polyline, useMap } from "react-leaflet";
import L from "leaflet";
import { Bike, ExternalLink, MapPin, Store } from "lucide-react";
import {
  RESTAURANT,
  coordsForAddress,
  haversineKm,
  googleDirectionsUrl,
} from "@/shared/constants";
import type { OrderStatus } from "@/shared/types";

delete (L.Icon.Default.prototype as unknown as { _getIconUrl?: unknown })._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png",
  iconUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png",
  shadowUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",
});

const SVG_STORE = `<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.25" stroke-linecap="round" stroke-linejoin="round"><path d="m2 7 4.41-4.41A2 2 0 0 1 7.83 2h8.34a2 2 0 0 1 1.42.59L22 7"/><path d="M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8"/><path d="M15 22v-4a2 2 0 0 0-2-2h-2a2 2 0 0 0-2 2v4"/><path d="M2 7h20"/><path d="M22 7v3a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V7z"/></svg>`;

const SVG_PIN = `<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.25" stroke-linecap="round" stroke-linejoin="round"><path d="M20 10c0 4.993-5.539 10.193-7.399 11.799a1 1 0 0 1-1.202 0C9.539 20.193 4 14.993 4 10a8 8 0 0 1 16 0"/><circle cx="12" cy="10" r="3"/></svg>`;

const SVG_BIKE = `<svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.25" stroke-linecap="round" stroke-linejoin="round"><circle cx="18.5" cy="17.5" r="3.5"/><circle cx="5.5" cy="17.5" r="3.5"/><circle cx="15" cy="5" r="1"/><path d="M12 17.5V14l-3-3 4-3 2 3h2"/></svg>`;

function markerIcon(htmlInner: string, bg: string, shape: "square" | "circle" | "pill"): L.DivIcon {
  const radius = shape === "square" ? "8px" : shape === "pill" ? "9999px" : "9999px";
  const size = shape === "pill" ? 34 : 30;
  return L.divIcon({
    html: `<div style="
      width:${size}px;height:${size}px;background:${bg};border:2px solid #fff;
      border-radius:${radius};display:flex;align-items:center;justify-content:center;
      box-shadow:0 2px 8px rgba(0,0,0,.28);">${htmlInner}</div>`,
    className: "",
    iconSize: [size, size],
    iconAnchor: [size / 2, size / 2],
    popupAnchor: [0, -(size / 2 + 4)],
  });
}

const restaurantIcon = markerIcon(SVG_STORE, "#d97706", "square");
const deliveryIcon = markerIcon(SVG_PIN, "#111827", "circle");
const driverIcon = markerIcon(SVG_BIKE, "#1d4ed8", "pill");

/** Progreso mock del repartidor sobre la línea local → entrega. */
function driverProgress(status: OrderStatus): number | null {
  if (status === "ASSIGNED") return 0.08;
  if (status === "PICKED_UP") return 0.22;
  if (status === "IN_TRANSIT") return 0.58;
  return null;
}

function lerpCoord(a: [number, number], b: [number, number], t: number): [number, number] {
  return [a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t];
}

function FitBounds({ points }: { points: [number, number][] }) {
  const map = useMap();
  useEffect(() => {
    if (points.length < 2) return;
    map.fitBounds(L.latLngBounds(points), { padding: [40, 40], maxZoom: 14 });
  }, [map, points]);
  return null;
}

interface DeliveryMapPreviewProps {
  deliveryAddress: string;
  status?: OrderStatus;
  driverName?: string;
  /** Rellena la celda del bento (altura flexible). */
  fill?: boolean;
  className?: string;
}

export default function DeliveryMapPreview({
  deliveryAddress,
  status,
  driverName,
  fill = false,
  className = "",
}: DeliveryMapPreviewProps) {
  const delivery = useMemo(() => coordsForAddress(deliveryAddress), [deliveryAddress]);
  const restaurant: [number, number] = [RESTAURANT.lat, RESTAURANT.lng];
  const distanceKm = haversineKm(
    { lat: RESTAURANT.lat, lng: RESTAURANT.lng },
    { lat: delivery[0], lng: delivery[1] },
  );
  const etaMin = Math.max(8, Math.round((distanceKm / 22) * 60));
  const directionsUrl = googleDirectionsUrl(deliveryAddress);

  const progress = status ? driverProgress(status) : null;
  const showDriver = progress !== null && Boolean(driverName);
  const driverPos = useMemo((): [number, number] | null => {
    if (!showDriver || progress === null) return null;
    return lerpCoord([RESTAURANT.lat, RESTAURANT.lng], delivery, progress);
  }, [showDriver, progress, delivery]);

  const fitPoints = useMemo((): [number, number][] => {
    const pts: [number, number][] = [[RESTAURANT.lat, RESTAURANT.lng], delivery];
    if (driverPos) pts.push(driverPos);
    return pts;
  }, [delivery, driverPos]);

  return (
    <div
      className={`rounded-xl border overflow-hidden flex flex-col min-h-0 bg-card border-border ${
        fill ? "h-full" : ""
      } ${className}`}
    >
      <div className="flex items-center justify-between gap-3 px-4 py-3 border-b border-border shrink-0">
        <div className="min-w-0">
          <div className="text-[10px] font-mono tracking-widest text-muted-foreground">
            RUTA AL CLIENTE
          </div>
          <div className="text-xs mt-0.5 truncate text-foreground">
            Local → {deliveryAddress}
          </div>
        </div>
        <div className="text-right shrink-0">
          <div className="text-lg sm:text-xl font-bold font-mono tabular-nums leading-none text-primary">
            {distanceKm.toFixed(1)} km
          </div>
          <div className="text-[10px] font-mono mt-0.5 text-muted-foreground">
            ~{etaMin} min en moto
          </div>
        </div>
      </div>

      <div
        className={`relative w-full bg-muted min-h-[180px] ${
          fill ? "flex-1" : "h-[180px] sm:h-[220px]"
        }`}
      >
        <MapContainer
          center={restaurant}
          zoom={13}
          style={{ height: "100%", width: "100%", minHeight: 180 }}
          zoomControl={false}
          attributionControl={false}
        >
          <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
          <FitBounds points={fitPoints} />
          <Polyline
            positions={[restaurant, delivery]}
            pathOptions={{ color: "#d97706", weight: 3, dashArray: "6 6", opacity: 0.85 }}
          />
          <Marker position={restaurant} icon={restaurantIcon}>
            <Popup>
              <div className="text-xs">
                <strong>Local</strong>
                <br />
                {RESTAURANT.address}
              </div>
            </Popup>
          </Marker>
          <Marker position={delivery} icon={deliveryIcon}>
            <Popup>
              <div className="text-xs">
                <strong>Entrega</strong>
                <br />
                {deliveryAddress}
              </div>
            </Popup>
          </Marker>
          {driverPos && (
            <Marker position={driverPos} icon={driverIcon} zIndexOffset={400}>
              <Popup>
                <div className="text-xs">
                  <strong>Repartidor</strong>
                  <br />
                  {driverName}
                </div>
              </Popup>
            </Marker>
          )}
        </MapContainer>
      </div>

      <div className="px-4 py-3 space-y-2.5 shrink-0">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
          <div className="flex items-start gap-2 text-xs min-w-0">
            <Store className="w-3.5 h-3.5 mt-0.5 shrink-0 text-primary" />
            <div className="min-w-0">
              <div className="text-[10px] font-mono tracking-widest text-muted-foreground">Local</div>
              <div className="text-foreground leading-snug line-clamp-2">{RESTAURANT.address}</div>
            </div>
          </div>
          <div className="flex items-start gap-2 text-xs min-w-0">
            <MapPin className="w-3.5 h-3.5 mt-0.5 shrink-0 text-foreground" />
            <div className="min-w-0">
              <div className="text-[10px] font-mono tracking-widest text-muted-foreground">
                Entrega
              </div>
              <div className="text-foreground leading-snug line-clamp-2">{deliveryAddress}</div>
            </div>
          </div>
        </div>

        {showDriver && (
          <div className="flex items-center gap-2 text-xs rounded-lg px-2.5 py-2 bg-blue-50 border border-blue-100">
            <Bike className="w-3.5 h-3.5 shrink-0 text-blue-700" />
            <span className="text-blue-900 min-w-0 truncate">
              <span className="font-medium">{driverName}</span>
              <span className="text-blue-700/80"> · en ruta hacia el cliente</span>
            </span>
          </div>
        )}

        <a
          href={directionsUrl}
          target="_blank"
          rel="noopener noreferrer"
          className="w-full inline-flex items-center justify-center gap-1.5 py-2 rounded-lg border text-xs font-medium cursor-pointer border-primary/35 text-primary bg-primary/5 hover:bg-primary/10"
        >
          Abrir ruta en Google Maps
          <ExternalLink className="w-3.5 h-3.5" />
        </a>
      </div>
    </div>
  );
}
