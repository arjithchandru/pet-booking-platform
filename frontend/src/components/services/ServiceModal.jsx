import React, { useState, useEffect } from 'react';
import { X, Scissors, Loader2 } from 'lucide-react';
import apiClient from '../../api/client';

export default function ServiceModal({ isOpen, serviceToEdit, onClose, onSuccess }) {
    const [formData, setFormData] = useState({
        name: '',
        description: '',
        category: 'Grooming',
        durationMinutes: 60,
        price: 50.0,
        status: 'ACTIVE',
    });
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => {
        if (serviceToEdit) {
            setFormData({
                name: serviceToEdit.name || '',
                description: serviceToEdit.description || '',
                category: serviceToEdit.category || 'Grooming',
                durationMinutes: serviceToEdit.durationMinutes || 60,
                price: serviceToEdit.price || 50.0,
                status: serviceToEdit.status || 'ACTIVE',
            });
        } else {
            setFormData({
                name: '',
                description: '',
                category: 'Grooming',
                durationMinutes: 60,
                price: 50.0,
                status: 'ACTIVE',
            });
        }
    }, [serviceToEdit, isOpen]);

    if (!isOpen) return null;

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSubmitting(true);
        setError('');

        try {
            if (serviceToEdit) {
                await apiClient.put(`/services/${serviceToEdit.id}`, formData);
            } else {
                await apiClient.post('/services', formData);
            }
            onSuccess();
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to save service.');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4">
            <div className="bg-white rounded-2xl shadow-2xl border border-slate-200 max-w-md w-full overflow-hidden">
                <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50">
                    <div className="flex items-center space-x-2">
                        <Scissors className="w-4 h-4 text-indigo-600" />
                        <h3 className="text-sm font-bold text-slate-800">
                            {serviceToEdit ? 'Edit Service' : 'Add New Service'}
                        </h3>
                    </div>
                    <button onClick={onClose} className="p-1 text-slate-400 hover:text-slate-600 rounded-lg">
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
                        <label className="block font-semibold text-slate-700 mb-1">Service Name *</label>
                        <input
                            type="text"
                            required
                            placeholder="e.g. Full Grooming"
                            value={formData.name}
                            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                            className="w-full bg-slate-50 border border-slate-300 rounded-xl px-3 py-2 text-slate-800 focus:bg-white focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                        />
                    </div>

                    <div>
                        <label className="block font-semibold text-slate-700 mb-1">Description</label>
                        <textarea
                            rows={2}
                            placeholder="Brief description of the service..."
                            value={formData.description}
                            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                            className="w-full bg-slate-50 border border-slate-300 rounded-xl px-3 py-2 text-slate-800 focus:bg-white focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                        />
                    </div>

                    <div className="grid grid-cols-2 gap-3">
                        <div>
                            <label className="block font-semibold text-slate-700 mb-1">Category *</label>
                            <input
                                type="text"
                                required
                                placeholder="e.g. Grooming"
                                value={formData.category}
                                onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                                className="w-full bg-slate-50 border border-slate-300 rounded-xl px-3 py-2 text-slate-800 focus:bg-white focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                            />
                        </div>

                        <div>
                            <label className="block font-semibold text-slate-700 mb-1">Duration (Mins) *</label>
                            <input
                                type="number"
                                min={5}
                                step={5}
                                required
                                value={formData.durationMinutes}
                                onChange={(e) => setFormData({ ...formData, durationMinutes: Number(e.target.value) })}
                                className="w-full bg-slate-50 border border-slate-300 rounded-xl px-3 py-2 text-slate-800 focus:bg-white focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                            />
                        </div>
                    </div>

                    <div className="grid grid-cols-2 gap-3">
                        <div>
                            <label className="block font-semibold text-slate-700 mb-1">Price ($) *</label>
                            <input
                                type="number"
                                min={0}
                                step="0.01"
                                required
                                value={formData.price}
                                onChange={(e) => setFormData({ ...formData, price: Number(e.target.value) })}
                                className="w-full bg-slate-50 border border-slate-300 rounded-xl px-3 py-2 text-slate-800 focus:bg-white focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                            />
                        </div>

                        <div>
                            <label className="block font-semibold text-slate-700 mb-1">Status *</label>
                            <select
                                value={formData.status}
                                onChange={(e) => setFormData({ ...formData, status: e.target.value })}
                                className="w-full bg-slate-50 border border-slate-300 rounded-xl px-3 py-2 text-slate-800 focus:bg-white focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                            >
                                <option value="ACTIVE">ACTIVE</option>
                                <option value="INACTIVE">INACTIVE</option>
                            </select>
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
                            <span>{serviceToEdit ? 'Save Changes' : 'Create Service'}</span>
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}