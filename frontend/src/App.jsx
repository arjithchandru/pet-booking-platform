import React, { useState, useEffect } from 'react';
import { Calendar, Scissors, Users, CalendarDays, Building2 } from 'lucide-react';
import apiClient from './api/client';
import ServicesCalendar from './components/calendar/ServicesCalendar';
import StaffAvailabilityCalendar from './components/calendar/StaffAvailabilityCalendar';
import ServicesList from './components/services/ServicesList';
import StaffList from './components/staff/StaffList';
import BookingModal from './components/booking/BookingModal';

export default function App() {
  const [activeTab, setActiveTab] = useState('calendar');
  const [currentUser, setCurrentUser] = useState(null);
  const [selectedTenant, setSelectedTenant] = useState(
      () => localStorage.getItem('okta_subject') || 'okta_happy_paws_admin'
  );
  const [bookingModalState, setBookingModalState] = useState({
    isOpen: false,
    initialSlot: null,
  });

  const fetchUserContext = async () => {
    try {
      const res = await apiClient.get('/me');
      setCurrentUser(res.data);
    } catch (err) {
      console.error('Failed to load user context:', err);
    }
  };

  useEffect(() => {
    fetchUserContext();
  }, [selectedTenant]);

  const handleTenantChange = (oktaSubject) => {
    localStorage.setItem('okta_subject', oktaSubject);
    setSelectedTenant(oktaSubject);
    window.location.reload();
  };

  return (
      <div className="min-h-screen bg-slate-50 flex flex-col font-sans text-slate-900">
        {/* Header */}
        <header className="bg-white border-b border-slate-200 sticky top-0 z-30 shadow-xs">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <div className="bg-indigo-600 p-2 rounded-xl text-white shadow-xs">
                <Scissors className="w-5 h-5" />
              </div>
              <div>
                <h1 className="text-base font-bold text-slate-900 leading-tight">PetCare Pro</h1>
                <p className="text-[11px] text-slate-400">Multi-Tenant Booking & Staff Platform</p>
              </div>
            </div>

            <div className="flex items-center space-x-4">
              <div className="flex items-center bg-slate-100 p-1.5 rounded-xl border border-slate-200 text-xs">
                <Building2 className="w-4 h-4 text-slate-500 ml-1.5 mr-1" />
                <select
                    value={selectedTenant}
                    onChange={(e) => handleTenantChange(e.target.value)}
                    className="bg-transparent font-semibold text-slate-700 py-0.5 pr-2 focus:outline-none cursor-pointer"
                >
                  <option value="okta_happy_paws_admin">Tenant 1: Happy Paws (3 Staff)</option>
                  <option value="okta_paws_play_admin">Tenant 2: Paws & Play (2 Staff)</option>
                </select>
              </div>

              {currentUser && (
                  <div className="text-right hidden sm:block">
                    <p className="text-xs font-bold text-slate-800">{currentUser.email}</p>
                    <span className="inline-block px-2 py-0.5 text-[10px] font-bold uppercase bg-indigo-100 text-indigo-700 rounded-full">
                  {currentUser.role}
                </span>
                  </div>
              )}
            </div>
          </div>
        </header>

        {/* 4 Tabs matching Sections 2 & 3 of Specification */}
        <nav className="bg-white border-b border-slate-200">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex space-x-8">
            <button
                onClick={() => setActiveTab('calendar')}
                className={`py-3.5 px-1 border-b-2 text-xs font-bold flex items-center space-x-2 transition ${
                    activeTab === 'calendar'
                        ? 'border-indigo-600 text-indigo-600'
                        : 'border-transparent text-slate-500 hover:text-slate-800'
                }`}
            >
              <Calendar className="w-4 h-4" />
              <span>Services Calendar</span>
            </button>
            <button
                onClick={() => setActiveTab('availability')}
                className={`py-3.5 px-1 border-b-2 text-xs font-bold flex items-center space-x-2 transition ${
                    activeTab === 'availability'
                        ? 'border-indigo-600 text-indigo-600'
                        : 'border-transparent text-slate-500 hover:text-slate-800'
                }`}
            >
              <CalendarDays className="w-4 h-4" />
              <span>Staff Availability (Weekly)</span>
            </button>
            <button
                onClick={() => setActiveTab('services')}
                className={`py-3.5 px-1 border-b-2 text-xs font-bold flex items-center space-x-2 transition ${
                    activeTab === 'services'
                        ? 'border-indigo-600 text-indigo-600'
                        : 'border-transparent text-slate-500 hover:text-slate-800'
                }`}
            >
              <Scissors className="w-4 h-4" />
              <span>Services Catalog</span>
            </button>
            <button
                onClick={() => setActiveTab('staff')}
                className={`py-3.5 px-1 border-b-2 text-xs font-bold flex items-center space-x-2 transition ${
                    activeTab === 'staff'
                        ? 'border-indigo-600 text-indigo-600'
                        : 'border-transparent text-slate-500 hover:text-slate-800'
                }`}
            >
              <Users className="w-4 h-4" />
              <span>Staff Roster</span>
            </button>
          </div>
        </nav>

        {/* Main Content */}
        <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-6">
          {activeTab === 'calendar' && (
              <ServicesCalendar onOpenBooking={(slot) => setBookingModalState({ isOpen: true, initialSlot: slot })} />
          )}
          {activeTab === 'availability' && <StaffAvailabilityCalendar />}
          {activeTab === 'services' && <ServicesList />}
          {activeTab === 'staff' && <StaffList />}
        </main>

        {/* Booking Drawer */}
        {bookingModalState.isOpen && (
            <BookingModal
                isOpen={bookingModalState.isOpen}
                initialSlot={bookingModalState.initialSlot}
                onClose={() => setBookingModalState({ isOpen: false, initialSlot: null })}
                onSuccess={() => {
                  setBookingModalState({ isOpen: false, initialSlot: null });
                  window.dispatchEvent(new Event('refresh_bookings'));
                }}
            />
        )}
      </div>
  );
}