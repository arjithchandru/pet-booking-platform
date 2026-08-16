import React, { useState, useEffect } from 'react';
import {
    X,
    CheckCircle2,
    AlertCircle,
    Calendar,
    Loader2
} from 'lucide-react';
import apiClient from '../../api/client';

const formatForDateTimeInput = (dateInput) => {
    const date = dateInput ? new Date(dateInput) : new Date();

    if (!dateInput) {
        const day = date.getDay();
        if (day === 0) date.setDate(date.getDate() + 1); // Sunday -> Monday
        if (day === 6) date.setDate(date.getDate() + 2); // Saturday -> Monday
        date.setHours(10, 0, 0, 0);
    }

    const pad = (num) => String(num).padStart(2, '0');
    const year = date.getFullYear();
    const month = pad(date.getMonth() + 1);
    const day = pad(date.getDate());
    const hours = pad(date.getHours());
    const minutes = pad(date.getMinutes());

    return `${year}-${month}-${day}T${hours}:${minutes}`;
};

const toUtcIsoString = (localDateTimeStr) => {
    if (!localDateTimeStr) return new Date().toISOString();
    return new Date(localDateTimeStr).toISOString();
};

export default function BookingModal({ isOpen, initialSlot, onClose, onSuccess }) {
    const [services, setServices] = useState([]);
    const [selectedServiceId, setSelectedServiceId] = useState('');
    const [startAtLocal, setStartAtLocal] = useState(() => formatForDateTimeInput(initialSlot));
    const [eligibleStaff, setEligibleStaff] = useState([]);
    const [selectedStaffId, setSelectedStaffId] = useState('');
    const [customerName, setCustomerName] = useState('');
    const [petName, setPetName] = useState('');

    const [step, setStep] = useState(1);
    const [loadingServices, setLoadingServices] = useState(true);
    const [loadingStaff, setLoadingStaff] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');
    const [confirmedBooking, setConfirmedBooking] = useState(null);

    useEffect(() => {
        if (initialSlot) {
            setStartAtLocal(formatForDateTimeInput(initialSlot));
        }
    }, [initialSlot]);

    useEffect(() => {
        let isMounted = true;
        setLoadingServices(true);

        apiClient
            .get('/services')
            .then((res) => {
                if (!isMounted) return;
                const active = (res.data || []).filter((s) => s.status === 'ACTIVE');
                setServices(active);
                if (active.length > 0) {
                    setSelectedServiceId(active[0].id);
                }
            })
            .catch(() => {
                if (!isMounted) return;
                setErrorMessage('Failed to load services catalog.');
            })
            .finally(() => {
                if (isMounted) setLoadingServices(false);
            });

        return () => {
            isMounted = false;
        };
    }, []);

    useEffect(() => {
        if (!selectedServiceId || !startAtLocal) {
            setEligibleStaff([]);
            setSelectedStaffId('');
            return;
        }

        let isMounted = true;
        setLoadingStaff(true);
        setErrorMessage('');

        const startAtUtcIso = toUtcIsoString(startAtLocal);

        apiClient
            .get(`/services/${selectedServiceId}/available-staff?startAt=${startAtUtcIso}`)
            .then((res) => {
                if (!isMounted) return;
                const staffList = res.data || [];
                setEligibleStaff(staffList);

                if (staffList.length > 0) {
                    setSelectedStaffId(staffList[0].id);
                } else {
                    setSelectedStaffId('');
                }
            })
            .catch(() => {
                if (!isMounted) return;
                setEligibleStaff([]);
                setSelectedStaffId('');
                setErrorMessage('Could not calculate available staff for this time window.');
            })
            .finally(() => {
                if (isMounted) setLoadingStaff(false);
            });

        return () => {
            isMounted = false;
        };
    }, [selectedServiceId, startAtLocal]);

    const handleBookingSubmit = async (e) => {
        e.preventDefault();
        if (!selectedServiceId || !selectedStaffId || !startAtLocal || !customerName.trim()) {
            setErrorMessage('Please fill in all required fields.');
            return;
        }

        setSubmitting(true);
        setErrorMessage('');

        try {
            const payload = {
                serviceId: selectedServiceId,
                staffId: selectedStaffId,
                startAt: toUtcIsoString(startAtLocal),
                customerName: customerName.trim(),
                petName: petName.trim() || 'Pet',
            };

            const res = await apiClient.post('/bookings', payload);
            setConfirmedBooking(res.data);
            setStep(2);
        } catch (err) {
            const errorMsg =
                err.response?.data?.message ||
                'Booking collision: This time slot was just booked by another user.';
            setErrorMessage(errorMsg);
        } finally {
            setSubmitting(false);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4">
            <div className="bg-white rounded-2xl shadow-2xl border border-slate-200 max-w-lg w-full overflow-hidden transition-all transform animate-in fade-in zoom-in-95 duration-150">

                {/* Modal Header */}
                <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/80">
                    <div className="flex items-center space-x-2">
                        <div className="p-1.5 bg-indigo-50 text-indigo-600 rounded-lg">
                            <Calendar className="w-4 h-4" />
                        </div>
                        <h3 className="text-sm font-bold text-slate-800">
                            {step === 2 ? 'Booking Confirmation' : 'Create New Booking'}
                        </h3>
                    </div>
                    <button
                        type="button"
                        onClick={onClose}
                        className="p-1.5 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-lg transition"
                    >
                        <X className="w-4 h-4" />
                    </button>
                </div>

                {errorMessage && (
                    <div className="mx-6 mt-4 p-3 bg-rose-50 border border-rose-200 text-rose-700 text-xs rounded-xl flex items-start space-x-2.5">
                        <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
                        <span className="leading-relaxed">{errorMessage}</span>
                    </div>
                )}

                {step === 1 ? (
                    <form onSubmit={handleBookingSubmit} className="p-6 space-y-4">
                        <div>
                            <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                                Select Service <span className="text-rose-500">*</span>
                            </label>
                            {loadingServices ? (
                                <div className="flex items-center text-xs text-slate-400 py-2">
                                    <Loader2 className="w-3.5 h-3.5 animate-spin mr-2" /> Loading catalog...
                                </div>
                            ) : (
                                <select
                                    value={selectedServiceId}
                                    onChange={(e) => setSelectedServiceId(e.target.value)}
                                    className="w-full bg-slate-50 border border-slate-300 rounded-xl px-3 py-2 text-xs text-slate-800 focus:bg-white focus:ring-2 focus:ring-indigo-500 focus:outline-none transition"
                                    required
                                >
                                    {services.map((service) => (
                                        <option key={service.id} value={service.id}>
                                            {service.name} ({service.durationMinutes} mins • ${service.price})
                                        </option>
                                    ))}
                                </select>
                            )}
                        </div>

                        <div>
                            <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                                Booking Date & Time <span className="text-rose-500">*</span>
                            </label>
                            <input
                                type="datetime-local"
                                value={startAtLocal}
                                onChange={(e) => setStartAtLocal(e.target.value)}
                                className="w-full bg-slate-50 border border-slate-300 rounded-xl px-3 py-2 text-xs text-slate-800 focus:bg-white focus:ring-2 focus:ring-indigo-500 focus:outline-none transition"
                                required
                            />
                            <p className="text-[11px] text-slate-400 mt-1">
                                Standard staff working hours are Mon–Fri, 09:00 to 18:00 UTC.
                            </p>
                        </div>

                        <div>
                            <label className="block text-xs font-semibold text-slate-700 mb-1.5 flex items-center justify-between">
                                <span>Available Staff <span className="text-rose-500">*</span></span>
                                {loadingStaff && (
                                    <span className="text-[11px] text-indigo-600 font-normal flex items-center">
                    <Loader2 className="w-3 h-3 animate-spin mr-1" /> Checking schedules...
                  </span>
                                )}
                            </label>

                            {eligibleStaff.length > 0 ? (
                                <select
                                    value={selectedStaffId}
                                    onChange={(e) => setSelectedStaffId(e.target.value)}
                                    className="w-full bg-slate-50 border border-slate-300 rounded-xl px-3 py-2 text-xs text-slate-800 focus:bg-white focus:ring-2 focus:ring-indigo-500 focus:outline-none transition"
                                    required
                                >
                                    {eligibleStaff.map((staff) => (
                                        <option key={staff.id} value={staff.id}>
                                            {staff.name} — Qualified & Available
                                        </option>
                                    ))}
                                </select>
                            ) : (
                                !loadingStaff && (
                                    <div className="p-3 bg-amber-50/80 border border-amber-200 rounded-xl text-amber-800 text-xs leading-relaxed">
                                        <strong>No staff available.</strong> Staff might be off-duty, taking a scheduled break, or already booked.
                                    </div>
                                )
                            )}
                        </div>

                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-2 border-t border-slate-100">
                            <div>
                                <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                                    Customer Name <span className="text-rose-500">*</span>
                                </label>
                                <input
                                    type="text"
                                    placeholder="e.g. Alice Smith"
                                    value={customerName}
                                    onChange={(e) => setCustomerName(e.target.value)}
                                    className="w-full bg-slate-50 border border-slate-300 rounded-xl px-3 py-2 text-xs text-slate-800 focus:bg-white focus:ring-2 focus:ring-indigo-500 focus:outline-none transition"
                                    required
                                />
                            </div>

                            <div>
                                <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                                    Pet Name
                                </label>
                                <input
                                    type="text"
                                    placeholder="e.g. Buddy"
                                    value={petName}
                                    onChange={(e) => setPetName(e.target.value)}
                                    className="w-full bg-slate-50 border border-slate-300 rounded-xl px-3 py-2 text-xs text-slate-800 focus:bg-white focus:ring-2 focus:ring-indigo-500 focus:outline-none transition"
                                />
                            </div>
                        </div>

                        <div className="pt-4 flex items-center justify-end space-x-2.5">
                            <button
                                type="button"
                                onClick={onClose}
                                className="px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl transition"
                            >
                                Cancel
                            </button>
                            <button
                                type="submit"
                                disabled={submitting || eligibleStaff.length === 0 || !customerName.trim()}
                                className="px-5 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-700 disabled:opacity-40 disabled:cursor-not-allowed rounded-xl shadow-xs transition flex items-center space-x-1.5"
                            >
                                {submitting && <Loader2 className="w-3.5 h-3.5 animate-spin mr-1" />}
                                <span>{submitting ? 'Locking Slot...' : 'Confirm Booking'}</span>
                            </button>
                        </div>
                    </form>
                ) : (
                    <div className="p-8 text-center space-y-4">
                        <div className="w-14 h-14 bg-emerald-50 text-emerald-600 rounded-full flex items-center justify-center mx-auto border border-emerald-100">
                            <CheckCircle2 className="w-8 h-8" />
                        </div>

                        <div>
                            <h4 className="text-base font-bold text-slate-800">Booking Confirmed!</h4>
                            <p className="text-xs text-slate-500 mt-1">
                                The appointment is locked and the calendar schedule has been updated.
                            </p>
                        </div>

                        {confirmedBooking && (
                            <div className="bg-slate-50 p-4 rounded-xl text-left border border-slate-200 text-xs space-y-1.5 text-slate-700">
                                <p><strong>Service:</strong> {confirmedBooking.serviceName}</p>
                                <p><strong>Staff:</strong> {confirmedBooking.staffName}</p>
                                <p><strong>Customer:</strong> {confirmedBooking.customerName} ({confirmedBooking.petName})</p>
                                <p><strong>Start Time:</strong> {new Date(confirmedBooking.startAt).toUTCString()}</p>
                            </div>
                        )}

                        <button
                            type="button"
                            onClick={onSuccess}
                            className="w-full py-2.5 px-4 text-xs font-semibold bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl shadow-sm transition"
                        >
                            Return to Calendar
                        </button>
                    </div>
                )}

            </div>
        </div>
    );
}