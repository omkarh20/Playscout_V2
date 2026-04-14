import React, { useContext, useState } from 'react';
import './Bookpop.css'; 
import { assets } from '../../assets/assets'; 
import { StoreContext } from '../../context/storeContextInstance';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';
import axios from 'axios';
import { toast } from 'react-toastify';
import { format } from 'date-fns';

const Bookpop = ({ setShowBooking, courtDetails }) => {
  const { venueId, courtName, courtLocation, price, game_icon, sport, courtImage } = courtDetails;
  const [selectedDate, setSelectedDate] = useState(null);
  const [selectedSlot, setSelectedSlot] = useState(null);
  const { url, getImageUrl, role } = useContext(StoreContext);
  const token = localStorage.getItem('token');

  const today = new Date();
  const availableDates = [
    today,
    new Date(today.getFullYear(), today.getMonth(), today.getDate() + 1),
    new Date(today.getFullYear(), today.getMonth(), today.getDate() + 2)
  ];

  const handleConfirmBooking = async () => {
    // Check if user is logged in
    const token = localStorage.getItem('token');
    const userId = localStorage.getItem('userId');
    
    if (!token || !userId) {
      toast.error('Please log in to make a booking');
      setShowBooking(false);
      return;
    }

    // Check if user is a player
    if (role !== 'PLAYER') {
      toast.error('Access denied');
      setShowBooking(false);
      return;
    }

    if (!selectedDate || !selectedSlot) {
      alert('Please select both a date and a time slot before confirming the booking.');
      return;
    }

    const formattedDate = format(selectedDate, 'dd-MM-yyyy');
    const paymentBody = {
      currency: 'inr',
      venueId: venueId,
      userId: userId,
      bookingDate: formattedDate,
      bookingSlot: selectedSlot
    };

    try {
      const response = await axios.post(`${url}/api/payments/checkout-session`, paymentBody, {
        headers: { Authorization: `Bearer ${token}` }
      });

      const redirectUrl = response?.data?.url;
      if (redirectUrl) {
        window.location.href = redirectUrl;
      } else {
        toast.error('Could not initialize payment session. Please try again.');
      }
    } catch (error) {
      console.error('Payment session error:', error);
      toast.error('Payment is unavailable right now. Booking is saved as pending.');

      // fallback to simple booking creation when Stripe fails
      try {
        const bookingData = {
          userId: localStorage.getItem('userId') || '',
          venueId: venueId,
          bookingDate: formattedDate,
          bookingSlot: selectedSlot
        };

        const fallbackResponse = await axios.post(`${url}/api/bookings/add-booking`, bookingData, {
          headers: { Authorization: `Bearer ${token}` }
        });

        if (fallbackResponse.data?.success) {
          toast.success('Booking created as pending while payment is unavailable.');
          setShowBooking(false);
        } else {
          toast.error('Booking could not be saved.');
        }
      } catch (fallbackError) {
        console.error('Booking fallback failed:', fallbackError);
      }
    }
  };

  return (
    <div className='book-popup'>
      <div className="book-popup-container">
        <div className="book-popup-header">
          <h2>Booking Confirmation</h2>
          <img 
            onClick={() => setShowBooking(false)} 
            src={assets.cross_icon} 
            alt="Close" 
            className='book-popup-close'
          />
        </div>
        <div className="book-popup-content">
          <div className="book-popup-info">
            <h3>{courtName}</h3>
            <img className="game_icon" src={getImageUrl(game_icon)} alt={sport} />
            <p className="sport-name">{sport}</p>
            <p className="court-location">{courtLocation}</p>
            <p className="court-price">Price: Rs {price}/hr</p>
          </div>
        </div>

        <div className="date-selection">
          <h4>Select Date:</h4>
          <DatePicker
            selected={selectedDate}
            onChange={(date) => setSelectedDate(date)}
            includeDates={availableDates}
            placeholderText="Select a date"
            inline
          />
          <p>Selected Date: {selectedDate ? selectedDate.toLocaleDateString() : "None"}</p>
        </div>

        <div className="slot-selection">
          <h4>Select a Time Slot:</h4>
          <div className="slot-options">
            {['8:00-10:00', '13:00-15:00', '16:00-18:00'].map((slot) => (
              <button 
                key={slot} 
                className={`slot-button ${selectedSlot === slot ? 'selected' : ''}`}
                onClick={() => setSelectedSlot(slot)}
              >
                {slot}
              </button>
            ))}
          </div>
        </div>

        <div className="book-popup-actions">
          <button className="confirm-button" onClick={handleConfirmBooking}>Confirm Booking</button>
        </div>
      </div>
    </div>
  );
};

export default Bookpop;
