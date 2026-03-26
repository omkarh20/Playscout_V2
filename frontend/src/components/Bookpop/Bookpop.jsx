import React, { useContext, useState } from 'react';
import './Bookpop.css'; 
import { assets } from '../../assets/assets'; 
import { StoreContext } from '../../context/storeContextInstance';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';
import axios from "axios"
import { toast } from 'react-toastify';
import { format } from 'date-fns';

const Bookpop = ({ setShowBooking, courtDetails }) => {
  const { id, courtName, courtLocation, price, game_icon, sport, courtImage } = courtDetails;
  const [selectedDate, setSelectedDate] = useState(null);
  const [selectedSlot, setSelectedSlot] = useState(null);
  const { url, getImageUrl } = useContext(StoreContext);
  const token = localStorage.getItem('token');

  const today = new Date();
  const availableDates = [
    today,
    new Date(today.getFullYear(), today.getMonth(), today.getDate() + 1),
    new Date(today.getFullYear(), today.getMonth(), today.getDate() + 2)
  ];

  const handleConfirmBooking = async () => {
    if (selectedDate && selectedSlot) {
      const bookingData = {
        venueId: id,
        bookingDate: format(selectedDate, 'yyyy-MM-dd'),
        slot: selectedSlot,
        totalMembers: 1
      };
    
      try {
        const response = await axios.post(`${url}/api/bookings/add-booking`, bookingData, {
          headers: { Authorization: `Bearer ${token}` }
        });
        if (response.data.success) {
          console.log("Booking confirmed and saved to database.");
          toast.success(response.data.message);
          setShowBooking(false);
        } else {
          console.error("Booking failed.");
          toast.error(response.data.message);
        }
      } catch (error) {
        console.error("Error saving booking:", error);
      }
      console.log(`Booking confirmed for ${courtName} on ${selectedDate.toLocaleDateString()} at ${selectedSlot}`);
      setShowBooking(false);
    } else {
      alert("Please select both a date and a time slot before confirming the booking.");
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
            <p className="court-price">Price: ₹{price}/hr</p>
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
