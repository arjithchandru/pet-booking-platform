import React, { useState, useEffect } from 'react';
import { Scissors, Clock, Plus, Edit2, Power, Loader2 } from 'lucide-react';
import apiClient from '../../api/client';
import ServiceModal from './ServiceModal';

export default function ServicesList() {
    const [services, setServices] = useState([]);
    const [loading, setLoading] = useState(true);
    const [modalOpen, setModalOpen] = useState(false);
    const [serviceToEdit, setServiceToEdit] = useState(null);

    const fetchServices = async () => {
        setLoading(true);
        try {
            const res = await apiClient.get('/services');
            setServices(res.data || []);
        } catch (err) {
            console.error('Failed to load services:', err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchServices();
    }, []);

    const handleDeactivate = async (serviceId) => {
        if (!window.confirm('Are you sure you want to deactivate this service?')) return;
        try {
            await apiClient.delete(`/services/${serviceId}`);
            fetchServices();
        } catch (err) {
            alert(err.response?.data?.message || 'Failed to deactivate');
        }
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center py-16 text-indigo-600 text-xs">
                <Loader2 className="w-5 h-5 animate-spin mr-2" /> Loading services catalog...
            </div>
        );
    }

    return (
        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
            <div className="flex flex-wrap items-center justify-between mb-6 gap-3">
                <div>
                    <h2 className="text-base font-bold text-slate-800">Services Catalog</h2>
                    <p className="text-xs text-slate-500">Manage services, prices, durations, and active statuses</p>
                </div>
                <button
                    onClick={() => {
                        setServiceToEdit(null);
                        setModalOpen(true);
                    }}
                    className="bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-semibold px-4 py-2 rounded-xl flex items-center space-x-1.5 shadow-sm transition"
                >
                    <Plus className="w-4 h-4" />
                    <span>Add Service</span>
                </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {services.map((service) => (
                    <div
                        key={service.id}
                        className="p-5 border border-slate-200 rounded-xl hover:border-indigo-300 hover:shadow-xs transition bg-slate-50/40 flex flex-col justify-between"
                    >
                        <div>
                            <div className="flex justify-between items-start">
                                <div>
                                    <h3 className="text-sm font-bold text-slate-900">{service.name}</h3>
                                    <p className="text-xs text-slate-500 mt-1">{service.description || 'No description provided.'}</p>
                                </div>
                                <span
                                    className={`px-2.5 py-0.5 text-[10px] font-bold rounded-full ${
                                        service.status === 'ACTIVE'
                                            ? 'bg-emerald-100 text-emerald-700'
                                            : 'bg-slate-200 text-slate-600'
                                    }`}
                                >
                  {service.status}
                </span>
                            </div>

                            <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between text-xs text-slate-600 font-medium">
                <span className="bg-white px-2 py-1 rounded-lg border border-slate-200 text-slate-700 font-semibold text-[11px]">
                  {service.category}
                </span>
                                <div className="flex items-center space-x-3">
                  <span className="flex items-center text-slate-500">
                    <Clock className="w-3.5 h-3.5 mr-1" /> {service.durationMinutes} mins
                  </span>
                                    <span className="text-slate-900 font-bold text-sm">
                    ${Number(service.price).toFixed(2)}
                  </span>
                                </div>
                            </div>
                        </div>

                        <div className="mt-4 pt-3 border-t border-slate-200/60 flex items-center justify-end space-x-2">
                            <button
                                onClick={() => {
                                    setServiceToEdit(service);
                                    setModalOpen(true);
                                }}
                                className="px-3 py-1.5 text-xs font-semibold text-indigo-600 hover:bg-indigo-50 rounded-lg flex items-center space-x-1 transition"
                            >
                                <Edit2 className="w-3 h-3" />
                                <span>Edit</span>
                            </button>
                            {service.status === 'ACTIVE' && (
                                <button
                                    onClick={() => handleDeactivate(service.id)}
                                    className="px-3 py-1.5 text-xs font-semibold text-rose-600 hover:bg-rose-50 rounded-lg flex items-center space-x-1 transition"
                                >
                                    <Power className="w-3 h-3" />
                                    <span>Deactivate</span>
                                </button>
                            )}
                        </div>
                    </div>
                ))}
            </div>

            <ServiceModal
                isOpen={modalOpen}
                serviceToEdit={serviceToEdit}
                onClose={() => setModalOpen(false)}
                onSuccess={() => {
                    setModalOpen(false);
                    fetchServices();
                }}
            />
        </div>
    );
}