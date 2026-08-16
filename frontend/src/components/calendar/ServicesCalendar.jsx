import React, { useState, useEffect } from 'react';
import { format, startOfWeek, addDays, addWeeks, subWeeks } from 'date-fns';
import {
    ChevronLeft,
    ChevronRight,
    Plus,
    User,
    Filter,
    Loader2,
    X,
    Trash2,
    Calendar as CalendarIcon
} from 'lucide-react';
import apiClient from '../../api/client';

export default function ServicesCalendar({ onOpenBooking }) {
    const [currentWeekStart, setCurrentWeekStart] = useState(() =>
        startOfWeek(new Date(), { weekStartsOn: 1 })
    );
    const [bookings, setBookings] = useState([]);
    const [services, setServices] = useState([]);
    const [staffList, setStaffList] = useState([]);
    const [loading, setLoading] = useState(false);

    // Filters (Section 3.1)
    const [selectedStaffFilter, setSelectedStaffFilter] = useState('');
    const [selectedServiceFilter, setSelectedServiceFilter] = useState('');

    const [selectedBooking, setSelectedBooking] = useState(null);
    const [cancelling, setCancelling] = useState(false);

    const weekDays = [...Array(7)].map((_, i) => addDays(currentWeekStart, i));
    const timeSlots = [
        '09:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00'
    ];

    useEffect(() => {
        apiClient.get('/services').then((res) => setServices(res.data || []));
        apiClient.get('/staff').then((res) => setStaffList(res.data || []));
    }, []);

    const fetchBookings = async () => {
        setLoading(true);
        try {
            const from = new Date(currentWeekStart);
            from.setHours(0, 0, 0, 0);

            const to = addDays(currentWeekStart, 7);
            to.setHours(23, 59, 59, 999);

            const res = await apiClient.get(
                `/bookings?from=${from.toISOString()}&to=${to.toISOString()}`
            );
            setBookings(res.data || []);
        } catch (err) {
            console.error('Failed to load bookings:', err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchBookings();
        const handler = () => fetchBookings();
        window.addEventListener('refresh_bookings', handler);
        return () => window.removeEventListener('refresh_bookings', handler);
    }, [currentWeekStart]);

    const filteredBookings = bookings.filter((b) => {
        if (selectedStaffFilter && b.staffName !== selectedStaffFilter) return false;
        if (selectedServiceFilter && b.serviceName !== selectedServiceFilter) return false;
        return true;
    });

    const getBookingForSlot = (day, timeStr) => {
        const [hours, minutes] = timeStr.split(':').map(Number);
        const slotStart = new Date(day);
        slotStart.setHours(hours, minutes, 0, 0);

        const slotEnd = new Date(slotStart);
        slotEnd.setHours(hours + 1, minutes, 0, 0);

        const booking = filteredBookings.find((b) => {
            if (b.status !== 'CONFIRMED') return false;
            const bStart = new Date(b.startAt);
            const bEnd = new Date(b.endAt);
            return bStart < slotEnd && bEnd > slotStart;
        });

        if (!booking) return null;

        const bStart = new Date(booking.startAt);
        const isStartSlot = bStart >= slotStart && bStart < slotEnd;

        return { booking, isStartSlot };
    };

    const handleCancelBooking = async (bookingId) => {
        setCancelling(true);
        try {
            await apiClient.post(`/bookings/${bookingId}/cancel`);
            setSelectedBooking(null);
            fetchBookings();
        } catch (err) {
            alert(err.response?.data?.message || 'Failed to cancel');
        } finally {
            setCancelling(false);
        }
    };

    return (
        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
            {/* Calendar Header with Navigation and Filters */}
            <div className="p-4 sm:p-5 border-b border-slate-200 flex flex-wrap items-center justify-between gap-4">
                <div className="flex flex-wrap items-center gap-3">
                    <h2 className="text-base font-bold text-slate-800">
                        {format(currentWeekStart, 'MMMM yyyy')}
                    </h2>
                    <div className="flex items-center border border-slate-300 rounded-xl overflow-hidden bg-slate-50">
                        <button
                            onClick={() => setCurrentWeekStart(subWeeks(currentWeekStart, 1))}
                            className="p-2 hover:bg-slate-200 text-slate-600 transition"
                            title="Previous Week"
                        >
                            <ChevronLeft className="w-4 h-4" />
                        </button>
                        <button
                            onClick={() =>
                                setCurrentWeekStart(startOfWeek(new Date(), { weekStartsOn: 1 }))
                            }
                            className="px-3 py-1 text-xs font-semibold border-x border-slate-300 hover:bg-slate-200 text-slate-700 transition"
                        >
                            Today
                        </button>
                        <button
                            onClick={() => setCurrentWeekStart(addWeeks(currentWeekStart, 1))}
                            className="p-2 hover:bg-slate-200 text-slate-600 transition"
                            title="Next Week"
                        >
                            <ChevronRight className="w-4 h-4" />
                        </button>
                    </div>

                    {/* Section 3.1 Filters */}
                    <div className="flex items-center space-x-2 pl-2 border-l border-slate-200">
                        <Filter className="w-3.5 h-3.5 text-slate-400" />
                        <select
                            value={selectedStaffFilter}
                            onChange={(e) => setSelectedStaffFilter(e.target.value)}
                            className="text-xs bg-slate-50 border border-slate-300 rounded-lg px-2 py-1 text-slate-700 focus:outline-none"
                        >
                            <option value="">All Staff</option>
                            {staffList.map((s) => (
                                <option key={s.id} value={s.name}>{s.name}</option>
                            ))}
                        </select>

                        <select
                            value={selectedServiceFilter}
                            onChange={(e) => setSelectedServiceFilter(e.target.value)}
                            className="text-xs bg-slate-50 border border-slate-300 rounded-lg px-2 py-1 text-slate-700 focus:outline-none"
                        >
                            <option value="">All Services</option>
                            {services.map((s) => (
                                <option key={s.id} value={s.name}>{s.name}</option>
                            ))}
                        </select>
                    </div>
                </div>

                <button
                    onClick={() => onOpenBooking()}
                    className="bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-semibold px-4 py-2 rounded-xl flex items-center space-x-1.5 shadow-sm transition"
                >
                    <Plus className="w-4 h-4" />
                    <span>New Booking</span>
                </button>
            </div>

            {/* Week Grid */}
            <div className="overflow-x-auto">
                <div className="min-w-[800px]">
                    <div className="grid grid-cols-8 border-b border-slate-200 bg-slate-50 text-center text-xs font-semibold text-slate-600">
                        <div className="py-3 border-r border-slate-200">Time</div>
                        {weekDays.map((day) => (
                            <div
                                key={day.toISOString()}
                                className="py-3 border-r border-slate-200 last:border-r-0"
                            >
                <span className="block text-[11px] text-slate-400 font-medium uppercase tracking-wider">
                  {format(day, 'EEE')}
                </span>
                                <span className="text-slate-800 font-bold">{format(day, 'd MMM')}</span>
                            </div>
                        ))}
                    </div>

                    {timeSlots.map((time) => (
                        <div
                            key={time}
                            className="grid grid-cols-8 border-b border-slate-100 text-xs min-h-[75px]"
                        >
                            <div className="py-2 px-2 border-r border-slate-200 text-slate-400 font-mono text-center flex items-center justify-center bg-slate-50/40">
                                {time}
                            </div>

                            {weekDays.map((day) => {
                                const slotData = getBookingForSlot(day, time);
                                const matchedBooking = slotData?.booking;
                                const isStartSlot = slotData?.isStartSlot;

                                const slotDate = new Date(day);
                                const [h, m] = time.split(':').map(Number);
                                slotDate.setHours(h, m, 0, 0);

                                return (
                                    <div
                                        key={day.toISOString() + time}
                                        className="border-r border-slate-100 last:border-r-0 p-1.5 relative group transition hover:bg-indigo-50/20"
                                    >
                                        {matchedBooking ? (
                                            isStartSlot ? (
                                                <div
                                                    onClick={() => setSelectedBooking(matchedBooking)}
                                                    className="bg-indigo-50 hover:bg-indigo-100/90 border border-indigo-200 rounded-xl p-2 h-full flex flex-col justify-between shadow-xs cursor-pointer transition"
                                                >
                                                    <div>
                                                        <p className="font-bold text-indigo-950 truncate text-[11px]">
                                                            {matchedBooking.serviceName}
                                                        </p>
                                                        <p className="text-[10px] text-indigo-700 truncate flex items-center mt-0.5">
                                                            <User className="w-3 h-3 inline mr-1 shrink-0" />
                                                            <span>{matchedBooking.staffName}</span>
                                                        </p>
                                                    </div>
                                                    <div className="text-[9px] font-medium text-indigo-900 bg-white/90 px-1.5 py-0.5 rounded-md border border-indigo-100 truncate mt-1">
                                                        {matchedBooking.customerName} ({matchedBooking.petName || 'Pet'})
                                                    </div>
                                                </div>
                                            ) : (
                                                <div
                                                    onClick={() => setSelectedBooking(matchedBooking)}
                                                    className="bg-indigo-50/40 border border-dashed border-indigo-200 rounded-xl p-2 h-full flex items-center justify-center cursor-pointer hover:bg-indigo-100/60 transition"
                                                >
                          <span className="text-[10px] font-medium text-indigo-500 italic truncate">
                            ↳ {matchedBooking.serviceName} (in-progress)
                          </span>
                                                </div>
                                            )
                                        ) : (
                                            <button
                                                type="button"
                                                onClick={() => onOpenBooking(slotDate.toISOString())}
                                                className="w-full h-full rounded-xl border border-dashed border-transparent hover:border-indigo-300 hover:bg-indigo-50/40 flex items-center justify-center text-slate-300 hover:text-indigo-600 transition cursor-pointer"
                                            >
                        <span className="text-[10px] font-medium opacity-0 group-hover:opacity-100">
                          + Book
                        </span>
                                            </button>
                                        )}
                                    </div>
                                );
                            })}
                        </div>
                    ))}
                </div>
            </div>

            {selectedBooking && (
                <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4">
                    <div className="bg-white rounded-2xl shadow-2xl border border-slate-200 max-w-sm w-full p-6 space-y-4">
                        <div className="flex items-center justify-between border-b border-slate-100 pb-3">
                            <div className="flex items-center space-x-2">
                                <CalendarIcon className="w-4 h-4 text-indigo-600" />
                                <h3 className="text-sm font-bold text-slate-800">Booking Details</h3>
                            </div>
                            <button
                                type="button"
                                onClick={() => setSelectedBooking(null)}
                                className="text-slate-400 hover:text-slate-600 p-1 rounded-lg"
                            >
                                <X className="w-4 h-4" />
                            </button>
                        </div>

                        <div className="text-xs space-y-2.5 text-slate-600">
                            <div className="flex justify-between py-1 border-b border-slate-50">
                                <span className="font-semibold text-slate-500">Service:</span>
                                <span className="font-bold text-slate-800">{selectedBooking.serviceName}</span>
                            </div>
                            <div className="flex justify-between py-1 border-b border-slate-50">
                                <span className="font-semibold text-slate-500">Staff Assigned:</span>
                                <span className="text-slate-800">{selectedBooking.staffName}</span>
                            </div>
                            <div className="flex justify-between py-1 border-b border-slate-50">
                                <span className="font-semibold text-slate-500">Customer:</span>
                                <span className="text-slate-800">{selectedBooking.customerName}</span>
                            </div>
                            <div className="flex justify-between py-1 border-b border-slate-50">
                                <span className="font-semibold text-slate-500">Pet:</span>
                                <span className="text-slate-800">{selectedBooking.petName || 'N/A'}</span>
                            </div>
                            <div className="flex justify-between py-1">
                                <span className="font-semibold text-slate-500">Status:</span>
                                <span className="inline-block px-2 py-0.5 bg-emerald-100 text-emerald-700 text-[10px] font-bold rounded-full">
                  {selectedBooking.status}
                </span>
                            </div>
                        </div>

                        <div className="pt-2 flex items-center justify-end space-x-2">
                            <button
                                type="button"
                                onClick={() => setSelectedBooking(null)}
                                className="px-3.5 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl transition"
                            >
                                Close
                            </button>
                            <button
                                type="button"
                                onClick={() => handleCancelBooking(selectedBooking.id)}
                                disabled={cancelling}
                                className="px-4 py-2 text-xs font-semibold bg-rose-600 hover:bg-rose-700 text-white rounded-xl shadow-xs transition flex items-center space-x-1.5"
                            >
                                {cancelling ? <Loader2 className="w-3.5 h-3.5 animate-spin mr-1" /> : <Trash2 className="w-3.5 h-3.5 mr-1" />}
                                <span>{cancelling ? 'Releasing...' : 'Cancel Booking'}</span>
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}