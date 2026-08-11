import { useCallback, useEffect, useRef, useState } from "react";
import type { DriverAssignment } from "@/shared/types";
import { acceptMyAssignment, completeMyAssignment, getMyActiveAssignment,
  pickupMyAssignment, rejectMyAssignment, startMyDelivery } from "@/features/drivers/services/driverApi";

const SEEN_KEY = "fleetbite:driver:seen-assignment-ids";
const POLL_MS = 8_000;

function readSeenIds(): Set<string> {
  try {
    const raw = sessionStorage.getItem(SEEN_KEY);
    if (!raw) return new Set();
    const parsed = JSON.parse(raw) as string[];
    return new Set(Array.isArray(parsed) ? parsed : []);
  } catch {
    return new Set();
  }
}

function writeSeenIds(ids: Set<string>) {
  sessionStorage.setItem(SEEN_KEY, JSON.stringify([...ids]));
}

/** Beep corto sin asset externo (Web Audio). */
export function playAssignmentChime() {
  try {
    const Ctx = window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
    const ctx = new Ctx();
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.type = "sine";
    osc.frequency.value = 880;
    gain.gain.value = 0.04;
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.start();
    gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.35);
    osc.stop(ctx.currentTime + 0.35);
  } catch {
    /* silenciosos si el browser bloquea audio */
  }
}

export function useDriverAssignmentPoll(driverId: string | undefined) {
  const [assignment, setAssignment] = useState<DriverAssignment | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [offerOpen, setOfferOpen] = useState(false);
  const [unreadPending, setUnreadPending] = useState(false);
  const seenRef = useRef<Set<string>>(readSeenIds());

  const refresh = useCallback(async () => {
    if (!driverId) {
      setAssignment(null);
      setLoading(false);
      return;
    }
    try {
      const active = await getMyActiveAssignment();
      setAssignment(active);
      setError("");

      if (active?.status === "PENDING" && !seenRef.current.has(active.id)) {
        seenRef.current.add(active.id);
        writeSeenIds(seenRef.current);
        setUnreadPending(true);
        setOfferOpen(true);
        playAssignmentChime();
      }
      if (!active || active.status !== "PENDING") {
        setUnreadPending(false);
      }
    } catch {
      setError("No se pudo consultar la asignación activa");
    } finally {
      setLoading(false);
    }
  }, [driverId]);

  useEffect(() => {
    setLoading(true);
    void refresh();
    if (!driverId) return;
    const id = window.setInterval(() => void refresh(), POLL_MS);
    return () => window.clearInterval(id);
  }, [driverId, refresh]);

  async function accept() {
    if (!driverId || !assignment || assignment.status !== "PENDING") return;
    await acceptMyAssignment(assignment.id);
    await refresh();
    setOfferOpen(false);
    setUnreadPending(false);
  }

  async function reject(reason = "Rechazado por el driver") {
    if (!driverId || !assignment || assignment.status !== "PENDING") return;
    await rejectMyAssignment(assignment.id, reason);
    setAssignment(null);
    setOfferOpen(false);
    setUnreadPending(false);
  }

  async function advance(
    next: "PICKED_UP" | "IN_TRANSIT" | "COMPLETED" | "FAILED",
  ) {
    if (!driverId || !assignment) return;
    if (next === "FAILED") throw new Error("El backend todavía no admite reportar entrega fallida");
    if (next === "PICKED_UP") await pickupMyAssignment(assignment.id);
    if (next === "IN_TRANSIT") await startMyDelivery(assignment.id);
    if (next === "COMPLETED") await completeMyAssignment(assignment.id);
    await refresh();
  }

  function openOffer() {
    if (assignment?.status === "PENDING") {
      setOfferOpen(true);
      setUnreadPending(false);
    }
  }

  function closeOffer() {
    setOfferOpen(false);
  }

  return {
    assignment,
    loading,
    error,
    offerOpen,
    unreadPending,
    refresh,
    accept,
    reject,
    advance,
    openOffer,
    closeOffer,
    setAssignment,
  };
}
