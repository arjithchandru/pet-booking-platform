import React, { useState, useEffect } from 'react';
import { X, User, Loader2, Check } from 'lucide-react';
import apiClient from '../../api/client';

export default function StaffModal({ isOpen, staffToEdit, onClose, onSuccess }) {
    const [formData, setFormData] = useState({
        name: '',
        email: '',
        status: 'ACTIVE',
        serviceIds: [],
    });
    const [availableServices, setAvailableServices] = useState([]);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState('');

    // Load available services for assignment checkboxes
    useEffect(() => {
        if (isOpen) {
            apiClient
                .get('/services')
                .then((res) => {
                    setAvailableServices(res.data || []);
                })
                .catch((err) => {
                    console.error('Failed to load services:', err);
                });
        }
    }, [isOpen]);

    // Pre-fill form if editing an existing staff member
    useEffect(() => {
        if (staffToEdit) {
            setFormData({
                name: staffToEdit.name || '',
                email: staffToEdit.email || '',
                status: staffToEdit.status || 'ACTIVE',
                serviceIds: staffToEdit.qualifiedServiceIds
                    ? Array.from(staffToEdit.qualifiedServiceIds)
                    : [],
            });
        } else {
            setFormData({
                name: '',
                email: '',
                status: 'ACTIVE',
                serviceIds: [],
            });
        }
    }, [staffToEdit, isOpen]);

    if (!isOpen) return null;

    const toggleService = (serviceId) => {
        const exists = formData.serviceIds.includes(serviceId);
        if (exists) {
            setFormData({
                ...formData,
                serviceIds: formData.serviceIds.filter((id) => id !== serviceId),
            });
        } else {
            setFormData({
                ...formData,
                serviceIds: [...formData.serviceIds, serviceId],
            });
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSubmitting(true);
        setError('');

        try {
            if (staffToEdit) {
                await apiClient.put(`/staff/${staffToEdit.id}`, formData);
            } else {
                await apiClient.post('/staff', formData);
            }
            onSuccess();
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to save staff member.');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4">
            <div className="bg-white rounded-2xl shadow-2xl border border-slate-200 max-w-md w-full overflow-hidden animate-in fade-in zoom-in-95 duration-150">
                <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50">
                    <div className="flex items-center space-x-2">
                        <User className="w-4 h-4 text-indigo-600" />
                        <h3 className="text-sm font-bold text-slate-800">
                            {staffToEdit ? 'Edit Staff Member' : 'Add New Staff Member'}
                        </h3>
                    </div>
                    <button
                        type="button"
                        onClick={onClose}
                        className="p-1 text-slate-400 hover:text-slate-600 rounded-lg transition"
                    >
                        <X className="w-4 h-4" />
                    </button>
                </div>

                {error && (
                    <div className="mx-6 mt-4 p-3 bg-rose-50 text-rose-700 text-xs rounded-xl border border-rose-200">
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit} className="p-6 space-y-4 text-xs">
                    <div>
                        <label className="block font-semibold text-slate-700 mb-1">
                            Full Name <span className="text-rose-500">*</span>
                        </label>
                        <input
                            type="text"
                            required
                            placeholder="e.g. Alex Green"
                            value={formData.name}
                            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                            className="w-full bg-slate-50 border border-slate-300 rounded-xl px-3 py-2 text-slate-800 focus:bg-white focus:ring-2 focus:ring-indigo-500 focus:outline-none transition"
                        />
                    </div>

                    <div>
                        <label className="block font-semibold text-slate-700 mb-1">
                            Email Address <span className="text-rose-500">*</span>
                        </label>
                        <input
                            type="email"
                            required
                            placeholder="alex@pawsplay.test"
                            value={formData.email}
                            onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                            className="w-full bg-slate-50 border border-slate-300 rounded-xl px-3 py-2 text-slate-800 focus:bg-white focus:ring-2 focus:ring-indigo-500 focus:outline-none transition"
                        />
                    </div>

                    <div>
                        <label className="block font-semibold text-slate-700 mb-1">
                            Status <span className="text-rose-500">*</span>
                        </label>
                        <select
                            value={formData.status}
                            onChange={(e) => setFormData({ ...formData, status: e.target.value })}
                            className="w-full bg-slate-50 border border-slate-300 rounded-xl px-3 py-2 text-slate-800 focus:bg-white focus:ring-2 focus:ring-indigo-500 focus:outline-none transition"
                        >
                            <option value="ACTIVE">ACTIVE</option>
                            <option value="INACTIVE">INACTIVE</option>
                        </select>
                    </div>

                    <div>
                        <label className="block font-semibold text-slate-700 mb-1.5">
                            Qualified Services (Service Assignment)
                        </label>
                        <div className="space-y-2 border border-slate-200 rounded-xl p-3 bg-slate-50/50 max-h-36 overflow-y-auto">
                            {availableServices.map((svc) => {
                                const isSelected = formData.serviceIds.includes(svc.id);
                                return (
                                    <div
                                        key={svc.id}
                                        onClick={() => toggleService(svc.id)}
                                        className={`flex items-center justify-between p-2 rounded-lg cursor-pointer border transition ${
                                            isSelected
                                                ? 'bg-indigo-50/70 border-indigo-300 text-indigo-900'
                                                : 'bg-white border-slate-200 hover:bg-slate-50 text-slate-700'
                                        }`}
                                    >
                                        <span className="font-semibold">{svc.name}</span>
                                        <span className="text-[11px] text-slate-400">
                      {isSelected ? (
                          <Check className="w-3.5 h-3.5 text-indigo-600" />
                      ) : (
                          '+ Assign'
                      )}
                    </span>
                                    </div>
                                );
                            })}
                        </div>
                    </div>

                    <div className="pt-3 flex items-center justify-end space-x-2">
                        <button
                            type="button"
                            onClick={onClose}
                            className="px-4 py-2 font-semibold text-slate-600 hover:bg-slate-100 rounded-xl transition"
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            disabled={submitting}
                            className="px-5 py-2 font-semibold text-white bg-indigo-600 hover:bg-indigo-700 rounded-xl shadow-xs transition flex items-center"
                        >
                            {submitting && <Loader2 className="w-3.5 h-3.5 animate-spin mr-1.5" />}
                            <span>{staffToEdit ? 'Save Changes' : 'Create Staff'}</span>
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}