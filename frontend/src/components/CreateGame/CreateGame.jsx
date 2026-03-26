import React, { useContext, useState } from 'react';
import './CreateGame.css'; 
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';
import { StoreContext } from '../../context/storeContextInstance';
import axios from "axios";
import { toast } from 'react-toastify';
import { format } from 'date-fns';
import { assets } from '../../assets/assets';

const CreateGame = ({ setShowCreateGame, courtDetails }) => {
  const { courtName, courtLocation, price, game_icon, sport, courtImage } = courtDetails;
  const [selectedDate, setSelectedDate] = useState(null);
  const [selectedSlot, setSelectedSlot] = useState(null);
  const [totalMembers, setTotalMembers] = useState('');
  const [availableMembers, setAvailableMembers] = useState('');
  const [level, setLevel] = useState('Casual');

  const { url,getImageUrl, fetchGameList } = useContext(StoreContext);
  const token = localStorage.getItem('token');

  const today = new Date();
  const availableDates = [
    today,
    new Date(today.getFullYear(), today.getMonth(), today.getDate() + 1),
    new Date(today.getFullYear(), today.getMonth(), today.getDate() + 2)
  ];

  const handleCreateGame = async () => {
    if (selectedDate && selectedSlot && totalMembers && availableMembers) {
      const formattedDate = `${format(selectedDate, 'dd MMM, yyyy')} ${selectedSlot}`;
      const sportIcon = `${sport.replace(/\s+/g, '').toLowerCase()}_icon`;
      const userImage = localStorage.getItem('userImage');
      
      const gameData = {
        date: formattedDate,
        sportIcon,
        sportName: sport,
        userImage,
        userName: '', 
        userID: '',   
        membersJoined: 1,
        totalMembers,
        level,
        courtName,
        location: courtLocation,
      };

      try {
        const response = await axios.post(`${url}/api/game/create-game`, gameData, {
          headers: { Authorization: `Bearer ${token}` }
        });
        if (response.data.success) {
          toast.success(response.data.message);
          setShowCreateGame(false);
          await fetchGameList();
        } else {
          toast.error(response.data.message);
        }
      } catch (error) {
        console.error("Error creating game:", error);
        toast.error("An error occurred while creating the game.");
      }
    } else {
      alert("Please fill in all the details before creating the game.");
    }
  };

  return (
    <div className='create-game-popup'>
      <div className="create-game-container">
        <div className="create-game-header">
          <h2>Create Game</h2>
          <img 
            onClick={() => setShowCreateGame(false)} 
            src={assets.cross_icon} 
            alt="Close" 
            className='create-game-close'
          />
        </div>
        <div className="create-game-content">
          <div className="create-info">
            <h3>{courtName}</h3>
            <img className="game_icon" src={getImageUrl(game_icon)} alt={sport} />
            <p className="sport-name">{sport}</p>
            <p className="court-location">{courtLocation}</p>
            <p className="court-price">Price: ₹{price}/hr</p>
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

          <div className="level-selection">
            <h4>Select Level:</h4>
            <select value={level} onChange={(e) => setLevel(e.target.value)}>
            <option value="Casual">Casual</option>
            <option value="Intermediate">Intermediate</option>
            <option value="Professional">Professional</option>
            </select>
          </div>

          <div className="members-input">
            <label>Total Members:</label>
            <input 
              type="number" 
              value={totalMembers} 
              onChange={(e) => setTotalMembers(e.target.value)} 
            />
            <label>Available Members:</label>
            <input 
              type="number" 
              value={availableMembers} 
              onChange={(e) => setAvailableMembers(e.target.value)} 
            />
          </div>
        </div>
        <div className="create-game-actions">
          <button className="confirm-button" onClick={handleCreateGame}>Create Game</button>
        </div>
      </div>
    </div>
  );
};

export default CreateGame;
