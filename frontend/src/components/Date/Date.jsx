import React, { useState } from 'react';
import DatePicker from 'react-datepicker';
import "react-datepicker/dist/react-datepicker.css";
import './Date.css';
import { assets } from '../../assets/assets';

const Date = ({ setStartDate }) => {
  const [startDateLocal, setStartDateLocal] = useState(null);
  const [showCalendar, setShowCalendar] = useState(false);

  const handleDateChange = (date) => {
    setStartDateLocal(date);
    setStartDate(date);
    setShowCalendar(false);
  };

  return (
    <div className="calendar-wrapper">
      <button className="date-btn-book" onClick={() => setShowCalendar(true)}>
        <img src={assets.calendar_icon} alt="Calendar" />
        <span>{startDateLocal ? startDateLocal.toLocaleDateString() : 'Select Date'}</span>
      </button>
      {showCalendar && (
        <div className="calendar-container">
          <DatePicker
            selected={startDateLocal}
            onChange={handleDateChange}
            className="datepicker"
            inline
          />
        </div>
      )}
    </div>
  );
};

export default Date;

