import { useCallback, useEffect, useRef, useState } from "react";
import type { DriverAssignment } from "@/shared/types";
import {
  acceptAssignment,
  advanceAssignment,
  fetchActiveAssignment,
  rejectAssignment,
} from "@/services/api/mocks/driverAssignments";

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
      const active = await fetchActiveAssignment(driverId);
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
    const updated = await acceptAssignment(driverId, assignment.id);
    setAssignment(updated);
    setOfferOpen(false);
    setUnreadPending(false);
  }

  async function reject() {
    if (!driverId || !assignment || assignment.status !== "PENDING") return;
    await rejectAssignment(driverId, assignment.id);
    setAssignment(null);
    setOfferOpen(false);
    setUnreadPending(false);
  }

  async function advance(
    next: "PICKED_UP" | "IN_TRANSIT" | "COMPLETED" | "FAILED",
  ) {
    if (!driverId || !assignment) return;
    if (assignment.driverId !== driverId) {
      throw new Error("No puedes operar asignaciones de otro motorizado");
    }
    const updated = await advanceAssignment(driverId, next);
    setAssignment(updated);
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
