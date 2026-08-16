import React, { useState, useEffect } from 'react';
import { Users, Mail, Award, Plus, Edit2, Loader2 } from 'lucide-react';
import apiClient from '../../api/client';
import StaffModal from './StaffModel';

export default function StaffList() {
    const [staffList, setStaffList] = useState([]);
    const [loading, setLoading] = useState(true);
    const [modalOpen, setModalOpen] = useState(false);
    const [staffToEdit, setStaffToEdit] = useState(null);

    const fetchStaff = async () => {
        setLoading(true);
        try {
            const res = await apiClient.get('/staff');
            setStaffList(res.data || []);
        } catch (err) {
            console.error('Failed to load staff:', err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchStaff();
    }, []);

    if (loading) {
        return (
            <div className="flex items-center justify-center py-16 text-indigo-600 text-xs">
                <Loader2 className="w-5 h-5 animate-spin mr-2" /> Loading staff roster...
            </div>
        );
    }

    return (
        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
            <div className="flex flex-wrap items-center justify-between mb-6 gap-3">
                <div>
                    <h2 className="text-base font-bold text-slate-800">Staff Members & Qualifications</h2>
                    <p className="text-xs text-slate-500">Manage staff profiles, active statuses, and service qualifications</p>
                </div>
                <button
                    onClick={() => {
                        setStaffToEdit(null);
                        setModalOpen(true);
                    }}
                    className="bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-semibold px-4 py-2 rounded-xl flex items-center space-x-1.5 shadow-sm transition"
                >
                    <Plus className="w-4 h-4" />
                    <span>Add Staff Member</span>
                </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                {staffList.map((staff) => (
                    <div
                        key={staff.id}
                        className="p-5 border border-slate-200 rounded-xl space-y-3 bg-slate-50/40 hover:border-indigo-300 transition flex flex-col justify-between"
                    >
                        <div className="space-y-3">
                            <div className="flex justify-between items-center">
                                <h3 className="text-sm font-bold text-slate-900">{staff.name}</h3>
                                <span
                                    className={`px-2 py-0.5 text-[10px] font-bold rounded-full ${
                                        staff.status === 'ACTIVE'
                                            ? 'bg-emerald-100 text-emerald-700'
                                            : 'bg-slate-200 text-slate-600'
                                    }`}
                                >
                  {staff.status}
                </span>
                            </div>

                            <div className="flex items-center text-xs text-slate-500">
                                <Mail className="w-3.5 h-3.5 mr-1.5 shrink-0" />
                                <span className="truncate">{staff.email}</span>
                            </div>

                            <div className="text-[11px] text-slate-700 bg-white p-2.5 rounded-lg border border-slate-200 flex items-center justify-between">
                <span className="flex items-center font-medium">
                  <Award className="w-3.5 h-3.5 mr-1.5 text-indigo-500" />
                  Qualifications:
                </span>
                                <span className="font-bold text-slate-900">
                  {(staff.qualifiedServiceIds || []).length || (staff.qualifiedServiceIds?.size) || 0} Services
                </span>
                            </div>
                        </div>

                        <div className="pt-2 border-t border-slate-200/60 flex justify-end">
                            <button
                                onClick={() => {
                                    setStaffToEdit(staff);
                                    setModalOpen(true);
                                }}
                                className="px-3 py-1 text-xs font-semibold text-indigo-600 hover:bg-indigo-50 rounded-lg flex items-center space-x-1 transition"
                            >
                                <Edit2 className="w-3 h-3" />
                                <span>Edit Staff</span>
                            </button>
                        </div>
                    </div>
                ))}
            </div>

            <StaffModal
                isOpen={modalOpen}
                staffToEdit={staffToEdit}
                onClose={() => setModalOpen(false)}
                onSuccess={() => {
                    setModalOpen(false);
                    fetchStaff();
                }}
            />
        </div>
    );
}