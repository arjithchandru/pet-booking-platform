import React, { useState, useEffect } from 'react';
import { User, Clock, Coffee, ShieldAlert, Loader2 } from 'lucide-react';
import apiClient from '../../api/client';

const DAYS = [
    { id: 1, name: 'Monday' },
    { id: 2, name: 'Tuesday' },
    { id: 3, name: 'Wednesday' },
    { id: 4, name: 'Thursday' },
    { id: 5, name: 'Friday' },
    { id: 6, name: 'Saturday' },
    { id: 7, name: 'Sunday' },
];

export default function StaffAvailabilityCalendar() {
    const [staffList, setStaffList] = useState([]);
    const [availabilityMap, setAvailabilityMap] = useState({});
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadStaffAndSchedules();
    }, []);

    const loadStaffAndSchedules = async () => {
        setLoading(true);
        try {
            const staffRes = await apiClient.get('/staff');
            const staff = staffRes.data || [];
            setStaffList(staff);

            const map = {};
            await Promise.all(
                staff.map(async (s) => {
                    try {
                        const availRes = await apiClient.get(`/staff/${s.id}/availability`);
                        map[s.id] = availRes.data || [];
                    } catch {
                        map[s.id] = [];
                    }
                })
            );
            setAvailabilityMap(map);
        } catch (err) {
            console.error('Failed to load staff availability:', err);
        } finally {
            setLoading(false);
        }
    };

    const getDaySchedule = (staffId, dayOfWeek) => {
        const windows = availabilityMap[staffId] || [];
        const dayWindows = windows.filter((w) => w.dayOfWeek === dayOfWeek);

        const workHours = dayWindows.find((w) => w.type === 'WORKING_HOURS');
        const breakWindow = dayWindows.find((w) => w.type === 'BREAK');

        return { workHours, breakWindow };
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center py-16 text-xs text-indigo-600">
                <Loader2 className="w-5 h-5 animate-spin mr-2" /> Loading weekly staff schedules...
            </div>
        );
    }

    return (
        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
            <div className="p-5 border-b border-slate-200 flex items-center justify-between">
                <div>
                    <h2 className="text-base font-bold text-slate-800">Staff Availability Roster</h2>
                    <p className="text-xs text-slate-500">Weekly recurring working hours and scheduled break periods</p>
                </div>
                <div className="flex items-center space-x-3 text-[11px]">
          <span className="flex items-center text-emerald-700 bg-emerald-50 px-2.5 py-1 rounded-lg border border-emerald-200">
            <span className="w-2 h-2 rounded-full bg-emerald-500 mr-1.5" /> Working Hours
          </span>
                    <span className="flex items-center text-amber-700 bg-amber-50 px-2.5 py-1 rounded-lg border border-amber-200">
            <Coffee className="w-3 h-3 mr-1 text-amber-600" /> Break
          </span>
                    <span className="flex items-center text-slate-500 bg-slate-100 px-2.5 py-1 rounded-lg">
            OFF
          </span>
                </div>
            </div>

            <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse min-w-[900px]">
                    <thead>
                    <tr className="bg-slate-50 border-b border-slate-200 text-xs font-semibold text-slate-600">
                        <th className="py-3.5 px-4 w-48">Staff Member</th>
                        {DAYS.map((d) => (
                            <th key={d.id} className="py-3.5 px-3 text-center border-l border-slate-200">
                                {d.name}
                            </th>
                        ))}
                    </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100 text-xs">
                    {staffList.map((staff) => (
                        <tr key={staff.id} className="hover:bg-slate-50/50 transition">
                            <td className="py-4 px-4 align-top">
                                <div className="flex items-start space-x-2.5">
                                    <div className="p-2 bg-indigo-50 text-indigo-600 rounded-xl mt-0.5">
                                        <User className="w-4 h-4" />
                                    </div>
                                    <div>
                                        <p className="font-bold text-slate-900">{staff.name}</p>
                                        <p className="text-[11px] text-slate-400 truncate max-w-[130px]">{staff.email}</p>
                                        <span className="inline-block mt-1 px-2 py-0.5 text-[9px] font-bold bg-slate-100 text-slate-600 rounded-full">
                        {staff.status}
                      </span>
                                    </div>
                                </div>
                            </td>

                            {DAYS.map((d) => {
                                const { workHours, breakWindow } = getDaySchedule(staff.id, d.id);

                                return (
                                    <td key={d.id} className="p-2.5 text-center align-top border-l border-slate-100">
                                        {workHours ? (
                                            <div className="space-y-1.5">
                                                <div className="bg-emerald-50/80 border border-emerald-200 text-emerald-800 rounded-xl p-2 font-mono text-[11px] font-semibold">
                                                    <Clock className="w-3 h-3 inline mr-1 text-emerald-600" />
                                                    {workHours.startTime.slice(0, 5)} - {workHours.endTime.slice(0, 5)}
                                                </div>
                                                {breakWindow && (
                                                    <div className="bg-amber-50 border border-amber-200 text-amber-800 rounded-lg py-1 px-1.5 text-[10px] flex items-center justify-center space-x-1 font-mono">
                                                        <Coffee className="w-3 h-3 text-amber-600 shrink-0" />
                                                        <span>{breakWindow.startTime.slice(0, 5)}-{breakWindow.endTime.slice(0, 5)}</span>
                                                    </div>
                                                )}
                                            </div>
                                        ) : (
                                            <div className="bg-slate-50 border border-slate-200 text-slate-400 rounded-xl py-3 text-[11px] font-medium">
                                                OFF
                                            </div>
                                        )}
                                    </td>
                                );
                            })}
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}