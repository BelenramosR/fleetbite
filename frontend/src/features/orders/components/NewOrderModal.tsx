import { useState } from "react";
import { X } from "lucide-react";
import { createOrder } from "@/features/orders/services/orderApi";
import DeliveryLocationPicker from "@/features/orders/components/DeliveryLocationPicker";
import { RESTAURANT } from "@/shared/constants";

interface NewOrderModalProps { onClose: () => void; onCreated: () => void }

export default function NewOrderModal({ onClose, onCreated }: NewOrderModalProps) {
  const [customerName, setCustomerName] = useState("");
  const [customerPhone, setCustomerPhone] = useState("");
  const [deliveryAddress, setDeliveryAddress] = useState("");
  const [totalAmount, setTotalAmount] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [latitude, setLatitude] = useState(RESTAURANT.lat);
  const [longitude, setLongitude] = useState(RESTAURANT.lng);
  const [mapSearchRequest, setMapSearchRequest] = useState(0);

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    if (!customerName.trim() || !customerPhone.trim() || !deliveryAddress.trim() || !totalAmount) {
      setError("Completa todos los campos."); return;
    }
    setBusy(true); setError("");
    try {
      await createOrder({ customerName, customerPhone, deliveryAddress,
        deliveryLatitude: latitude, deliveryLongitude: longitude, totalAmount: Number(totalAmount) });
      onCreated();
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "No se pudo crear el pedido.");
    } finally { setBusy(false); }
  }

  const inputClass = "w-full px-3 py-2 rounded border text-sm outline-none bg-muted border-border text-foreground";
  const labelClass = "text-[10px] font-mono tracking-widest text-muted-foreground";
  return <div className="fixed inset-0 flex items-center justify-center z-40" style={{ background: "rgba(0 0 0 / 0.6)" }} onClick={onClose}>
    <form className="rounded-lg border p-6 w-full max-w-xl max-h-[95vh] overflow-y-auto space-y-4 bg-card border-border" onSubmit={submit} onClick={(e) => e.stopPropagation()}>
      <div className="flex items-center justify-between"><h3 className="text-sm font-semibold">Nuevo pedido</h3>
        <button type="button" onClick={onClose} className="cursor-pointer p-0.5" aria-label="Cerrar"><X className="w-4 h-4" /></button></div>
      <div className="space-y-1.5"><label htmlFor="customer-name" className={labelClass}>NOMBRE DEL CLIENTE</label>
        <input id="customer-name" value={customerName} onChange={(e) => setCustomerName(e.target.value)} placeholder="Valentina Morales" className={inputClass} /></div>
      <div className="space-y-1.5"><label htmlFor="customer-phone" className={labelClass}>TELÉFONO</label>
        <input id="customer-phone" type="tel" value={customerPhone} onChange={(e) => setCustomerPhone(e.target.value)} placeholder="+51 999 000 111" className={inputClass} /></div>
      <div className="space-y-1.5"><label htmlFor="delivery-address" className={labelClass}>DIRECCIÓN DE ENTREGA</label>
        <input id="delivery-address" value={deliveryAddress} onChange={(e) => setDeliveryAddress(e.target.value)}
          onKeyDown={(event) => { if (event.key === "Enter") { event.preventDefault(); setMapSearchRequest((value) => value + 1); } }}
          placeholder="Av. Larco 1150, Miraflores" className={inputClass} /></div>
      <DeliveryLocationPicker address={deliveryAddress} latitude={latitude} longitude={longitude} searchRequest={mapSearchRequest}
        onAddressChange={setDeliveryAddress}
        onChange={(lat, lng) => { setLatitude(lat); setLongitude(lng); }} />
      <div className="space-y-1.5"><label htmlFor="total-amount" className={labelClass}>MONTO TOTAL (S/)</label>
        <input id="total-amount" type="number" min="0" step="0.01" value={totalAmount} onChange={(e) => setTotalAmount(e.target.value)} placeholder="45.00" className={inputClass} /></div>
      {error && <p className="rounded-lg bg-red-50 px-3 py-2 text-xs text-red-700">{error}</p>}
      <div className="flex gap-2 pt-2">
        <button type="button" onClick={onClose} className="flex-1 py-2 rounded border text-sm cursor-pointer border-border">Cancelar</button>
        <button type="submit" disabled={busy} className="flex-1 py-2 rounded text-sm font-semibold cursor-pointer bg-primary text-primary-foreground disabled:opacity-60">{busy ? "Creando…" : "Crear pedido"}</button>
      </div>
    </form>
  </div>;
}
